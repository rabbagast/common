package no.geosoft.common.geodetics;

/**
 * A class representing a geographical position through an ECEF
 * (Earth Centered, Earth Fixed) point.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class EcefPoint
{
  /** X coordinate. */
  private final double x_;

  /** Y coordinate. */
  private final double y_;

  /** Z coordinate. */
  private final double z_;

  /**
   * Create a new ECEF point.
   *
   * @param x  X coordinate.
   * @param y  Y coordinate.
   * @param z  Z coordinate.
   */
  public EcefPoint(double x, double y, double z)
  {
    x_ = x;
    y_ = y;
    z_ = z;
  }

  /**
   * Create a ECEF point from a latitude,longitude position and a
   * specified ellipsoid.
   *
   * @param latLongPoint  Position to convert. Non-null.
   * @param ellipsoid     Ellipsoid. Non-null.
   * @throws IllegalArgumentException  If latLogPoint or ellipsoid is null.
   */
  public EcefPoint(LatLongPoint latLongPoint, Ellipsoid ellipsoid)
  {
    if (latLongPoint == null)
      throw new IllegalArgumentException("Position cannot be null");

    if (ellipsoid == null)
      throw new IllegalArgumentException("Ellipsoid cannot be null");

    double latitude  = latLongPoint.getLatitude();
    double longitude = latLongPoint.getLongitude();
    double height    = latLongPoint.getHeight();

    double a  = ellipsoid.getEquatorialRadius();
    double ee = ellipsoid.getEccentricitySquared();
    double N  = a / Math.sqrt (1 - ee * Math.sin (latitude) * Math.sin (latitude));

    x_ = (N + height) * Math.cos (latitude) * Math.cos (longitude);
    y_ = (N + height) * Math.cos (latitude) * Math.sin (longitude);
    z_ = (N * (1 - ee) + height) * Math.sin (latitude);
  }

  /**
   * Create a ECEF point from a latitude,longitude position using the
   * standard ellipsoid model (WGS 84).
   *
   * @param latLongPoint  Position to convert. Non.null.
   * @throws IllegalArgumentException  If latLogPoint is null.
   */
  public EcefPoint(LatLongPoint latLongPoint)
  {
    this(latLongPoint, Ellipsoid.WGS_84);
  }

  /**
   * Return X coordinate of this ECEF point.
   *
   * @return  X coordinate of this ECEF point.
   */
  public double getX()
  {
    return x_;
  }

  /**
   * Return Y coordinate of this ECEF point.
   *
   * @return  Y coordinate of this ECEF point.
   */
  public double getY()
  {
    return y_;
  }

  /**
   * Return Z coordinate of this ECEF point.
   *
   * @return  Z coordinate of this ECEF point.
   */
  public double getZ()
  {
    return z_;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return x_ + "," + y_ + "," + z_;
  }
}
