package no.geosoft.common.util;

/**
 * Model a pair of two instances of any two types.
 *
 * @param <T1>  Type of first element.
 * @param <T2>  Type of second element.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Pair<T1,T2>
{
  /** First element. May be null. */
  private final T1 e1_;

  /** First element. May be null. */
  private final T2 e2_;

  /**
   * Create a pair instance.
   *
   * @param e1  First element. May be null.
   * @param e2  Second element. May be null.
   */
  public Pair(T1 e1, T2 e2)
  {
    e1_ = e1;
    e2_ = e2;
  }

  /**
   * Return first element.
   *
   * @return  First element. May be null if originally specified as null.
   */
  public T1 getFirst()
  {
    return e1_;
  }

  /**
   * Return second element.
   *
   * @return  Second element. May be null if originally specified as null.
   */
  public T2 getSecond()
  {
    return e2_;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return e1_ + "," + e2_;
  }
}

