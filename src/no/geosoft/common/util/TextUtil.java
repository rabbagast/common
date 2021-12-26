package no.geosoft.common.util;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.geosoft.common.locale.LocaleManager;

/**
 * A collection of text utilities.
 *
 * <p>
 * <b>Synchronization:</b>
 * <p>
 * This class is thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class TextUtil
{
  /**
   * Private constructor to prevent instantiation.
   */
  private TextUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return a truncated version of the specified string with an ellipsis
   * suffix:
   * <pre>
   *   "This is a long line" -> "This is a ..."
   * </pre>
   *
   * @param string     String to get short version of. Non-null.
   * @param maxLength  Maximum length for string to return. [1,>.
   * @return           A short ellipsis suffixed version of the string,
   *                   or the input string without ellipsis if it is already
   *                   shorter than the specified length. Never null.
   * @throws IllegalArgumentException  If string is null or maxLength < 1.
   */
  public static String truncate(String string, int maxLength)
  {
    if (string == null)
      throw new IllegalArgumentException("string cannot be null");

    if (maxLength < 1)
      throw new IllegalArgumentException("Invalid maxLength: " + maxLength);

    if (string.length() <= maxLength + 3)
      return string;

    return string.substring(0, maxLength) + "...";
  }

  /**
   * Split the specified text in tokens based on the given delimiter.
   * <p>
   * Some special cases:
   * <ul>
   *   <li>Tokens can be in quotes, in case they can contain the delimiter
   *   <li>Quotes is pair of " or ' characters
   *   <li>Token in quotes can contain quote characters of the other type
   *   <li>Tokens are always trimmed unless they are in quotes
   *   <li>If delimiter is space, no empty tokens are returned (unless quoted)
   * </ul>
   * Examples:
   * <pre>
   *   "1 2 3 4"            split on ' ' gives "1","2","3","4"
   *   "   1  2  3 4  "     split on ' ' gives "1","2","3","4"
   *   "   '1 2' 3 '4 ' ''" split on ' ' gives "1 2","3","4 ",""
   *   "1 2 '3"' 4"         split on ' ' gives "1","2","3"","4"
   *   " "                  split on ' ' gives nothing
   *   ","                  split on ',' gives "",""
   *   "1,2,3,4"            split on ',' gives "1","2","3","4"
   *   "  1,  2,  3,  4 "   split on ',' gives "1","2","3","4"
   *   ",,1,2,,3,4,5,,"     split on ',' gives "","","1","2","","3","4","5","",""
   * </pre>
   * Issue: How to handle this case:
   * <pre>
   *   "1 2 '3''4' 5"  split on ' ' gives "1","2","34","5"
   * </pre>
   * Not sure what the optimal solution should be.
   * <p>
   * <b>Performance:</b>
   * <p>
   * For comma (or similar) delimited strings without quotes the String.split()
   * method will behave slightly better (40%). For space delimited strings where
   * "\\s+" is used to suppress additional whitespace, this method is about 3x faster.
   * It is possible to use String.split() with regex to handle quotes, but it is
   * slower, and it will leave the quotes in there so they must be removed afterwards.
   *
   * @param text       Text to split. Non-null.
   * @param delimiter  Delimiter character such as comma or space etc.
   * @return           The tokens of the text. Never null.
   * @throws IllegalArgumentException  If text is null.
   */
  public static List<String> split(String text, char delimiter)
  {
    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    final boolean isSpaceDelimited = delimiter == ' ';

    // Prepare the return array
    List<String> tokens = new ArrayList<>();

    // If not equal '\0' this indicates that we are inside a quoted string
    // and the type of quotes
    char inString = '\0';

    // We don't trim quoted text so keep track if the current is quoted
    boolean isTokenQuoted = false;

    String token;

    // Pointers indicating substring that make up the next token
    int p0 = 0;
    int p1 = 0;

    char prevChar = '\0';
    char currentChar = '\0';

    for (int i = 0; i < text.length(); i++) {
      prevChar = currentChar;
      currentChar = text.charAt(i);

      boolean isQuote = currentChar == '\"' || currentChar == '\'';

      // Start quote
      if (isQuote && inString == '\0') {
        inString = currentChar;
        isTokenQuoted = true;
        p0 = i + 1;
        p1 = p0;
        continue;
      }

      // End quote
      if (isQuote && inString == currentChar) {
        inString = '\0';
        continue;
      }

      // Delimiter that is not inside quotes
      if (currentChar == delimiter && inString == '\0') {
        token = isTokenQuoted ? text.substring(p0, p1) : text.substring(p0, p1).trim();
        if (!token.isEmpty() || !isSpaceDelimited || isTokenQuoted)
          tokens.add(token);

        p0 = isTokenQuoted ? p1 + 2 : p1 + 1;
        p1 = p0;

        isTokenQuoted = false;
        continue;
      }

      p1++;
    }

    // We are done. Capture that last token (if any)
    token = isTokenQuoted ? text.substring(p0, p1) : text.substring(p0, p1).trim();
    if (!token.isEmpty() || !isSpaceDelimited || isTokenQuoted)
      tokens.add(token);

    return tokens;
  }

  /**
   * Return name without extension. The extension is defined as the
   * part from the last "." character (inclusive). If there is no
   * extension, the name is returned as is.
   *
   * @param name  Name to return without extension. Non-null.
   * @return      Name without extension (if any). Never null.
   * @throws IllegalArgumentException  If name is null.
   */
  public static String stripExtension(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    int pos = name.lastIndexOf('.');
    return pos == -1 ? name : name.substring(0, pos);
  }

  /**
   * Return extension of the specified name. The extension is defined as the
   * part from the last "." character (exclusive). If there is no
   * extension and empty string is returned.
   *
   * @param name  Name to get extension of. Non-null.
   * @return      Extension of name. Never null.
   * @throws IllegalArgumentException  If name is null.
   */
  public static String getExtension(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    int pos = name.lastIndexOf('.');
    return pos == -1 ? "" : name.substring(pos + 1);
  }

  /**
   * XML encode the specified string, i.e. make sure XML reserved
   * tags and characters are properly escaped.
   * <p>
   * The following characters are replaced with corresponding character
   * entities :
   * <table border='1' cellpadding='3' cellspacing='0' summary="">
   *   <tr><th> Character </th><th> Encoding </th></tr>
   *   <tr><td> &lt; </td><td> &lt; </td></tr>
   *   <tr><td> &gt; </td><td> &gt; </td></tr>
   *   <tr><td> &amp; </td><td> &amp; </td></tr>
   *   <tr><td> " </td><td> &quot;</td></tr>
   *   <tr><td> ' </td><td> &#039;</td></tr>
   * </table>
   *
   * <P>
   * Note that JSTL's {@code <c:out>} escapes the exact same set of
   * characters as this method. <span class='highlight'>
   * That is, {@code <c:out>} is good for escaping to produce valid XML,
   * but not for producing safe HTML.</span>"
   *
   * @param s  String to encode. Non-null.
   * @return   XML encoded version of s. Never null.
   * @throws IllegalArgumentException  If s is null.
   */
  public static String xmlEncode(String s)
  {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null");

    StringBuilder result = new StringBuilder();

    StringCharacterIterator iterator = new StringCharacterIterator(s);
    char c = iterator.current();
    while (c != CharacterIterator.DONE ){
      if (c == '<') {
        result.append("&lt;");
      }
      else if (c == '>') {
        result.append("&gt;");
      }
      else if (c == '\"') { // "
        result.append("&quot;");
      }
      else if (c == '\'') {
        result.append("&#039;");
      }
      else if (c == '&') {
        result.append("&amp;");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        result.append(c);
      }
      c = iterator.next();
    }

    return result.toString();
  }

  /**
   * HTML encode the specified string, i.e. make sure HTML reserved
   * tags and characters are properly escaped.
   *
   * @param s  String to encode. Non-null.
   * @return   HTML encoded version of s. Never null.
   * @throws IllegalArgumentException  If s is null.
   */
  public static String htmlEncode(String s)
  {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null");

    StringBuilder result = new StringBuilder();

    boolean isNbsp = false;

    for (char c : s.toCharArray()) {
      switch(c) {

        // Since multiple spaces will be trimmed in HTML we use &nbsp; to
        // preserve them all, but since &nbsp; is "non-breaking" we insert
        // it only for every other
        case ' '  : result.append(isNbsp ? "&nbsp;" : " "); break;

        // Other special charaters
        case '<'  : result.append("&lt;"); break;
        case '>'  : result.append("&gt;"); break;
        case '&'  : result.append("&amp;"); break;
        case '"'  : result.append("&quot;"); break;
        case '\n' : result.append("<br>"); break;
        case '\t' : result.append("&nbsp; &nbsp; "); break;

        default:
          // Normal printable character
          if (c < 128)
            result.append(c);

          // Special character
          else
            result.append("&#" + (int) c + ";");
      }

      // Toggle or reset the &nbsp; setting
      isNbsp = c == ' ' ? !isNbsp : false;
    }

    return result.toString();
  }

  /**
   * Unicode encode the specified string, i.e. replace \u9999 entries
   * with their actual Unicode character.
   *
   * @param string  Source string. Non-null.
   * @return        Result string. Never null.
   * @throws IllegalArgumentException  If string is null.
   */
  public static String unicodeEncode(String string)
  {
    if (string == null)
      throw new IllegalArgumentException("string cannot be null");

    int length = string.length();
    StringBuilder result = new StringBuilder(length);

    for (int x = 0; x < length; ) {
      char c = string.charAt(x++);

      if (c == '\\') {
        c = string.charAt(x++);
        if (c == 'u') {
          // Read the xxxx
          int value = 0;

          for (int i = 0; i < 4; i++) {
            c = string.charAt(x++);
            switch (c) {
              case '0': case '1': case '2': case '3': case '4':
              case '5': case '6': case '7': case '8': case '9':
                value = (value << 4) + c - '0';
                break;
              case 'a': case 'b': case 'c':
              case 'd': case 'e': case 'f':
                value = (value << 4) + 10 + c - 'a';
                break;
              case 'A': case 'B': case 'C':
              case 'D': case 'E': case 'F':
                value = (value << 4) + 10 + c - 'A';
                break;
              default:
                value = 0;
            }
          }

          result.append((char) value);
        }
        else {
          if      (c == 't') c = '\t';
          else if (c == 'r') c = '\r';
          else if (c == 'n') c = '\n';
          else if (c == 'f') c = '\f';

          result.append(c);
        }
      }
      else
        result.append(c);
    }

    return result.toString();
  }

  /**
   * Return a string of n consecutive c segments.
   *
   * @param c  String segment to fill string with. Non-null.
   * @param n  Number of times to repeat c. &gt;= 0.
   * @return   String of n consecutive c segments. Never null.
   * @throws IllegalArgumentException  If c is null or n &lt; 0.
   */
  public static String createString(String c, int n)
  {
    if (c == null)
      throw new IllegalArgumentException("c cannot be null");

    if (n < 0)
      throw new IllegalArgumentException("Invalid n: " + n);

    StringBuilder s = new StringBuilder();
    for (int i = 0; i < n; i++)
      s.append(c);

    return s.toString();
  }

  /**
   * Check if the specified string <em>appears</em> to be ASCII text.
   *
   * @param s  String to check. Non-null.
   * @return   True if the string appears to be ASCII, false otherwise.
   * @throws IllegalArgumentException  If s is null.
   */
  public static boolean isAscii(String s)
  {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null");

    Charset asciiCharset = StandardCharsets.US_ASCII;
    CharsetEncoder encoder = asciiCharset.newEncoder();
    return encoder.canEncode(s);
  }

  /**
   * Check if the specified string <em>appears</em> to be EBCDIC text.
   *
   * @param s  String to check. Non-null.
   * @return   True if the string appears to be EBCDIC text, false otherwise.
   * @throws IllegalArgumentException  If s is null.
   */
  public static boolean isEbcdic(String s)
  {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null");

    Charset ebcdicCharset = Charset.forName("IBM500");
    CharsetEncoder encoder = ebcdicCharset.newEncoder();
    return !isAscii(s) && encoder.canEncode(s);
  }

  /**
   * Check if the specified string <em>appears</em> to be binary.
   *
   * @param s  String to check. Non-null.
   * @return   True if the string appears to be binary, false otherwise.
   * @throws IllegalArgumentException  If s is null.
   */
  public static boolean isBinary(String s)
  {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null");

    return !TextUtil.isAscii(s) && !TextUtil.isEbcdic(s);
  }

  /**
   * Count number of lines in the specified string.
   *
   * @param s  String to check. Non-null.
   * @return   Number of lines in s. [0,&gt;.
   * @throws IllegalArgumentException  If s is null.
   */
  public static int getNLines(String s)
  {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null");

    if (s.isEmpty())
      return 0;

    int nLines = 1;

    int pos = 0;
    while (true) {
      pos = s.indexOf("\n", pos) + 1;
      if (pos == 0)
        break;

      nLines++;
    }

    return nLines;
  }

  /**
   * Replace unprintable characters in text by space.
   *
   * @param text  Text to consider. Non-null.
   * @return      Requested text. Never null.
   * @throws IllegalArgumentException  If text is null.
   */
  public static String removeUnprintables(String text)
  {
    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    StringBuilder s = new StringBuilder();

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      s.append(c != '\n' && Character.isISOControl(c) ? ' ' : c);
    }

    return s.toString();
  }

  /**
   * Return the specified number of bytes as a suitable localized string.
   * with unit. We use 1kb = 1000 bytes etc. according to IEC standard.
   *
   * @param nBytes  Number of bytes. [0,&gt;.
   * @return        Textual string with unit. Never null.
   * @throws IllegalArgumentException  If nBytes &lt; 0.
   */
  public static String getNBytesAsString(long nBytes)
  {
    if (nBytes < 0)
      throw new IllegalArgumentException("Invalid nBytes: " + nBytes);

    double BYTES_IN_KB = 1000.0;

    LocaleManager localeManager = LocaleManager.getInstance();
    Locale currentLocale = localeManager.getCurrentLocale();

    String value;
    String unit;

    // bytes
    if (nBytes < BYTES_IN_KB) {
      value = "" + nBytes;
      unit = localeManager.getText(nBytes == 1 ? "byte" : "bytes");
    }

    // kB
    else if (nBytes < BYTES_IN_KB * BYTES_IN_KB) {
      double kb = nBytes / BYTES_IN_KB;
      value = String.format(currentLocale, "%.1f", kb);
      unit = "kB";
    }

    // MB
    else if (nBytes < BYTES_IN_KB * BYTES_IN_KB * BYTES_IN_KB) {
      double mb = nBytes / (BYTES_IN_KB * BYTES_IN_KB);
      value = String.format(currentLocale, "%.1f", mb);
      unit = "MB";
    }

    // GB
    else {
      double gb = nBytes / (BYTES_IN_KB * BYTES_IN_KB * BYTES_IN_KB);
      value = String.format(currentLocale, "%.1f", gb);
      unit = "GB";
    }

    return value + " " + unit;
  }
}
