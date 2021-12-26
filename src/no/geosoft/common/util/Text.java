package no.geosoft.common.util;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A class holding a (portion) of some line based text.
 * <p>
 * Convenient for holding the content of a text file,
 * and a portion of this if the file becomes very large.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Text
{
  /** The text portion lines, sorted by line number. */
  private final SortedMap<Integer,String> lines_ = new TreeMap<>();

  /**
   * Create an empty text.
   */
  public Text()
  {
    // Nothing
  }

  /**
   * Convenience constructor to create a text instance from
   * a complete text string.
   *
   * @param string  String to create text instance from. Non-null.
   * @throws IllegalArgumentException  If string is null.
   */
  public Text(String string)
  {
    if (string == null)
      throw new IllegalArgumentException("string cannot be null");

    String[] allLines = string.split("\n");

    int nLines = allLines.length;
    int nInitialLines = Math.min(nLines, 100);
    int nEndingLines = Math.min(nLines - nInitialLines, 10);
    int nSkippedLines = nLines - nInitialLines - nEndingLines;

    if (nSkippedLines <= nInitialLines + nEndingLines) {
      nInitialLines = nLines;
      nEndingLines = 0;
    }

    for (int i = 1; i <= nInitialLines; i++)
      setLine(i, allLines[i - 1]);

    for (int i = nLines - nEndingLines; i <= nLines; i++)
      setLine(i, allLines[i - 1]);
  }

  /**
   * Set a specific line of this text.
   *
   * @param lineNo  Line number to set. 1-based. [1,&gt;
   * @param line    Line to set. Non-null.
   * @throws IllegalArgumentException  If lineNo &lt; 1 or line is null.
   */
  public void setLine(int lineNo, String line)
  {
    if (lineNo < 1)
      throw new IllegalArgumentException("Invalid lineNo: " + lineNo);

    if (line == null)
      throw new IllegalArgumentException("line cannot be null");

    lines_.put(lineNo, line);
  }

  /**
   * Return total number of lines in the text.
   * <p>
   * Not all the lines may be present in this instance.
   *
   * @return  Total number of lines of the text. [0,&gt;
   */
  public int getNLines()
  {
    return lines_.isEmpty() ? 0 : lines_.lastKey();
  }

  /**
   * Return the lines of this text as a sorted map
   * on line number (1-based).
   *
   * @return The lines of this text.
   */
  public Map<Integer,String> getLines()
  {
    return Collections.unmodifiableMap(lines_);
  }

  /**
   * Return the (existing) text of this text instance.
   *
   * @return  The text of this text instance. Never null.
   */
  public String getText()
  {
    StringBuilder s = new StringBuilder();
    lines_.forEach((lineNo, line) -> s.append(line + '\n'));
    return s.toString();
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();

    int previousLineNo = 0;

    for (Map.Entry<Integer,String> entry : lines_.entrySet()) {
      int lineNo = entry.getKey();
      String line = entry.getValue();

      int nSkippedLines = lineNo - previousLineNo - 1;
      if (nSkippedLines > 0) {
        s.append(":\n");
        s.append(": " + nSkippedLines  + " lines skipped\n");
        s.append(":\n");
      }

      s.append(lineNo + " ");
      s.append(line);
      s.append("\n");

      previousLineNo = lineNo;
    }

    return s.toString();
  }
}
