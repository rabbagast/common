package no.geosoft.common.graphics.demos;


import java.awt.*;

import javax.swing.*;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.*;



/**
 * G demo program. Demonstrates:
 *
 * <ul>
 *   <li> Basic graphic window and scene setup
 *   <li> Setting canvas background color
 *   <li> Basic scene composition
 *   <li> The use of style
 *   <li> Example geometry generation
 *   <li> Simple annotation with positioning
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class DemoX extends JFrame
{
  public DemoX()
  {
    super ("G Graphics Library - Demo 1");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Create the graphic canvas
    GWindow window = new GWindow(new Color (210, 235, 255));
    getContentPane().add (window.getCanvas());

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene (window);
    scene.setWorldExtent (0.0, 0.0, 100000.0, 100000.0);

    // Create the graphics object and add to the scene
    GObject helloWorld = new HelloWorld();
    scene.add (helloWorld);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);
  }



  /**
   * Defines the geometry and presentation for the sample
   * graphic object.
   */
  public class HelloWorld extends GObject
  {
    private GSegment star_;


    public HelloWorld()
    {
      star_ = new GSegment();
      addSegment (star_);
    }



    /**
     * This method is called whenever the canvas needs to redraw this
     * object. For efficiency, prepare as much of the graphic object
     * up front (such as sub object, segment and style setup) and
     * set geometry only in this method.
     */
    public void draw()
    {
      star_.setGeometryXy (Geometry.createCircle (50000.0, 50000.0, 40000.0));
    }
  }



  public static void main (String[] args)
  {
    new DemoX();
  }
}
