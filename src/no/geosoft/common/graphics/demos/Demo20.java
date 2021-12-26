package no.geosoft.common.graphics.demos;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.*;



/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li> A math application
 * <li> Polyline performance
 * <li> Geometry generation
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo20 extends JFrame
  implements ActionListener
{
  private JButton        addLevelButton_;
  private JButton        removeLevelButton_;
  private JLabel         nEdgesLabel_;
  private JLabel         lengthLabel_;
  private JLabel         areaLabel_;
  private KochSnowflake  kochSnowflake_;



  public Demo20()
  {
    super ("G Graphics Library - Demo 20");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    JPanel controlPanel = new JPanel();
    addLevelButton_ = new JButton("+");
    addLevelButton_.addActionListener (this);
    controlPanel.add (addLevelButton_);

    removeLevelButton_ = new JButton("-");
    removeLevelButton_.addActionListener (this);
    controlPanel.add (removeLevelButton_);

    nEdgesLabel_ = new JLabel();
    controlPanel.add (nEdgesLabel_);

    lengthLabel_ = new JLabel();
    controlPanel.add (lengthLabel_);

    areaLabel_ = new JLabel();
    controlPanel.add (areaLabel_);
    getContentPane().add (controlPanel, BorderLayout.NORTH);

    // Create the graphic canvas
    GWindow window = new GWindow (new Color (255, 255, 255));
    getContentPane().add (window.getCanvas(), BorderLayout.CENTER);

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene (window);
    double w0[] = {0.0,  0.0, 0.0};
    double w1[] = {1.0,  0.0, 0.0};
    double w2[] = {0.0,  1.0, 0.0};
    scene.setWorldExtent (w0, w1, w2);

    // Create the graphics object and add to the scene
    kochSnowflake_ = new KochSnowflake();
    scene.add (kochSnowflake_);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window.startInteraction (new ZoomInteraction (scene));
    refresh();
  }



  public void actionPerformed (ActionEvent event)
  {
    if (event.getSource() == addLevelButton_)
      kochSnowflake_.addLevel();
    else
      kochSnowflake_.removeLevel();

    refresh();
  }



  public void refresh()
  {
    nEdgesLabel_.setText ("#edges = " + kochSnowflake_.getNEdges());

    DecimalFormat format = new DecimalFormat ("0.00000");

    double length = kochSnowflake_.computeLength();
    lengthLabel_.setText ("  Length = " + format.format (length));

    double area = kochSnowflake_.computeArea();
    areaLabel_.setText ("  Area = " + format.format (area));

    kochSnowflake_.refresh();
  }



  /**
   * Defines the geometry and presentation for the sample
   * graphic object.
   */
  private class KochSnowflake extends GObject
  {
    private GSegment  curve_;
    private double[]  geometry_;


    KochSnowflake()
    {
      GStyle style = new GStyle();
      style.setForegroundColor (new Color (255, 0, 0));
      style.setBackgroundColor (new Color (255, 255, 0));
      setStyle (style);

      curve_ = new GSegment();
      geometry_ = new double[] {0.2, 0.2,
                                0.8, 0.2,
                                0.5, 0.2 + 0.6 * Math.sin (Math.PI / 3.0),
                                0.2, 0.2};
      addSegment (curve_);
    }


    public int getNEdges()
    {
      return geometry_.length / 2 - 1;
    }


    public double computeLength()
    {
      double length = 0;
      for (int i = 0; i < geometry_.length - 2; i += 2) {
        length += Geometry.length (geometry_[i+0], geometry_[i+1],
                                   geometry_[i+2], geometry_[i+3]);
      }
      return length;
    }



    public double computeArea()
    {
      return Geometry.computePolygonArea (geometry_);
    }



    public void addLevel()
    {
      int nLegs   = geometry_.length / 2 - 1;
      int nPoints = nLegs * 4 * 2 + 2;

      double[] geometry = new double[nPoints];

      int j = 0;
      for (int i = 0; i < nLegs*2; i += 2) {
        geometry[j+0] = geometry_[i+0];
        geometry[j+1] = geometry_[i+1];

        double[] v0 = Geometry.computePointOnLine (geometry_[i+0],
                                                   geometry_[i+1],
                                                   geometry_[i+2],
                                                   geometry_[i+3],
                                                   1.0 / 3.0);
        geometry[j+2] = v0[0];
        geometry[j+3] = v0[1];

        double[] v1 = Geometry.computePointOnLine (geometry_[i+0],
                                                   geometry_[i+1],
                                                   geometry_[i+2],
                                                   geometry_[i+3],
                                                   2.0 / 3.0);

        geometry[j+6] = v1[0];
        geometry[j+7] = v1[1];

        double length = Geometry.length (v0[0], v0[1], v1[0], v1[1]);

        double dx = geometry_[i+2] - geometry_[i+0];
        double dy = geometry_[i+3] - geometry_[i+1];

        double l = Math.sqrt (dx * dx + dy * dy);
        double angle = Math.acos (dx / l);
        if (dy < 0) angle = 2.0 * Math.PI - angle;
        angle -= Math.PI / 3.0;

        geometry[j+4] = geometry[j+2] + length * Math.cos (angle);
        geometry[j+5] = geometry[j+3] + length * Math.sin (angle);

        j+= 8;
      }

      geometry[geometry.length-2] = geometry[0];
      geometry[geometry.length-1] = geometry[1];

      geometry_ = geometry;
      redraw();
    }



    public void removeLevel()
    {
      int nLegs   = geometry_.length / 2 - 1;
      if (nLegs == 3) return;

      int nPoints = nLegs / 4 * 2 + 2;

      double[] geometry = new double[nPoints];

      int j = 0;
      for (int i = 0; i < geometry.length - 2; i += 2) {
        geometry[i+0] = geometry_[j+0];
        geometry[i+1] = geometry_[j+1];
        j += 8;
      }

      geometry[geometry.length-2] = geometry[0];
      geometry[geometry.length-1] = geometry[1];

      geometry_ = geometry;
      redraw();
    }



    public void draw()
    {
      curve_.setGeometryXy (geometry_);
    }
  }



  public static void main (String[] args)
  {
    new Demo20();
  }
}
