package no.geosoft.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Class for managing a set of value intervals along an axis.
 * <p>
 * This class is not thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Intervals
{
  /**
   * Set of intervals. Each interval is a pair of two: a start and an
   * end value. Intervals are non-overlapping and sorted from left to right.
   */
  private final TreeSet<Double[]> intervals_ = new TreeSet<>(new IntervalComparator());

  /**
   * Create a new empty intervals instance.
   */
  public Intervals()
  {
    // Nothing
  }

  /**
   * Remove all intervals.
   */
  public void clear()
  {
    intervals_.clear();
  }

  /**
   * Check if the intervals is empty
   *
   * @return  True if the intervals is empty, false otherwise.
   */
  public boolean isEmpty()
  {
    return intervals_.isEmpty();
  }

  /**
   * Return minimum value of the intervals.
   *
   * @return  Minimum value. NaN if empty.
   */
  public double getMin()
  {
    return !intervals_.isEmpty() ? intervals_.first()[0] : Double.NaN;
  }

  /**
   * Return maximum value of the intervals.
   *
   * @return  Minimum value. NaN if empty.
   */
  public double getMax()
  {
    return !intervals_.isEmpty() ? intervals_.last()[1] : Double.NaN;
  }

  /**
   * Return the intervals.
   *
   * @return  The intervals. Never null.
   */
  public List<Double[]> getIntervals()
  {
    List<Double[]> intervals = new ArrayList<>();

    for (Double[] interval : intervals_)
      intervals.add(new Double[] {interval[0], interval[1]});

    return intervals;
  }

  /**
   * Check if the specified value is contained in this intervals.
   *
   * @param x  Value to check.
   * @return   True if x is contains in the intervals, false otherwise.
   */
  public boolean contains(double x)
  {
    for (Double[] interval : intervals_) {
      if (x >= interval[0] && x <= interval[1])
        return true;
    }

    return false;
  }

  /**
   * Return number of intervals.
   *
   * @return  Number of intervals.
   */
  public int getNIntervals()
  {
    return intervals_.size();
  }

  /**
   * Add the specified interval to this instance.
   *
   * @param x0  One end of interval to add.
   * @param x1  Other end of interval to add.
   */
  public void add(double x0, double x1)
  {
    // Nothing to do if the values is not an interval
    if (x0 == x1)
      return;

    // Sort
    double xMin = Math.min(x0, x1);
    double xMax = Math.max(x0, x1);

    for (Iterator<Double[]> i = intervals_.iterator(); i.hasNext(); ) {
      Double[] interval = i.next();

      //
      // Case 1: The new interval is inside an existing one
      //         => Then there is nothing to do.
      //
      if (xMin >= interval[0] && xMax <= interval[1])
        return;

      //
      // Case 2: The new interval completely covers the existing
      //         one => We remove the existing one, and add once more.
      //
      if (xMin <= interval[0] && xMax >= interval[1]) {
        i.remove();
        add(xMin, xMax);
        return;
      }

      //
      // Case 3: The new interval is partly into the low end of an existing one.
      //         => Remove the existing, Adjust the extent on the new
      //         and add again.
      //
      if (xMin < interval[0] && xMax >= interval[0] && xMax <= interval[1]) {
        i.remove();
        add(xMin, interval[1]);
        return;
      }

      //
      // Case 4: Same as case 3, but at the high end.
      //
      if (xMax > interval[1] && xMin <= interval[1] && xMin >= interval[0]) {
        i.remove();
        add(interval[0], xMax);
        return;
      }
    }

    intervals_.add(new Double[] {xMin, xMax});
  }

  /**
   * Remove the specified interval from this instance.
   *
   * @param x0  One end of interval to remove.
   * @param x1  Other end of interval to remove.
   */
  public void remove(double x0, double x1)
  {
    // Nothing to do if the values is not an interval
    if (x0 == x1)
      return;

    // Sort
    double xMin = Math.min(x0, x1);
    double xMax = Math.max(x0, x1);

    for (Iterator<Double[]> i = intervals_.iterator(); i.hasNext(); ) {
      Double[] interval = i.next();

      //
      // Case 1: The removed interval is inside an existing one
      //         => Replace existing with two parts.
      //
      if (xMin > interval[0] && xMax < interval[1]) {
        i.remove();
        add(interval[0], xMin);
        add(xMax, interval[1]);
        return;
      }

      //
      // Case 2: The removed interval completely covers the existing one
      //         => Remove existing one, and remove again.
      //
      if (xMin <= interval[0] && xMax >= interval[1]) {
        i.remove();
        remove(xMin, xMax);
        return;
      }

      //
      // Case 3: The removed interval is half way into an existing one.
      //         => Remove existing one, add the missing piece, and remove again.
      //
      if (xMin < interval[0] && xMax > interval[0] && xMax <= interval[1]) {
        i.remove();
        add(xMax, interval[1]);
        remove(xMin, xMax);
        return;
      }

      //
      // Case 4: Same as case 3, but at the other end.
      //
      if (xMax > interval[1] && xMin < interval[1] && xMin >= interval[0]) {
        i.remove();
        add(interval[0], xMin);
        remove(xMin, xMax);
        return;
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("Intervals:\n");
    for (Double[] interval : intervals_)
      s.append("  " + String.format("%.2f", interval[0]) + "-" + String.format("%.2f", interval[1]) + "\n");

    return s.toString();
  }

  /**
   * Class for comparing intervals. Left-most interval sorts first.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private class IntervalComparator implements Comparator<Double[]>
  {
    /** {@inheritDoc} */
    @Override
    public int compare(Double[] interval1, Double[] interval2)
    {
      return Double.compare(interval1[0], interval2[0]);
    }
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    Intervals in = new Intervals();
    in.add(10, 50);
    in.add(30, 70);
    System.out.println(in);
  }
}
