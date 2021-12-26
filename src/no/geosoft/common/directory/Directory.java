package no.geosoft.common.directory;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A default TreeModel implementation.
 * <p>
 * Useful for small trees that can be prepopulated.
 * <p>
 * Example usage:
 * <pre>
 *    Directory.Entry root = Directory.Entry.newFolder("root", null);
 *
 *    Directory.Entry folder1 = Directory.Entry.newFolder("folder1", null);
 *    root.add(folder1);
 *
 *    Directory.Entry folder2 = Directory.Entry.newFolder("folder2", null);
 *    root.add(folder2);
 *
 *    Directory.Entry folder3 = Directory.Entry.newFolder("folder3", null);
 *    root.add(folder3);
 *
 *    folder1.add(Directory.Entry.newItem("item", null));
 *    :
 *
 *
 *    Directory directory = new Directory(root);
 *    JTree tree = new JTree(directory);
 * </pre>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Directory implements TreeModel
{
  /** Root entry. Non-null. */
  private final Entry root_;

  /** Directory listaeners. */
  private final Collection<TreeModelListener> listeners_ = new ArrayList<TreeModelListener>();

  /**
   * Create a new directory wit hthe specified root entry.
   *
   * @param root  Root entry. Non-null.
   * @throws IllegalArgumentException  If root is null.
   */
  public Directory(Entry root)
  {
    if (root == null)
      throw new IllegalArgumentException("root cannot be null");

    root_ = root;
  }

  /** {@inheritDoc} */
  @Override
  public Object getChild(Object parent, int index)
  {
    if (parent == null)
      throw new IllegalArgumentException("parent cannot be null");

    Entry parentEntry = (Entry) parent;
    return parentEntry.getEntry(index, false);
  }

  /** {@inheritDoc} */
  @Override
  public int getChildCount(Object parent)
  {
    if (parent == null)
      throw new IllegalArgumentException("parent cannot be null");

    Entry parentEntry = (Entry) parent;
    return parentEntry.getNChildren();
  }

  /** {@inheritDoc} */
  @Override
  public int getIndexOfChild(Object parent, Object child)
  {
    if (parent == null)
      throw new IllegalArgumentException("parent cannot be null");

    if (child == null)
      throw new IllegalArgumentException("child cannot be null");

    Entry parentEntry = (Entry) parent;
    Entry childEntry = (Entry) child;
    return parentEntry.getIndexOfEntry(childEntry);
  }

  /** {@inheritDoc} */
  @Override
  public Object getRoot()
  {
    return root_;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isLeaf(Object node)
  {
    if (node == null)
      throw new IllegalArgumentException("parent cannot be null");

    Entry entry = (Entry) node;
    return entry.isLeaf();
  }

  /** {@inheritDoc} */
  @Override
  public void valueForPathChanged(TreePath path, Object newValue)
  {
    // Nothing
  }

  /** {@inheritDoc} */
  @Override
  public void addTreeModelListener(TreeModelListener listener)
  {
    if (listener == null)
      throw new IllegalArgumentException("listener cannot be null");

    if (!listeners_.contains(listener))
      listeners_.add(listener);
  }

  /** {@inheritDoc} */
  @Override
  public void removeTreeModelListener(TreeModelListener listener)
  {
    if (listener == null)
      throw new IllegalArgumentException("listener cannot be null");

    listeners_.remove(listener);
  }

  /**
   * Notify listeners about changes in the model.
   *
   * @param event  Event describing the chenge.
   */
  private void fireTreeStructureChanged(TreeModelEvent event)
  {
    assert event != null : "event cannot be null";

    synchronized (listeners_) {
      for (TreeModelListener listener : listeners_)
        listener.treeStructureChanged(event);
    }
  }

  /**
   * Notify listeners about changes in the model.
   */
  public void notifyListeners()
  {
    TreeModelEvent event = new TreeModelEvent(this, root_.getPath());
    fireTreeStructureChanged(event);
  }

  /**
   * Class modelling one directory entry.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  public static class Entry implements Comparable<Entry>
  {
    /** Set of links pointing to this. */
    private final Collection<Entry> links_ = new CopyOnWriteArrayList<Entry>();

    /** The back-end item of this entry. */
    private final Object item_;

    /** Children entries. Always empty if isLeaf == true. */
    private final List<Entry> children_ = new CopyOnWriteArrayList<Entry>();

    /** Leaf node. Per definition and independent of N children. */
    private final boolean isLeaf_;

    /** Link to another actual entry. The actual entry is then the item_. */
    private final boolean isLink_;

    /** An aid for a visualizer. */
    private boolean isExpanded_;

    /** Parent entry (or null if this is a root node). */
    private Entry parent_;

    /** Name of this entry. If not set, item_.toString() is used as name. */
    private String name_;

    /** Levels of symbolic links. Used to encounter cyclic linking. */
    private int nLevelsOfSymbolicLinks_;

    /**
     * Create a new directory entry.
     *
     * @param item     Back-end instance. May be null, but not if name is null.
     * @param name     Display name. May be null, but not if item is null.
     * @param isLeaf   True if this is a leaf node, false if it is a folder.
     * @param isLink   True if this is a symbolic link, false otherwise.
     */
    private Entry(Object item, String name, boolean isLeaf, boolean isLink)
    {
      assert item != null || name != null : "Both item and name cannot be null";

      item_ = item;
      name_ = name;
      isLeaf_ = isLeaf;
      isLink_ = isLink;
    }

    /**
     * Create a new leaf entry.
     *
     * @param name  Name of this entry. If specified as null, the name of the
     *              entry will be the toString() of the item.
     * @param item  Client object of this entry.
     * @return      The new entry. Add it to the tree by one of the add methods.
     * @throws IllegalArgumentException  If both name and item is null.
     */
    public static Entry newItem(String name, Object item)
    {
      if (name == null && item == null)
        throw new IllegalArgumentException("Both name and item can't be null");

      return new Entry(item, name, true, false);
    }

    /**
     * Create a new folder entry.
     *
     * @param name  Name of this entry. If specified as null, the name of the
     *              entry will be the toString() of the item.
     * @param item  Client object of this entry.
     * @return      The new entry. Add it to the tree by on of the add methods.
     * @throws IllegalArgumentException  If both name and item is null.
     */
    public static Entry newFolder(String name, Object item)
    {
      if (name == null && item == null)
        throw new IllegalArgumentException("Both name and item can't be null");

      return new Entry(item, name, false, false);
    }

    /**
     * Create a new link entry.
     *
     * @param name           Name of this entry. If specified as null, the
     *                       name of the entry will be the name of the existing entry.
     * @param existingEntry  The entry linking to.
     * @return               The new entry. Add it to the tree by on of the
     *                       add methods.
     * @throws IllegalArgumentException  If existingEntry is null.
     */
    public static Entry newLink(String name, Entry existingEntry)
    {
      if (existingEntry == null)
        throw new IllegalArgumentException("existingEntry can't be null");

      Entry link = new Entry(existingEntry, name, existingEntry.isLeaf(), true);

      existingEntry.links_.add(link);

      return link;
    }

    /**
     * Check if this entry is a link to some other entry.
     *
     * @return True if this entry is a link, false otherwise.
     */
    public boolean isLink()
    {
      return isLink_;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Entry entry)
    {
      Collator collator = Collator.getInstance();

      if (this.isFolder() && entry.isLeaf())
        return -1;
      else if (this.isLeaf() && entry.isFolder())
        return +1;
      else
        return collator.compare(this.getName(), entry.getName());
    }

    /**
     * Add the specified entry at the given index of the child list of this.
     *
     * @param child  Child entry to add. Non-null.
     * @param index  Position amoung existing children to add to, First is 0.
     * @throws IllegalArgumentException  If child is null or index &lt; 0.
     * @throws IllegalStateException     If this is a leaf node.
     */
    public void add(Entry child, int index)
    {
      if (child == null)
        throw new IllegalArgumentException("child cannot be null");

      if (isLeaf_)
        throw new IllegalStateException("Unable to add to leaf node: " + this);

      if (index < 0)
        throw new IllegalArgumentException("Invalid index: " + index);

      // In a multi-threaded environment index may no longer be valid so we quit here
      if (index > getNChildren())
        return;

      Entry actualThis = getActual();

      if (child.isLink_) {
        Entry actualEntry = (Entry) child.item_;
        if (actualEntry.getRoot() != getRoot())
          throw new IllegalStateException("Adding link to differnent tree than source: " + child);
      }

      actualThis.children_.add(index, child);
      child.parent_ = actualThis;
    }

    /**
     * Add the specified entry at the end of the child list of this.
     *
     * @param child  Child entry to add. Non-null.
     * @throws IllegalArgumentException  If child is null.
     * @throws IllegalStateException     If this is a leaf node.
     */
    public void add(Entry child)
    {
      if (child == null)
        throw new IllegalArgumentException("child cannot be null");

      if (isLeaf_)
        throw new IllegalStateException("Unable to add to leaf node: " + this);

      add(child, getNChildren());
    }

    /**
     * Add the specified entry to the child list of this in a sorted manner.
     *
     * @param child  Child entry to add.
     * @throws IllegalArgumentException  If child is null.
     * @throws IllegalStateException     If this is a leaf node or child is
     *                                    already connected to a node.
     */
    public void addSorted(Entry child)
    {
      if (child == null)
        throw new IllegalArgumentException("child cannot be null");

      // Find index
      int index = 0;
      for (Entry existingChild : getActualChildren()) {
        if (existingChild.compareTo(child) > 0)
          break;
        index++;
      }

      // Add to specified index
      add(child, index);
    }

    /**
     * Remove this from parent. If this is not connected to a parent
     * nothing is done. Also remove all entries that links to this.
     */
    public void remove()
    {
      if (parent_ == null)
        return;

      // Remove from parent (NOTE: parent is never a link)
      assert parent_.children_.contains(this) : "Illegal state";
      parent_.children_.remove(this);
      parent_ = null;

      // Remove all links linking to this as well
      for (Entry link : links_)
        link.remove();

      links_.clear();
    }

    /**
     * Remove all children entries from this entry.
     */
    public void clear()
    {
      List<Entry> toRemove = new ArrayList<Entry>();
      toRemove.addAll(children_);

      for (Entry child : toRemove)
        child.remove();
    }

    /**
     * Return the root of the directory hirerarchy of this.
     *
     * @return  Root of the directory hirerarchy of this. Never null.
     */
    public Entry getRoot()
    {
      Entry root = this;
      while (!root.isRoot())
        root = root.getParent();
      return root;
    }

    /**
     * Return number of children of this entry.
     *
     * @return  Number of children of this entry.
     */
    public int getNChildren()
    {
      return getActualChildren().size();
    }

    /**
     * Check if this entry is empty, i.e. has no children.
     * A leaf node will always be empty.
     *
     * @return  True if this entry is empty, false otherwise.
     */
    public boolean isEmpty()
    {
      return getNChildren() == 0;
    }

    /**
     * Return the actual entry of this, i.e. if this is a link, get the
     * linked entry, otherwise return this.
     *
     * @return The actual entry of this entry.
     * @throws IllegalStateException  If cyclic links are encountered.
     */
    public Entry getActual()
    {
      if (isLink_)
        nLevelsOfSymbolicLinks_++;
      else
        nLevelsOfSymbolicLinks_ = 0;

      if (nLevelsOfSymbolicLinks_ > 1000)
        throw new IllegalStateException("Too many levels of symbolic links");

      return isLink_ ? ((Entry) item_).getActual() : this;
    }

    /**
     * Return children as the actual list, but following links.
     *
     * @return  Children list (modifiable). Never null.
     */
    private List<Entry> getActualChildren()
    {
      return getActual().children_;
    }

    /**
     * Return a non-modifyable copy of the children list of this entry.
     *
     * @return  A non-modifyable copy of the children list of this entry. Never null.
     */
    public List<Entry> getChildren()
    {
      return Collections.unmodifiableList(getActualChildren());
    }

    /**
     * Return all entries that are mere links to this entry.
     *
     * @return  Collection of all entries that are mere links to this entry. Never null.
     */
    public Collection<Entry> getLinks()
    {
      return Collections.unmodifiableCollection(links_);
    }

    /**
     * Check if this entry is a leaf node. Leaf is by definition. A non-leaf
     * node (a folder) may be empty but is still not a leaf node in this sense.
     *
     * @return  True if this entry is a leaf, false otherwise.
     */
    public boolean isLeaf()
    {
      return isLeaf_;
    }

    /**
     * Check if this entry is a folder node. @see isLeaf.
     *
     * @return  True if this is a folder, false otherwise.
     */
    public boolean isFolder()
    {
      return !isLeaf();
    }

    /**
     * Check if this is a root entry, i.e. has no parent.
     *
     * @return  True if this entry is a root node, false otherwise.
     */
    public boolean isRoot()
    {
      return getParent() == null;
    }

    /**
     * Rename this entry. If the entry has no item, it is illegal to set
     * name to null, as it then will be nameless.
     *
     * @param name  New name of node.
     * @throws IllegalArgumentException I name is null and entry has no item.
     */
    public void setName(String name)
    {
      if (name == null && item_ == null)
        throw new IllegalArgumentException("name cannot be null when item is null: " + this);

      name_ = name;
    }

    /**
     * Return name of this entry.
     *
     * @return  Name of this entry.
     */
    public String getName()
    {
      return name_ != null ? name_ : item_.toString();
    }

    /**
     * Return a string representation of this entry.
     *
     * @return  A string representation of this node.
     */
    public String toString()
    {
      return getName();
    }

    /**
     * Return the client data item of this entry.
     *
     * @return  Client data item of this entry. Null if none.
     */
    public Object getItem()
    {
      return getActual().item_;
    }

    /**
     * Return the parent of this entry.
     *
     * @return  Parent of this entry.
     */
    public Entry getParent()
    {
      return parent_;
    }

    /**
     * Return the child entry at the specified position.
     *
     * @param index        Position of entry to return.
     * @param isRecursive  True if entire subtree should be considered, false
     *                     if only the immediate children should be considered.
     * @return             Entry at the specified index.
     * @throws IndexOutOfBoundsException  If index is illegal.
     */
    public Entry getEntry(int index, boolean isRecursive)
    {
      boolean invalidIndex = index < 0  ||
                             isLeaf_ && index > getNChildren() ||
                             !isLeaf_ && !isRecursive && index >= getNChildren();

      if (invalidIndex)
        throw new IndexOutOfBoundsException("Invalid index: " + index);

      if (isRecursive) {
        List<Entry> entries = new ArrayList<Entry>();
        return getEntry(index + 1, entries);
      }
      else {
        return getActualChildren().get(index);
      }
    }

    /**
     * TODO.
     *
     * @param index  TODO.
     * @param Entry  TODO.
     * @return       TODO.
     */
    private Entry getEntry(int index, List<Entry> entries)
    {
      if (entries.size() == index)
        return this;

      entries.add(this);

      for (Entry child : getActualChildren()) {
        Entry foundEntry = getEntry(index, entries);
        if (foundEntry != null) return foundEntry;
      }

      // Index not reached
      return null;
    }

    /**
     * Return position of the specified entry. Null if entry does not exist.
     *
     * @param child  Child entry to find index of.
     * @return       Index of entry (or -1 if no such entry exists)
     * @throws IllegalArgumentException if entry is null
     */
    public int getIndexOfEntry(Entry child)
    {
      if (child == null)
        throw new IllegalArgumentException("child cannot be null");

      return getActualChildren().indexOf(child);
    }

    /**
     * Return the path to this entry as an object array.
     * Useful when working with TreePaths.
     *
     * @return  Array of this and all ancestors in an array. Never null.
     */
    public Object[] getPath()
    {
      List<Entry> path = new ArrayList<Entry>();
      path.add(this);

      Entry parent = getParent();
      while (parent != null) {
        path.add(0, parent);
        parent = parent.getParent();
      }

      return path.toArray();
    }

    /**
     * Search for an entry with specified client data item (may be null).
     * Start in this entry and search depth first.
     *
     * @param item         Item to look for (using equals()). Null permitted.
     * @param isRecursive  True if the entire sub tree should be searched,
     *                     false if only this and the immediate level below
     *                     should be searched.
     * @return             The requested entry or null if not found.
     */
    public Entry find(Object item, boolean isRecursive)
    {
      boolean same = item_ == null ? item == null : item_.equals(item);

      if (same)
        return this;

      if (isRecursive) {
        for (Entry child : getActualChildren()) {
          Entry entry = child.find(item, isRecursive);
          if (entry != null) return entry;
        }
      }

      return null;
    }

    /**
     * Search for an entry with the specified name.
     * Start in this entry and search depth first.
     *
     * @param name         Item to look for (using equals()). Null permitted.
     * @param isRecursive  True if the entire sub tree should be searched,
     *                     false if only this and the immediate level below
     *                     should be searched.
     * @return             The directory entry of item if found, null otherwise.
     */
    public Entry find(String name, boolean isRecursive)
    {
      if (getName().equals(name))
        return this;

      if (isRecursive) {
        for (Entry child : getActualChildren()) {
          Entry entry = child.find(name, isRecursive);
          if (entry != null) return entry;
        }
      }

      return null;
    }

    /**
     * Search for a folder with the specified name.
     * Start in this entry and search depth first.
     *
     * @param name         Item to look for (using equals()). Null permitted.
     * @param isRecursive  True if the entire sub tree should be searched,
     *                     false if only this and the immediate level below
     *                     should be searched.
     * @return             The directory entry of item if found, null otherwise.
     */
    public Entry findFolder(String name, boolean isRecursive)
    {
      if (isLeaf_)
        return null;

      if (getName().equals(name))
        return this;

      if (isRecursive) {
        for (Entry child : getActualChildren()) {
          Entry entry = child.findFolder(name, isRecursive);
          if (entry != null) return entry;
        }
      }

      return null;
    }

    /**
     * Expand this node and possibly subnodes. This method only alters the
     * isExpanded setting as an aid for a visualizer.
     *
     * @param isRecursive  If true, all entries rooted in this entry will be
     *                     expanded as well. If false, only this entry will be
     *                     set to expanded.
     */
    public void expand(boolean isRecursive)
    {
      isExpanded_ = true;

      if (isRecursive) {
        for (Entry child : children_)
          child.expand(isRecursive);
      }
    }

    /**
     * Collapse this node and possibly subnodes. This method only alters the
     * isExpanded setting as an aid for a visualizer.
     *
     * @param isRecursive  If true, all entries rooted in this entry will be
     *                     collapsed as well. If false, only this entry will be
     *                     set to collapsed.
     */
    public void collapse(boolean isRecursive)
    {
      isExpanded_ = false;

      if (isRecursive) {
        for (Entry child : children_)
          child.collapse(isRecursive);
      }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
      if (item_ != null)
        return item_.hashCode();
      else if (name_ != null)
        return name_.hashCode();
      else
        return 17;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object)
    {
      if (object == this)
        return true;
      if (object == null)
        return false;
      if (!(object instanceof Entry))
        return false;

      Entry entry = (Entry) object;

      if (item_ != null && !item_.equals(entry.item_))
        return false;

      if (item_ == null && entry.item_ != null)
        return false;

      if (name_ != null && !name_.equals(entry.name_))
        return false;

      if (name_ == null && entry.name_ != null)
        return false;

      if (isLeaf_ != entry.isLeaf_)
        return false;

      if (isLink_ != entry.isLink_)
        return false;

      return true;
    }
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    Directory.Entry root = Directory.Entry.newFolder("root", null);
    Directory.Entry folder1 = Directory.Entry.newFolder("folder1", null);
    Directory.Entry folder2 = Directory.Entry.newFolder("folder2", null);
    Directory.Entry folder3 = Directory.Entry.newFolder("folder3", null);

    root.add(folder1);
    root.add(folder2);
    root.add(folder3);

    folder1.add(Directory.Entry.newItem("item", null));

    Directory directory = new Directory(root);

    f.add(new javax.swing.JTree(directory));

    f.pack();
    f.setVisible(true);
  }
}
