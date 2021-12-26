package no.geosoft.common.graphics;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Test extends JPanel
  implements ActionListener
{
  private int trackWidth = 200;

  private int headerHeight = 50;

  private final GWindow headerWindow_ = new GWindow(new Color(100, 0, 0));

  private final GWindow trackWindow_ = new GWindow(new Color(0, 100, 0));

  private final GScene headerScene_ = new GScene(headerWindow_);

  private final GScene trackScene_ = new GScene(trackWindow_);

  private final JScrollBar vScrollBar_ = new JScrollBar(JScrollBar.VERTICAL);

  private final JScrollBar hScrollBar_ = new JScrollBar(JScrollBar.HORIZONTAL);

  private final JButton button1_ = new JButton("B1");

  private final JButton button2_ = new JButton("B2");

  public Test()
  {
    Component c = build();

    setLayout(new BorderLayout());
    add(c, BorderLayout.CENTER);

    trackScene_.shouldZoomOnResize(false);

    //trackScene_.installScrollHandler(hScrollBar_, vScrollBar_);
    //headerScene_.installScrollHandler(hScrollBar_, null);
  }

  public void reset(int headerHeight, int nTracks, double trackLength)
  {
    Component headerCanvas = headerWindow_.getCanvas();
    headerCanvas.setPreferredSize(new Dimension(1, headerHeight));
    headerCanvas.setMinimumSize(new Dimension(1, headerHeight));

    int windowWidth = trackWindow_.getWidth();
    int windowHeight = trackWindow_.getHeight();

    double pixelsPerMeter = 4000.0;

    double pixelHeight = pixelsPerMeter * trackLength;
    double worldHeight = trackLength * windowHeight / pixelHeight;

    double worldWidth = (double) windowWidth / (double) trackWidth;
    System.out.println(worldWidth);

    trackScene_.setWorldExtent(new double[] {0.0, trackLength, 0.0},
                               new double[] {worldWidth, trackLength, 0.0},
                               new double[] {0.0, 0.0, 0.0});

    /*
    trackScene_.zoom(new double[] {0.0, worldHeight, 0.0},
                     new double[] {worldWidth, worldHeight, 0.0},
                     new double[] {0.0, 0.0, 0.0});
    */

    trackScene_.add(new TestCurve());
  }

  private Component build()
  {
    GridBagConstraints c;

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());

    //
    // Header
    //
    Component headerCanvas = headerWindow_.getCanvas();
    c = new GridBagConstraints();
    c.fill     = GridBagConstraints.HORIZONTAL;
    c.anchor   = GridBagConstraints.NORTHWEST;
    c.gridx    = 0;
    c.gridy    = 0;
    c.weightx  = 1.0;
    c.weighty  = 0.0;
    c.insets   = new Insets(0, 0, 0, 0);
    ((GridBagLayout) mainPanel.getLayout()).setConstraints(headerCanvas, c);
    mainPanel.add(headerCanvas);

    //
    // Tracks
    //
    Component trackCanvas = trackWindow_.getCanvas();
    c = new GridBagConstraints();
    c.fill     = GridBagConstraints.BOTH;
    c.anchor   = GridBagConstraints.NORTHWEST;
    c.gridx    = 0;
    c.gridy    = 1;
    c.weightx  = 1.0;
    c.weighty  = 1.0;
    c.insets   = new Insets(0, 0, 0, 0);
    ((GridBagLayout) mainPanel.getLayout()).setConstraints(trackCanvas, c);
    mainPanel.add(trackCanvas);

    //
    // Vertical scrollbar
    //
    c = new GridBagConstraints();
    c.fill     = GridBagConstraints.VERTICAL;
    c.anchor   = GridBagConstraints.NORTHWEST;
    c.gridx    = 1;
    c.gridy    = 1;
    c.weightx  = 0.0;
    c.weighty  = 1.0;
    c.insets   = new Insets(0, 0, 0, 0);
    ((GridBagLayout) mainPanel.getLayout()).setConstraints(vScrollBar_, c);
    mainPanel.add(vScrollBar_);

    //
    // Horizontal scrollbar
    //
    c = new GridBagConstraints();
    c.fill     = GridBagConstraints.HORIZONTAL;
    c.anchor   = GridBagConstraints.NORTHWEST;
    c.gridx    = 0;
    c.gridy    = 2;
    c.weightx  = 1.0;
    c.weighty  = 0.0;
    c.insets   = new Insets(0, 0, 0, 0);
    ((GridBagLayout) mainPanel.getLayout()).setConstraints(hScrollBar_, c);
    mainPanel.add(hScrollBar_);

    //
    // Buttons
    //
    JPanel p = new JPanel();
    c = new GridBagConstraints();
    c.fill     = GridBagConstraints.HORIZONTAL;
    c.anchor   = GridBagConstraints.NORTHWEST;
    c.gridx    = 0;
    c.gridy    = 3;
    c.weightx  = 1.0;
    c.weighty  = 0.0;
    c.insets   = new Insets(0, 0, 0, 0);
    ((GridBagLayout) mainPanel.getLayout()).setConstraints(p, c);
    mainPanel.add(p);

    p.add(button1_);
    p.add(button2_);

    button1_.addActionListener(this);
    button2_.addActionListener(this);

    return mainPanel;
  }

  public void refresh()
  {
    trackWindow_.refresh();
  }

  public void actionPerformed(ActionEvent event)
  {
    Object source = event.getSource();

    if (source == button1_) {
      reset(100, 2, 1200.0);
      trackScene_.add(new TestCurve());
      trackWindow_.refresh();
    }

    if (source == button2_) {
    }
  }




  private class TestCurve extends GObject
  {
    private final GSegment segment_ = new GSegment();

    TestCurve()
    {
      GStyle style = new GStyle();
      style.setForegroundColor(Color.YELLOW);
      style.setFillPattern(GStyle.FILL_SOLID);

      setStyle(style);

      addSegment(segment_);
    }

    public void draw()
    {
      double[] x = {0.0, 0.1, 1.0, 0.0, 0.0};
      double[] y = {0.0, 0.0, 1200.0, 1200.0, 0.0};

      segment_.setGeometry(x, y);
    }
  }

  public static void main(String[] arguments)
  {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    Test t = new Test();
    f.add(t);

    f.pack();
    f.setSize(500, 500);

    f.setVisible(true);

    t.reset(100, 2, 1200.0);
    t.refresh();
  }
}
