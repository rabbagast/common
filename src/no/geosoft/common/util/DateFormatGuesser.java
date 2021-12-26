package no.geosoft.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class for guessing the time/date from a text string,
 * by applying different possible format strings.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class DateFormatGuesser
{
  /**
   * The different date formats to check.
   * Add more if required, but beware of order: The more specific ones should appear first.
   */
  private final static String[] TIME_FORMATS = {
    "HH:mm:ss",
    "dd-MMM-yyyy",
    "dd-MMM-yy",
    "dd/MMMM/yyyy",
    "dd/MM/yy-HH:mm:ss",
    "HH:mm:ss/dd-MMM-yy",
    "HH:mm:ss/dd-MMM-yyyy",
    "MM/dd/yyyy",
    "dd.MM.yyyy",
    "dd.MM.yyyy HH:mm",
    "dd.MM.yyyy HH:mm:ss",
    "yy/MM/dd",
    "yy-MM-dd"
  };

  /**
   * Private constructor to prevent client instantiation.
   */
  private DateFormatGuesser()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * If we assume the specified value is a time/date string, try
   * to determine its <em>format</em>.
   *
   * @param timeString  Time/date string. Non-null.
   * @return            The format of the input, or null if none is matching.
   * @throws IllegalArgumentException  If timeString is null.
   */
  public static DateFormat guessFormat(String timeString)
  {
    if (timeString == null)
      throw new IllegalArgumentException("valud cannot be null");

    // Remove spaces at ends and duplicate spaces within
    timeString = timeString.trim();
    timeString = timeString.replaceAll("\\s+", " ");

    for (String format : TIME_FORMATS) {
      // TODO: This is not entirely correct as it will fail with
      // long month names when it should match. However, it seems
      // difficult to find a matching method that enforce the use
      // of the entire value string AND format string, even when
      // using ParsePosition
      if (format.length() != timeString.length())
        continue;

      DateFormat timeFormat = new SimpleDateFormat(format, Locale.US);
      timeFormat.setLenient(false);
      timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

      try {
        timeFormat.parse(timeString);
        return timeFormat;
      }
      catch (ParseException exception) {
        // Not matching. Continue with the next attempt.
      }
    }

    // None found
    return null;
  }
}

