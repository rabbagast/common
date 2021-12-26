package no.geosoft.common.graphics.demos;

import java.awt.*;

import javax.swing.*;

import no.geosoft.common.graphics.*;



/**
 * G demo program. Demonstrates:
 *
 * <ul>
 * <li>A sample game application
 * <li>Graphics animation
 * <li>GObject reparenting
 * </ul>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Demo14 extends JFrame
{
  private TowersOfHanoi  towersOfHanoi_;
  private GWindow        window_;
  private Peg[]          pegs_;
  private int            nDiscs_;
  private JButton        startButton_;



  public Demo14 (int nDiscs)
  {
    super ("G Graphics Library - Demo 14");
    setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

    nDiscs_ = nDiscs;

    // Create the graphic canvas
    window_ = new GWindow (new Color (200, 230, 200));
    getContentPane().add (window_.getCanvas());

    // Create scene
    GScene scene = new GScene (window_);
    double w0[] = {0.0,  0.0, 0.0};
    double w1[] = {4.0,  0.0, 0.0};
    double w2[] = {0.0,  nDiscs_ * 2, 0.0};
    scene.setWorldExtent (w0, w1, w2);

    // Add title object and add to scene
    scene.add (new Title());

    // Create the 3 pegs and add to the scene
    int nPegs = 3;
    pegs_ = new Peg[nPegs];
    for (int i = 0; i < nPegs; i++) {
      pegs_[i] = new Peg (i + 1.0);
      scene.add (pegs_[i]);
    }

    // Create the discs and add to the first peg
    for (int i = 0; i < nDiscs; i++) {
      Disc disc = new Disc ((double) (nDiscs - i) / nDiscs);
      disc.setPosition (1.0, i);
      pegs_[0].add (disc);
    }

    pack();
    setSize (new Dimension (500, 500));
    setVisible (true);

    // Create the puzzle and execute the solution
    towersOfHanoi_ = new TowersOfHanoi();
    towersOfHanoi_.solve();
  }

  public void discMoved (int source, int destination)
  {
    // This is the disc to move
    Disc disc = (Disc) pegs_[source].getChild (pegs_[source].getNChildren()-1);

    double y0 = disc.getY();
    double y1 = nDiscs_ + 4.0;

    double x0 = pegs_[source].getX();
    double x1 = pegs_[destination].getX();

    // Animate vertical up movement
    double step = 0.2;
    double y = y0;
    while (y < y1) {
      disc.setPosition(x0, y);
      disc.redraw();
      window_.refresh();
      y += step;
    }

    // Reparent peg
    pegs_[source].remove (disc);
    pegs_[destination].add (disc);

    // Animate horizontal movement
    step = 0.05;
    double x = x0;
    while (Math.abs(x - x1) > 0.001) {
      disc.setPosition (x, y);
      disc.redraw();
      window_.refresh();
      x += (x1 > x0 ? step : -step);
      if (Math.abs (x - x1) < 0.01) x = x1;
    }

    // Animate vertical down movement
    step = 0.2;
    y  = y1;
    y1 = pegs_[destination].getNChildren() - 1;
    while (y > y1) {
      if (Math.abs (y - y1) < 0.01) y = y1;
      disc.setPosition (x, y);
      disc.redraw();
      window_.refresh();
      y -= step;
    }
  }



  /**
   * Graphics object for canvas title.
   */
  class Title extends GObject
  {
    private GSegment  anchor_;

    public Title()
    {
      GStyle style = new GStyle();
      style.setLineStyle (GStyle.LINESTYLE_INVISIBLE);
      style.setForegroundColor (new Color (100, 100, 200));
      style.setFont (new Font ("serif", Font.PLAIN, 36));
      setStyle (style);

      anchor_ = new GSegment();
      addSegment (anchor_);

      GText text = new GText ("Towers of Hanoi", GPosition.SOUTHEAST);
      anchor_.setText (text);
    }


    public void draw()
    {
      anchor_.setGeometry (20, 20);
    }
  }



  /**
   * Graphics representation of a peg.
   */
  class Peg extends GObject
  {
    private double    x_;
    private GSegment  peg_;
    private double[]  xy_;


    public Peg (double x)
    {
      x_ = x;

      GStyle style = new GStyle();
      style.setBackgroundColor (new Color (100, 100, 100));
      setStyle (style);

      peg_ = new GSegment();
      addSegment (peg_);

      xy_ = new double[] {x_ - 0.05, 0.0,
                          x_ - 0.05, nDiscs_ + 2,
                          x_ + 0.05, nDiscs_ + 2,
                          x_ + 0.05, 0.0,
                          x_ - 0.05, 0.0};
    }


    public double getX()
    {
      return x_;
    }


    public void draw()
    {
      peg_.setGeometryXy (xy_);
    }
  }



  /**
   * Graphics representation of a disc.
   */
  class Disc extends GObject
  {
    private double    size_;
    private GSegment  disc_;
    private double    x_, y_;


    public Disc (double size)
    {
      size_ = size;

      GStyle style = new GStyle();
      style.setForegroundColor (new Color (255, 0, 0));
      style.setBackgroundColor (new Color (255, 150, 150));
      setStyle (style);

      disc_ = new GSegment();
      addSegment (disc_);
    }


    public void setPosition (double x, double y)
    {
      x_ = x;
      y_ = y;
    }


    public double getY()
    {
      return y_;
    }


    public void draw()
    {
      double[] xy = new double[] {x_ - size_ / 2.0, y_,
                                  x_ - size_ / 2.0, y_ + 1.0,
                                  x_ + size_ / 2.0, y_ + 1.0,
                                  x_ + size_ / 2.0, y_,
                                  x_ - size_ / 2.0, y_};

      disc_.setGeometryXy (xy);
    }
  }




  /**
   * Class for solving the "Towers of Hanoi" puzzle.
   */
  class TowersOfHanoi
  {
    public void solve()
    {
      solve (nDiscs_, 0, 2, 1);
    }


    private void solve (int nDiscs, int source, int destination, int auxiliary)
    {
      if (nDiscs == 1)
        discMoved (source, destination);

      else if (nDiscs > 1) {
        solve (nDiscs - 1, source, auxiliary, destination);
        discMoved (source, destination);
        solve (nDiscs - 1, auxiliary, destination, source);
      }
    }
  }

  public static void main (String[] arguments)
  {
    int nDiscs = 8;
    new Demo14(nDiscs);
  }
}
