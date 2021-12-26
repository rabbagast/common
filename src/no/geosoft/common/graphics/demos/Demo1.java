package no.geosoft.common.graphics.demos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.GWindow;
import no.geosoft.common.graphics.GScene;
import no.geosoft.common.graphics.GObject;
import no.geosoft.common.graphics.GSegment;
import no.geosoft.common.graphics.GText;
import no.geosoft.common.graphics.GPosition;
import no.geosoft.common.graphics.GStyle;

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
public final class Demo1 extends JFrame
{
  public Demo1()
  {
    super("G Graphics Library - Demo 1");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create the graphic canvas
    GWindow window = new GWindow(new Color(210, 235, 255));
    getContentPane().add(window.getCanvas());

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene(window);

    // Create the graphics object and add to the scene
    GObject helloWorld = new HelloWorld();
    scene.add(helloWorld);

    pack();
    setSize(new Dimension(500, 500));
    setVisible(true);
  }

  /**
   * Defines the geometry and presentation for the sample
   * graphic object.
   */
  public class HelloWorld extends GObject
  {
    private final GSegment star_ = new GSegment();

    public HelloWorld()
    {
      GStyle starStyle = new GStyle();
      starStyle.setForegroundColor(new Color(255, 0, 0));
      starStyle.setBackgroundColor(new Color(255, 255, 0));
      starStyle.setLineWidth(3);
      setStyle(starStyle);
      addSegment(star_);

      GText text = new GText("HelloWorld", GPosition.MIDDLE);
      GStyle textStyle = new GStyle();
      textStyle.setForegroundColor(new Color(100, 100, 150));
      textStyle.setBackgroundColor(null);
      textStyle.setFont(new Font("Dialog", Font.BOLD, 48));
      text.setStyle(textStyle);
      star_.setText(text);
    }

    /**
     * This method is called whenever the canvas needs to redraw this
     * object. For efficiency, prepare as much of the graphic object
     * up front (such as sub object, segment and style setup) and
     * set geometry only in this method.
     */
    public void draw()
    {
      star_.setGeometry(Geometry.createStar(220, 220, 200, 80, 15));
    }
  }

  public static void main(String[] arguments)
  {
    new Demo1();
  }
}
