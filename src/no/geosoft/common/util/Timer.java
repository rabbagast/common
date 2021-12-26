package no.geosoft.common.util;

/**
 * Class for program event timing.
 * <p>
 * Usage:
 *
 *   <pre>
 *   Timer timer = new Timer();
 *
 *   // do stuff
 *
 *   System.out.println(timer);  // prints time elapsed since
 *                              // object was created.
 *   </pre>
 *
 * <p>
 * <b>Synchronization:</b>
 * <p>
 * This class is thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Timer
{
  /** Time when this class was instantiated. */
  private final long t0_ = System.currentTimeMillis();

  /**
   * Returns a formatted string showing the elapsed time
   * since the instance was created.
   *
   * @return  Formatted time string.
   */
  @Override
  public String toString()
  {
    long nMillis = System.currentTimeMillis() - t0_;

    long nHours   = nMillis / 1000 / 60 / 60;
    nMillis -= nHours * 1000 * 60 * 60;

    long nMinutes = nMillis / 1000 / 60;
    nMillis -= nMinutes * 1000  * 60;

    long nSeconds = nMillis / 1000;
    nMillis -= nSeconds * 1000;

    StringBuilder time = new StringBuilder();
    if (nHours > 0)
      time.append(nHours + ":");
    if (nHours > 0 && nMinutes < 10)
      time.append("0");
    time.append(nMinutes + ":");
    if (nSeconds < 10)
      time.append("0");
    time.append(nSeconds);
    time.append(".");
    if (nMillis < 100)
      time.append("0");
    if (nMillis <  10)
      time.append("0");
    time.append(nMillis);

    return time.toString();
  }
}
