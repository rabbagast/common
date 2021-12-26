package no.geosoft.common.geometry;



// NOT FORMERLY CREATED
public class Disc
{
  /**
   * Create geometry for a disc with specified center and spacial angles.
   *
   * @param x            X of disc center.
   * @param y            Y of disc center.
   * @param z            Z of disc center.
   * @param inclination  Inclination of disc.
   * @param azimuth      North angle.
   * @param r            Radii of disc.
   * @param n            Number of points to generate.
   * @return             Geometry of disc [x,y,z,...]
   */
  public static double[] create (double x, double y, double z,
                                 double inclination, double azimuth,
                                 double r, int n)
  {
    int nTotal  = n * 3;        // X,Y and Z
    int i90     = nTotal / 4;
    int i180    = i90 * 2;
    int i270    = i90 * 3;

    double[] circle = new double[nTotal];

    int    nSteps   = n / 4;  // One quadrant
    double stepSize = Math.PI * 2 / n;
    double angle    = 0.0;

    int index = 0;
    for (int i = 0; i < nSteps; i++) {
      double cos = r * Math.cos (angle);
      double sin = r * Math.sin (angle);

      circle[index+0]      = x + cos;
      circle[index+1]      = y + sin;
      circle[index+2]      = z;

      circle[i90+index+0]  = x - sin;
      circle[i90+index+1]  = y + cos;
      circle[i90+index+2]  = z;

      circle[i180+index+0] = x - cos;
      circle[i180+index+1] = y - sin;
      circle[i180+index+2] = z;

      circle[i270+index+0] = x + sin;
      circle[i270+index+1] = y - cos;
      circle[i270+index+2] = z;

      angle += stepSize;
      index += 3;
    }

    // Rotation
    if (inclination != 0.0 || azimuth != 0.0) {
      Matrix4x4 transform = new Matrix4x4();
      transform.translate (-x, -y, -z);
      transform.rotateX (inclination);
      transform.rotateY (azimuth);
      transform.translate (+x, +y, +z);
      transform.transformPoints (circle);
    }

    return circle;
  }
}
