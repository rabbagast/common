package no.geosoft.common.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A universal, session specific counter instance staring at 1.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Counter
{
  /** The counter instance initialized to 1. */
  private final static AtomicLong counter_ = new AtomicLong(1L);

  /**
   * Private constructor to prevent client instantiation.
   */
  private Counter()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return next counter value.
   *
   * @return  Next counter value. [1,&gt;.
   */
  public static long get()
  {
    return counter_.getAndIncrement();
  }
}
