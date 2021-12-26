package no.geosoft.common.graphics.demos;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import no.geosoft.common.graphics.*;



/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Point annotations
 * <li>Annotation algorithm
 * <li>Annotation background color
 * <li>Point images
 * <li>Printing feature
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo12 extends JFrame
  implements ActionListener
{
  private GWindow  window_;

  /**
   * Class for creating the demo canvas and hande Swing events.
   */
  public Demo12()
  {
    super ("G Graphics Library - Demo 12");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Create the GUI
    JPanel topLevel = new JPanel();
    topLevel.setLayout (new BorderLayout());
    getContentPane().add (topLevel);

    JPanel buttonPanel = new JPanel();
    JButton printButton = new JButton ("Print");
    printButton.addActionListener (this);
    buttonPanel.add (printButton);
    topLevel.add (buttonPanel, BorderLayout.NORTH);

    // Create the graphic canvas
    window_ = new GWindow (new Color (255, 255, 255));
    topLevel.add (window_.getCanvas(), BorderLayout.CENTER);

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene (window_, "Scene");

    double w0[] = {0.0, 0.0, 0.0};
    double w1[] = {1.0, 0.0, 0.0};
    double w2[] = {0.0, 1.0, 0.0};
    scene.setWorldExtent (w0, w1, w2);

    TestObject testObject = new TestObject();
    scene.add (testObject);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window_.startInteraction (new ZoomInteraction (scene));
  }



  public void actionPerformed (ActionEvent event)
  {
    try {
      window_.print();
    }
    catch (Exception exception) {
      System.out.println ("Print failed!");
    }
  }



  /**
   * Defines the geometry and presentation for a sample graphic object.
   */
  private class TestObject extends GObject
  {
    private GSegment  line_;
    private double[]  x_, y_;


    /**
     * As a rule of thumb we create as much of the object during
     * construction as possible. Try to do geometry only in the
     * draw method.
     */
    TestObject()
    {
      GStyle lineStyle = new GStyle();
      lineStyle.setLineWidth (2);
      lineStyle.setForegroundColor (new Color (100, 100, 100));
      lineStyle.setAntialiased (true);

      GStyle symbolStyle = new GStyle();
      symbolStyle.setForegroundColor (new Color (0, 0, 255));
      symbolStyle.setBackgroundColor (new Color (255, 255, 255));

      GStyle textStyle = new GStyle();
      textStyle.setFont (new Font ("Dialog", Font.BOLD, 14));
      textStyle.setForegroundColor (new Color (255, 255, 0));
      textStyle.setBackgroundColor (new Color (100, 100, 100));

      line_ = new GSegment();
      line_.setStyle (lineStyle);
      addSegment (line_);

      GImage symbol = new GImage (GImage.SYMBOL_SQUARE2);
      symbol.setStyle (symbolStyle);
      line_.setVertexImage (symbol);

      int nPoints = 10;

      for (int i = 0; i < nPoints; i++) {
        GText text = new GText ("Point " + i,
                                GPosition.NORTH |
                                GPosition.STATIC);
        text.setStyle (textStyle);
        line_.addText (text);
      }

      // Geometry
      x_ = new double[nPoints];
      y_ = new double[nPoints];

      for (int i = 0; i < nPoints; i++) {
        x_[i] = 0.2 + 0.8 * i * (1.0 / nPoints);
        y_[i] = 0.1 + 0.8 * Math.random();
      }
    }



    public void draw()
    {
      line_.setGeometry (x_, y_);
    }
  }



  public static void main (String[] args)
  {
    new Demo12();
  }
}
