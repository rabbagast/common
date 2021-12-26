package no.geosoft.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Find "nice numbers" within an interval. The interval is given by
 * a min and a max value:
 *
 *   NiceNumbers niceNumbers = new NiceNumbers(min, max, n, false);
 *
 * Then the application can iterate over the nice numbers generated.
 *
 * This class is handy for producing quality annotation on graphic
 * displays, for instance along an axis.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class NiceNumbers
  implements Iterable<NiceNumbers.Tick>, Iterator<NiceNumbers.Tick>
{
  /**
   * Flag if this is a bounded interval, i.e. if min and max should
   * should be reported as major ticks regardless of their niceness.
   */
  private final boolean isBounded_;

  /** Interval from value. */
  private final double fromValue_;

  /** Interval from value. */
  private final double toValue_;

  /** First nice number outside of or equal to fromValue_. */
  private final double firstValue_;

  /** First nice number outside of or equal to toValue_. */
  private final double lastValue_;

  /** Step between major ticks. */
  private final double majorStep_;

  /** Step between minor ticks. */
  private final double minorStep_;

  /** Actual step between reported ticks. */
  private final double step_;

  /** Number of ticks. */
  private final int nValues_;

  /** Current value during iteration. */
  private int valueNo_;

  /**
   * Create a nice number instance.
   *
   * @param fromValue       From value.
   * @param toValue         To value.
   * @param nNumbersApprox  Approximate number of intermediates to report.
   * @param isBounded       Should fromValue and toValue be absolute end
   *                        points and hence reported as nice numbers?
   */
  public NiceNumbers(double fromValue, double toValue, int nNumbersApprox,
                     boolean isBounded)
  {
    if (nNumbersApprox <= 0)
      throw new IllegalArgumentException("Illegal nNumbersApprox: " +
                                         nNumbersApprox);

    fromValue_ = fromValue;
    toValue_   = toValue;
    isBounded_ = isBounded;

    double step = (toValue_ - fromValue_) / nNumbersApprox;
    if (step == 0.0 || !Double.isFinite(step)) step = 1.0;

    boolean isAscending = step > 0.0;

    // Scale abs(step) to interval 1 - 10
    double scaleFactor = 1.0;
    while (Math.abs(step) > 10.0) {
      step /= 10.0;
      scaleFactor *= 10.0;
    }
    while (Math.abs(step) < 1.0) {
      step *= 10.0;
      scaleFactor /= 10.0;
    }

    // Find nice major step value
    double majorStep = Math.abs(step);
    if      (majorStep > 7.5) majorStep = 10.0;
    else if (majorStep > 3.5) majorStep =  5.0;
    else if (majorStep > 2.0) majorStep =  2.5;
    else                      majorStep =  1.0;

    // Find corresponding minor step value
    double minorStep;
    if      (majorStep == 10.0) minorStep = 5.0;
    else if (majorStep ==  5.0) minorStep = 1.0;
    else if (majorStep ==  2.5) minorStep = 0.5;
    else                        minorStep = 0.25;

    if (step < 0) {
      majorStep = -majorStep;
      minorStep = -minorStep;
    }

    majorStep *= scaleFactor;
    minorStep *= scaleFactor;

    step_ = minorStep != 0.0 ? minorStep : majorStep;

    // Find first nice value before fromValue
    double firstValue = ((int) (fromValue_ / majorStep)) * majorStep;
    if (isAscending && firstValue > fromValue_)
      firstValue -= majorStep;
    else if (!isAscending && firstValue < fromValue_)
      firstValue -= majorStep;
    firstValue_ = firstValue;

    // Find last nice value after toValue
    double lastValue = ((int) (toValue_ / majorStep)) * majorStep;
    if (isAscending && lastValue < toValue_)
      lastValue += majorStep;
    else if (!isAscending && lastValue > toValue_)
      lastValue += majorStep;
    lastValue_ = lastValue;

    // Move the steps from value space to count space
    majorStep_ = (double) Math.round(majorStep / step_);
    minorStep_ = (double) Math.round(minorStep / step_);

    // Find total number of values
    nValues_ = (int) Math.round((lastValue_ - firstValue_) / step_) + 1;
  }

  /**
   * Return closest nice-number for the specified value.
   *
   * @param value  Value to get nice number of.
   * @return       Nice number.
   */
  public static double get(double value)
  {
    if (Double.isNaN(value))
      return value;

    if (Double.isInfinite(value))
      return value;

    // TODO: Implement using log10
    double scaleFactor = 1.0;
    while (Math.abs(value) > 10.0) {
      value /= 10.0;
      scaleFactor *= 10.0;
    }
    while (Math.abs(value) < 1.0) {
      value *= 10.0;
      scaleFactor /= 10.0;
    }

    double niceValue = Math.abs(value);
    if      (value > 7.5) niceValue = 10.0;
    else if (value > 3.5) niceValue =  5.0;
    else if (value > 2.0) niceValue =  2.5;
    else                  niceValue =  1.0;

    return niceValue * scaleFactor;
  }

  /**
   * Return a tick-iterator of this nice number scale.
   *
   * @return  A tick-iterator of this nice number scale.
   */
  public Iterator<Tick> iterator()
  {
    valueNo_ = 0;
    return this;
  }

  /**
   * Check if there is more ticks.
   *
   * @return  True if there are more ticks, false otherwise.
   */
  public boolean hasNext()
  {
    return valueNo_ < nValues_;
  }

  /**
   * Return first nice number.
   *
   * @return  First nice number.
   */
  public double getFirstValue()
  {
    return isBounded_ ? fromValue_ : firstValue_;
  }

  /**
   * Return last nice number.
   *
   * @return  Last nice number.
   */
  public double getLastValue()
  {
    return isBounded_ ? toValue_ : lastValue_;
  }

  /**
   * Return total number of values in the interval.
   *
   * @return  Total number of values. [0,&gt;.
   */
  public int getNValues()
  {
    return nValues_;
  }

  /**
   * Return the next tick.
   *
   * @return  The next tick.
   */
  public Tick next()
  {
    if (!hasNext())
      throw new NoSuchElementException();

    // Solve the bounded case
    if (isBounded_) {
      if (valueNo_ == 0) {
        double value = firstValue_;
        while ((fromValue_ < toValue_ && value <= fromValue_) ||
               (fromValue_ > toValue_ && value >= fromValue_)) {
          valueNo_++;
          value = firstValue_ + valueNo_ * step_;
        }

        return new Tick(fromValue_, true, 0.0);
      }
      else {
        double value = firstValue_ + valueNo_ * step_;
        if ((fromValue_ < toValue_ && value >= toValue_) ||
            (fromValue_ > toValue_ && value <= toValue_)) {
          valueNo_ = nValues_;
          return new Tick(toValue_, true, 1.0);
        }
      }
    }

    double value = firstValue_ + valueNo_ * step_;

    boolean isMajor = valueNo_ % (int) majorStep_ == 0.0;

    // Find position
    double first = getFirstValue();
    double last  = getLastValue();
    double position = first == last ? 0.0 : (value - first) / (last - first);

    // May happen due to overflow errors
    if (position < 0.0) position = 0.0;
    if (position > 1.0) position = 1.0;

    Tick tick = new Tick(value, isMajor, position);

    valueNo_++;

    return tick;
  }

  /**
   * From the Iterator interface.
   *
   * We don't support this operation so just throw an exception.
   */
  public void remove()
  {
    throw new UnsupportedOperationException("remove is not supported");
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return firstValue_ + " - " + lastValue_;
  }

  /**
   * Class representing one nice number tick along a scale.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  public static final class Tick
  {
    /** Numeric value of this tick. */
    private final double value_;

    /** Flag indicating if this is a major or a minor tick. */
    private final boolean isMajor_;

    /** Realtive position along scale. First is 0.0, last is 1.0 */
    private final double position_;

    /**
     * Create a new tick.
     *
     * @param value     Value of tick.
     * @param isMajor   True if this is a major tick.
     * @param position  Relative position along scale.
     */
    public Tick(double value, boolean isMajor, double position)
    {
      if (position < 0.0 || position > 1.0)
        throw new IllegalArgumentException("Invalid position: " + position);

      value_ = value;
      isMajor_ = isMajor;
      position_ = position;
    }

    /**
     * Return value of this tick.
     *
     * @return  Value of this tick.
     */
    public double getValue()
    {
      return value_;
    }

    /**
     * Check if this is a major or minor tick.
     *
     * @return  True if this is a major tick, false it is a minor tick.
     */
    public boolean isMajor()
    {
      return isMajor_;
    }

    /**
     * Return relative position of this tick along scale.
     *
     * @return  Relative position of this tick along scale.
     */
    public double getPosition()
    {
      return position_;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      return (isMajor_ ? "==" : "-") + value_;
    }
  }

  public static void main(String[] arguments)
  {
    System.out.println(NiceNumbers.get(7499));
  }
}
