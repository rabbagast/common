package no.geosoft.common.graphics.demos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.GObject;
import no.geosoft.common.graphics.GPosition;
import no.geosoft.common.graphics.GScene;
import no.geosoft.common.graphics.GSegment;
import no.geosoft.common.graphics.GStyle;
import no.geosoft.common.graphics.GText;
import no.geosoft.common.graphics.GWindow;
import no.geosoft.common.graphics.ZoomInteraction;

/**
 * G demo program. Demonstrates:
 *
 * <ul>
 *   <li> The effect of using world coordinates
 *   <li> Default zoom interaction
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo1a extends JFrame
{
  public Demo1a()
  {
    super("G Graphics Library - Demo 1a");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane();

    javax.swing.JPanel tab1 = new javax.swing.JPanel();
    tab1.setLayout(new java.awt.BorderLayout());
    tab1.setOpaque(true);
    tab1.add(new javax.swing.JButton("BUTTON"));
    tabbedPane.addTab("Tab 1", tab1);

    javax.swing.JPanel tab2 = new javax.swing.JPanel();
    tab2.setLayout(new java.awt.BorderLayout());
    tab2.setBackground(Color.RED);
    tab2.setOpaque(true);
    tabbedPane.addTab("Tab 2", tab2);

    getContentPane().add(tabbedPane);

    // Create the graphic canvas
    GWindow window = new GWindow(new Color (210, 235, 255));

    // getContentPane().add(window.getCanvas());
    tab2.add(window.getCanvas());

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene(window);
    scene.setWorldExtent(0.0, 0.0, 1.0, 1.0);

    // Create the graphics object and add to the scene
    GObject object = new HelloWorld();
    scene.add(object);

    pack();
    setSize(new Dimension(500, 500));
    setVisible(true);

    window.startInteraction(new ZoomInteraction(scene));
  }

  /**
   * Defines the geometry and presentation for the sample
   * graphic object.
   */
  public class HelloWorld extends GObject
  {
    private final GSegment star_;

    public HelloWorld()
    {
      star_ = new GSegment();
      GStyle starStyle = new GStyle();
      starStyle.setForegroundColor(new Color (255, 0, 0));
      starStyle.setGradient(0.0, 0.0, new Color(255, 255, 255),
                            0.0, 1.0, new Color(0, 0, 0));
      starStyle.setLineWidth(3);
      setStyle(starStyle);
      addSegment(star_);

      GText text = new GText("HelloWorld", GPosition.MIDDLE);
      GStyle textStyle = new GStyle();
      textStyle.setForegroundColor(new Color (100, 100, 150));
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
      star_.setGeometryXy(Geometry.createStar(0.5, 0.5, 0.4, 0.2, 15));
    }
  }

  public static void main(String[] arguments)
  {
    new Demo1a();
  }
}
