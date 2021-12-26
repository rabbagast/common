package no.geosoft.common.util;

/**
 * A collection of useful utilities for handling numbers.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class NumberUtil
{
  /**
   * Compare two floating point numbers for equality.
   *
   * @param v1  First number to compare. May be null.
   * @param v2  Second number to compare. May be null.
   * @param e   Relative epsilon such as 0.0001 (will give 10000 == 10001) etc. [0,&lt;
   * @return    True if the two are equal within some reasonable margin, false otherwise.
   * @throws IllegalArgumentException  If e is invalid.
   */
  public static boolean isEqual(Number v1, Number v2, double e)
  {
    if (e < 0.0)
      throw new IllegalArgumentException("Invalid e: " + e);

    if (v1 == v2)
      return true;

    if (v1 == null || v2 == null)
      return false;

    double d1 = v1.doubleValue();
    double d2 = v2.doubleValue();

    double m = Math.max(Math.abs(d1), Math.abs(d2));
    if (m == 0.0)
      return true;

    double d = Math.abs(d1 - d2);

    return d < m * Math.abs(e);
  }

  /**
   * Return the number of digits in the specified number.
   *
   * @param v  Number to count digits in.
   * @return   Number of digits in v. [1,&gt;.
   */
  public static int countDigits(long v)
  {
    // Twice as fast as (Math.abs(v) + "").length()
    return v == 0 ? 1 : (int) Math.log10(Math.abs(v)) + 1;
  }

  /**
   * Return number of <em>significant decimals</em> in the specified
   * floating point value.
   * <p>
   * Example:
   * <pre>
   *   0.991 = 3
   *   0.9901 = 4
   *   0.99001 = 5
   *   0.990001 = 6
   *   0.9900001 = 2
   * </pre>
   * However, if the whole part is large this will influence the result
   * as there is a maximum total number of significant digits:
   * <pre>
   *   12345678.991 = 3
   *   12345678.9901 = 4
   *   12345678.99001 = 4   // ideally 2 but we don't capture this
   *   12345678.990001 = 4  // ideally 2 but we don't capture this
   * </pre>
   *
   * @param d  Number to check.
   * @return   Number of significant decimals. [0,&gt;.
   */
  public static int countDecimals(double d)
  {
    if (!Double.isFinite(d))
      return 0;

    // We strip away sign and the whole part as we care about
    // the decimals only. Left is something like 0.12....
    d = Math.abs(d);
    long wholePart = Math.round(d);
    int nSignificant = countDigits(wholePart);
    double fractionPart = Math.abs(d - wholePart);

    // We start with the fraction, say 0.12345678 and loop
    // over it 10x at the time, so for this example we will get:
    //   0.12345678
    //   1.2345678
    //   12.345678
    //   123.45678
    //   :
    int nDecimals = 0;
    int order = 1;
    while (true) {

      // This is the full floating point number and the integer part
      // (rounded) i.e. 12.789 and 13 etc.
      double floating = fractionPart * order;
      long whole = Math.round(floating);

      // We find the difference of the two, like 0.211 and find
      // what is the fraction of this with the whole.
      double difference = Math.abs(whole - floating);
      double fraction = whole != 0.0 ? difference / whole : difference;

      // If this fraction is very small then the fractional part of the
      // number no longer contributes significantly to the whole and we
      // conclude that the rest is not significant decimals of this number.
      if (fraction < 0.0001)
        break;

      // If we reach maximum number of signigicant digit the computer
      // can adequately represent we stop. We use 12 as a conservative limit.
      // 1234567890.12345678 is 1234567890.12 and nDecimals are 2.
      if (nSignificant >= 12)
        break;

      order *= 10;
      nDecimals++;
      nSignificant++;
    }

    return nDecimals;
  }
}
