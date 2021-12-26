package no.geosoft.common.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import no.geosoft.common.io.FileUtil;

/**
 * Class for extracting information from the Broennoeysundregistrene.
 * <p>
 * See
 * <a href="https://data.brreg.no/enhetsregisteret/api/docs/index.html">
 *   https://data.brreg.no/enhetsregisteret/api/docs/index.html
 * </a>
 * for a complete description of their open REST API.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Brreg
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(Brreg.class.getName());

  /** The brreg.no base URL. */
  private static final String BASE_URL = "https://data.brreg.no/enhetsregisteret/api/enheter";

  /**
   * Private constructor to prevent client instantiation.
   */
  private Brreg()
  {
    // Nothing
  }

  /**
   * Return the Brreg information about the specified company as a json
   * text string.
   *
   * @param organizationNumber  Organization number of company to look up. Non-null.
   * @return                    Associated information from Brreg. Null if not found
   *                            or the access failed for some reason.
   */
  private static String getInfo(String organizationNumber)
  {
    assert organizationNumber != null : "organizationNumber cannot be null";

    String urlString = BASE_URL + "?organisasjonsnummer=" + organizationNumber;

    try {
      URL url = new URL(urlString);
      return FileUtil.read(url.openStream());
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

  /**
   * Return web page for the company of the specified organization number.
   * <p>
   * <b>Note:</b> Many companies doesn't have their web page registered with
   * Brreg, we assume more will in the future.
   *
   * @param organizationNumber  Organization number of company to look up. Non-null.
   * @return                    Associated web page. Or null if none, or the access
   *                            failed for some reason.
   * @throws IllegalArgumentException  If organizationNumber is null.
   */
  public static String getCompanyWebPage(String organizationNumber)
  {
    if (organizationNumber == null)
      throw new IllegalArgumentException("organizationNumber cannot be null");

    String urlString = null;

    String text = getInfo(organizationNumber);

    if (text != null) {
      int pos1 = text.indexOf("hjemmeside");
      if (pos1 != -1) {
        pos1 = text.indexOf(':', pos1) + 2;
        int pos2 = text.indexOf(',', pos1) - 1;
        urlString = text.substring(pos1, pos2);
      }
    }

    return urlString;
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    System.out.println(Brreg.getInfo("989795848"));
    System.out.println(Brreg.getCompanyWebPage("989795848")); // Aker BP (which have homepage)
  }
}
