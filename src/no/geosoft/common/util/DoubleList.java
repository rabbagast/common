package no.geosoft.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * A List implementation wrapping a native double array.
 * <p>
 * Useful if the array becomes large.
 * Comparing DoubleList and List&lt;Double&gt; gives:
 * <br>
 * <ul>
 * <li><b>Memory consumption:</b> 8 vs 24 bytes per entry.
 * <li><b>Speed:</b> Add: ~25x faster, Get: ~4x faster
 * </ul>
 * The only limitation of DoubleList compared to List&lt;Double&gt; is that
 * it cannot distinguish between entries of null and NaN. Since the backing
 * array doesn't support null, both of these are stored as NaN and delivered
 * through the API as null.
 * <p>
 * Consequently:
 * <pre>
 *    DoubleList d = new DoubleList();
 *    d.add(Double.NaN);
 *    Double v = d.get(0); // Returns null
 * </pre>
 * It <em>could</em> have returned Double.NaN in this case, but then the
 * opposite case would have give nNaN as well:
 * <pre>
 *    DoubleList d = new DoubleList();
 *    d.add(null);
 *    Double v = d.get(0); // Returns Double.NaN
 * </pre>
 * Most clients will be happy with the first interpretation which is
 * adopted here.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class DoubleList implements List<Double>, RandomAccess, Cloneable
{
  /** The default capacity for new DoubleLists. */
  private static final int DEFAULT_CAPACITY = 1000;

  /** The back-end array. Non-null. */
  private double[] values_;

  /** Current list size. [0,&gt;. */
  private int size_ = 0;

  /**
   * The number of times this list has been <i>structurally modified</i>.
   * Structural modifications are those that change the size of the
   * list, or otherwise perturb it in such a fashion that iterations in
   * progress may yield incorrect results.
   */
  private transient int modificationCount_ = 0;

  /**
   * Create a double list with the specified initial capacity.
   *
   * @param capacity  Initial capacity. [0,&gt;.
   * @throws IllegalArgumentException  If capacity is out of bounds.
   */
  public DoubleList(int capacity)
  {
    if (capacity < 0)
      throw new IllegalArgumentException("Invalid capacity: " + capacity);

    values_ = new double[capacity];
  }

  /**
   * Create a double list with default capacity.
   */
  public DoubleList()
  {
    this(DEFAULT_CAPACITY);
  }

  /** {@inheritDoc} */
  @Override
  public Object clone()
  {
    try {
      DoubleList clone = (DoubleList) super.clone();
      clone.values_ = values_.clone();
      clone.size_ = size_;
      return clone;
    }
    catch (CloneNotSupportedException exception) {
      assert false : "This will not happen";
      return null;
    }
  }

  /**
   * Ensure that the backing list has enough capacity for the specified
   * number of entries. Extend by at least half its current size whenever full.
   *
   * @param size  Requested size. &gt;0,&gt;.
   */
  private void ensureCapacity(int size)
  {
    assert size > 0 : "Invalid size: " + size;

    int oldCapacity = values_.length;
    if (size > oldCapacity) {
      int newCapacity = Math.max((oldCapacity * 3) / 2 + 1, size);
      values_ = Arrays.copyOf(values_, newCapacity);
    }
  }

  /**
   * Trims the capacity of this <tt>DoubleList</tt> instance to be the
   * list's current size. An application can use this operation to minimize
   * the storage of an <tt>DoubleList</tt> instance.
   * Typically called when the client knows that the list will not grow any further.
   */
  public void trimToSize()
  {
    // Size is not changed, but since the data is moved to a new location
    modificationCount_++;

    int capacity = values_.length;
    if (size_ < capacity)
      values_ = Arrays.copyOf(values_, size_);
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(Double value)
  {
    ensureCapacity(size_ + 1);

    modificationCount_++;

    values_[size_] = value != null ? value : Double.NaN;
    size_++;

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void add(int index, Double value)
  {
    if (index < 0 || index > size_)
      throw new IndexOutOfBoundsException("Invalid index: " + index + " for [0," + (size_ - 1) + "]");

    ensureCapacity(size_ + 1);

    modificationCount_++;

    System.arraycopy(values_, index, values_, index + 1, size_ - index);
    values_[index] = value != null ? value : Double.NaN;
    size_++;
  }

  /** {@inheritDoc} */
  @Override
  public boolean addAll(Collection<? extends Double> values)
  {
    if (values.isEmpty())
      return false;

    ensureCapacity(size_ + values.size());

    modificationCount_++;

    values.forEach(this::add);

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean addAll(int index, Collection<? extends Double> values)
  {
    if (values.isEmpty())
      return false;

    ensureCapacity(size_ + values.size());

    modificationCount_++;

    int d = 0;
    for (Double value : values) {
      add(index + d, value);
      d++;
    }

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void clear()
  {
    modificationCount_++;
    size_ = 0;
  }

  /** {@inheritDoc} */
  @Override
  public int indexOf(Object value)
  {
    // There are only Doubles in the list
    if (value != null && !(value instanceof Double))
      return -1;

    // Return index of first instance of value
    Double v = value == null ? Double.NaN : (Double) value;
    for (int index = 0; index < size_; index++) {
      if (v.equals(values_[index]))
        return index;
    }

    // Not found
    return -1;
  }

  /** {@inheritDoc} */
  @Override
  public int lastIndexOf(Object value)
  {
    // There are only Doubles in the list
    if (value != null && !(value instanceof Double))
      return -1;

    // Return index of last instance of value
    Double v = value == null ? Double.NaN : (Double) value;
    for (int index = size_ - 1; index >= 0; index--) {
      if (v.equals(values_[index]))
        return index;
    }

    // Not found
    return -1;
  }

  /** {@inheritDoc} */
  @Override
  public boolean contains(Object value)
  {
    return indexOf(value) != -1;
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsAll(Collection<?> collection)
  {
    for (Object value : collection) {
      if (!contains(value))
        return false;
    }

    // All is there
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public Double get(int index)
  {
    if (index < 0 || index >= size_)
      throw new IndexOutOfBoundsException("Invalid index: " + index + " for [0," + (size_ - 1) + "]");

    double v = values_[index];
    return Double.isNaN(v) ? null : v;
  }

  /** {@inheritDoc} */
  @Override
  public Double set(int index, Double value)
  {
    if (index < 0 || index >= size_)
      throw new IndexOutOfBoundsException("Invalid index: " + index + " for [0," + (size_ - 1) + "]");

    double oldValue = values_[index];
    values_[index] = value != null ? value : Double.NaN;
    return Double.isNaN(oldValue) ? null : oldValue;
  }

  /** {@inheritDoc} */
  @Override
  public int size()
  {
    return size_;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty()
  {
    return size_ == 0;
  }

  /** {@inheritDoc} */
  @Override
  public Double remove(int index)
  {
    if (index < 0 || index >= size_)
      throw new IndexOutOfBoundsException("Invalid index: " + index + " for [0," + (size_ - 1) + "]");

    modificationCount_++;

    double oldValue = values_[index];

    int nMoved = size_ - index - 1;
    if (nMoved > 0)
      System.arraycopy(values_, index + 1, values_, index, nMoved);

    size_--;

    return Double.isNaN(oldValue) ? null : oldValue;
  }

  /** {@inheritDoc} */
  @Override
  public boolean remove(Object object)
  {
    if (!(object instanceof Double))
      return false;

    int index = indexOf(object);
    if (index == -1)
      return false;

    remove(index);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean removeAll(Collection<?> collection)
  {
    int originalSize = size_;

    for (int index = size_ - 1; index >= 0; index--) {
      Double value = get(index);
      if (collection.contains(value))
        remove(index);
    }

    return size_ < originalSize;
  }

  /** {@inheritDoc} */
  @Override
  public boolean retainAll(Collection<?> collection)
  {
    int originalSize = size_;

    for (int index = size_ - 1; index >= 0; index--) {
      Double value = get(index);
      if (!collection.contains(value))
        remove(index);
    }

    return size_ < originalSize;
  }

  /** {@inheritDoc} */
  @Override
  public Object[] toArray()
  {
    Object[] array = new Object[size_];
    for (int i = 0; i < size_; i++) {
      double value = values_[i];
      array[i] = Double.isNaN(value) ? null : value;
    }

    return array;
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public <Double> Double[] toArray(Double[] array)
  {
    Object[] a = array.length < size_ ? new Object[size_] : (Object[]) array;

    for (int i = 0; i < size_; i++)
      a[i] = get(i);

    return (Double[]) a;
  }

  /** {@inheritDoc} */
  @Override
  public ListIterator<Double> listIterator(int index)
  {
    if (index < 0 || index >= size_)
      throw new IndexOutOfBoundsException("Invalid index: " + index + " for [0," + (size_ - 1) + "]");

    return new DoubleListIterator(index);
  }

  /** {@inheritDoc} */
  @Override
  public ListIterator<Double> listIterator()
  {
    return new DoubleListIterator(0);
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Double> iterator()
  {
    return listIterator();
  }

  /** {@inheritDoc} */
  @Override
  public List<Double> subList(int fromIndex, int toIndex)
  {
    if (fromIndex < 0 || fromIndex > size_ || toIndex > size_ || fromIndex > toIndex)
      throw new IndexOutOfBoundsException("Invalid index: " + fromIndex + "," + toIndex + " for [0," + size_ + "]");

    throw new UnsupportedOperationException("Not supported.");
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return Objects.hash(values_, size_);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object object)
  {
    if (object == this)
      return true;

    if (!(object instanceof List))
      return false;

    ListIterator<Double> e1 = listIterator();
    ListIterator<?> e2 = ((List) object).listIterator();

    while (e1.hasNext() && e2.hasNext()) {
      Double o1 = e1.next();
      Object o2 = e2.next();

      if (o1 == null ? o2 != null : !o1.equals(o2))
        return false;
    }

    return e1.hasNext() || e2.hasNext();
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder("DoubleList[" + size_ + ":" + values_.length + "] = {");
    for (int i = 0; i < size_; i++) {
      if (i > 0)
        s.append(',');

      double value = values_[i];
      s.append(Double.isNaN(value) ? "null" : value);
    }
    s.append('}');

    return s.toString();
  }

  /**
   * A list iterator (forwards/backwards traverse) for the list values
   * in the outer class.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private class DoubleListIterator
    implements ListIterator<Double>
  {
    /** index of next element to return. */
    private int cursor_;

    /** Index of last element returned. -1 if no such. */
    private int lastReturned_ = -1;

    /**
     * The modificationCount_ value that the iterator believes that the
     * backing List should have. If this expectation is violated, the iterator
     * has detected concurrent modification.
     */
    private int expectedModificationCount_ = modificationCount_;

    /**
     * Create a list iterator pointing at the specified element.
     *
     * @param index  Initial element of the iteration. [0,size&gt;.
     */
    private DoubleListIterator(int index)
    {
      assert index >= 0 && index < size_ : "Invalid index: " + index;
      cursor_ = index;
    }

    /**
     * Throw a ConcurrentModificationException if the current modification
     * count is not as expected.
     */
    private void checkForComodification()
    {
      if (modificationCount_ != expectedModificationCount_)
        throw new ConcurrentModificationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext()
    {
      return cursor_ != size_;
    }

    /** {@inheritDoc} */
    @Override
    public int nextIndex()
    {
      return cursor_;
    }

    /** {@inheritDoc} */
    @Override
    public Double next()
    {
      checkForComodification();

      int index = cursor_;
      if (index >= size_)
        throw new NoSuchElementException();

      double[] array = values_;
      if (index >= values_.length)
        throw new ConcurrentModificationException();

      cursor_ = index + 1;

      double value = values_[index];
      lastReturned_ = index;
      return Double.isNaN(value) ? null : value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasPrevious()
    {
      return cursor_ != 0;
    }

    /** {@inheritDoc} */
    @Override
    public int previousIndex()
    {
      return cursor_ - 1;
    }

    /** {@inheritDoc} */
    @Override
    public Double previous()
    {
      checkForComodification();

      int index = cursor_ - 1;
      if (index < 0)
        throw new NoSuchElementException();

      double[] array = values_;
      if (index >= values_.length)
        throw new ConcurrentModificationException();

      cursor_ = index;
      double value = array[index];
      lastReturned_ = index;

      return Double.isNaN(value) ? null : value;
    }

    /** {@inheritDoc} */
    @Override
    public void add(Double value)
    {
      checkForComodification();

      try {
        int index = cursor_;
        DoubleList.this.add(index, value);
        cursor_ = index + 1;
        lastReturned_ = -1;
        expectedModificationCount_ = modificationCount_;
      }
      catch (IndexOutOfBoundsException exception) {
        throw new ConcurrentModificationException();
      }
    }

    /** {@inheritDoc} */
    @Override
    public void set(Double value)
    {
      if (lastReturned_ < 0)
        throw new IllegalStateException();

      checkForComodification();

      try {
        DoubleList.this.set(lastReturned_, value);
        expectedModificationCount_ = modificationCount_;
      }
      catch (IndexOutOfBoundsException exception) {
        throw new ConcurrentModificationException();
      }
    }

    /** {@inheritDoc} */
    @Override
    public void remove()
    {
      if (lastReturned_ < 0)
        throw new IllegalStateException();

      checkForComodification();

      try {
        DoubleList.this.remove(lastReturned_);
        cursor_ = lastReturned_;
        lastReturned_ = -1;
        expectedModificationCount_ = modificationCount_;
      }
      catch (IndexOutOfBoundsException exception) {
        throw new ConcurrentModificationException();
      }
    }
  }
}
