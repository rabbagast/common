package no.geosoft.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class for keeping track of a <em>track</em>; An element
 * collection where we can traverse back and forward like in
 * a web browser.
 * <p>
 * There are different approaches to this. This follows the
 * simplistic version of Google Chrome.
 * <p>
 * Threading: This class is not thread-safe.
 *
 * @param <T>  The <em>type</em> of elements the track contains.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Track<T>
{
  /**
   * Maximum number of elements to keep. If we exceed this limit we
   * remove the last element in the track.
   */
  private final int capacity_;

  /** The track of elements. The first refers to the last one added. */
  private final List<T> track_ = new ArrayList<>();

  /** Current position in the track. */
  private int current_ = -1;

  /**
   * Create a new track of the specified capacity.
   *
   * @param capacity Capacity of track. &lt;0,&gt;.
   * @throws IllegalArgumentException  If capacity &lt;= 0.
   */
  public Track(int capacity)
  {
    if (capacity <= 0)
      throw new IllegalArgumentException("Invalid capacity: " + capacity);

    capacity_ = capacity;
  }

  /**
   * Clear this track.
   */
  public void clear()
  {
    track_.clear();
    current_ = -1;
  }

  /**
   * Add the specified object to the front of the track.
   *
   * @param object  Object to add. Non-null.
   * @throws IllegalArgumentException  If object is null.
   */
  public void add(T object)
  {
    if (object == null)
      throw new IllegalArgumentException("object cannot be null");

    // Remove the ones in the previous track. Not optimal solution,
    // this is the simple Google Chrome approach.
    for (int i = 0; i < current_; i++)
      track_.remove(0);

    track_.add(0, object);
    current_ = 0;

    if (track_.size() > capacity_)
      track_.remove(track_.size() - 1);
  }

  /**
   * Add the specified track to this track.
   *
   * @param track  Track elements to add. First element of the list
   *               will be the first (i.e. current) element in the track.
   */
  public void addAll(List<T> track)
  {
    if (track == null)
      throw new IllegalArgumentException("track cannot be null");

    for (int i = track.size() - 1; i >= 0; i--)
      add(track.get(i));
  }

  /**
   * Return current index in the track. 0 indicates first.
   * <p>
   * Increases when moving back etc. The index is not needed for
   * using the track, but is important if the track is <em>stored</em>.
   *
   * @return  Current index. -1 if the track is empty.
   */
  public int getIndex()
  {
    return current_;
  }

  /**
   * Return the current track element.
   *
   * @return  Current track element, or null if track is empty.
   */
  public T getCurrent()
  {
    return track_.isEmpty() ? null : track_.get(current_);
  }

  /**
   * Check if it is possible to go back.
   *
   * @return  True if it is possible to go back, false otherwise.
   */
  public boolean canGoBack()
  {
    return !track_.isEmpty() && current_ < track_.size() - 1;
  }

  /**
   * Go back along the track and return the new current element.
   *
   * @return  The new current element after going back.
   */
  public T back()
  {
    if (canGoBack())
      current_++;

    return getCurrent();
  }

  /**
   * Check if it is possible to go forward.
   *
   * @return  True if it is possible to go forward, false otherwise.
   */
  public boolean canGoForward()
  {
    return !track_.isEmpty() && current_ > 0;
  }

  /**
   * Go forward along the track and return the new current element.
   *
   * @return  The new current element after going forward.
   */
  public T forward()
  {
    if (canGoForward())
      current_--;

    return getCurrent();
  }

  /**
   * Return all the elements of this track.
   *
   * @return  Return all the elements of this track.
   */
  public List<T> getAll()
  {
    return Collections.unmodifiableList(track_);
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("(");

    for (int i = 0; i < track_.size(); i++) {
      if (i != 0)
        s.append(" - ");

      if (i == current_)
        s.append("[");

      s.append(track_.get(i).toString());

      if (i == current_)
        s.append("]");
    }

    s.append(")");
    return s.toString();
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    Track<String> track = new Track<>(10);
    System.out.println(track);

    track.add("E1");
    track.add("E2");
    track.add("E3");

    System.out.println(track);

    System.out.println("Back: " + track.back());
    System.out.println(track);

    track.add("E4");
    System.out.println(track);
  }
}
