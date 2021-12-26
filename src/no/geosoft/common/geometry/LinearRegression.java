package no.geosoft.common.geometry;

/**
 * Compute slope and y-axis interception, being a and b in
 * <pre>
 *       y = a x + b
 * </pre>
 * being the <em>regression</em> (best fit) line of the data set.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class LinearRegression
{
  /**
   * Private constructor to prevent client instantaition.
   */
  private LinearRegression()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Performs a linear regression on the data points {@code (x[i], y[i])}.
   *
   * @param x  The values along the x-axis. Non-null.
   * @param y  The values along the y-axis. Non-null.
   * @return   Array of two: slope and y-axis interception for the regression line.
   * @throws IllegalArgumentException  If x or y is null or is they have different lengths.
   */
  public static double[] compute(double[] x, double[] y)
  {
    if (x == null)
      throw new IllegalArgumentException("x cannot be null");

    if (y == null)
      throw new IllegalArgumentException("y cannot be null");

    if (x.length != y.length)
      throw new IllegalArgumentException("Invalid array length");

    if (x.length == 0)
      return new double[] {Double.NaN, Double.NaN};

    int n = x.length;

    // TODO: See if it is possible to do this progressive to
    // avoid inaccuracy caused by huge numbers

    double xSum = 0.0;
    double ySum = 0.0;
    double xxSum = 0.0;
    for (int i = 0; i < n; i++) {
      xSum += x[i];
      xxSum += x[i] * x[i];
      ySum += y[i];
    }
    double xBar = xSum / n;
    double yBar = ySum / n;

    double xxBar = 0.0;
    double yyBar = 0.0;
    double xyBar = 0.0;
    for (int i = 0; i < n; i++) {
      xxBar += (x[i] - xBar) * (x[i] - xBar);
      yyBar += (y[i] - yBar) * (y[i] - yBar);
      xyBar += (x[i] - xBar) * (y[i] - yBar);
    }

    double slope = xyBar / xxBar;
    double interception = yBar - slope * xBar;

    return new double[] {slope, interception};
  }
}
