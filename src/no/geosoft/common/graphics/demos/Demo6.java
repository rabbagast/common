package no.geosoft.common.graphics.demos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import no.geosoft.common.graphics.GInteraction;
import no.geosoft.common.graphics.GObject;
import no.geosoft.common.graphics.GScene;
import no.geosoft.common.graphics.GSegment;
import no.geosoft.common.graphics.GStyle;
import no.geosoft.common.graphics.GText;
import no.geosoft.common.graphics.GWindow;

/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>Custom selection interaction
 * <li>Object detection features
 * <li>Style inheritance and manipulation
 * <li>Dynamic style setting
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Demo6 extends JFrame
  implements GInteraction, ActionListener
{
  private final JButton typeButton_;
  private final GStyle selectionStyle_;
  private final GStyle textStyle_;
  private final GStyle selectedTextStyle_;
  private final GScene scene_;
  private final GObject rubberBand_;
  private final Collection<GSegment> selection_ = new ArrayList<GSegment>();

  private int x0_, y0_;

  /**
   * Class for creating the demo canvas and hande Swing events.
   */
  public Demo6()
  {
    super("G Graphics Library - Demo 6");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    selectionStyle_ = new GStyle();
    selectionStyle_.setForegroundColor(new Color (255, 255, 150));
    selectionStyle_.setLineWidth(2);

    textStyle_ = new GStyle();
    textStyle_.setForegroundColor(new Color (0, 0, 0));
    textStyle_.setFont(new Font("Dialog", Font.BOLD, 14));

    selectedTextStyle_ = new GStyle();
    selectedTextStyle_.setForegroundColor(new Color (255, 255, 255));
    selectedTextStyle_.setFont(new Font("Dialog", Font.BOLD, 14));

    // Create the GUI
    JPanel topLevel = new JPanel();
    topLevel.setLayout(new BorderLayout());
    getContentPane().add(topLevel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(new JLabel("Highlight lines "));

    typeButton_ = new JButton("inside");
    typeButton_.addActionListener(this);
    buttonPanel.add(typeButton_);

    buttonPanel.add(new JLabel(" rubberband"));
    topLevel.add(buttonPanel, BorderLayout.NORTH);

    // Create the graphic canvas
    GWindow window = new GWindow();
    topLevel.add(window.getCanvas(), BorderLayout.CENTER);

    // Create scene with default viewport and world extent settings
    scene_ = new GScene (window, "Scene");

    // Create som graphic objects
    GObject testObject = new TestObject(scene_, 40);
    scene_.add(testObject);

    rubberBand_ = new GObject("Interaction");
    GStyle rubberBandStyle = new GStyle();
    rubberBandStyle.setBackgroundColor(new Color(1.0f, 0.0f, 0.0f, 0.2f));
    rubberBand_.setStyle(rubberBandStyle);
    scene_.add(rubberBand_);

    pack();
    setSize (new Dimension(500, 500));
    setVisible(true);

    window.startInteraction(this);
  }

  public void actionPerformed(ActionEvent event)
  {
    String text = typeButton_.getText();
    if (text.equals ("inside")) typeButton_.setText ("intersecting");
    else                        typeButton_.setText ("inside");
  }

  public void event(GScene scene, int event, int x, int y)
  {
    switch (event) {
      case GWindow.BUTTON1_DOWN :
        x0_ = x;
        y0_ = y;
        rubberBand_.addSegment (new GSegment());
        break;

      case GWindow.BUTTON1_UP :
        rubberBand_.removeSegments();

        // Undo visual selection of current selection
        for (GSegment line : selection_) {
          GText text = line.getText();
          text.setStyle(textStyle_);
          line.setStyle(null);
        }

        scene_.refresh();
        break;

      case GWindow.BUTTON1_DRAG :
        int[] xRubber = new int[] {x0_, x, x, x0_, x0_};
        int[] yRubber = new int[] {y0_, y0_, y, y, y0_};

        GSegment rubberBand = rubberBand_.getSegment();
        rubberBand.setGeometry (xRubber, yRubber);

        // Undo visual selection of current selection
        for (GSegment line : selection_) {
          GText text = line.getText();
          text.setStyle (textStyle_);
          line.setStyle (null);
        }

        selection_.clear();
        if (typeButton_.getText().equals ("inside"))
          selection_.addAll(scene_.findSegmentsInside(Math.min (x0_, x),
                                                      Math.min (y0_, y),
                                                      Math.max (x0_, x),
                                                      Math.max (y0_, y)));
        else
          selection_.addAll(scene_.findSegments(Math.min (x0_, x),
                                                Math.min (y0_, y),
                                                Math.max (x0_, x),
                                                Math.max (y0_, y)));

        // Remove rubber band from selection
        selection_.remove(rubberBand);

        // Set visual appaerance of new selection
        for (GSegment line : selection_) {
          line.setStyle(selectionStyle_);
          GText text = line.getText();
          text.setStyle(selectedTextStyle_);
        }

        scene_.refresh();
        break;
    }
  }

  /**
   * Defines the geometry and presentation for a sample
   * graphic object.
   */
  private final class TestObject extends GObject
  {
    private final GSegment[] lines_;

    TestObject (GScene scene, int nLines)
    {
      lines_ = new GSegment[nLines];

      // Add style to object itself so it is inherited by segments
      GStyle lineStyle = new GStyle();
      lineStyle.setForegroundColor (new Color (100, 100, 100));
      setStyle (lineStyle);

      for (int i = 0; i < nLines; i++) {
        lines_[i] = new GSegment();
        addSegment(lines_[i]);

        GText text = new GText(Integer.toString (i));
        text.setStyle(textStyle_);
        lines_[i].setText(text);
      }
    }

    public void draw()
    {
      // Viewport dimensions
      int width  = (int) Math.round(getScene().getViewport().getWidth());
      int height = (int) Math.round(getScene().getViewport().getHeight());

      for (int i = 0; i < lines_.length; i++) {
        int x0 = (int) Math.round(width  * Math.random());
        int y0 = (int) Math.round(height * Math.random());
        int x1 = (int) Math.round(width  * Math.random());
        int y1 = (int) Math.round(height * Math.random());

        lines_[i].setGeometry(x0, y0, x1, y1);
      }
    }
  }

  public static void main (String[] arguments)
  {
    new Demo6();
  }
}
