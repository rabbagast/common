package no.geosoft.common.graphics.demos;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import no.geosoft.common.geometry.Geometry;
import no.geosoft.common.graphics.*;



/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li> An actual game application
 * <li> Embedded AWT components
 * <li> Custom selection interaction
 * <li> Dynamic annotations
 * <li> Geomtry generation
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo19 extends JFrame
{
  public Demo19()
  {
    super ("G Graphics Library - Demo 19");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    // Create the graphic canvas
    GWindow window = new GWindow (new Color (170, 180, 190));
    getContentPane().add (window.getCanvas(), BorderLayout.CENTER);

    // Create scene with default viewport and world extent settings
    GScene scene = new GScene (window);

    // Create the graphics object and add to the scene
    Simon  simon = new Simon();
    GSimon gSimon = new GSimon (simon);
    scene.add (gSimon);

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    window.startInteraction (gSimon);
  }



  /**
   * Defines the geometry and presentation for the sample
   * graphic object.
   */
  private class GSimon extends GObject
    implements ActionListener, GInteraction
  {
    private Simon       simon_;
    private GSegment    background_;
    private GSegment[]  sector_;
    private GSegment    title_;
    private GSegment    score_;
    private GSegment    gameOver_;


    GSimon (Simon simon)
    {
      simon_ = simon;

      background_ = new GSegment();
      GStyle backgroundStyle = new GStyle();
      backgroundStyle.setForegroundColor (new Color (0, 0, 0));
      backgroundStyle.setBackgroundColor (new Color (0, 0, 0));
      background_.setStyle (backgroundStyle);
      addSegment (background_);

      sector_ = new GSegment[4];

      sector_[0] = new GSegment();
      GStyle redStyle = new GStyle();
      redStyle.setForegroundColor (new Color (200, 0, 0));
      redStyle.setBackgroundColor (new Color (200, 0, 0));
      sector_[0].setStyle (redStyle);
      addSegment (sector_[0]);

      sector_[1] = new GSegment();
      GStyle greenStyle = new GStyle();
      greenStyle.setForegroundColor (new Color (0, 200, 0));
      greenStyle.setBackgroundColor (new Color (0, 200, 0));
      sector_[1].setStyle (greenStyle);
      addSegment (sector_[1]);

      sector_[2] = new GSegment();
      GStyle yellowStyle = new GStyle();
      yellowStyle.setForegroundColor (new Color (200, 200, 0));
      yellowStyle.setBackgroundColor (new Color (200, 200, 0));
      sector_[2].setStyle (yellowStyle);
      addSegment (sector_[2]);

      sector_[3] = new GSegment();
      GStyle blueStyle = new GStyle();
      blueStyle.setForegroundColor (new Color (0, 0, 200));
      blueStyle.setBackgroundColor (new Color (0, 0, 200));
      sector_[3].setStyle (blueStyle);
      addSegment (sector_[3]);

      title_ = new GSegment();
      GStyle titleStyle = new GStyle();
      titleStyle.setForegroundColor (new Color (255, 255, 255));
      titleStyle.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      titleStyle.setFont (new Font ("Dialog", Font.BOLD, 36));
      title_.setStyle (titleStyle);
      title_.setText (new GText ("SIMON", GPosition.NORTH));
      JButton startButton = new JButton ("Start");
      startButton.addActionListener (this);
      title_.setComponent (new GComponent (startButton, GPosition.SOUTH));
      addSegment (title_);

      score_ = new GSegment();
      GStyle scoreStyle = new GStyle();
      scoreStyle.setForegroundColor (new Color (255, 255, 255));
      scoreStyle.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      scoreStyle.setFont (new Font ("Dialog", Font.BOLD, 48));
      score_.setStyle (scoreStyle);
      score_.setText (new GText ("", GPosition.MIDDLE));
      addSegment (score_);

      gameOver_ = new GSegment();
      GStyle gameOverStyle = new GStyle();
      gameOverStyle.setForegroundColor (new Color (1.0f, 0.0f, 0.0f, 0.8f));
      gameOverStyle.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      gameOverStyle.setFont (new Font ("Dialog", Font.BOLD, 18));
      gameOver_.setStyle (gameOverStyle);
      gameOver_.setText (new GText ("", GPosition.MIDDLE));
      addSegment (gameOver_);
    }


    public void actionPerformed (ActionEvent event)
    {
      gameOver_.getText().setText ("");
      score_.getText().setText ("");
      refresh();

      simon_.newGame();
      play (simon_.getSequence());
      getWindow().startInteraction (this);
    }


    public void event (GScene scene, int event, int x, int y)
    {
      if (event == GWindow.BUTTON1_UP) {
        System.out.println (Thread.currentThread());

        GSegment segment = findSegment (x, y);
        int guess = -1;
        for (int i = 0; i < 4; i++)
          if (segment == sector_[i]) guess = i;

        if (guess == -1) return;

        boolean isCorrect = simon_.guess (guess);
        if (isCorrect) {
          highlight (guess, 500);
          if (simon_.isDone()) {
            GText scoreText = score_.getText();
            scoreText.setText (Integer.toString (simon_.getSequence().length - 1));
            refresh();

            // Pause
            try {
              Thread.sleep(500);
            }
            catch (Exception e) {
            }

            play(simon_.getSequence());
          }
        }
        else {
          gameOver_.getText().setText ("GAME OVER");
          refresh();
          getWindow().stopInteraction();
        }
      }
    }

    private void highlight(int sectorNo, int nMillis)
    {
      GSegment sector = sector_[sectorNo];
      GStyle style = sector.getStyle();
      Color color = style.getBackgroundColor();
      Color highlight = color.brighter();
      style.setForegroundColor(highlight);
      style.setBackgroundColor(highlight);
      refresh();

      // Pause
      try {
        Thread.sleep (500);
      }
      catch (Exception e) {
      }

      style.setForegroundColor(color);
      style.setBackgroundColor(color);
      refresh();
    }

    private void play(int[] code)
    {
      for (int i = 0; i < code.length; i++)
        highlight (code[i], 500);
    }

    private int[] createZone (int x0, int y0, int r0, int r1,
                              double angle0, double angle1)
    {
      int[] inner = Geometry.createSector (x0, y0, r0, angle0, angle1);
      int[] outer = Geometry.createSector (x0, y0, r1, angle0, angle1);

      int n = outer.length + inner.length - 6;

      int[] zone = new int[n];
      int i = 0;
      for (int j = 0; j < inner.length - 4; j++)
        zone[i++] = inner[j];

      for (int j = outer.length - 6; j >= 0; j-=2) {
        zone[i++] = outer[j+0];
        zone[i++] = outer[j+1];
      }

      zone[i++] = zone[0];
      zone[i++] = zone[1];

      return zone;
    }


    public void draw()
    {
      background_.setGeometry (Geometry.createCircle (250, 250, 215));

      sector_[0].setGeometry (createZone (250, 250, 80, 190, 0.0, Math.PI / 2.0));
      sector_[0].translate (10, -10);

      sector_[1].setGeometry (createZone (250, 250, 80, 190, Math.PI / 2.0, Math.PI));
      sector_[1].translate (-10, -10);

      sector_[3].setGeometry (createZone (250, 250, 80, 190, Math.PI, 3.0 * Math.PI / 2.0));
      sector_[3].translate (-10, 10);

      sector_[2].setGeometry (createZone (250, 250, 80, 190, 3.0 * Math.PI / 2.0, Math.PI * 2.0));
      sector_[2].translate (10, 10);

      title_.setGeometry (250, 240);
      score_.setGeometry (250, 300);
      gameOver_.setGeometry (250, 300);
    }
  }


  private class Simon
  {
    private int[]    sequence_;
    private int      current_;
    private boolean  isDone_;


    int[] getSequence()
    {
      return sequence_;
    }


    void newGame()
    {
      sequence_ = new int[0];
      add();
      isDone_ = false;
    }


    private void add()
    {
      int[] newSequence = new int[sequence_.length + 1];
      System.arraycopy (sequence_, 0, newSequence, 0, sequence_.length);
      sequence_ = newSequence;
      sequence_[sequence_.length - 1] = (int) Math.round (Math.random() * 3);
      current_ = 0;
    }


    boolean guess (int guess)
    {
      boolean isCorrect = sequence_[current_++] == guess;
      isDone_ = current_ == sequence_.length;
      if (isCorrect && isDone_) add();
      return isCorrect;
    }


    boolean isDone()
    {
      return isDone_;
    }
  }



  public static void main (String[] args)
  {
    new Demo19();
  }
}
