package no.geosoft.common.graphics.demos;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.*;



/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Custom move interaction
 * <li>Switching interactions
 * <li>Update world extent geometry
 * <li>Scroll handling
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo11 extends JFrame
  implements ActionListener, GInteraction
{
  private JButton   zoomButton_;
  private JButton   moveButton_;
  private GWindow   window_;
  private GSegment  interactionSegment_;
  private int       x0_, y0_;


  public Demo11()
  {
    super ("G Graphics Library - Demo 11");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    getContentPane().setLayout (new BorderLayout());

    // Create the GUI
    JScrollBar hScrollBar = new JScrollBar (JScrollBar.HORIZONTAL);
    getContentPane().add (hScrollBar, BorderLayout.SOUTH);

    JScrollBar vScrollBar = new JScrollBar (JScrollBar.VERTICAL);
    getContentPane().add (vScrollBar, BorderLayout.EAST);

    JPanel buttonPanel = new JPanel();
    zoomButton_ = new JButton ("Zoom");
    zoomButton_.addActionListener (this);
    buttonPanel.add (zoomButton_);

    moveButton_ = new JButton ("Move");
    moveButton_.addActionListener (this);
    buttonPanel.add (moveButton_);
    getContentPane().add (buttonPanel, BorderLayout.NORTH);

    // Create the graphic canvas
    window_ = new GWindow();
    getContentPane().add (window_.getCanvas(), BorderLayout.CENTER);

    // Create scane with default viewport and world extent settings
    GScene scene = new GScene (window_);

    // Use a normalized world extent
    double w0[] = {0.0, 0.0, 0.0};
    double w1[] = {1.0, 0.0, 0.0};
    double w2[] = {0.0, 1.0, 0.0};
    scene.setWorldExtent (w0, w1, w2);

    // Create a graphic object
    GObject object = new TestObject();
    scene.add (object);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window_.startInteraction (new ZoomInteraction (scene));

    scene.shouldWorldExtentFitViewport (false);
    scene.shouldZoomOnResize (false);
    scene.installScrollHandler (hScrollBar, vScrollBar);
  }


  public void actionPerformed (ActionEvent event)
  {
    if (event.getSource() == zoomButton_)
      window_.startInteraction (new ZoomInteraction (window_.getScene()));
    else
      window_.startInteraction (this);
  }



  // Move interaction
  public void event (GScene scene, int event, int x, int y)
  {
    switch (event) {
      case GWindow.BUTTON1_DOWN :
        interactionSegment_ = scene.findSegment (x, y);
        x0_ = x;
        y0_ = y;
        break;

      case GWindow.BUTTON1_DRAG :
        int dx = x - x0_;
        int dy = y - y0_;
        if (interactionSegment_ != null) {
          TestObject testObject = (TestObject) interactionSegment_.getOwner();
          testObject.translate (interactionSegment_, dx, dy);
          scene.refresh();
        }
        x0_ = x;
        y0_ = y;
        break;

      case GWindow.BUTTON1_UP :
        interactionSegment_ = null;
        break;
    }
  }



  /**
   * Defines the geometry and presentation for a sample graphic object.
   */
  private class TestObject extends GObject
  {
    private GSegment[] stars_;
    private double[][] geometry_;


    TestObject()
    {
      int nStars = 20;

      stars_    = new GSegment[nStars];
      geometry_ = new double[nStars][];

      for (int i = 0; i < nStars; i++) {
        stars_[i] = new GSegment();
        stars_[i].setUserData (new Integer(i));
        addSegment (stars_[i]);

        double[] xy = Geometry.createStar (Math.random(), Math.random(),
                                           0.05, 0.1, 20);
        geometry_[i] = xy;

        GStyle style = new GStyle();
        style.setForegroundColor (new Color ((float) Math.random(),
                                             (float) Math.random(),
                                             (float) Math.random()));

        style.setBackgroundColor (new Color ((float) Math.random(),
                                             (float) Math.random(),
                                             (float) Math.random()));
        style.setLineWidth (2);
        stars_[i].setStyle (style);
      }
    }


    // Convert the world extent geometry of the specified
    // segment according to specified device translation
    public void translate (GSegment segment, int dx, int dy)
    {
      GTransformer transformer = getTransformer();
      double[] dw0 = transformer.deviceToWorld (0,  0);
      double[] dw1 = transformer.deviceToWorld (dx, dy);

      int index = ((Integer) segment.getUserData()).intValue();
      double[] geometry = geometry_[index];
      for (int i = 0; i < geometry.length; i += 2) {
        geometry[i + 0] += dw1[0] - dw0[0];
        geometry[i + 1] += dw1[1] - dw0[1];
      }

      segment.setGeometryXy (geometry);
    }


    public void draw()
    {
      for (int i = 0; i < stars_.length; i++)
        stars_[i].setGeometryXy (geometry_[i]);
    }
  }



  public static void main (String[] args)
  {
    new Demo11();
  }
}
