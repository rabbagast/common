package no.geosoft.common.graphics;


import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JButton;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.GWindow;
import no.geosoft.common.graphics.GScene;
import no.geosoft.common.graphics.GSegment;
import no.geosoft.common.graphics.GStyle;
import no.geosoft.common.graphics.GObject;
import no.geosoft.common.graphics.GTransformer;
import no.geosoft.common.graphics.GInteraction;
import no.geosoft.common.graphics.ZoomInteraction;

public class TrackCanvas extends JPanel
  implements ActionListener, GInteraction
{
  private final GWindow headerWindow_ = new GWindow(new Color(100, 50, 50));

  private final GScene headerScene_ = new GScene(headerWindow_);

  private final GWindow trackWindow_ = new GWindow(new Color(150, 150, 200));

  private final GScene trackScene_ = new GScene(trackWindow_);

  private final JScrollBar hScrollBar_ = new JScrollBar(JScrollBar.HORIZONTAL);

  private final JScrollBar vScrollBar_ = new JScrollBar(JScrollBar.VERTICAL);

  private int nTracks_;

  private JButton   unzoomButton_;
  private JButton   zoomButton_;
  private JButton   moveButton_;
  private JButton   addTrackButton_;
  private JButton   removeTrackButton_;


  private GSegment  interactionSegment_;
  private int       x0_, y0_;

  public TrackCanvas()
  {
    build();
  }

  private void build()
  {
    GridBagConstraints c;

    setLayout(new GridBagLayout());

    //
    // Header canvas
    //
    Component headerCanvas = headerWindow_.getCanvas();
    c = new GridBagConstraints();
    c.fill      = GridBagConstraints.HORIZONTAL;
    c.anchor    = GridBagConstraints.NORTHWEST;
    c.gridx     = 0;
    c.gridy     = 0;
    c.weightx   = 1.0;
    c.weighty   = 0.0;
    c.insets    = new Insets(0, 0, 0, 0);
    ((GridBagLayout) getLayout()).setConstraints(headerCanvas, c);
    add(headerCanvas);

    //
    // Tracks canvas
    //
    Component trackCanvas = trackWindow_.getCanvas();
    c = new GridBagConstraints();
    c.fill      = GridBagConstraints.BOTH;
    c.anchor    = GridBagConstraints.NORTHWEST;
    c.gridx     = 0;
    c.gridy     = 1;
    c.weightx   = 1.0;
    c.weighty   = 0.0;
    c.insets    = new Insets(0, 0, 0, 0);
    ((GridBagLayout) getLayout()).setConstraints(trackCanvas, c);
    add(trackCanvas);

    //
    // Vertical scrollbar
    //
    c = new GridBagConstraints();
    c.fill      = GridBagConstraints.VERTICAL;
    c.anchor    = GridBagConstraints.NORTHWEST;
    c.gridx     = 1;
    c.gridy     = 1;
    c.weightx   = 0.0;
    c.weighty   = 1.0;
    c.insets    = new Insets(0, 0, 0, 0);
    ((GridBagLayout) getLayout()).setConstraints(vScrollBar_, c);
    add(vScrollBar_);

    //
    // Horizontal scrollbar
    //
    c = new GridBagConstraints();
    c.fill      = GridBagConstraints.HORIZONTAL;
    c.anchor    = GridBagConstraints.NORTHWEST;
    c.gridx     = 0;
    c.gridy     = 2;
    c.weightx   = 1.0;
    c.weighty   = 0.0;
    c.insets    = new Insets(0, 0, 0, 0);
    ((GridBagLayout) getLayout()).setConstraints(hScrollBar_, c);
    add(hScrollBar_);

    //headerScene_.installScrollHandler(hScrollBar_, null);
    trackScene_.installScrollHandler(hScrollBar_, vScrollBar_);

    //
    // TO DELETE
    //
    JPanel buttonPanel = new JPanel();

    unzoomButton_ = new JButton("Unzoom");
    unzoomButton_.addActionListener(this);
    buttonPanel.add(unzoomButton_);

    zoomButton_ = new JButton("Zoom");
    zoomButton_.addActionListener(this);
    buttonPanel.add(zoomButton_);

    moveButton_ = new JButton("Move");
    moveButton_.addActionListener(this);
    buttonPanel.add(moveButton_);

    addTrackButton_ = new JButton(" + ");
    addTrackButton_.addActionListener(this);
    buttonPanel.add(addTrackButton_);

    removeTrackButton_ = new JButton(" - ");
    removeTrackButton_.addActionListener(this);
    buttonPanel.add(removeTrackButton_);

    c = new GridBagConstraints();
    c.fill      = GridBagConstraints.HORIZONTAL;
    c.anchor    = GridBagConstraints.NORTHWEST;
    c.gridx     = 0;
    c.gridy     = 3;
    c.weightx   = 1.0;
    c.weighty   = 0.0;
    c.insets    = new Insets(0, 0, 0, 0);
    ((GridBagLayout) getLayout()).setConstraints(buttonPanel, c);
    add(buttonPanel);
  }

  public void reset()
  {
    trackScene_.unzoom();
  }

  public void setup(double trackLength, int headerHeight)
  {
    double[] w0 = new double[] {0.0,     trackLength, 0.0};
    double[] w1 = new double[] {nTracks_, trackLength, 0.0};
    double[] w2 = new double[] {0.0,     0.0,         0.0};
    trackScene_.setWorldExtent(w0, w1, w2);

    w0 = new double[] {0.0,     1.0, 0.0};
    w1 = new double[] {nTracks_, 1.0, 0.0};
    w2 = new double[] {0.0,     0.0, 0.0};
    headerScene_.setWorldExtent(w0, w1, w2);

    createTracks();
  }

  public void createTracks()
  {
    headerScene_.removeAll();
    trackScene_.removeAll();

    for (int i = 0; i < nTracks_; i++) {
      GObject object = new TestObject(i);
      trackScene_.add(object);
    }
  }

  public void actionPerformed (ActionEvent event)
  {
    Object source = event.getSource();

    if (source == zoomButton_) {
      trackWindow_.startInteraction(new ZoomInteraction(trackWindow_.getScene()));
    }

    if (source == moveButton_) {
      trackWindow_.startInteraction(this);
    }

    if (source == unzoomButton_) {
      trackScene_.unzoom();
    }

    if (source == addTrackButton_) {
      nTracks_++;
      //setup();
    }

    if (source == addTrackButton_) {
      nTracks_--;
      //setup();
    }
  }

  // Move interaction
  public void event(GScene scene, int event, int x, int y)
  {
    switch (event) {
      case GWindow.BUTTON1_DOWN :
        interactionSegment_ = scene.findSegment (x, y);
        x0_ = x;
        y0_ = y;
        break;

      case GWindow.BUTTON1_DRAG :
        int dx = x - x0_;
        int dy = y - y0_;
        if (interactionSegment_ != null) {
          TestObject testObject = (TestObject) interactionSegment_.getOwner();
          testObject.translate (interactionSegment_, dx, dy);
          scene.refresh();
        }
        x0_ = x;
        y0_ = y;
        break;

      case GWindow.BUTTON1_UP :
        interactionSegment_ = null;
        break;
    }
  }

  /**
   * Defines the geometry and presentation for a sample graphic object.
   */
  private class TestObject extends GObject
  {
    private GSegment[] stars_;
    private double[][] geometry_;

    TestObject(int no)
    {
      int nStars = 2000;

      stars_    = new GSegment[nStars];
      geometry_ = new double[nStars][];

      GStyle style = new GStyle();
      style.setForegroundColor(new Color ((float) Math.random(),
                                          (float) Math.random(),
                                          (float) Math.random()));

      style.setBackgroundColor(new Color ((float) Math.random(),
                                          (float) Math.random(),
                                          (float) Math.random()));
      style.setLineWidth (2);

      for (int i = 0; i < nStars; i++) {
        stars_[i] = new GSegment();
        stars_[i].setStyle(style);
        stars_[i].setUserData(new Integer(i));
        addSegment (stars_[i]);

        double[] xy = Geometry.createStar(Math.random() + no,
                                          Math.random() * 2000,
                                          0.05, 0.1, 20);
        geometry_[i] = xy;
      }
    }

    // Convert the world extent geometry of the specified
    // segment according to specified device translation
    public void translate (GSegment segment, int dx, int dy)
    {
      GTransformer transformer = getTransformer();
      double[] dw0 = transformer.deviceToWorld (0,  0);
      double[] dw1 = transformer.deviceToWorld (dx, dy);

      int index = ((Integer) segment.getUserData()).intValue();
      double[] geometry = geometry_[index];
      for (int i = 0; i < geometry.length; i += 2) {
        geometry[i + 0] += dw1[0] - dw0[0];
        geometry[i + 1] += dw1[1] - dw0[1];
      }

      segment.setGeometryXy (geometry);
    }


    public void draw()
    {
      for (int i = 0; i < stars_.length; i++)
        stars_[i].setGeometryXy(geometry_[i]);
    }
  }

  public static void main(String[] arguments)
  {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    TrackCanvas t = new TrackCanvas();
    t.setup(2000.0, 50);
    t.reset();

    f.add(t);

    f.pack();
    f.setSize(700, 500);
    f.setVisible(true);
  }
}

