package no.geosoft.common.util;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * Utility class for capturing favicon from a web page.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Favicon
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(Favicon.class.getName());

  /**
   * Private constructor to prevent client instantiation.
   */
  private Favicon()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return favicon for the specified web page.
   *
   * @param urlString  Web page to get favicon of. <b>NOTE: </b> Not including protocol ("http://") etc.
   *                   Non-null.
   * @return           Associated favicon.
   *                   A default image from Google if favicon is not provided.
   *                   Null if the URL is invalid or the netwaork access failed for some reason.
   * @throws IllegalArgumentException  If urlString is null.
   */
  public static Image get(String urlString)
  {
    if (urlString == null)
      throw new IllegalArgumentException("urlString cannot be null");

    String googleUrl = "http://www.google.com/s2/favicons?domain_url=http%3A%2F%2F" + urlString + "%2F";

    try {
      URL url = new URL(googleUrl);
      return ImageIO.read(url);
    }
    catch (MalformedURLException exception) {
      logger_.log(Level.WARNING, "Invalid URL: " + urlString, exception);
      return null;
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to read image: " + urlString, exception);
      return null;
    }
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    Image image = Favicon.get("geosoft.no");

    javax.swing.JLabel label = new javax.swing.JLabel();
    label.setIcon(new javax.swing.ImageIcon(image));

    f.getContentPane().add(label);

    f.pack();
    f.setVisible(true);
  }
}
