package no.geosoft.common.geometry.spline;



/**
 * An abstract class defining a general spline object.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
abstract class Spline
{
  protected double controlPoints_[];
  protected int    nParts_;

  abstract double[] generate();
}
