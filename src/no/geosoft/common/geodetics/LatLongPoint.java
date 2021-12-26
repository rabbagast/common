package no.geosoft.common.geodetics;

/**
 * A class representing a geographical position.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class LatLongPoint
{
  /** Latitude (angle with equator) in radians */
  private final double latitude_;

  /** Longitude (angle with prime meridian) in radians */
  private final double longitude_;

  /** Point height */
  private final double height_;

  /**
   * Create a new latitude,longitude point.
   *
   * @param latitude   Latitude of point. Radians.
   * @param longitude  Longitude of point. Radians.
   * @param height     Height of point.
   */
  public LatLongPoint(double latitude, double longitude, double height)
  {
    latitude_ = latitude;
    longitude_ = longitude;
    height_ = height;
  }

  /**
   * Create a new latitude,longitude point with height = 0.0.
   *
   * @param latitude   Latitude of point. Radians.
   * @param longitude  Longitude of point. Radians.
   */
  public LatLongPoint(double latitude, double longitude)
  {
    this(latitude, longitude, 0.0);
  }

  /**
   * Create a latitude,longitude representation of the specified UTM
   * point of the specified ellipsoid.
   *
   * @param utmPoint   UTM point to convert. Non-null.
   * @param ellipsoid  Ellipsoid to use. Non-null.
   * @throws IllegalArgumentException  If utmPoint or ellipsoid is null.
   */
  public LatLongPoint(UtmPoint utmPoint, Ellipsoid ellipsoid)
  {
    if (utmPoint == null)
      throw new IllegalArgumentException("UTM point cannot be null");

    if (ellipsoid == null)
      throw new IllegalArgumentException("Ellipsoid cannot be null");

    double easting = utmPoint.getEasting();
    double northing = utmPoint.getNorthing();
    int zoneNumber = utmPoint.getZoneNumber();
    double radius = ellipsoid.getEquatorialRadius();
    double ee = ellipsoid.getEccentricitySquared();
    double k0 = 0.9996;
    double e1 = (1.0 - Math.sqrt(1.0 - ee)) / (1 + Math.sqrt(1.0 - ee));
    double x = easting - 500000.0; // remove 500,000 meter offset for longitude
    double y = utmPoint.isSouthernHemisphere() ? northing - 10000000.0 : northing;

    // Find central meridian of this zone
    double centralMeridian = UtmZone.getCentralMeridian(zoneNumber);

    double ePrimeSquared = ee / (1.0 - ee);
    double M = y / k0;
    double mu = M / (radius * (1 - ee / 4 -
                               3 * ee * ee / 64 -
                               5 * ee * ee * ee / 256));

    double phi = mu +
                 (3 * e1 / 2 -
                  27 * e1 * e1 * e1 / 32) * Math.sin (2 * mu) +
                 (21 * e1 * e1 / 16 -
                  55 * e1 * e1 * e1 * e1 /32) * Math.sin (4 * mu) +
                 (151 * e1 * e1 * e1 / 96) * Math.sin (6 * mu);

    double N1 = radius / Math.sqrt (1 - ee * Math.sin (phi) * Math.sin (phi));
    double T1 = Math.tan (phi) * Math.tan (phi);
    double C1 = ePrimeSquared * Math.cos (phi) * Math.cos (phi);
    double R1 = radius * (1.0 - ee) /
                Math.pow (1.0 - ee *
                          Math.sin (phi) * Math.sin (phi), 1.5);
    double D = x / (N1 * k0);

    latitude_ = phi - (N1 * Math.tan (phi) / R1) *
                (D * D / 2 -
                 (5 + 3 * T1 + 10 * C1 - 4 * C1 * C1 -
                  9 * ePrimeSquared) * D * D * D * D / 24.0 +
                 (61 + 90 * T1 + 298 * C1 + 45 * T1 * T1 -
                  252 * ePrimeSquared - 3 * C1 * C1) *
                 D * D * D * D * D * D / 720.0);

    longitude_ = (D - (1 + 2 * T1 + C1) * D * D * D /
                  6 + (5 - 2 * C1 + 28 * T1 - 3 * C1 * C1 +
                       8 * ePrimeSquared + 24 * T1 * T1) *
                  D * D * D * D * D / 120.0) / Math.cos (phi) +
                 centralMeridian;

    height_ = 0.0;
  }

  /**
   * Create a latitude,longitude representation of the specified UTM
   * point using standard ellipsoid (WGS 84).
   *
   * @param utmPoint UTM point to convert. Non-null.
   * @throws IllegalArgumentException  If utmPoint is null.
   */
  public LatLongPoint(UtmPoint utmPoint)
  {
    this(utmPoint, Ellipsoid.WGS_84);
  }

  /**
   * Cretae a latitude,longitude representation of the specified
   * ECEF point of the specified ellipsoid.
   *
   * @param ecefPoint  ECEF point to convert. Non-null.
   * @param ellipsoid  Ellipsoid. Non-null.
   * @throws IllegalArgumentException  If ecefPoint or ellipsoid is null.
   */
  public LatLongPoint(EcefPoint ecefPoint, Ellipsoid ellipsoid)
  {
    if (ecefPoint == null)
      throw new IllegalArgumentException("ECEF point cannot be null");

    if (ellipsoid == null)
      throw new IllegalArgumentException("Ellipsoid cannot be null");

    double x   = ecefPoint.getX();
    double y   = ecefPoint.getY();
    double z   = ecefPoint.getZ();

    double a   = ellipsoid.getEquatorialRadius();
    double b   = ellipsoid.getPolarRadius();
    double ee  = ellipsoid.getEccentricitySquared();
    double eeP = ee / (1.0 - ee);

    double p   = Math.sqrt(x*x + y*y);
    double phi = Math.atan((z / p) * (a / b));

    latitude_ = Math.atan((z + eeP * b * Math.sin (phi) *
                           Math.sin (phi) *
                           Math.sin (phi)) /
                          (p - ee * a * Math.cos (phi) *
                           Math.cos (phi) *
                           Math.cos (phi)));

    longitude_ = Math.atan2(y, x);

    double N = a / Math.sqrt(1 - ee * Math.sin(latitude_) *
                             Math.sin (latitude_));

    height_ = p / Math.cos(latitude_) - N;
  }

  /**
   * Cretae a latitude,longitude representation of the specified
   * ECEF point using standard ellipsoid (WGS 84).
   *
   * @param ecefPoint  ECEF point to convert. Non-null.
   * @throws IllegalArgumentException  If ecefPoint is null.
   */
  public LatLongPoint(EcefPoint ecefPoint)
  {
    this(ecefPoint, Ellipsoid.WGS_84);
  }

  /**
   * Convert this latitude,longitude point between datums.
   *
   * @param fromDatum  Datum of this latitude,longitude point. Non-null.
   * @param toDatum    Datum of destination point. Non-null.
   * @return           Converted latitude,longitude point. Never null.
   * @throws IllegalArgumentException  If fromDatum or toDatum is null.
   */
  public LatLongPoint convert(Datum fromDatum, Datum toDatum)
  {
    if (fromDatum == null)
      throw new IllegalArgumentException("From datum cannot be null");

    if (toDatum == null)
      throw new IllegalArgumentException("To datum cannot be null");

    // Convert to XYZ (fromDatum)
    EcefPoint p0 = new EcefPoint(this, fromDatum.getEllipsoid());

    // Convert to WGS-84
    double x = p0.getX() + fromDatum.getDx();
    double y = p0.getY() + fromDatum.getDy();
    double z = p0.getZ() + fromDatum.getDz();

    // Convert back to toDatum
    x -= toDatum.getDx();
    y -= toDatum.getDy();
    z -= toDatum.getDz();

    EcefPoint p1 = new EcefPoint(x, y, z);

    // Convert to lat long (toDatum)
    LatLongPoint toPosition = new LatLongPoint(p1, toDatum.getEllipsoid());

    return toPosition;
  }

  /**
   * Return latitude of this point.
   *
   * @return  Latitude of this point.
   */
  public double getLatitude()
  {
    return latitude_;
  }

  /**
   * Return longitude of this point.
   *
   * @return  Longitude of this point.
   */
  public double getLongitude()
  {
    return longitude_;
  }

  /**
   * Return height of this point.
   *
   * @return  Height of this point.
   */
  public double getHeight()
  {
    return height_;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return "Latitude=" + latitude_ + " Longitude=" + longitude_ + " Height=" + height_;
  }
}
