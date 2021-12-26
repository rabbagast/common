package no.geosoft.common.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.geosoft.common.locale.LocaleManager;

/**
 * Utility class with static date and time functionality.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class DateTimeUtil
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private DateTimeUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return a given millisecond interval as a descriptive text, such
   * as 2:13:02,100 or 01:15 etc. Hours and milliseconds are included as
   * needed; Minutes and seconds are always present.
   *
   * @param timeInterval  Time interval in milliseconds. &gt;= 0.
   * @return              Interval in text. Never null.
   * @throws IllegalArgumentException  If timeInterval is &lt; 0.
   */
  public static String getTimeIntervalAsText(long timeInterval)
  {
    if (timeInterval < 0L)
      throw new IllegalArgumentException("Invalid timeInterval: " + timeInterval);

    long hours = timeInterval / 1000 / 60 / 60;
    long minutes = timeInterval / 1000 / 60 - hours * 60;
    long seconds = timeInterval / 1000 - 60 * (hours * 60 + minutes);
    long milliseconds = timeInterval - 1000 * (hours * 60 * 60 + minutes * 60 + seconds);

    StringBuilder s = new StringBuilder();

    // Hours
    if (hours > 0)
      s.append(hours + ":");

    // Minutes
    if (hours > 0 && minutes < 10)
      s.append("0");
    s.append(minutes + ":");

    // Seconds
    if (seconds < 10)
      s.append("0");
    s.append(seconds + "");

    // Milliseconds
    if (milliseconds > 0) {
      s.append(",");
      if (milliseconds < 100)
        s.append("0");
      if (milliseconds < 10)
        s.append("0");
      s.append(milliseconds);
    }

    return s.toString();
  }

  /**
   * Convert a text (according to the format defined by getTimeIntervalAsText())
   * to the equivalent number of milliseconds.
   * <p>
   * This is the format:
   * <pre>
   *   hours:minutes:seconds,milliseconds
   * </pre>
   * where each of the initial parts may be absent.
   * <p>
   * Examples (whitespace added for clarity only):
   * <pre>
   *   "          4" = 4 ms
   *   "      1,004" = 1004 ms
   *   "   1:01,004" = 61004 ms
   *   "1:01:01,004" = 3661004 ms
   *   "1:01:01    " = 3661000 ms
   "   "   1:01    " = 61000 ms
   * </pre>
   *
   * @param text  Text to convert. Non-null.
   * @return      Equivalent number of milliseconds. [0,&gt;.
   * @throws IllegalArgumentException  If text is null.
   * @throws ParseException            If the text cannot be parsed according to the format.
   */
  public static long getTimeIntervalAsMilliseconds(String text)
    throws ParseException
  {
    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    text = text.trim();

    Pattern pattern = Pattern.compile("^(((?<hours>\\d+):)?(?<minutes>\\d+):)?" +
                                      "(?<sorms>\\d+)" +
                                      "(,(?<milliseconds>\\d+))?$");
    Matcher matcher = pattern.matcher(text);

    boolean isMatching = matcher.find();
    if (!isMatching)
      throw new ParseException(text, 0);

    String hours = matcher.group("hours");
    String minutes = matcher.group("minutes");
    String s_or_ms = matcher.group("sorms");
    String milliseconds = matcher.group("milliseconds");

    long ms = 0L;

    try {
      if (milliseconds != null)
        ms += Long.parseLong(milliseconds);

      if (minutes == null && milliseconds == null)
        ms += Long.parseLong(s_or_ms);
      else
        ms += Long.parseLong(s_or_ms) * 1000;

      if (minutes != null)
        ms += Long.parseLong(minutes) * 1000 * 60;

      if (hours != null)
        ms += Long.parseLong(hours) * 1000 * 60 * 60;
    }
    catch (NumberFormatException exception) {
      assert false : "This cannot happen if the regex is correct";
    }

    return ms;
  }

  /**
   * Get a textual fuzzy difference in time between two given times.
   *
   * <pre>
   *   Difference     reports as
   *   ------------------------------
   *   &lt;2 seconds    n milliseconds
   *   &lt;2 minutes    n seconds
   *   &lt;2 hours      n minutes
   *   &lt;2 days       n hours
   *   &lt;2 weeks      n days
   *   &lt;2 months     n weeks
   *   &lt;2 years      n months
   *   otherwise      n years
   * </pre>
   *
   * @param time1  First time to compare.
   *               Order of time1 and time2 is insignificant.
   * @param time2  Second time to compare.
   *               Order of time1 and time2 is insignificant.
   * @return       Textual difference between the two days. Never null.
   */
  public static String fuzzyDifference(long time1, long time2)
  {
    long difference = Math.abs(time1 - time2);

    long nMilliseconds = difference;
    long nSeconds = nMilliseconds / 1000;
    long nMinutes = nSeconds / 60;
    long nHours = nMinutes / 60;
    long nDays = nHours / 24;
    long nWeeks = nDays / 7;
    long nMonths = nDays / 30;
    long nYears = nDays / 365;

    LocaleManager localeManager = LocaleManager.getInstance();

    if (nSeconds < 2)
      return nMilliseconds + " " + localeManager.getText("milliseconds");

    if (nMinutes < 2)
      return nSeconds + " " + localeManager.getText("seconds");

    if (nHours < 2)
      return nMinutes + " " + localeManager.getText("minutes");

    if (nDays < 2)
      return nHours + " " + localeManager.getText("hours");

    if (nWeeks < 2)
      return nDays + " " + localeManager.getText("days");

    if (nMonths < 2)
      return nWeeks + " " + localeManager.getText("weeks");

    if (nYears < 2)
      return nMonths + " " + localeManager.getText("months");

    return nYears + " " + localeManager.getText("years");
  }

  /**
   * Return a time interval in milliseconds as a fuzzy textual
   * description.
   *
   * <pre>
   *   Time interval    reports as
   *   ------------------------------
   *   &lt;2 seconds    n milliseconds
   *   &lt;2 minutes    n seconds
   *   &lt;2 hours      n minutes
   *   &lt;2 days       n hours
   *   &lt;2 weeks      n days
   *   &lt;2 months     n weeks
   *   &lt;2 years      n months
   *   otherwise        n years
   * </pre>
   *
   * @param time  Time interval to consider.
   * @return      Time interval as a text string.
   */
  public static String toFuzzyString(long time)
  {
    return fuzzyDifference(0L, time);
  }

  /**
   * Return a specified time interval as a textual string like:
   * <pre>
   *  "2 hours 5 minutes"
   *  "3 days 7 hours"
   *  "27 seconds"
   * </pre>
   *
   * etc.
   *
   * @param milliseconds       The time interval to report in milliseconds. &gt; 0.
   * @param reportYears        If # years should be part of the string.
   * @param reportMonths       If # months should be part of the string.
   * @param reportWeeks        If # weeks should be part of the string.
   * @param reportDays         If # days should be part of the string.
   * @param reportHours        If # hours should be part of the string.
   * @param reportMinutes      If # minutes should be part of the string.
   * @param reportSeconds      If # seconds should be part of the string.
   * @param reportMilliseconds If # milliseconds should be part of the string.
   * @return  String representation as requested. Never null.
   */
  public static String getTimeIntervalAsText(long milliseconds,
                                             boolean reportYears,
                                             boolean reportMonths,
                                             boolean reportWeeks,
                                             boolean reportDays,
                                             boolean reportHours,
                                             boolean reportMinutes,
                                             boolean reportSeconds,
                                             boolean reportMilliseconds)
  {
    if (milliseconds < 0L)
      throw new IllegalArgumentException("invalid milliseconds: " +
                                         milliseconds);

    StringBuilder s = new StringBuilder();

    long timeLeft = milliseconds;

    //
    // Years
    //
    if (reportYears) {
      long nYears = timeLeft / 1000L / 60L / 60L / 24L / 365L;
      if (nYears > 0) {
        s.append(nYears);
        s.append(" ");
        s.append(nYears == 1 ? "year" : "years");
        s.append(" ");

        timeLeft -= nYears * 1000L * 60L * 60L * 24L * 365L;
      }
    }

    //
    // Months
    //
    if (reportMonths) {
      long nMonths = timeLeft / 1000L / 60L / 60L / 24L / 30L;
      if (nMonths > 0) {
        s.append(nMonths);
        s.append(" ");
        s.append(nMonths == 1 ? "month" : "months");
        s.append(" ");

        timeLeft -= nMonths * 1000L * 60L * 60L * 24L * 30L;
      }
    }

    //
    // Weeks
    //
    if (reportWeeks) {
      long nWeeks = timeLeft / 1000L / 60L / 60L / 24L / 7L;
      if (nWeeks > 0) {
        s.append(nWeeks);
        s.append(" ");
        s.append(nWeeks == 1 ? "week" : "weeks");
        s.append(" ");

        timeLeft -= nWeeks * 1000L * 60L * 60L * 24L * 7L;
      }
    }

    //
    // Days
    //
    if (reportDays) {
      long nDays = timeLeft / 1000L / 60L / 60L / 24L;
      if (nDays > 0) {
        s.append(nDays);
        s.append(" ");
        s.append(nDays == 1 ? "day" : "days");
        s.append(" ");

        timeLeft -= nDays * 1000L * 60L * 60L * 24L;
      }
    }

    //
    // Hours
    //
    if (reportHours) {
      long nHours = timeLeft / 1000L / 60L / 60L;
      if (nHours > 0) {
        s.append(nHours);
        s.append(" ");
        s.append(nHours == 1 ? "hour" : "hours");
        s.append(" ");

        timeLeft -= nHours * 1000L * 60L * 60L;
      }
    }

    //
    // Minutes
    //
    if (reportMinutes) {
      long nMinutes = timeLeft / 1000L / 60L;
      if (nMinutes > 0) {
        s.append(nMinutes);
        s.append(" ");
        s.append(nMinutes == 1 ? "minute" : "minutes");
        s.append(" ");

        timeLeft -= nMinutes * 1000L * 60L;
      }
    }

    //
    // Seconds
    //
    if (reportSeconds) {
      long nSeconds = timeLeft / 1000L;
      if (nSeconds > 0) {
        s.append(nSeconds);
        s.append(" ");
        s.append(nSeconds == 1 ? "second" : "seconds");
        s.append(" ");

        timeLeft -= nSeconds * 1000L;
      }
    }

    //
    // Milliseconds
    //
    if (reportMilliseconds) {
      long nMilliseconds = timeLeft;
      if (nMilliseconds > 0) {
        s.append(nMilliseconds);
        s.append(" ");
        s.append(nMilliseconds == 1 ? "millisecond" : "milliseconds");
        s.append(" ");
      }
    }

    return s.length() == 0 ? "0" : s.toString();
  }
}


