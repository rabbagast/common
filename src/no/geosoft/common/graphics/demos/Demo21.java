package no.geosoft.common.graphics.demos;

import java.awt.*;

import javax.swing.*;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.geometry.Matrix4x4;
import no.geosoft.common.geometry.spline.SplineFactory;
import no.geosoft.common.graphics.*;



/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li> A rudimentary sheet music library
 * <li> GObject extension
 * <li> Advanced geometry generation
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo21 extends JFrame
{
  public Demo21()
  {
    super ("G Graphics Library - Demo 21");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Create the graphic canvas
    GWindow window = new GWindow (new Color (255, 255, 255));
    getContentPane().add (window.getCanvas());

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene (window);

    // Create a stave
    GStave stave1 = new GStave (25);
    stave1.setLocation (30, 50, 440);

    GNote note;
    note = new GNote ("f", 1.0, 10);
    stave1.addNote (note);

    note = new GNote ("g", 0.5, 10);
    stave1.addNote (note);

    note = new GNote ("d", 0.5, 10);
    stave1.addNote (note);

    note = new GNote ("a", 1.0, 10);
    stave1.addNote (note);

    note = new GNote ("a", 1.0, 10);
    stave1.addNote (note);


    // Another stave
    GStave stave2 = new GStave (25);
    stave2.setLocation (30, 250, 440);

    note = new GNote ("a", 1.0, 10);
    stave2.addNote (note);

    note = new GNote ("a", 1.0, 10);
    stave2.addNote (note);

    note = new GNote ("h", 0.5, 10);
    stave2.addNote (note);

    note = new GNote ("e", 1.0, 10);
    stave2.addNote (note);

    note = new GNote ("g", 0.5, 10);
    stave2.addNote (note);

    scene.add (stave1);
    scene.add (stave2);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);
  }



  private class GStave extends GObject
  {
    private int         x_, y_;
    private int         length_;
    private int         lineGap_;
    private GSegment[]  lines_;
    private GSegment    startLine_;
    private GSegment    endLine_;
    private GSegment    endBar_;
    private int         current_;


    public GStave (int lineGap)
    {
      lineGap_ = lineGap;

      GStyle style = new GStyle();
      style.setBackgroundColor (new Color (0, 0, 0));
      style.setForegroundColor (new Color (0, 0, 0));
      style.setLineWidth((int) Math.max(lineGap / 20.0, 1));
      setStyle (style);

      lines_ = new GSegment[5];
      for (int i = 0; i < 5; i++) {
        lines_[i] = new GSegment();
        addSegment (lines_[i]);
      }

      startLine_ = new GSegment();
      addSegment (startLine_);

      endLine_ = new GSegment();
      addSegment (endLine_);

      endBar_ = new GSegment();
      addSegment (endBar_);
    }


    public void setLocation (int x, int y, int length)
    {
      x_ = x;
      y_ = y;
      length_ = length;
      current_ = x_ + 2 * lineGap_;
    }


    public void addNote (GNote note)
    {
      String value = note.getValue();

      int y0 = 0;
      if      (value.equals ("c")) y0 = 10;
      else if (value.equals ("d")) y0 =  9;
      else if (value.equals ("e")) y0 =  8;
      else if (value.equals ("f")) y0 =  7;
      else if (value.equals ("g")) y0 =  6;
      else if (value.equals ("a")) y0 =  5;
      else if (value.equals ("h")) y0 =  4;

      note.setLocation (current_, y_ + (int) Math.round (y0 * lineGap_ * 0.5));
      add (note);

      current_ += lineGap_ * 3;
    }


    public void draw()
    {
      for (int i = 0; i < 5; i++) {
        lines_[i].setGeometry (x_, y_ + i * lineGap_,
                               x_ + length_, y_ + i * lineGap_);
      }

      startLine_.setGeometry (x_, y_, x_, y_ + 4 * lineGap_);

      endBar_.setGeometry (new int[] {x_ + length_,                          y_,
                                      x_ + length_ - (int) (lineGap_ * 0.5), y_,
                                      x_ + length_ - (int) (lineGap_ * 0.5), y_ + 4 * lineGap_,
                                      x_ + length_,                          y_ + 4 * lineGap_,
                                      x_ + length_,                          y_});

      endLine_.setGeometry (x_ + length_ - (int) (lineGap_ * 0.9), y_,
                            x_ + length_ - (int) (lineGap_ * 0.9),y_ + 4 * lineGap_);
    }
  }



  private class GNote extends GObject
  {
    private String     value_;
    private double     duration_;
    private GSegment   head_;
    private GSegment   stem_;
    private GSegment   flag_;
    private int        x_, y_;
    private int        headSize_;
    private int        stemLength_;
    private Matrix4x4  headRotation_;



    public GNote (String value, double duration, int size)
    {
      value_    = value;
      duration_ = duration;
      headSize_ = size;

      GStyle style = new GStyle();
      style.setBackgroundColor (new Color (0, 0, 0));
      style.setForegroundColor (new Color (0, 0, 0));
      setStyle (style);

      head_ = new GSegment();
      addSegment (head_);

      stem_ = new GSegment();
      addSegment (stem_);

      flag_ = new GSegment();
      addSegment (flag_);
    }


    public String getValue()
    {
      return value_;
    }


    public double getDuration()
    {
      return duration_;
    }



    public void setLocation (int x, int y)
    {
      x_ = x;
      y_ = y;
    }



    public void draw()
    {
      //
      // Head
      //
      int[] head = Geometry.createEllipse (0, 0,
                                           headSize_,
                                           (int) ((double) headSize_ * 1.5));

      Matrix4x4 m = new Matrix4x4();
      m.rotateZ (Math.PI / 3.0);
      m.translate (x_, y_, 0.0);
      m.transformXyPoints (head);

      head_.setGeometry (head);

      //
      // Stem
      //
      int stemWidth = (int) Math.round ((double) headSize_ / 5.0);
      if (stemWidth < 1) stemWidth = 1;

      int stemLength = headSize_ * 8;
      int stemX0 = x_ + (int) ((double) headSize_ * 1.40);
      int stemY0 = y_ - (int) (headSize_ * 0.2);

      int[] stem = new int[] {stemX0,             stemY0,
                              stemX0 - stemWidth, stemY0,
                              stemX0 - stemWidth, stemY0 - stemLength,
                              stemX0,             stemY0 - stemLength,
                              stemX0,             stemY0};

      stem_.setGeometry (stem);

      //
      // Flag
      //
      if (duration_ < 1.0) {
        int flagX0 = stemX0;
        int flagY0 = stemY0 - stemLength;

        double[] cp = new double[]
                      {flagX0, flagY0, 0.0,
                       flagX0 + (int) (headSize_ * 0.5), flagY0 + (int) (stemLength * 0.2), 0.0,
                       flagX0 + (int) (headSize_ * 2.0), flagY0 + (int) (stemLength * 0.5), 0.0,
                       flagX0 + (int) (headSize_ * 1.8), flagY0 + (int) (stemLength * 0.9), 0.0};
        double[] spline1 = SplineFactory.createCatmullRom (cp, 20);

        cp = new double[]
             {flagX0 + (int) (headSize_ * 1.8), flagY0 + (int) (stemLength * 0.9), 0.0,
              flagX0 + (int) (headSize_ * 1.7), flagY0 + (int) (stemLength * 0.7), 0.0,
              flagX0 + (int) (headSize_ * 0.5), flagY0 + (int) (stemLength * 0.45), 0.0,
              flagX0,                           flagY0 + (int) (stemLength * 0.35), 0.0};
        double[] spline2 = SplineFactory.createCatmullRom (cp, 20);

        int[] flag = new int[(spline1.length + spline2.length) / 3 * 2];

        int j = 0;
        for (int i = 0; i < spline1.length; i+=3) {
          flag[j++] = (int) Math.round (spline1[i+0]);
          flag[j++] = (int) Math.round (spline1[i+1]);
        }
        for (int i = 0; i < spline2.length; i+=3) {
          flag[j++] = (int) Math.round (spline2[i+0]);
          flag[j++] = (int) Math.round (spline2[i+1]);
        }

        flag_.setGeometry (flag);
      }
    }
  }




  public static void main (String[] args)
  {
    new Demo21();
  }
}
