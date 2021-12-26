package no.geosoft.common.graphics.demos;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;

import javax.swing.*;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.geometry.Matrix4x4;
import no.geosoft.common.graphics.*;

/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li> Advaced geometry generation
 * <li> Dynamic update
 * <li> Threading
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo23 extends JFrame
{
  public Demo23()
  {
    super ("G Graphics Library - Demo 23");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Create the graphic canvas
    GWindow window = new GWindow (new Color (210, 235, 255));
    getContentPane().add (window.getCanvas());

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene (window);
    double w0[] = {  0.0,   0.0, 0.0};
    double w1[] = {1000.0,   0.0, 0.0};
    double w2[] = {  0.0, 1000.0, 0.0};
    scene.setWorldExtent (w0, w1, w2);

    // Create clock
    GClock clock1 = new GClock (500, 500, 350);
    scene.add (clock1);

    // GClock clock2 = new GClock (40, 80, 15);
    // scene.add (clock2);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window.startInteraction (new ZoomInteraction (scene));

    Timer timer = new Timer (true);
    timer.schedule (new Ticker (clock1), 0, 1000);
  }

  /**
   * Defines the geometry and presentation for the sample
   * graphic object.
   */
  private class GClock extends GObject
  {
    private double      x0_, y0_;
    private double      radius_;
    private GSegment    background_;
    private GSegment    disc_;
    private GSegment[]  ticks_;
    private GSegment[]  labels_;
    private GSegment    hourHandle_;
    private GSegment    minuteHandle_;
    private GSegment    secondHandle_;
    private GSegment    brand_;
    private double[]    hourHandleGeometry_;
    private double[]    minuteHandleGeometry_;
    private double[]    secondHandleGeometry_;

    public GClock (double x0, double y0, double radius)
    {
      x0_     = x0;
      y0_     = y0;
      radius_ = radius;

      background_ = new GSegment();
      GStyle backgroundStyle = new GStyle();
      backgroundStyle.setBackgroundColor (new Color (122, 136, 161));
      backgroundStyle.setForegroundColor (new Color (0, 0, 0));
      background_.setStyle (backgroundStyle);
      addSegment (background_);

      disc_ = new GSegment();
      GStyle discStyle = new GStyle();
      discStyle.setBackgroundColor (new Color (255, 255, 255));
      discStyle.setForegroundColor (new Color (255, 255, 255));
      disc_.setStyle (discStyle);
      addSegment (disc_);

      ticks_ = new GSegment[60];
      GStyle minuteStyle = new GStyle();
      minuteStyle.setForegroundColor (new Color (0, 0, 0));
      minuteStyle.setLineWidth (1);

      GStyle tickStyle = (GStyle) minuteStyle.clone();
      tickStyle.setLineWidth (3);

      for (int i = 0; i < 60; i++) {
        ticks_[i] = new GSegment();
        ticks_[i].setStyle (i % 5 == 0 ? tickStyle : minuteStyle);
        addSegment (ticks_[i]);
      }

      labels_ = new GSegment[12];
      GStyle labelStyle = new GStyle();
      labelStyle.setForegroundColor (new Color (0, 0, 0));
      labelStyle.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      labelStyle.setFont (new Font ("Dialog", Font.BOLD, 24));
      for (int i = 0; i < 12; i++) {
        labels_[i] = new GSegment();
        labels_[i].setStyle (labelStyle);
        int hour = (14 - i) % 12 + 1;
        labels_[i].setText (new GText (Integer.toString (hour)));
        addSegment (labels_[i]);
      }

      brand_ = new GSegment();
      GStyle brandStyle = new GStyle();
      brandStyle.setForegroundColor (new Color (0, 0, 0));
      brandStyle.setFont (new Font ("Times", Font.PLAIN, 12));
      brandStyle.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      brand_.setStyle (brandStyle);
      brand_.setText (new GText("GeoSoft", GPosition.MIDDLE));
      addSegment (brand_);

      GStyle handleStyle = new GStyle();
      handleStyle.setForegroundColor (new Color (0.0f, 0.0f, 0.0f, 0.5f));
      handleStyle.setBackgroundColor (new Color (0.0f, 0.0f, 0.0f, 0.3f));
      handleStyle.setLineWidth (1);

      GStyle secondHandleStyle = new GStyle();
      secondHandleStyle.setForegroundColor (new Color (255, 0, 0));
      secondHandleStyle.setBackgroundColor (new Color (255, 0, 0));
      secondHandleStyle.setLineWidth (1);

      hourHandle_ = new GSegment();
      hourHandle_.setStyle (handleStyle);
      addSegment (hourHandle_);

      hourHandleGeometry_ = new double[]
                            {- 0.10*radius_, - 0.04*radius_,
                             - 0.10*radius_, + 0.04*radius_,
                             + 0.60*radius_, + 0.04*radius_,
                             + 0.65*radius_, 0.0,
                             + 0.60*radius_, - 0.04*radius_,
                             - 0.10*radius_, - 0.04*radius_};

      minuteHandle_ = new GSegment();
      minuteHandle_.setStyle (handleStyle);
      addSegment (minuteHandle_);

      minuteHandleGeometry_ = new double[]
                            {- 0.10*radius_, - 0.04*radius_,
                             - 0.10*radius_, + 0.04*radius_,
                             + 0.90*radius_, + 0.04*radius_,
                             + 0.95*radius_, 0.0,
                             + 0.90*radius_, - 0.04*radius_,
                             - 0.10*radius_, - 0.04*radius_};

      secondHandle_ = new GSegment();
      secondHandle_.setStyle (secondHandleStyle);
      addSegment (secondHandle_);

      secondHandleGeometry_ = new double[]
                            {- 0.10*radius_, - 0.02*radius_,
                             + 0.85*radius_, 0.0,
                             - 0.10*radius_, + 0.02*radius_,
                             - 0.10*radius_, - 0.02*radius_};
    }

    private void update()
    {
      Calendar time = Calendar.getInstance();

      int hour   = time.get (Calendar.HOUR_OF_DAY);
      int minute = time.get (Calendar.MINUTE);
      int second = time.get (Calendar.SECOND);
      int secondOfDay = second + minute*60 + hour*60*60;

      double hourAngle = Math.PI / 2.0 -
                         (double) secondOfDay / (24.0 * 60.0 * 60.0) *
                         Math.PI * 4.0;

      secondOfDay -= hour*60*60;

      double minuteAngle = Math.PI / 2.0 -
                           (double) secondOfDay / (60.0 * 60.0) *
                           Math.PI * 2.0;

      secondOfDay -= minute*60;

      double secondAngle = Math.PI / 2.0 -
                           (double) secondOfDay / 60.0 *
                           Math.PI * 2.0;

      double[] geometry = new double[hourHandleGeometry_.length];
      System.arraycopy (hourHandleGeometry_, 0, geometry, 0,
                        hourHandleGeometry_.length);
      Matrix4x4 m = new Matrix4x4();
      m.rotateZ (hourAngle);
      m.translate (x0_, y0_, 0.0);
      m.transformXyPoints (geometry);
      hourHandle_.setGeometryXy (geometry);

      geometry = new double[minuteHandleGeometry_.length];
      System.arraycopy (minuteHandleGeometry_, 0, geometry, 0,
                        minuteHandleGeometry_.length);
      m = new Matrix4x4();
      m.rotateZ (minuteAngle);
      m.translate (x0_, y0_, 0.0);
      m.transformXyPoints (geometry);
      minuteHandle_.setGeometryXy (geometry);

      geometry = new double[secondHandleGeometry_.length];
      System.arraycopy (secondHandleGeometry_, 0, geometry, 0,
                        secondHandleGeometry_.length);
      m = new Matrix4x4();
      m.rotateZ (secondAngle);
      m.translate (x0_, y0_, 0.0);
      m.transformXyPoints (geometry);
      secondHandle_.setGeometryXy (geometry);
    }

    public void draw()
    {
      background_.setGeometryXy (Geometry.createCircle (x0_, y0_, radius_ * 1.2));
      disc_.setGeometryXy (Geometry.createCircle (x0_, y0_, radius_));

      for (int i = 0; i < 60; i++) {
        double x0 = radius_ * (i % 5 == 0 ? 0.88 : 0.92);
        double x1 = radius_ * 0.98;

        double[] geometry = new double[] {x0, 0, x1, 0};
        Matrix4x4 m = new Matrix4x4();
        m.rotateZ (2.0 * Math.PI * i / 60.0);
        m.translate (x0_, y0_, 0.0);
        m.transformXyPoints (geometry);
        ticks_[i].setGeometryXy (geometry);
      }

      for (int i = 0; i < 12; i++) {
        double[] geometry = new double[] {radius_ * 0.75, 0};

        Matrix4x4 m = new Matrix4x4();
        m.rotateZ (2.0 * Math.PI * i / 12.0);
        m.translate (x0_, y0_, 0.0);
        m.transformXyPoints (geometry);

        labels_[i].setGeometryXy (geometry);
      }

      brand_.setGeometry (x0_, y0_ - radius_ * 0.3);

      update();
    }
  }

  private class Ticker extends TimerTask
  {
    private GClock clock_;

    public Ticker (GClock clock)
    {
      clock_ = clock;
    }

    public void run()
    {
      clock_.update();
      clock_.refresh();
    }
  }

  public static void main (String[] args)
  {
    new Demo23();
  }
}
