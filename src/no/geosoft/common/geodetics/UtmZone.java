package no.geosoft.common.geodetics;

/**
 * Static utility methods on UTM zones.
 * <p>
 * References:
 * <pre>
 *    http://www.ncgia.ucsb.edu/giscc/units/u013/u013.html
 *    http://shookweb.jpl.nasa.gov/Validation/UTM/
 * </pre>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class UtmZone
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private UtmZone()
  {
    assert false : "this constructor should never be called";
  }

  /**
   * Return the UTM zone of a given latitude,longitude coordinate.
   *
   * @param latitude   Latitude in radians.
   * @param longitude  Longitude in radians.
   * @return           UTM zone.
   */
  public static int get(double latitude, double longitude)
  {
    // Convert latitude,longitude to degrees
    latitude  *= (180.0 / Math.PI);
    longitude *= (180.0 / Math.PI);

    // Basic case
    int zoneNumber = (int)((longitude + 180.0) / 6.0) + 1;

    // North sea zone 32 partly covers zone 31
    if (latitude >= 56.0 && latitude < 64.0 &&
        longitude >= 3.0 && longitude < 12.0)
      zoneNumber = 32;

    // The Barents sea zones are special too
    if (latitude >= 72.0 && latitude < 84.0) {
      if (longitude >=  0.0 && longitude <  9.0)
        zoneNumber = 31;
      else if (longitude >=  9.0 && longitude < 21.0)
        zoneNumber = 33;
      else if (longitude >= 21.0 && longitude < 33.0)
        zoneNumber = 35;
      else if (longitude >= 33.0 && longitude < 42.0)
        zoneNumber = 37;
    }

    return zoneNumber;
  }

  /**
   * Return the central meridian of a zone in radians.
   *
   * @param zoneNumber  Zone number to get central meridian of.
   * @return            Central meridian of specified zone.
   */
  public static double getCentralMeridian(int zoneNumber)
  {
    // TODO: Validate zoneNumber

    double centralMeridian = (zoneNumber - 1) * 6.0 - 180.0 + 3;
    return centralMeridian * Math.PI / 180.0;
  }

  /**
   * Return UTM zone letter designator for given latitude.
   *
   * @param latitude  Latitude in radians.
   * @return          UTM zone letter designator for given latitude.
   */
  public static char getLetterDesignator(double latitude)
  {
    // TODO: Check this.
    latitude *= (180.0 / Math.PI);

    char letterDesignator;

    if      (latitude < -72) letterDesignator = 'C';
    else if (latitude < -64) letterDesignator = 'D';
    else if (latitude < -56) letterDesignator = 'E';
    else if (latitude < -48) letterDesignator = 'F';
    else if (latitude < -40) letterDesignator = 'G';
    else if (latitude < -32) letterDesignator = 'H';
    else if (latitude < -24) letterDesignator = 'J';
    else if (latitude < -16) letterDesignator = 'K';
    else if (latitude <  -8) letterDesignator = 'L';
    else if (latitude <   0) letterDesignator = 'M';
    else if (latitude <   8) letterDesignator = 'N';
    else if (latitude <  16) letterDesignator = 'P';
    else if (latitude <  24) letterDesignator = 'Q';
    else if (latitude <  32) letterDesignator = 'R';
    else if (latitude <  40) letterDesignator = 'S';
    else if (latitude <  48) letterDesignator = 'T';
    else if (latitude <  56) letterDesignator = 'U';
    else if (latitude <  64) letterDesignator = 'V';
    else if (latitude <  72) letterDesignator = 'W';
    else                     letterDesignator = 'X';

    return letterDesignator;
  }

  /**
   * Check if a specified zone letter represents a zone in the
   * northern hemisphere.
   *
   * @param zoneLetter  Letter of zone to check.
   * @return            True if the zone letter represents a zone
   *                    in the northern hemisphere, false otherwise.
   */
  public static boolean isNorthernHemisphere(char zoneLetter)
  {
    // TODO: Validate zone letter

    return zoneLetter >= 'N';
  }

  /**
   * Check if a specified zone letter represents a zone in the
   * southern hemisphere.
   *
   * @param zoneLetter  Letter of zone to check.
   * @return            True if the zone letter represents a zone
   *                    in the southern hemisphere, false otherwise.
   */
  public static boolean isSouthernHemisphere(char zoneLetter)
  {
    // TODO: Validate zone letter

    return !UtmZone.isNorthernHemisphere (zoneLetter);
  }
}
