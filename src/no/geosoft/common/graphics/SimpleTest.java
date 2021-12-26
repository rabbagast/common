package no.geosoft.common.graphics;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.util.Screen;

public class SimpleTest extends JPanel
{
  private double scaleFactor_ = 0.01;

  private final GWindow window_ = new GWindow(new Color(200, 180, 180));

  private final JScrollBar hScrollBar_ = new JScrollBar(JScrollBar.HORIZONTAL);

  private final JScrollBar vScrollBar_ = new JScrollBar(JScrollBar.VERTICAL);

  public SimpleTest()
  {
    build();
    setToolTipTextRecursively(this, "Tesdt");
  }

  private static void setToolTipTextRecursively(JComponent c, String text)
  {
    c.setToolTipText(text);

    for (Component cc : c.getComponents())
      if (cc instanceof JComponent)
        setToolTipTextRecursively((JComponent) cc, text);
  }

  private void build()
  {
    setLayout(new BorderLayout());
    add(window_.getCanvas(), BorderLayout.CENTER);
    add(hScrollBar_, BorderLayout.SOUTH);
    add(vScrollBar_, BorderLayout.EAST);
  }

  public void addScene(int position)
  {
    TrackScene scene = new TrackScene(window_);
    scene.setPosition(position);

    scene.shouldZoomOnResize(false);
    scene.shouldWorldExtentFitViewport(true);

    scene.setWorldExtent(new double[] {0.0, 2000.0, 0.0},
                         new double[] {1.0, 2000.0, 0.0},
                         new double[] {0.0,    0.0, 0.0});

    scene.add(new HelloWorld());
    scene.add(new WorldBorder());

    scene.installScrollHandler(hScrollBar_, vScrollBar_);

    // window_.refresh();
  }

  public class TrackScene extends GScene
  {
    private int x_ = 0;
    private int trackWidth_ = 200;

    public TrackScene(GWindow window)
    {
      super(window);
    }

    public void setPosition(int x)
    {
      x_ = x;
    }

    public void setTrackWidth(int trackWidth)
    {
      trackWidth_ = trackWidth;
    }

    protected void resize(double dx, double dy)
    {
      double depth = getWorldExtent().get(2)[1];

      super.resize(dx, dy);

      int windowWidth = Math.round(getWindow().getWidth());
      int windowHeight = Math.round(getWindow().getHeight());

      double pixelsPerMeter = Screen.getPixelsPerMeter();
      double heightInMeters = windowHeight / pixelsPerMeter;
      double viewInMeters = heightInMeters / scaleFactor_;

      setViewport(x_, 0, trackWidth_, windowHeight);

      zoom(new double[] {0.0,  depth + viewInMeters, 0.0},
           new double[] {1.0,  depth + viewInMeters, 0.0},
           new double[] {0.0,  depth, 0.0});
    }
  }


  public class WorldBorder extends GObject
  {
    private final GSegment border_ = new GSegment();

    WorldBorder()
    {
      GStyle style = new GStyle();
      style.setForegroundColor (new Color (255, 0, 0));
      style.setBackgroundColor (new Color (255, 255, 0));
      style.setLineWidth(3);
      setStyle(style);
      addSegment(border_);
    }

    public void draw()
    {
      GWorldExtent worldExtent = getScene().getInitialWorldExtent();
      double[] w0 = worldExtent.get(0);
      double[] w1 = worldExtent.get(1);
      double[] w2 = worldExtent.get(2);

      double[] xyz = new double[] {w0[0], w0[1], w0[2],
                                   w1[0], w1[1], w1[2],
                                   w2[0], w2[1], w2[2],
                                   w0[0], w0[1], w0[2]};

      border_.setGeometry(xyz);
    }
  }

  public class HelloWorld extends GObject
  {
    private GSegment star_;

    public HelloWorld()
    {
      star_ = new GSegment();
      GStyle starStyle = new GStyle();
      starStyle.setForegroundColor (new Color (255, 0, 0));
      starStyle.setBackgroundColor (new Color (255, 255, 0));
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
      star_.setGeometryXy(Geometry.createStar(0.5, 1000.0, 1.0, 0.8, 15));
    }
  }

  public static void main(String[] arguments)
  {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    SimpleTest t = new SimpleTest();
    f.add(t);

    f.pack();
    f.setSize(500, 500);

    f.setVisible(true);

    t.addScene(0);
    t.addScene(300);
  }
}
