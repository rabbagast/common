package no.geosoft.common.graphics.demos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
 *   <li> Custom world extent
 *   <li> Zoom interaction
 *   <li> Text background color
 *   <li> The effect of transparency
 *   <li> Annotation layout algorithm
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Demo5 extends JFrame
{
  /**
   * Class for creating the demo canvas and hande Swing events.
   */
  public Demo5()
  {
    super("G Graphics Library - Demo 5");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create the GUI
    JPanel topLevel = new JPanel();
    topLevel.setLayout(new BorderLayout());
    getContentPane().add(topLevel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(new JLabel("Zoom with rubberband and mouse buttons"));
    topLevel.add (buttonPanel,  BorderLayout.NORTH);

    // Create the graphic canvas
    GWindow window = new GWindow(new Color(240, 230, 230));
    topLevel.add (window.getCanvas(), BorderLayout.CENTER);

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene (window, "Scene");
    scene.setWorldExtent (0.0, -Math.PI / 2.0, 5.0 * Math.PI, 1.0 * Math.PI);

    // Create curve object
    GObject curve = new Curve();
    scene.add (curve);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window.startInteraction (new ZoomInteraction (scene));
  }

  /**
   * Defines the geometry and presentation for the sample
   * graphics object.
   */
  public final class Curve extends GObject
  {
    private final GSegment curve_ = new GSegment();

    public Curve()
    {
      addSegment(curve_);

      GStyle curveStyle = new GStyle();
      curveStyle.setForegroundColor(new Color(255, 0, 0));
      curveStyle.setLineWidth (12);
      setStyle (curveStyle);

      GStyle textStyle = new GStyle();
      textStyle.setForegroundColor(new Color(255, 255, 255));
      textStyle.setBackgroundColor(new Color(0.0f, 0.0f, 0.0f, 0.3f));
      textStyle.setFont (new Font("Dialog", Font.BOLD, 14));

      for (int i = 0; i < 10; i++) {
        GText text = new GText("Text " + i, GPosition.LEFT | GPosition.EAST);
        text.setStyle(textStyle);
        curve_.addText(text);
      }
    }

    public void draw()
    {
      int nPoints = 500;

      double[] x = new double[nPoints];
      double[] y = new double[nPoints];

      double step = Math.PI * 10.0 / nPoints;
      for (int i = 0; i < nPoints; i++) {
        x[i] = i * step;
        y[i] = Math.sin (x[i]);
      }

      curve_.setGeometry(x, y);
    }
  }

  public static void main (String[] arguments)
  {
    new Demo5();
  }
}
