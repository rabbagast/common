package no.geosoft.common.graphics.demos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.geometry.Matrix4x4;
import no.geosoft.common.graphics.GObject;
import no.geosoft.common.graphics.GPosition;
import no.geosoft.common.graphics.GScene;
import no.geosoft.common.graphics.GSegment;
import no.geosoft.common.graphics.GStyle;
import no.geosoft.common.graphics.GText;
import no.geosoft.common.graphics.GWindow;

/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Simple animation technique
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo9 extends JFrame
{
  public Demo9()
  {
    super ("G Graphics Library - Demo 9");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create graphic canvas
    GWindow window = new GWindow(new Color(200, 210, 200));
    getContentPane().add(window.getCanvas());

    // Create a view with default viewport and world extent
    GScene scene = new GScene(window);

    // Create a graphic object and put into view
    TestObject object = new TestObject();
    scene.add(object);

    pack();
    setSize (new Dimension(500, 500));
    setVisible(true);

    // Keep program running. Add sleep() if it rotates too fast.
    while (!false) {
      try {
        Thread.sleep(25);
      }
      catch (Exception e){
      }

      object.rotate();
      window.refresh();
    }
  }

  class TestObject extends GObject
  {
    private static final int AXIS1_LENGTH = 500;
    private static final int AXIS2_LENGTH = 220;
    private static final int RADII        = 100;

    private double[]    spikePos_;
    private GSegment    weel_;
    private GSegment[]  spikes_;
    private GSegment    axis1_;
    private GSegment    axis2_;
    private double      position_;

    public TestObject()
    {
      spikePos_ = new double[] {0.0 * Math.PI / 3.0,
                                1.0 * Math.PI / 3.0,
                                2.0 * Math.PI / 3.0,
                                3.0 * Math.PI / 3.0,
                                4.0 * Math.PI / 3.0,
                                5.0 * Math.PI / 3.0};
      spikes_ = new GSegment[spikePos_.length];

      GStyle spikeStyle = new GStyle();
      spikeStyle.setLineWidth (6);
      spikeStyle.setCapStyle (BasicStroke.CAP_ROUND);
      spikeStyle.setForegroundColor (new Color (130, 140, 130));

      weel_ = new GSegment();
      GStyle weelStyle = new GStyle();
      weelStyle.setForegroundColor (new Color (70, 80, 70));
      weelStyle.setLineWidth (20);
      weel_.setStyle (weelStyle);
      addSegment (weel_);

      for (int i = 0; i < spikePos_.length; i++) {
        spikes_[i] = new GSegment();
        spikes_[i].setStyle (spikeStyle);
        addSegment (spikes_[i]);
      }

      axis2_ = new GSegment();
      GStyle style = new GStyle();
      style.setForegroundColor (new Color (0.3f, 0.4f, 0.3f, 0.5f));
      style.setLineWidth (30);
      axis2_.setStyle (style);
      addSegment (axis2_);

      GText text = new GText (null, GPosition.RIGHT | GPosition.WEST);
      style = new GStyle();
      style.setForegroundColor (new Color (255, 255, 255));
      style.setFont (new Font ("Dialog", Font.BOLD, 18));
      text.setStyle (style);
      axis2_.setText (text);

      axis1_ = new GSegment();
      style = new GStyle();
      style.setForegroundColor (new Color (160, 160, 160));
      style.setLineWidth (15);
      // style.setCapStyle (BasicStroke.CAP_BUTT);
      axis1_.setStyle (style);
      addSegment (axis1_);

      position_ = 0.0;
    }

    public void draw()
    {
      // Center of viewport
      int x0 = (int) getScene().getViewport().getCenterX() + 100;
      int y0 = (int) getScene().getViewport().getCenterY();

      // Geometry for the wheel circumference
      int[] xy = Geometry.createEllipse (x0, y0, RADII, RADII);
      weel_.setGeometry (xy);

      // Wheel spikes
      for (int i = 0; i < spikePos_.length; i++) {
        double position = spikePos_[i] + position_;
        if (position_ > Math.PI * 2) position -= Math.PI * 2;

        Matrix4x4 m = new Matrix4x4();
        m.rotateZ (position);
        m.translate (x0, y0, 0);

        double[] xyz = {RADII, 0.0, 0.0};
        xyz = m.transformPoint (xyz);
        spikes_[i].setGeometry (x0, y0, (int) xyz[0], (int) xyz[1]);

        // On first spike add the axis
        if (i == 0) {
          int axisX0 = (int) xyz[0];
          int axisY0 = (int) xyz[1];
          int axisY1 = y0;

          int dy2 = (axisY1 - axisY0) * (axisY1 - axisY0);
          int dx = (int) Math.sqrt (AXIS2_LENGTH * AXIS2_LENGTH - dy2);

          int axisX1 = axisX0 - dx;

          axis1_.setGeometry (axisX1, axisY1, axisX1 - AXIS1_LENGTH, axisY1);
          axis2_.setGeometry (axisX0, axisY0, axisX1, axisY1);
        }
      }

      GText text = axis2_.getText();
      text.setText ("position = " + Long.toString (Math.round (position_ * 180.0 / Math.PI)) + ".0");
    }

    // Make a 6 degree turn
    public void rotate()
    {
      position_ += Math.PI / 60;
      if (position_ > Math.PI * 2) position_ -= Math.PI * 2;
      draw();
    }
  }

  public static void main (String[] args)
  {
    new Demo9();
  }
}
