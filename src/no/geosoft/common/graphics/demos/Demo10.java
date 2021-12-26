package no.geosoft.common.graphics.demos;

import java.io.File;
import java.awt.*;

import javax.swing.*;

import no.geosoft.common.graphics.*;

/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Simple image handling
 * <li>Simple drawing interaction
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo10 extends JFrame
  implements GInteraction
{
  private GScene    scene_;
  private GSegment  route_;
  private int[]     xy_;


  public Demo10 (String imageFileName)
  {
    super ("G Graphics Library - Demo 10");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    getContentPane().setLayout (new BorderLayout());
    getContentPane().add (new JLabel ("Draw line on map using mouse button 1"),
                          BorderLayout.NORTH);

    // Create the graphic canvas
    GWindow window = new GWindow();
    getContentPane().add (window.getCanvas(), BorderLayout.CENTER);

    // Create scane with default viewport and world extent settings
    scene_ = new GScene (window);

    // Create a graphic object
    GObject object = new TestObject (imageFileName);
    scene_.add (object);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window.startInteraction (this);
  }



  public void event (GScene scene, int event, int x, int y)
  {
    switch (event) {
      case GWindow.BUTTON1_DOWN :
        xy_ = new int[] {x, y};
        route_.setGeometry (xy_);
        scene_.refresh();
        break;

      case GWindow.BUTTON1_DRAG :
        int[] a = new int[xy_.length + 2];
        System.arraycopy (xy_, 0, a, 0, xy_.length);
        a[a.length-2] = x;
        a[a.length-1] = y;
        xy_ = a;
        route_.setGeometry (xy_);
        scene_.refresh();
        break;
    }
  }



  /**
   * Defines the geometry and presentation for a sample
   * graphic object.
   */
  private class TestObject extends GObject
  {
    private GSegment segment_;


    TestObject (String imageFileName)
    {
      segment_ = new GSegment();
      addSegment (segment_);

      GImage image = new GImage (new File (imageFileName));
      image.setPositionHint (GPosition.SOUTHEAST);
      segment_.setImage (image);

      route_ = new GSegment();
      addSegment (route_);

      GStyle routeStyle = new GStyle();
      routeStyle.setForegroundColor (new Color (255, 0, 0));
      routeStyle.setLineWidth (4);
      routeStyle.setAntialiased (true);
      route_.setStyle (routeStyle);
    }



    public void draw()
    {
      segment_.setGeometry (0, 0);
      route_.setGeometry (xy_);
    }
  }



  public static void main (String[] args)
  {
    if (args.length != 1) {
      System.out.println ("Provide an image file name");
      System.exit (0);
    }

    new Demo10 (args[0]);
  }
}
