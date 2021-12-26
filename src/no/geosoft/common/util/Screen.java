package no.geosoft.common.util;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Class for returning screen size in pixels and actual measures.
 * <p>
 * The API methods being called are apparently not entirely trustable
 * but at the moment perhaps the best we can do.
 *
 * BEWARE: Actual measures deviates from the computed ones!
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Screen
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private Screen()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return the size of the default screen in pixels.
   *
   * @return  Pixel size of screen. Never null.
   */
  public static Dimension getPixelSize()
  {
    return Toolkit.getDefaultToolkit().getScreenSize();
  }

  /**
   * Return screen width in meters.
   *
   * @return Screen width in meters. [0,&gt;.
   */
  public static double getWidth()
  {
    Dimension pixelSize = Toolkit.getDefaultToolkit().getScreenSize();

    int pixelWidth = pixelSize.width;
    int pixelsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();

    double inchWidth = (double) pixelWidth / (double) pixelsPerInch;

    double width = inchWidth * 0.0254;
    return width;
  }

  /**
   * Return screen height in meters.
   *
   * @return Screen height in meters. [0,&gt;.
   */
  public static double getHeight()
  {
    Dimension pixelSize = Toolkit.getDefaultToolkit().getScreenSize();

    int pixelHeight = pixelSize.height;
    int pixelsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();

    double inchHeight = (double) pixelHeight / (double) pixelsPerInch;

    double height = inchHeight * 0.0254;
    return height;
  }

  /**
   * Return number of pixels per meter for the present screen.
   *
   * @return  Number of pixels per meter. [0,&gt;.
   */
  public static double getPixelsPerMeter()
  {
    int pixelsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();
    return pixelsPerInch / 0.0254;
  }

  public static void main(String[] arguments)
  {
    System.out.println(Screen.getWidth() + "x" + Screen.getHeight());
  }
}
