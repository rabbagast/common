package no.geosoft.common.geodetics;

/**
 * A class representing a UTM (Universal Transverse Mercator) point.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class UtmPoint
{
  /** Northing of this point. meter. */
  private final double northing_;

  /** Easting of this point. meter. */
  private final double easting_;

  /** Zone number of this point. */
  private final int zoneNumber_;

  /** Zone letter of this point, but might be invalid */
  private final char zoneLetter_;

  /** True if the zone letter is valid, false otherwise */
  private final boolean isZoneLetterValid_;

  /** True if the point is in the northern hemisphere, false if in the southern */
  private final boolean isNorthernHemisphere_;

  /**
   * Create a new UTM point.
   *
   * @param easting     Position easting.
   * @param northing    Position northing.
   * @param zoneNumber  Zone number.
   * @param zoneLetter  Zone letter.
   */
  public UtmPoint(double easting, double northing,
                  int zoneNumber, char zoneLetter)
  {
    easting_ = easting;
    northing_ = northing;
    zoneNumber_ = zoneNumber;
    zoneLetter_ = zoneLetter;
    isZoneLetterValid_ = true;
    isNorthernHemisphere_ = UtmZone.isNorthernHemisphere(zoneLetter);
  }

  /**
   * Create a new UTM point.
   *
   * @param easting               Position easting.
   * @param northing              Position northing.
   * @param zoneNumber            Zone number.
   * @param isNorthernHemisphere  True if northern hemisphere, false otherwise.
   */
  public UtmPoint(double easting, double northing,
                  int zoneNumber, boolean isNorthernHemisphere)
  {
    easting_ = easting;
    northing_ = northing;
    zoneNumber_ = zoneNumber;
    isZoneLetterValid_ = false;
    zoneLetter_ = 'X'; // Invalid
    isNorthernHemisphere_ = isNorthernHemisphere;
  }

  /**
   * Create an UTM representation of the specified latitude,longitude point
   * of the specified ellipsoid.
   *
   * @param latLongPoint  Latitude,longigtude point. Non-null.
   * @param ellipsoid     Ellipsoid. Non-null.
   * @throws IllegalArgumentException  If latLongPoint or ellipsoid is null.
   */
  public UtmPoint(LatLongPoint latLongPoint, Ellipsoid ellipsoid)
  {
    if (latLongPoint == null)
      throw new IllegalArgumentException("LatLong point cannot be null");

    if (ellipsoid == null)
      throw new IllegalArgumentException("Ellipsoid cannot be null");

    double latitude  = latLongPoint.getLatitude();
    double longitude = latLongPoint.getLongitude();
    double radius    = ellipsoid.getEquatorialRadius();
    double ee        = ellipsoid.getEccentricitySquared();
    double k0 = 0.9996;

    // Find UTM zone number of this position
    zoneNumber_ = UtmZone.get(latitude, longitude);

    // Find central meridian of this zone
    double centralMeridian = UtmZone.getCentralMeridian(zoneNumber_);

    double ePrimeSquared = ee / (1.0 - ee);

    double N = radius / Math.sqrt(1.0 - ee * Math.sin (latitude) * Math.sin (latitude));
    double T = Math.tan(latitude) * Math.tan (latitude);
    double C = ePrimeSquared * Math.cos (latitude) * Math.cos (latitude);
    double A = Math.cos (latitude) * (longitude - centralMeridian);

    double M = radius *
               ((1.0 -
                 1.0 * ee / 4.0 -
                 3.0 * ee * ee / 64.0 -
                 5.0 * ee * ee * ee / 256.0) * latitude -
                (3.0 * ee / 8.0 +
                 3.0 * ee * ee / 32.0 +
                 45.0 * ee * ee * ee / 1024.0) * Math.sin (2 * latitude) +
                (15.0 * ee * ee / 256.0 +
                 45.0 * ee * ee * ee / 1024.0) * Math.sin (4 * latitude) -
                (35.0 * ee * ee * ee / 3072.0) * Math.sin (6 * latitude));

    double east = k0 * N * (A + (1 - T + C) * A * A * A / 6.0 +
                            (5 -
                             18 * T + T * T +
                             72 * C -
                             58 * ePrimeSquared) *
                            A * A * A * A * A / 120.0) +
                  500000.0;

    double north = k0 * (M + N * Math.tan (latitude) *
                         (A * A / 2 + (5 - T + 9 * C + 4 * C * C) *
                          A * A * A * A / 24.0 +
                          (61 -
                           58 * T +
                           T * T +
                           600 * C -
                           330 * ePrimeSquared) *
                          A * A * A * A * A * A / 720.0));

    if (latitude < 0.0)
      north += 10000000.0;

    easting_ = east;
    northing_ = north;
    zoneLetter_ = UtmZone.getLetterDesignator(latitude);
    isZoneLetterValid_ = true;
    isNorthernHemisphere_ = UtmZone.isNorthernHemisphere(zoneLetter_);
  }

  /**
   * Create an UTM representation of the specified latitude,longitude point
   * using standard ellipsoid (WGS 84).
   *
   * @param latLongPoint  Latitude,longigtude point. Non-null.
   * @throws IllegalArgumentException  If latLongPoint is null.
   */
  public UtmPoint(LatLongPoint latLongPoint)
  {
    this(latLongPoint, Ellipsoid.WGS_84);
  }

  /**
   * Return northing of this UTM point.
   *
   * @return Northing of this UTM point.
   */
  public double getNorthing()
  {
    return northing_;
  }

  /**
   * Return easting of this UTM point.
   *
   * @return Easting of this UTM point.
   */
  public double getEasting()
  {
    return easting_;
  }

  /**
   * Return UTM zone number of this UTM point.
   *
   * @return UTM zone number of this UTM point.
   */
  public int getZoneNumber()
  {
    return zoneNumber_;
  }

  /**
   * Return UTM zone letter of this UTM point.
   *
   * @return UTM zone number of this UTM point.
   */
  public char getZoneLetter()
  {
    return zoneLetter_;
  }

  /**
   * Check if this UTM point is in the northern hemisphere.
   *
   * @return  True if this UTM point is in the northern hemisphere,
   *          false otherwise.
   */
  public boolean isNorthernHemisphere()
  {
    return isNorthernHemisphere_;
  }

  /**
   * Check if this UTM point is in the southern hemisphere.
   *
   * @return  True if this UTM point is in the southern hemisphere,
   *          false otherwise.
   */
  public boolean isSouthernHemisphere()
  {
    return !isNorthernHemisphere_;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return "N=" + northing_ + " E=" + easting_ + " " +
      zoneNumber_ + zoneLetter_ + " " + (isNorthernHemisphere_ ? "N" : "S");
  }
}
