package no.geosoft.common.geometry;

/**
 * Collection of circle related functions.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Circle
{
  /**
   * Find the size of the curvature described by the specified curvature.
   *
   * @param curvature  rad/m
   * @return  Radius of curvature.
   */
  public static double getRadiusOfCurvature (double curvature)
  {
    return 1.0 / curvature;
  }

  public static double getCurvatureOfRadius (double r)
  {
    return 1.0 / r;
  }
}
