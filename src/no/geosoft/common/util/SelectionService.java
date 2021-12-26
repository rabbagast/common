package no.geosoft.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.geosoft.common.event.EventManager;

/**
 * Class for managing <em>selections</em> in an application.
 * <p>
 * The definition of a selection is up to the client application; Typically it
 * can be the selected entry in a tree component, but it can be anything.
 * <p>
 * The reason there is a separate service for this task, is that parts of the
 * application that has no direct connection to the selection source (the tree, say)
 * should still be able to access the selection.
 * <p>
 * Each selection is associated with a <em>selection source</em> so that the
 * application may distinguish different types of selection. There may be zero
 * or more selections for each selection source.
 * <p>
 * A selection is an object only; The client should know what type to expect
 * for a selection, perhaps based on the selection source.
 * <p>
 * A <tt>SelectionChanged</tt> event is sent when the selection is changed.
 * The callback is identified with the source ID and the complete selection
 * from that source.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class SelectionService
{
  /** The sole instance of this class. */
  private final static SelectionService instance_ = new SelectionService();

  /** Selections by selection source. */
  private final Map<String, List<Object>> selections_ = new CopyOnWriteHashMap<>();

  /**
   * Private constructor to prevent client instantiation.
   */
  private SelectionService()
  {
    // Nothing
  }

  /**
   * Return the sole instance of this class.
   *
   * @return The sole instance of this class. Never null.
   */
  public static SelectionService getInstance()
  {
    return instance_;
  }

  /**
   * Get the current list of selections for the specified source.
   * Create empty list if not already existing.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @return                   List of selections for the specified source.
   *                           Never null.
   */
  private List<Object> getOrCreateSelection(String selectionSourceId)
  {
    assert selectionSourceId != null : "selectionSourceId cannot be null";

    // Create empty selection list if the ID is not known already
    if (!selections_.containsKey(selectionSourceId))
      selections_.put(selectionSourceId, new ArrayList<>());

    // Return selection for specified ID
    return selections_.get(selectionSourceId);
  }

  /**
   * Add the specified object to the existing selection of the specified source.
   * <p>
   * If the selection source already contains the specified object,
   * this method call has no effect, i.e. no notification messages are sent.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @param object             The object to set as selection. Non-null.
   * @throws IllegalArgumentException  If selectionSourceId or object is null.
   */
  public void addSelection(String selectionSourceId, Object object)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    if (object == null)
      throw new IllegalArgumentException("object cannot be null");

    // Get current selection for this source
    List<Object> selection = getOrCreateSelection(selectionSourceId);

    // First remove the specified object from the selection so we re-add at 0th
    boolean isRemoved = selection.remove(object);

    // We add the object to the front of the list. When the client
    // asks for selection through getSelection() we return the front
    // element, i.e. the last one added.
    selection.add(0, object);

    // Notify listeners that selection has changed
    if (!isRemoved)
      EventManager.getInstance().notify("SelectionChanged", selectionSourceId,
                                        Collections.unmodifiableCollection(selection));
  }

  /**
   * Set the selection for the specified source. It will effectively
   * remove all current selections for the same source.
   * <p>
   * If the selection source already contains the specified object, and
   * this is the only one in the selection, this method call has no
   * effect, i.e. no notification messages are sent.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @param object             The object to set as selection. Non-null.
   * @throws IllegalArgumentException  If selectionSourceId or object is null.
   */
  public void setSelection(String selectionSourceId, Object object)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    if (object == null)
      throw new IllegalArgumentException("object cannot be null");

    // Get current selection for this source
    List<Object> selection = getOrCreateSelection(selectionSourceId);

    // Return here if there is no change to the selection
    if (selection.contains(object) && selection.size() == 1)
      return;

    // Clear the present selection
    selection.clear();

    // Add the specified object to the selection (and notify listeners)
    addSelection(selectionSourceId, object);
  }

  /**
   * Set the selection for the specified source. It will effectively
   * remove all current selections for the same source.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @param objects            The objects to set as selection. Non-null.
   * @throws IllegalArgumentException  If selectionSourceId or objects is null.
   */
  public void setSelections(String selectionSourceId, List<? extends Object> objects)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    if (objects == null)
      throw new IllegalArgumentException("objects cannot be null");

    // Get the current selection for this source
    List<Object> selection = getOrCreateSelection(selectionSourceId);

    // Nothing to do if this is already the selection
    if (selection.containsAll(objects) && selection.size() == objects.size())
      return;

    // Replace the current selection with the requested one, but avoid duplicates
    selection.clear();
    for (Object object : objects) {
      if (!selection.contains(object))
        selection.add(object);
    }

    if (selection.isEmpty())
      selections_.remove(selectionSourceId);

    // Notify listeners that selection has changed
    EventManager.getInstance().notify("SelectionChanged", selectionSourceId,
                                      Collections.unmodifiableCollection(objects));
  }

  /**
   * Remove the specified object from the existing selection of the specified source.
   * <p>
   * If the object is not present in the selection of the specified source,
   * this method call has no effect.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @param object             The object to set as selection. Non-null.
   * @throws IllegalArgumentException  If selectionSourceId or object is null.
   */
  public void removeSelection(String selectionSourceId, Object object)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    if (object == null)
      throw new IllegalArgumentException("object cannot be null");

    // Nothing to do if the source doesn't exist
    if (!selections_.containsKey(selectionSourceId))
      return;

    // Get current selection for this source
    List<Object> selection = selections_.get(selectionSourceId);

    // Try to remove the specified object from the selection
    boolean isRemoved = selection.remove(object);

    // Remove entry if this was the last selection
    if (selection.isEmpty())
      selections_.remove(selectionSourceId);

    // If it was removed (i.e. if it was present in the first place) notify listeners
    if (isRemoved)
      EventManager.getInstance().notify("SelectionChanged", selectionSourceId,
                                        Collections.unmodifiableCollection(selection));
  }

  /**
   * Clear the selection for the specified selection source.
   * <p>
   * If the selection is already empty for the specified source
   * this method call has no effect.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @throws IllegalArgumentException  If selectionSourceId is null.
   */
  public void clearSelection(String selectionSourceId)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    // Nothing to do if the sourceId doesn't exists
    if (!selections_.containsKey(selectionSourceId))
      return;

    // Get current selection for this source
    List<Object> selection = selections_.get(selectionSourceId);

    // Remove the entry
    selections_.remove(selectionSourceId);

    // Notify listeners
    EventManager.getInstance().notify("SelectionChanged", selectionSourceId,
                                      Collections.unmodifiableCollection(selection));
  }

  /**
   * Clear all selections from all selection sources.
   * <p>
   * <b>NOTE:</b> This method basically exists to simplify unit testing.
   */
  public void clear()
  {
    for (String sourceId : selections_.keySet())
      clearSelection(sourceId);
  }

  /**
   * Return all selected elements of the specified selection source.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @return                   List of selections for the specified source. Never null.
   *                           If the selectionSourceId doesn't exist, an empty list is returned.
   * @throws IllegalArgumentException  If selectionSourceId is null.
   */
  public List<Object> getSelections(String selectionSourceId)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    List<Object> selection = selections_.get(selectionSourceId);
    return selection != null ? Collections.unmodifiableList(selection) : new ArrayList<>();
  }

  /**
   * Return all selected elements of the specified type from the
   * given selection source.
   *
   * @param <T>                Type of selections to get.
   * @param selectionSourceId  Selection source ID. Non-null.
   * @param clazz              Class of objects to return. Non-null.
   * @return                   List of selections for the specified source.
   *                           Never null.
   * @throws IllegalArgumentException  If selectionSourceId or clazz is null.
   */
  public <T> List<T> getSelections(String selectionSourceId, Class<T> clazz)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    if (clazz == null)
      throw new IllegalArgumentException("clazz cannot be null");

    List<Object> allSelection = getSelections(selectionSourceId);

    List<T> selection = new ArrayList<>();
    for (Object element : allSelection) {
      if (element.getClass() == clazz)  // TODO: Handle inheritance
        selection.add(clazz.cast(element));
    }

    return selection;
  }

  /**
   * Return one selected object from the selection of specified source.
   * <p>
   * This is a convenience if the client known there are most one selected
   * object, or if only one selected object is needed.
   * <p>
   * If there are more than one object selected, the most recently added
   * object will be returned.
   *
   * @param selectionSourceId  Selection source ID. Non-null.
   * @return                   Selected object, or null if none.
   * @throws IllegalArgumentException  If selectionSourceId is null.
   */
  public Object getSelection(String selectionSourceId)
  {
    if (selectionSourceId == null)
      throw new IllegalArgumentException("selectionSourceId cannot be null");

    List<Object> selection = getSelections(selectionSourceId);
    return selection.isEmpty() ? null : selection.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    selections_.forEach((key, value) -> s.append(key + " -> " + value.size() + " selection(s)\n"));
    return s.toString();
  }
}
