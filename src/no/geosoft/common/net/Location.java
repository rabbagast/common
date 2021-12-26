package no.geosoft.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import no.geosoft.common.io.FileUtil;

/**
 * Class for holding the geographic location of the
 * present client machine based on its IP address.
 *
 * <pre>
 *   Location location = Location.getInstance();
 *   location.initialize(); // Do this in a thread
 *
 *   location.getXxx();
 * </pre>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Location
{
  /** URL for getting current IP address. */
  private final static String CHECK_IP_URL = "http://checkip.amazonaws.com";

  /** URL for getting current location from given IP address. */
  private final static String FIND_LOCATION_URL = "http://www.geoplugin.net/xml.gp";

  /** Sole instance of this class. */
  private final static Location instance_ = new Location();

  /** IP address of the current machine. Null if not initialized or not available. */
  private String ipAddress_;

  /** Two letter ISO country code of the present location. Null if not initialized or not available. */
  private String countryCode_;

  /** Country name (English) of the present location. Null if not initialized or not available. */
  private String country_;

  /** City name of present location. Null if not initialized or not available. */
  private String city_;

  /**
   * Create a new Location instance.
   */
  private Location()
  {
    // Nothing
  }

  /**
   * Return the sole instance of this class.
   *
   * @return  The sole instance of this class.
   */
  public static Location getInstance()
  {
    return instance_;
  }

  /**
   * Return IP address of the present machine.
   *
   * @return  IP of the present machine. Null if not initialized or not available.
   */
  public String getIpAddress()
  {
    synchronized (this) {
      return ipAddress_;
    }
  }

  /**
   * Return country of the present location.
   *
   * @return  Country name of the present location.
   *          Null if not initialized or not available.
   */
  public String getCountry()
  {
    synchronized (this) {
      return country_;
    }
  }

  /**
   * Return ISO country code of the present location.
   *
   * @return  ISO country code of the present location.
   *          Null if not initialized or not available.
   */
  public String getCountryCode()
  {
    synchronized (this) {
      return countryCode_;
    }
  }

  /**
   * Return city of the present location.
   *
   * @return  City of the present location.
   *          Null if not initialized or not available.
   */
  public String getCity()
  {
    synchronized (this) {
      return city_;
    }
  }

  /**
   * Get the current public IP address through a web service.
   *
   * @return Public IP address of the current machine. Never null.
   * @throws IOException  If the we services doesn't deliver for some reason.
   */
  private String findIpAddress()
    throws IOException
  {
    URL url = new URL(CHECK_IP_URL);
    InputStream stream = null;

    try {
      stream = url.openStream();
      ipAddress_ = FileUtil.read(stream);
    }
    finally {
      if (stream != null)
        stream.close();
    }

    return ipAddress_;
  }

  /**
   * Find element value of specified tag within given XML.
   *
   * @param xml   XML to search. Non-null.
   * @param tag   Element tag to find value of. Non-null.
   * @return      Requested element, or null if not found.
   */
  private static String getValueOfTag(String xml, String tag)
  {
    assert xml != null : "xml cannot be null";
    assert tag != null : "tag cannot be null";

    String actualTag = '<' + tag + '>';

    int p0 = xml.indexOf(actualTag);
    if (p0 == -1)
      return null;

    p0 += actualTag.length();
    int p1 = xml.indexOf('<', p0);
    if (p1 == -1)
      return null;

    return xml.substring(p0, p1).trim();
  }

  /**
   * Initialize the Location instance by accessing necessary
   * web services.
   * <p>
   * NOTE: The operation is normally fast, but dependent on network
   * connection this call may hang, so make sure this method is
   * called from a thread.
   *
   * @throws IOException  If the we services doesn't deliver for some reason.
   */
  public void initialize()
    throws IOException
  {
    synchronized (this) {
      String ipAddress = findIpAddress();
      URL url = new URL(FIND_LOCATION_URL + "?ip=" + ipAddress);

      InputStream stream = null;

      try {
        stream = url.openStream();
        String xml = FileUtil.read(stream);

        String s = getValueOfTag(xml, "geoplugin_countryCode");
        countryCode_ = s != null && !s.isEmpty() ? s : null;

        s = getValueOfTag(xml, "geoplugin_countryName");
        country_ = s != null && !s.isEmpty() ? s : null;

        s = getValueOfTag(xml, "geoplugin_city");
        city_ = s != null && !s.isEmpty() ? s : null;
      }
      finally {
        if (stream != null)
          stream.close();
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();

    Location location = Location.getInstance();

    try {
      location.initialize();
    }
    catch (IOException exception) {
      // Ignore. Elements are left as null.
    }

    s.append("IP address...: " + location.getIpAddress() + "\n");
    s.append("Country......: " + location.getCountry() + "\n");
    s.append("Country code.: " + location.getCountryCode() + "\n");
    s.append("City.........: " + location.getCity());

    return s.toString();
  }
}
