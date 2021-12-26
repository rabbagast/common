package no.geosoft.common.util;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * Interface to the yr.no weather database.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Yr
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(Yr.class.getName());

  /** The yr.no base URL. */
  private static final String BASE_URL = "http://www.yr.no";

  /**
   * Private constructor to prevent client instantiation.
   */
  private Yr()
  {
    assert false : "this ctor should never be called";
  }

  /**
   * Get meteogram for the specified geographic place,
   * such as "Norge/Rogaland/Stavanger/Stavanger".
   *
   * @param place  Place to get meteogram for. Non-null
   * @return       Meteogram image of the specified place, or null
   *               if the place was unknown.
   * @throws IllegalArgumentException  If place is null.
   */
  public static Image getMeteogram(String place)
  {
    if (place == null)
      throw new IllegalArgumentException("place cannot be null");

    String urlString = BASE_URL + "/place/" + place + "/meteogram.png";

    try {
      URL url = new URL(urlString);
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
   * Get marinogram for the specified geographic location.
   *
   * @param latitude   Latitude of location in radians.
   * @param longitude  Longitude of location in Radians.
   * @return           Marinogram of the specified location,
   *                   or null if no such available.
   */
  public static Image getMarinogram(double latitude, double longitude)
  {
    // Create a number format that ensures comma as decimal separator
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setDecimalSeparator(',');
    NumberFormat numberFormat = new DecimalFormat("0.0", decimalFormatSymbols);

    double latitudeDegrees = latitude * 180.0 / Math.PI;
    double longitudeDegrees = longitude * 180.0 / Math.PI;

    String urlString = BASE_URL + "/place/Ocean/" +
                       numberFormat.format(latitudeDegrees) + "_" +
                       numberFormat.format(longitudeDegrees) +
                       "/marinogram.png";

    try {
      URL url = new URL(urlString);
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
   * @param arguments  Not used.
   */
  public static void main(String[] arguments)
  {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    // Image image = Yr.getMarinogram(0.96, 0.09);
    Image image = Yr.getMeteogram("Norge/Rogaland/Stavanger/Madla");

    javax.swing.JLabel l = new javax.swing.JLabel();
    l.setIcon(new javax.swing.ImageIcon(image));

    f.getContentPane().add(l);

    f.pack();
    f.setVisible(true);
  }
}
