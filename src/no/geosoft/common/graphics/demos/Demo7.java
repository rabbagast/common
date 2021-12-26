package no.geosoft.common.graphics.demos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.GWindow;
import no.geosoft.common.graphics.GScene;
import no.geosoft.common.graphics.GObject;
import no.geosoft.common.graphics.GStyle;
import no.geosoft.common.graphics.GInteraction;
import no.geosoft.common.graphics.GText;
import no.geosoft.common.graphics.GPosition;
import no.geosoft.common.graphics.GSegment;

/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Annotation techniques
 * <li>Custom interaction
 * <li>Invisible lines with annotation
 * <li>Using transparent colors
 * <li>True scale resize
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Demo7 extends JFrame
  implements GInteraction
{
  private final GScene scene_;
  private final GObject rubberBand_;
  private GObject interaction_;
  private GSegment interactionSegment_;

  private int x0_, y0_;

  /**
   * Class for creating the demo canvas and hande Swing events.
   */
  public Demo7()
  {
    super("G Graphics Library - Demo 7");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create the GUI
    JPanel topLevel = new JPanel();
    topLevel.setLayout (new BorderLayout());
    getContentPane().add (topLevel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(new JLabel ("Move mouse to highlight"));
    topLevel.add(buttonPanel,   BorderLayout.NORTH);

    // Create the graphic canvas
    GWindow window = new GWindow (new Color (100, 100, 100));
    topLevel.add (window.getCanvas(), BorderLayout.CENTER);

    // Create scene with default viewport and world extent settings
    scene_ = new GScene (window, "Scene");

    double w0[] = {-1.0, -1.0, 0.0};
    double w1[] = {10.0, -1.0, 0.0};
    double w2[] = {-1.0, 10.0, 0.0};
    scene_.setWorldExtent (w0, w1, w2);
    scene_.shouldWorldExtentFitViewport (false);
    // scene_.shouldZoomOnResize(false);

    // Create som graphic objects
    GObject testObject = new TestObject (scene_);
    scene_.add (testObject);

    rubberBand_ = new GObject("Interaction");
    scene_.add(rubberBand_);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window.startInteraction (this);
  }

  public void event(GScene scene, int event, int x, int y)
  {
    switch (event) {
      case GWindow.MOTION :
        double[] w = scene_.getTransformer().deviceToWorld (x, y);

        if (w[0] < 0 || w[0] > 8 || w[1] < 0 || w[1] > 8)
          interaction_.removeSegment (interactionSegment_);
        else {
          interaction_.addSegment (interactionSegment_);
          interactionSegment_.setGeometry (Geometry.createCircle (x, y, 30));
        }

        scene_.refresh();
    }
  }

  /**
   * Defines the geometry and presentation for the sample
   * graphic object.
   */
  private final class TestObject extends GObject
  {
    private final GSegment[]  fields_;
    private final GSegment[]  labels_;

    /**
     * As a rule of thumb we create as much of the object during
     * construction as possible. Trye to do geometry only in the
     * draw method.
     */
    TestObject (GScene scene)
    {
      GStyle black = new GStyle();
      black.setFillPattern (GStyle.FILL_SOLID);
      black.setForegroundColor (new Color (0, 0, 0));

      GStyle white = new GStyle();
      white.setFillPattern (GStyle.FILL_SOLID);
      white.setForegroundColor (new Color (255, 255, 255));

      GStyle label = new GStyle();
      label.setFont (new Font ("Dialog", Font.PLAIN, 24));
      label.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      label.setForegroundColor (new Color (210, 210, 210));

      GStyle interaction = new GStyle();
      interaction.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      interaction.setBackgroundColor (new Color (1.0f, 0.0f, 0.0f, 0.7f));

      fields_ = new GSegment[64];
      labels_ = new GSegment[16];

      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          GSegment field = new GSegment();
          addSegment (field);
          field.setStyle ((i + j) % 2 != 0 ? black : white);
          fields_[i*8 + j] = field;
        }
      }

      for (int i = 0; i < 8; i++) {
        labels_[i] = new GSegment();
        labels_[i].setStyle (label);
        String text = "" + (i+1);
        labels_[i].addText (new GText (text, GPosition.TOP));
        labels_[i].addText (new GText (text, GPosition.BOTTOM));
        addSegment (labels_[i]);

        labels_[i+8] = new GSegment();
        labels_[i+8].setStyle (label);
        labels_[i+8].addText (new GText ((new Character ((char)('h' - i))).toString(),
                              GPosition.LEFT));
        labels_[i+8].addText (new GText ((new Character ((char)('h' - i))).toString(),
                              GPosition.RIGHT));
        addSegment (labels_[i+8]);
      }

      interaction_ = new GObject();
      interaction_.setStyle (interaction);
      interactionSegment_ = new GSegment();
      interaction_.addSegment (interactionSegment_);
      add (interaction_, front());
    }

    public void draw()
    {
      // Field geometry
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
          double[] x = new double[] {i, i+1, i+1, i};
          double[] y = new double[] {j, j,   j+1, j+1};

          fields_[i*8 + j].setGeometry (x, y);
        }
      }

      // Label line geometry
      for (int i = 0; i < 8; i++) {
        labels_[i].setGeometry (i+0.5, 0.0, i+0.5, 8.0);
        labels_[i+8].setGeometry (0.0, i+0.5, 8.0, i+0.5);
      }
    }
  }

  public static void main (String[] args)
  {
    new Demo7();
  }
}
