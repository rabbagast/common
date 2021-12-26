package no.geosoft.common.util;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * Class for accessing live oil prices and returning
 * associated chart image.
 *
 * @author <a href="mailto:jdre@statoil.com">Jacob Dreyer</a>
 */
public class OilPrice
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(OilPrice.class.getName());

  /**
   * Private constructor to prevent client instantiation.
   */
  private OilPrice()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return URL of oil price image.
   *
   * @param width   Width of image. &lt;0,&gt;.
   * @param height  Height of image. &lt;0,&gt;.
   * @return  URL of current oil price image. Never null.
   */
  private static String getUrlString(int width, int height)
  {
    assert width > 0 : "Invalid width: " + width;
    assert height > 0 : "Invalid height: " + height;

    String url = "http://freeserv.dukascopy.com:8080/ChartServer/chart" + "?" +
                 "stock_id=505" + "&" +
                 "interval=60" + "&" +
                 "points_number=" + width / 10 + "&" +
                 "view_type=candle" + "&" +
                 "width=" + width + "&" +
                 "height=" + height + "&" +
                 "show_labels=true" + "&" +
                 "osc_type=Volume" + "&" +
                 "rfi=false" + "&" +
                 "osc_height=100" + "&" +
                 "p1=2" + "&" +
                 "p2=3" + "&" +
                 "p3=7" + "&" +
                 "c=18325";

    logger_.log(Level.INFO, "Access chart at URL: " + url);

    return url;
  }

  /**
   * Return oil price image of specified size.
   *
   * @param width   Width of image. &lt;0,&gt;.
   * @param height  Height of image. &lt;0,&gt;.
   * @return  Requested image. Null if not available.
   */
  public static Image getImage(int width, int height)
  {
    if (width <= 0)
      throw new IllegalArgumentException("Invalid width: " + width);

    if (height <= 0)
      throw new IllegalArgumentException("Invalid height: " + height);

    String urlString = getUrlString(width, height);

    try {
      URL url = new URL(urlString);
      return ImageIO.read(url);
    }
    catch (MalformedURLException exception) {
      logger_.log(Level.WARNING, "Invalid URL: " + urlString);
      return null;
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to read image: " + urlString);
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

    javax.swing.JLabel label = new javax.swing.JLabel();
    Image image = OilPrice.getImage(700, 300);

    javax.swing.ImageIcon icon = null;
    if (image != null)
      icon = new javax.swing.ImageIcon(image);

    label.setIcon(icon);
    f.add(label);

    f.pack();
    f.setVisible(true);
  }
}