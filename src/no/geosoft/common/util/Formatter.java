package no.geosoft.common.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A class capable of formatting (i.e write as text) numbers so that
 * they get uniform appearance and can be presented together, typically
 * in a column with decimal symbol aligned etc.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Formatter
{
  /** Default number of significant digits. */
  private static final int N_SIGNIFICANT_DEFAULT = 7;

  /** Default number of decimals. */
  private static final int N_DECIMALS_DEFAULT = 2;

  /** The smallest value before switching to scientific notation. */
  private static final double MIN_NON_SCIENTIFIC = 0.0001;

  /** The largest value before switching to scientific notation. */
  private static final double MAX_NON_SCIENTIFIC = 9999999.0;

  /** The format to use. Non-null. */
  private final DecimalFormat format_;

  /**
   * Create a common formatter for the specified set of numbers.
   *
   * @param values              Representative values to use for creating the formatter.
   *                            Null to create a generic formatter independent of any
   *                            actual values.
   * @param nSignificantDigits  Number of significant digits. Defaults to 7 if null.
   *                            Ignored if nDecimals is specified.
   * @param nDecimals           Number of decimals. If null, decide by significan digits.
   * @param locale              Locale to present numbers in. Null for default.
   */
  public Formatter(double[] values, Integer nSignificantDigits, Integer nDecimals, Locale locale)
  {
    int nActualSignificantDigits = nSignificantDigits != null ? nSignificantDigits : N_SIGNIFICANT_DEFAULT;
    int nActualDecimals = N_DECIMALS_DEFAULT;
    Locale actualLocale = locale != null ? locale : Locale.ROOT;

    double maxValue = 0.0;

    boolean isScientific = false;

    // Maximum number of decimals needed to represent the values provided
    int nMaxDecimalsNeeded = 0;

    //
    // Loop over all the representative values to find the maximum
    // and to check if we should use scientific notation.
    //
    if (values != null) {
      for (int i = 0; i < values.length; i++) {

        double value = values[i];

        // Leave non-printable characters
        if (Double.isNaN(value) || Double.isInfinite(value))
          continue;

        // Work with the absolute value only
        value = Math.abs(value);

        //
        // Check if we should go scientific
        //
        if (value > MAX_NON_SCIENTIFIC || value != 0.0 && value < MIN_NON_SCIENTIFIC) {
          isScientific = true;
          nActualDecimals = nActualSignificantDigits - 1;
          break;
        }

        // Keep track of maximum numeric value of the lot
        if (value > maxValue)
          maxValue = value;

        // Find how many decimals is needed to represent this value correctly
        int nDecimalsNeeded = NumberUtil.countDecimals(value);
        if (nDecimalsNeeded > nMaxDecimalsNeeded)
          nMaxDecimalsNeeded = nDecimalsNeeded;
      }
    }

    //
    // Determine n decimals for the non-scietific case
    //
    if (!isScientific) {
      long wholePart = Math.round(maxValue);
      int length = ("" + wholePart).length();
      nActualDecimals = Math.max(nActualSignificantDigits - length, 0);

      // If there are values provided, and they need fewer decimals
      // than computed, we reduce this
      if (values != null && values.length > 0 && nMaxDecimalsNeeded < nActualDecimals)
        nActualDecimals = nMaxDecimalsNeeded;
    }

    // Override n decimals on users request
    if (nDecimals != null)
      nActualDecimals = nDecimals;

    //
    // Create the format string
    //
    StringBuilder formatString = new StringBuilder();
    if (isScientific) {
      formatString.append("0.E0");
      for (int i = 0; i < nActualDecimals; i++)
        formatString.insert(2, '0');
    }
    else {
      formatString.append(nActualDecimals > 0 ? "0." : "0");
      for (int i = 0; i < nActualDecimals; i++)
        formatString.append('0');
    }

    //
    // Create the actual decimal format
    //
    DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(actualLocale);
    format_ = new DecimalFormat(formatString.toString(), formatSymbols);
  }

  /**
   * Create a common formatter for the specified set of numbers.
   *
   * @param values              Representative values to use for creating the formatter.
   *                            Null to create a generic formatter independent of any
   *                            actual values.
   */
  public Formatter(double[] values)
  {
    this(values, null, null, null);
  }

  /**
   * Create a default number formatter,
   */
  public Formatter()
  {
    this(null, null, null, null);
  }

  /**
   * Format the specified value according to the formatting defined
   * by this formatter.
   *
   * @param value  Value to format,
   * @return       Text representation of the value according to the format.
   */
  public String format(double value)
  {
    // Handle infinity
    if (value == Double.POSITIVE_INFINITY)
      return "\u221e"; // oo

    if (value == Double.NEGATIVE_INFINITY)
      return "-\u221e"; // -oo

    // Handle the non-printable characters
    if (Double.isNaN(value))
      return "";

    // 0.0 is handled specifically to avoid 0.0000 etc
    if (value == 0.0)
      return "0.0";

    return format_.format(value);
  }

  /**
   * Return the back-end decimal format of this formatter.
   *
   * @return  The back-end decimal format of this formatter. Never null.
   */
  public DecimalFormat getFormat()
  {
    return format_;
  }

  /**
   * Return a suitable string representation of the <em>first</em>
   * value of the arguments, based on a format created across all of them.
   * <p>
   * This method is a convenience short hand for explicitly creating a
   * formatter instance for the values and then use it to format its
   * content. Typically used if the number of values are small, like one
   * or two.
   *
   * @param values  The values to use as a template for the format.
   * @return        A string representation for the <em>first</em> value.
   * @throws IllegalArgumentException  If values is null or empty.
   */
  public static String toString(double ... values)
  {
    if (values == null)
      throw new IllegalArgumentException("values cannot be null");

    if (values.length == 0)
      throw new IllegalArgumentException("values cannot be empty");

    Formatter formatter = new Formatter(values);
    return formatter.format(values[0]);
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("99");
    s.append(format_.getDecimalFormatSymbols().getDecimalSeparator());
    for (int i = 0; i < format_.getMinimumFractionDigits(); i++)
      s.append('9');
    return s.toString();
  }
}
