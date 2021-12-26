package no.geosoft.common.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An ID generator guaranteed to produce unique IDs in a multi
 * threaded environment.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class UniqueId
{
  /** The unique ID counter, initialized with time. */
  private final static AtomicLong uniqueId_ = new AtomicLong(System.currentTimeMillis());

  /**
   * Private constructor to prevent client instantiation.
   */
  private UniqueId()
  {
    assert false : "This constructor should never be called.";
  }

  /**
   * Return a unique ID as a number of maximum possible radix
   * making it more compact than a base 10 value.
   *
   * @return  Requested unique ID. Never null.
   */
  public static String get()
  {
    String s = "" + uniqueId_.incrementAndGet();
    return Long.toString(Long.parseLong(s, 10), Character.MAX_RADIX);
  }
}
