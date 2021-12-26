package no.geosoft.common.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator for properly compare strings containing numbers,
 * such as "8 1/2" and "12 1/4". A conventional string compare will
 * put the latter in front, while the present comparator will sort
 * them according to the user expectation.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class AlphaNumComparator
  implements Comparator<String>, Serializable
{
  /** Total length of padded numbers. */
  private static final int NUM_LENGTH = 10;

  /**
   * Pad all numeric (integer) components in the specified string
   * with zeros to a standard length so that they can then be properly
   * compared using a traditional alphabetical comparator.
   * <p>
   * Public so it can be used in client comparators.
   * <p>
   * Examples:
   * <pre>
   *  "13"         -&gt; "0000000013"
   *  "abc"        -&gt; "abc"
   *  "92abc 3/14" -&gt; "0000000092abc 0000000003/0000000014"
   * </pre>
   *
   * @param string  String to convert. Non-null.
   * @return        Padded string. Never null.
   * @throws IllegalArgumentException  If string is null.
   */
  public static String padNumbers(String string)
  {
    if (string == null)
      throw new IllegalArgumentException("string cannot be null");

    StringBuilder s = new StringBuilder(string);

    Pattern pattern = Pattern.compile("\\d+");
    Matcher matcher = pattern.matcher(string);

    int nPad = 0;

    while (matcher.find()) {
      int index = matcher.start();

      // This is a numeric sub-part in the input string
      String numberString = matcher.group();

      // Create a zero string to pad it with
      int nZeros = NUM_LENGTH - numberString.length();
      String padding = TextUtil.createString("0", Math.max(0, nZeros));

      // Insert the zero padding into the output
      s.insert(index + nPad, padding);

      nPad += padding.length();
    }

    return s.toString();
  }

  /**
   * Compare two strings according to the alpha numeric rules
   * of this class.
   *
   * @param s1  First string to compare. Non-null.
   * @param s2  Second string to compare. Non-null.
   * @return    -1 if first string is less, +1 if second string is less,
   *            0 if they are equal.
   * @throws IllegalArgumentException  If s1 or s2 is null.
   */
  public static int alphaNumCompare(String s1, String s2)
  {
    if (s1 == null)
      throw new IllegalArgumentException("s1 cannot be null");

    if (s2 == null)
      throw new IllegalArgumentException("s2 cannot be null");

    // Alphabetical compare the padded versions
    return padNumbers(s1).compareTo(padNumbers(s2));
  }

  /** {@inheritDoc} */
  @Override
  public int compare(String s1, String s2)
  {
    return AlphaNumComparator.alphaNumCompare(s1, s2);
  }
}
