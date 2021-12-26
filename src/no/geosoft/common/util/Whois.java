package no.geosoft.common.util;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import no.geosoft.common.io.FileUtil;

/**
 * Class for getting WHOIS information for a domain name or
 * an IP address.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Whois
{
  /** Base URL for the JSON service we are using. */
  private final static String BASE_URL = "http://www.enclout.com/whois/show.json";

  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(Whois.class.getName());

  /**
   * Private constructor to prevent client instantiation.
   */
  private Whois()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return WHOIS information for the specified URL as a string.
   *
   * @param urlAddress  URL address to consider. Non-null.
   * @return            Requested WHOIS information. Never null.
   * @throws IllegalArgumentException  If urlAddress is null.
   */
  public static String get(String urlAddress)
  {
    if (urlAddress == null)
      throw new IllegalArgumentException("urlAddress cannot be null");

    String urlString = BASE_URL + "?" + "url=" + urlAddress;

    try {
      URL url = new URL(urlString);
      String s = FileUtil.read(url.openStream());

      if (s.contains("Invalid URL")) {
        logger_.log(Level.WARNING, "Invalid URL: " + urlAddress);
        return null;
      }

      int p0 = s.indexOf("\"body\":");
      int p1 = p0 != -1 ? s.indexOf("}", p0) : -1;

      s = p0 != -1 && p1 != -1 ? s.substring(p0 + 8, p1 - 1) : s;
      s = s.trim();

      s = s.replace("\\n", "\n");
      s = s.replace("\\r", "");

      return s;
    }
    catch (MalformedURLException exception) {
      logger_.log(Level.WARNING, "Invalid URL: " + urlString, exception);
      return null;
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to read URL: " + urlString, exception);
      return null;
    }
  }

  public static void main(String[] arguments)
  {
    System.out.println(Whois.get("jwitsml.org"));
  }
}
