package no.geosoft.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A class for keeping track of the <em>most recently used</em> instance
 * of a given type.
 *
 * @param <T>  The <em>type</em> of the instances the MRU class
 *             keeps track of.

 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Mru<T>
{
  /**
   * Maximum allowed number of elements.
   * If we get more than this, we remove the <em>least recently used</em> (LRU)
   */
  private final int capacity_;

  /** The elements. The <em>most recently used</em> (MRU) is first. */
  private final List<T> elements_ = new ArrayList<>();

  /**
   * Create an empty MRU list with the specified capacity.
   *
   * @param capacity  Capacity of the MRU list. &lt;0,&gt;.
   * @throws IllegalArgumentException  If capacity &lt;= 0.
   */
  public Mru(int capacity)
  {
    if (capacity <= 0)
      throw new IllegalArgumentException("Invalid capacity: " + capacity);

    capacity_ = capacity;
  }

  /**
   * Clear this MRU, i.e. remove all its elements.
   */
  public void clear()
  {
    elements_.clear();
  }

  /**
   * Return the <em>most recently used</em> (MRU) element.
   *
   * @return  The most recently used element, or null if the collection is empty.
   */
  public T get()
  {
    return elements_.size() > 0 ? elements_.get(0) : null;
  }

  /**
   * Add the specified element as MRU to this collection.
   *
   * @param element  Element to add. Non-null.
   * @throws IllegalArgumentException  If element is null.
   */
  public void add(T element)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    // First remove duplicates
    for (Iterator<T> i = elements_.iterator(); i.hasNext(); ) {
      T existingElement = i.next();
      if (existingElement.equals(element))
        i.remove();
    }

    // Then add at MRU position
    elements_.add(0, element);

    // Remove LRU if we exceed capacity
    if (elements_.size() > capacity_)
      elements_.remove(elements_.size() - 1);
  }

  /**
   * Return size of this MRU.
   *
   * @return  Size of this MRU. [0,&gt;.
   */
  public int size()
  {
    return elements_.size();
  }

  /**
   * Check if this MRU is empty
   *
   * @return  True if the MRU is empty, false otherwise.
   */
  public boolean isEmpty()
  {
    return size() == 0;
  }

  /**
   * Return all the elements. Most recently used is first.
   *
   * @return  All the elements. Never null.
   */
  public List<T> getAll()
  {
    return Collections.unmodifiableList(elements_);
  }

  /**
   * Remove the entry at the specified index.
   *
   * @param index  Index of element to remove. 0-based. [0,size&gt;
   * @throws IllegalArgumentException  If index is out of bounds.
   */
  public void remove(int index)
  {
    if (index < 0 || index >= elements_.size())
      throw new IllegalArgumentException("Invalid index [0," + (elements_.size() - 1) + "]: " + index);

    elements_.remove(index);
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < elements_.size(); i++) {
      T element = elements_.get(i);

      s.append(i == 0 ? "MRU: " : i + ": ");
      s.append(element.toString());
      s.append("\n");
    }

    return s.toString();
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    Mru<String> mru = new Mru<>(5);

    mru.add("First");
    mru.add("Second");
    mru.add("Third");
    mru.add("First");
    mru.add("444");
    mru.add("555");
    mru.add("666");
    System.out.println(mru);

    mru.remove(99);
    System.out.println(mru);
  }
}
