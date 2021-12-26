package no.geosoft.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Date;
import java.util.List;
import java.nio.charset.Charset;

/**
 * Class for producing random values of different types.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Random
{
  /**
   * Private constructor to prevent instantiation.
   */
  private Random()
  {
    assert false : "This should never be called";
  }

  /**
   * Get a random element from the specified collection.
   *
   * @param <E>         Type of collection elements.
   * @param collection  Collection to get element from. Non-null.
   * @return            Requested element. Null if the collection is empty.
   */
  public static <E> E getElement(Collection<E> collection)
  {
    if (collection == null)
      throw new IllegalArgumentException("collection cannot be null");

    if (collection.isEmpty())
      return null;

    List<E> list = new ArrayList<>(collection);

    int index = getInteger(0, list.size() - 1);
    return list.get(index);
  }

  /**
   * Get a random element from the specified array.
   *
   * @param <E>         Type of collection elements.
   * @param array  Arrayd to get element from. Non-null.
   * @return       Requested element. Null if the array is empty.
   */
  public static <E> E getElement(E[] array)
  {
    if (array == null)
      throw new IllegalArgumentException("array cannot be null");

    if (array.length == 0)
      return null;

    int index = getInteger(0, array.length - 1);
    return array[index];
  }

  /**
   * Return a random boolean value.
   *
   * @return  A random boolean.
   */
  public static boolean getBoolean()
  {
    return Math.random() < 0.5;
  }

  /**
   * Return a random boolean value or null.
   *
   * @return  A random boolean or null.
   */
  public static Boolean getBooleanOrNull()
  {
    // Return null in 20% of the cases
    return Math.random() < 0.2 ? null : getBoolean();
  }

  /**
   * Return a random character within the specified limits.
   *
   * @param limit1  First limit.
   * @param limit2  Second limit.
   * @return        A random character.
   */
  public static char getChar(char limit1, char limit2)
  {
    return (char) Random.getInteger((int) limit1, (int) limit2);
  }

  /**
   * Return a ransom character.
   *
   * @return  A random character.
   */
  public static char getChar()
  {
    char c = Random.getChar((char) 0, (char) 65535);
    return c;
  }

  /**
   * Return an ASCII (32-126) string of the specified length.
   *
   * @param minLength  Minimum string length [0,&gt;.
   * @param maxLength  Maximum string length [minLength,&gt;.
   * @return A random ASCII string. Never null.
   */
  public static String getAsciiString(int minLength, int maxLength)
  {
    if (minLength < 0)
      throw new IllegalArgumentException("Illegal minLength: " + minLength);

    if (maxLength < minLength)
      throw new IllegalArgumentException("Illegal maxLength: " + maxLength);

    // Find a random length
    int length = Random.getInteger(minLength, maxLength);

    // Find the random content
    byte[] chars = new byte[length];
    for (int i = 0; i < length; i++)
      chars[i] = Random.getByte((byte) 32, (byte) 126);

    // Return string
    String s = new String(chars, Charset.forName("UTF-8"));
    return s;
  }

  /**
   * Return a random ASCII string of the specified length.
   *
   * @param length  Length of string to return.
   * @return Random string of the specified length. Never null.
   */
  public static String getAsciiString(int length)
  {
    if (length < 0)
      throw new IllegalArgumentException("Illegal length: " + length);

    return Random.getAsciiString(length, length);
  }

  /**
   * Return a random length ASCII string of random content.
   *
   * @return  A random length string of random content. Never null.
   */
  public static String getAsciiString()
  {
    return Random.getAsciiString(0, 1000);
  }

  /**
   * Return a random ASCII string of random length or null.
   *
   * @return  Random ASCII string or null.
   */
  public static String getAsciiStringOrNull()
  {
    // Return null in 20% of the cases
    return Math.random() < 0.2 ? null : getAsciiString();
  }

  /**
   * Return a UTF-8 string of random content and random length.
   *
   * @param minLength  Minimum string length [0,&gt;.
   * @param maxLength  Maximum string length [minLength,&gt;.
   * @return A random string. Never null.
   */
  public static String getString(int minLength, int maxLength)
  {
    if (minLength < 0)
      throw new IllegalArgumentException("Illegal minLength: " + minLength);

    if (maxLength < minLength)
      throw new IllegalArgumentException("Illegal maxLength: " + maxLength);

    // Find a random length
    int length = Random.getInteger(minLength, maxLength);

    // Find the random content
    byte[] chars = new byte[length];
    for (int i = 0; i < length; i++)
      chars[i] = Random.getByte();

    // Return string
    String s = new String(chars, Charset.forName("UTF-8"));
    return s;
  }

  /**
   * Return a random string of the specified length.
   *
   * @param length  Length of string to return.
   * @return Random string of the specified length. Never null.
   */
  public static String getString(int length)
  {
    if (length < 0)
      throw new IllegalArgumentException("Illegal length: " + length);

    return Random.getString(length, length);
  }

  /**
   * Return a random length string of random content.
   *
   * @return  A random length string of random content. Never null.
   */
  public static String getString()
  {
    return Random.getString(0, 1000);
  }

  /**
   * Return a random string of random length or null.
   *
   * @return  Random string or null.
   */
  public static String getStringOrNull()
  {
    // Return null in 20% of the cases
    return Math.random() < 0.2 ? null : getString();
  }

  /**
   * Return a random double value within the specified limits.
   * <p>
   * <em>Linear</em> implies that all values in the specified range
   * will have equal probability.
   *
   * @param limit1  First limit.
   * @param limit2  Second limit.
   * @return        A random double within the specified interval.
   */
  public static double getDoubleLinear(double limit1, double limit2)
  {
    double min = Math.min(limit1, limit2);

    double center = limit1 * 0.5 + limit2 * 0.5;
    double half = center - min;
    double random = center + (Math.random() * 2.0 - 1.0) * half;

    return random;
  }

  /**
   * Return a random double value within the specified limits.
   * <p>
   * Value is picked in log10 space so that the probability of being
   * between 1 and 10 is the same as being between 10 and 100 and so on.
   * In general this will select values closer to 0.0 while still
   * having a broad range.
   *
   * @param limit1  First limit of range.
   * @param limit2  Second limit of range.
   * @return        Random value within the specified range.
   */
  public static double getDouble(double limit1, double limit2)
  {
    // This case is not solved by the logic below
    if (limit1 == limit2)
      return limit1;

    // Divide the problem in half (or a third) to avoid overflow
    double factor = limit1 == 2.0 || limit2 == 2.0 ? 3.0 : 2.0;
    double min = Math.min(limit1, limit2) / factor;
    double max = Math.max(limit1, limit2) / factor;

    // Handle positive and negative side separately
    double p0 = min < 0.0 ? 0.0 : min;
    double p1 = max < 0.0 ? 0.0 : max;

    double n0 = max > 0.0 ? 0.0 : Math.abs(max);
    double n1 = min > 0.0 ? 0.0 : Math.abs(min);

    // Convert to log10 space
    double pw0 = p0 != 0.0 ? Math.log10(p0) : 0.0;
    double pw1 = p1 != 0.0 ? Math.log10(p1) : 0.0;
    double pwMin = Math.min(pw0, pw1);
    double pwMax = Math.max(pw0, pw1);
    double pSpan = pwMax - pwMin;

    //System.out.println("P: " + pw0 + " " + pw1);

    double nw0 = n0 != 0.0 ? Math.log10(n0) : 0.0;
    double nw1 = n1 != 0.0 ? Math.log10(n1) : 0.0;
    double nwMin = Math.min(nw0, nw1);
    double nwMax = Math.max(nw0, nw1);
    double nSpan = nwMax - nwMin;

    // Pick randomly the positive of negative side
    double p = pSpan / (pSpan + nSpan);
    boolean isPositive = Math.random() < p;

    // Pick random value within the log10 space
    double v;
    if (isPositive)
      v = pwMin == 0.0 && pwMax < 0.0 ? pwMax / Math.random() : pwMax == 0.0 && pwMin < 0.0 ? pwMin / Math.random() : pwMin + Math.random() * pSpan;
    else
      v = nwMin == 0.0 && nwMax < 0.0 ? nwMax / Math.random() : nwMax == 0.0 && nwMin < 0.0 ? nwMin / Math.random() : nwMin + Math.random() * nSpan;

    // Transform back to user space
    double r = Math.pow(10.0, v) * factor;
    return isPositive ? r : -r;
  }

  /**
   * Return a random double value.
   *
   * @return  A random double value from a wide portion of the
   *          double domain.
   */
  public static double getDouble()
  {
    return getDouble(-1E10, 1E10);
  }

  /**
   * Return a random double or null.
   *
   * @return  Random double or null.
   */
  public static Double getDoubleOrNull()
  {
    // Return null in 20% of the cases
    return Math.random() < 0.2 ? null : getDouble();
  }

  /**
   * Return a random float value within the specified limits.
   *
   * @param limit1  First limit.
   * @param limit2  Second limit.
   * @return        A random float within specified limits.
   */
  public static float getFloat(float limit1, float limit2)
  {
    double random = getDouble((double) limit1, (double) limit2);
    return (float) random;
  }

  /**
   * Return a random float value.
   *
   * @return  A random float value from the entrie double domain.
   */
  public static float getFloat()
  {
    return getFloat(-Float.MIN_VALUE, +Float.MAX_VALUE);
  }

  /**
   * Return a random byte value within the specified limits.
   *
   * @param limit1  First limit.
   * @param limit2  Second limit.
   * @return        A random byte within specified limits.
   */
  public static byte getByte(byte limit1, byte limit2)
  {
    double b1 = limit1 - 0.5;
    double b2 = limit2 + 0.5;

    double random = getDoubleLinear((double) limit1 - 0.5, (double) limit2 + 0.5);
    return (byte) Math.round(random);
  }

  /**
   * Return a random byte value.
   *
   * @return  A random byte value from the entrie double domain.
   */
  public static byte getByte()
  {
    return getByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
  }

  /**
   * Return a random short value within the specified limits.
   *
   * @param limit1  First limit.
   * @param limit2  Second limit.
   * @return        A random short within specified limits.
   */
  public static short getShort(short limit1, short limit2)
  {
    double random = getDoubleLinear((double) limit1 - 0.5, (double) limit2 + 0.5);
    return (short) Math.round(random);
  }

  /**
   * Return a random short value.
   *
   * @return  A random short value from the entrie double domain.
   */
  public static short getShort()
  {
    return getShort(Short.MIN_VALUE, Short.MAX_VALUE);
  }

  /**
   * Return a random integer value within the specified limits.
   *
   * @param limit1  First limit. Inclusive.
   * @param limit2  Second limit. Inclusive.
   * @return        A random integer within specified limits.
   */
  public static int getInteger(int limit1, int limit2)
  {
    double random = getDoubleLinear((double) limit1 - 0.5, (double) limit2 + 0.5);
    return (int) Math.round(random);
  }

  /**
   * Return a random integer value.
   *
   * @return  A random integer value from the entrie double domain.
   */
  public static int getInteger()
  {
    return getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  /**
   * Return a random integer or null.
   *
   * @return  Random integer or null.
   */
  public static Integer getIntegerOrNull()
  {
    // Return null in 20% of the cases
    return Math.random() < 0.2 ? null : getInteger();
  }

  /**
   * Return a random long value within the specified limits.
   *
   * @param limit1  First limit.
   * @param limit2  Second limit.
   * @return        A random long within specified limits.
   */
  public static long getLong(long limit1, long limit2)
  {
    double random = getDoubleLinear((double) limit1 - 0.5, (double) limit2 + 0.5);
    return Math.round(random);
  }

  /**
   * Return a random long value.
   *
   * @return  A random long value from the entrie double domain.
   */
  public static long getLong()
  {
    return getLong(Long.MIN_VALUE, Long.MAX_VALUE);
  }

  /**
   * Return a random date.
   *
   * @return  A random date. Never null.
   */
  public static Date getDate()
  {
    // Keep the date within reasonable interval so it can be
    // properly represented by ISO 8601 if necessary
    long now = System.currentTimeMillis();
    long oneDay = 24 * 60 * 60 * 1000L;

    return new Date(now + getInteger(0, 400) * oneDay);
  }

  /**
   * Return a random date or null.
   *
   * @return  A random date or null.
   */
  public static Date getDateOrNull()
  {
    // Return null in 20% of the cases
    return Math.random() < 0.2 ? null : getDate();
  }

  /**
   * Return a random URL.
   *
   * @return  A random URL. Never null.
   */
  public static URL getUrl()
  {
    try {
      URL[] urls = new URL[] {new URL("http://google.com"),
                              new URL("https://geosoft.no"),
                              new URL("http://facebook.com"),
                              new URL("https://sbanken.no"),
                              new URL("http://amazon.com"),
                              new URL("http://nrk.no"),
                              new URL("http://baidu.com"),
                              new URL("http://reddit.com"),
                              new URL("http://wikipedia.org"),
                              new URL("http://live.com"),
                              new URL("http://twitter.com"),
                              new URL("https://taobao.com")};

      return getElement(urls);
    }
    catch (MalformedURLException exception) {
      assert false : "Programming error" + exception;
      return null;
    }
  }

  /**
   * Return a random value within the specified normal distribution.
   *
   * @param mean              Normal distribution mean.
   * @param standardDeviation Normal distribution standard deviation.
   * @return                  Random value within speciofied distribution.
   */
  public static double getNormal(double mean, double standardDeviation)
  {
    double x;
    double y;
    double w;

    // Box-Muller algorithm
    do {
      x = Math.random() * 2.0 - 1.0; // [-1.0, +1.0]
      y = Math.random() * 2.0 - 1.0; // [-1.0, +1.0]
      w = x * x + y * y;
    } while (w >= 1.0);

    w = Math.sqrt((-2.0 * Math.log(w)) / w);

    return mean + x * w * standardDeviation;
  }

  /**
   * Get a random element from the specified enumertaion class.
   * <p>
   * Example: Given a Status enumeration like:
   * <pre>
   *   enum Status {
   *     INITIAL,
   *     RUNNING,
   *     DONE,
   *     FAILED;
   *   };
   * </pre>
   *
   * To obtain a random element from Status, call:
   * <pre>
   *   Status s = Random.getEnumeration(Status.class);
   * </pre>
   *
   * @param <E>    Enumaration class.
   * @param clazz  An enumeration class. Non-null.
   * @return       A random element from the specified enumeration.
   */
  public static <E extends Enum<E>> E getEnumeration(Class<E> clazz)
  {
    List<E> elements = new ArrayList<>(EnumSet.allOf(clazz));
    return getElement(elements);
  }

  /**
   * Return a random object of some type.
   *
   * @return  A random object. Never null.
   */
  public static Object getObject()
  {
    List<Object> objects = new ArrayList<>();
    objects.add(getAsciiString());
    objects.add(new java.util.Date());
    objects.add(new java.util.ArrayList<Double>());
    objects.add(new Object());
    objects.add(Random.class);
    objects.add(Integer.valueOf(-101));
    objects.add(new java.util.BitSet());
    objects.add(new StringBuilder());
    objects.add(java.util.Locale.ROOT);
    objects.add(java.util.TimeZone.getDefault());
    objects.add(java.util.UUID.randomUUID());
    objects.add(new java.io.File("file"));
    objects.add(java.awt.Color.BLACK);
    objects.add(java.math.RoundingMode.FLOOR);
    objects.add(Double.valueOf(42.0));
    objects.add(Double.NaN);
    objects.add(new javax.swing.JPanel());
    objects.add(new java.util.concurrent.atomic.AtomicBoolean());

    return getElement(objects);
  }

  /**
   * Return a random object of some type, or null
   *
   * @return  A random object or null.
   */
  public static Object getObjectOrNull()
  {
    return Math.random() > 0.2 ? getObject() : null;
  }

  /**
   * Return a random object from the supplied ones.
   *
   * @param objects  List object object to pick randomly from. Non-null.
   * @return         A random object from the supllied ones.
   *                 May be null if null is among the supplied ones.
   * @throws IllegalArgumentException  If objects is null.
   */
  public static Object getObject(Object... objects)
  {
    int index = getInteger(0, objects.length - 1);
    return objects[index];
  }
}
