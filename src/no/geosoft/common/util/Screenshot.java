package no.geosoft.common.util;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for putting screenshots on the system clipboard.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Screenshot implements ClipboardOwner
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(Screenshot.class.getName());

  /**
   * Create screenshot of the specified screen rectangle.
   *
   * @param rectangle  Area on screen to make screenshot of. Non-null.
   * @throws IllegalArgumentException  If rectangle is null.
   */
  public static void capture(Rectangle rectangle)
  {
    if (rectangle == null)
      throw new IllegalArgumentException("rectangle cannot be null");

    new Screenshot(rectangle);
  }

  /**
   * Create screenshot of the entire screen.
   */
  public static void capture()
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    new Screenshot(new Rectangle(screenSize));
  }

  /**
   * Create screenshot of the specified component.
   *
   * @param component  Component to make screenshot of. Non-null.
   * @throws IllegalArgumentException  If component is null.
   */
  public static void capture(Component component)
  {
    if (component == null)
      throw new IllegalArgumentException("component cannot be null");

    Rectangle rectangle = new Rectangle(component.getLocationOnScreen(), component.getSize());
    new Screenshot(rectangle);
  }

  /**
   * Create a screenshot instance.
   *
   * @param rectangle  Rectangle to screenshot. Non-null.
   */
  private Screenshot(Rectangle rectangle)
  {
    assert rectangle != null : "rectangle cannot be null";

    try {
      Robot robot = new Robot();
      BufferedImage image = robot.createScreenCapture(rectangle);
      TransferableImage transferableImage = new TransferableImage(image);

      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(transferableImage, this);

      String rectangleString = "[x=" + rectangle.x +
                               ",y=" + rectangle.y +
                               ",width=" + rectangle.getWidth() +
                               ",height=" + rectangle.getHeight() + "]";

      logger_.log(Level.INFO, "Screenshot of " + rectangleString + " put on system clipboard");
    }
    catch (AWTException exception) {
      logger_.log(Level.WARNING, "Unable to make screenshot", exception);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void lostOwnership(Clipboard clipboard, Transferable transferable)
  {
    // Nothing
  }

  /**
   * An implementation of Transferable containing an image.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private class TransferableImage implements Transferable
  {
    /** The image to transfer. */
    private final Image image_;

    /**
     * Create an image transferable.
     *
     * @param image  Image to transfer. Non-null.
     */
    private TransferableImage(Image image)
    {
      assert image != null : "image cannot be null";
      image_ = image;
    }

    /** {@inheritDoc} */
    @Override
    public Object getTransferData(DataFlavor dataFlavor)
      throws UnsupportedFlavorException, IOException
    {
      if (dataFlavor.equals(DataFlavor.imageFlavor) && image_ != null)
        return image_;
      else
        throw new UnsupportedFlavorException(dataFlavor);
    }

    /** {@inheritDoc} */
    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
      return new DataFlavor[] {DataFlavor.imageFlavor};
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor)
    {
      DataFlavor[] dataFlavors = getTransferDataFlavors();
      for (DataFlavor d : dataFlavors)
        if (d.equals(dataFlavor))
          return true;

      return false;
    }
  }
}
