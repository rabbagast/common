package no.geosoft.common.geodetics;

import java.util.Arrays;

/**
 * Spheric model of the earth given by:
 *
 * <pre>
 *   a = semi-major axis = equatorial radius = distance from center of
 *                                             the world to the equator
 *   b = semi-minor axis = polar radius      = distance from center of the
 *                                             earth to the north pole
 * </pre>
 *
 * Derived from these are:
 * <pre>
 *   f = flattening = (a - b) / a
 *
 *   e^2 = eccentricity squared = 2f - f^2 = a measure of the ellipsoid form
 *
 *        e^2 = (a^2 - b^2) / a^2
 * </pre>
 *
 * Given e^2, f can be computed as f = 1 - sqrt(1 - e^2)
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class Ellipsoid
{
  public static final Ellipsoid AIRY_1830 = new Ellipsoid("Airy 1830", "AA", 6377563.3960, 6356256.9090);
  public static final Ellipsoid AUSTRALIAN_NATIONAL = new Ellipsoid("Australian National", "AN", 6378160.0000, 6356774.7190);
  public static final Ellipsoid BESSEL_1841 = new Ellipsoid("Bessel 1841", "BR", 6377397.1550, 6356078.9630);
  public static final Ellipsoid BESSEL_1841_NAMIBIA = new Ellipsoid("Bessel 1841 (Namibia)", "BN", 6377483.8650, 6356165.3830);
  public static final Ellipsoid CLARKE_1866 = new Ellipsoid("Clarke 1866", "CC", 6378206.4000, 6356583.8000);
  public static final Ellipsoid CLARKE_1880 = new Ellipsoid("Clarke 1880", "CD", 6378249.1450, 6356514.8700);
  public static final Ellipsoid EVEREST = new Ellipsoid("Everest", "EA", 6377276.3450, 6356075.4130);
  public static final Ellipsoid EVEREST_MALAYSIA_BRUNEI = new Ellipsoid("Everest (E. Malaysia, Brunei)", "EB", 6377298.5560, 6356097.5500);
  public static final Ellipsoid EVEREST_1956 = new Ellipsoid("Everest 1956 (India)", "EC", 6377301.2430, 6356100.2280);
  public static final Ellipsoid EVEREST_1969 = new Ellipsoid("Everest 1969 (W. Malaysia)", "ED", 6377295.6640, 6356094.6680);
  public static final Ellipsoid EVEREST_1948 = new Ellipsoid("Everest 1948 (Malaysia & Singapore)", "EE", 6377304.0630, 6356103.0390);
  public static final Ellipsoid FISHER_1960_MERCURY = new Ellipsoid("Fisher 1960 Mercury", null, 6378166.0000, 6356784.2820);
  public static final Ellipsoid FISHER_1968 = new Ellipsoid("Fisher 1968", null, 6378150.0000, 6356768.3360);
  public static final Ellipsoid GRS_1967 = new Ellipsoid("GRS 1967", null, 6378160.0000, 6356774.5170);
  public static final Ellipsoid GRS_1980 = new Ellipsoid("GRS 1980", "RF", 6378137.0000, 6356752.3141);
  public static final Ellipsoid HELMERT_1906 = new Ellipsoid("Helmert 1906", "HE", 6378200.0000, 6356818.1700);
  public static final Ellipsoid HOUGH_1960 = new Ellipsoid("Hough 1960", "HO", 6378270.0000, 6356794.3430);
  public static final Ellipsoid INDONESIAN_1974 = new Ellipsoid("Indonesian 1974", "ID", 6378160.0000, 6356774.5040);
  public static final Ellipsoid INTERNATIONAL_1924 = new Ellipsoid("International 1924", "IN", 6378388.0000, 6356911.9460);
  public static final Ellipsoid KRASSOVSKY_1940 = new Ellipsoid("Krassovsky 1940", "KA", 6378245.0000, 6356863.0190);
  public static final Ellipsoid MODIFIED_AIRY = new Ellipsoid("Modified Airy", "AM", 6377340.1890, 6356034.4480);
  public static final Ellipsoid MODIFIED_EVEREST = new Ellipsoid("Modified Everest", null, 6377304.0000, 6356102.9750);
  public static final Ellipsoid MODIFIED_FISCHER_1960 = new Ellipsoid("Modified Fischer 1960", "FA", 6378155.0000, 6356773.3200);
  public static final Ellipsoid SOUTH_AMERICAN_1969 = new Ellipsoid("South American 1969", "SA", 6378160.0000, 6356774.7190);
  public static final Ellipsoid WGS_60 = new Ellipsoid("WSG 60", null, 6378165.0000, 6356783.286);
  public static final Ellipsoid WGS_66 = new Ellipsoid("WGS 66", null, 6378145.0000, 6356759.7690);
  public static final Ellipsoid WGS_72 = new Ellipsoid("WGS 72", "WD", 6378135.0000, 6356750.5200);
  public static final Ellipsoid WGS_84 = new Ellipsoid("WGS 84", "WE", 6378137.0000, 6356752.3142);

  /** Ellipsoid name. Non null */
  private final String name_;

  /** Ellipsoid mnemonic. May be null. */
  private final String mnemonic_;

  /** Equatorial radius */
  private final double equatorialRadius_;

  /** Polar radius */
  private final double polarRadius_;

  /** A collection of predefined ellipsoids */
  private static final Ellipsoid[] predefined_ =
  {
    AIRY_1830,
    AUSTRALIAN_NATIONAL,
    BESSEL_1841,
    BESSEL_1841_NAMIBIA,
    CLARKE_1866,
    CLARKE_1880,
    EVEREST,
    EVEREST_MALAYSIA_BRUNEI,
    EVEREST_1956,
    EVEREST_1969,
    EVEREST_1948,
    FISHER_1960_MERCURY,
    FISHER_1968,
    GRS_1967,
    GRS_1980,
    HELMERT_1906,
    HOUGH_1960,
    INDONESIAN_1974,
    INTERNATIONAL_1924,
    KRASSOVSKY_1940,
    MODIFIED_AIRY,
    MODIFIED_EVEREST,
    MODIFIED_FISCHER_1960,
    SOUTH_AMERICAN_1969,
    WGS_60,
    WGS_66,
    WGS_72,
    WGS_84
  };


  /**
   * Create a named ellipsoid, typically an earth model.
   *
   * @param name              Ellipsoid name. Non null.
   * @param mnemonic          Optional mnemonic.
   * @param equatorialRadius  Ellipsoid radius along equator. &lt;0,&gt;.
   * @param polarRadius       Ellipsoid radius along polar axis. &lt;0,&gt;.
   * @throws IllegalArgumentException  If name is null or equatorialRadius or
   *                          polarRadius is out of bounds.
   */
  public Ellipsoid(String name, String mnemonic,
                   double equatorialRadius, double polarRadius)
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    if (equatorialRadius <= 0.0)
      throw new IllegalArgumentException("Invalid radius: " + equatorialRadius);

    if (polarRadius <= 0.0)
      throw new IllegalArgumentException("Invalid radius: " + polarRadius);

    name_ = name;
    mnemonic_ = mnemonic;
    equatorialRadius_ = equatorialRadius;
    polarRadius_ = polarRadius;
  }

  /**
   * Return the name of this ellipsoid.
   *
   * @return  Name of this ellipsoid.
   */
  public String getName()
  {
    return name_;
  }

  /**
   * Return the mnemonic of this ellipsoid.
   *
   * @return Mnemonic of this ellipsoid.
   */
  public String getMnemonic()
  {
    return mnemonic_;
  }

  /**
   * Return the equatorial radius of this ellipsoid.
   *
   * @return  The equatorial radius of this ellipsoid.
   */
  public double getEquatorialRadius()
  {
    return equatorialRadius_;
  }

  /**
   * Return the polar radius of this ellipsoid.
   *
   * @return  The polar radius of this ellipsoid.
   */
  public double getPolarRadius()
  {
    return polarRadius_;
  }

  /**
   * Return the flattening this ellipsoid.
   *
   * @return  The flattening of this ellipsoid.
   */
  public double getFlattening()
  {
    double flattening = (equatorialRadius_ - polarRadius_) / equatorialRadius_;
    return flattening;
  }

  /**
   * Return the squared eccentricity of this ellipsoid.
   *
   * @return  The squared eccentricity of this ellipsoid.
   */
  public double getEccentricitySquared()
  {
    double flattening = getFlattening();
    double eccentricitySquared = 2 * flattening - flattening * flattening;
    return eccentricitySquared;
  }

  /**
   * Return the eccentricity of this ellipsoid.
   *
   * @return  The eccentricity of this ellipsoid.
   */
  public double getEccentricity()
  {
    double eccentricitySquared = getEccentricitySquared();
    double eccentricity = Math.sqrt(eccentricitySquared);
    return eccentricity;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return name_ + " " + mnemonic_ + " (" + equatorialRadius_ + "," + polarRadius_ + ")";
  }

  /**
   * Return the a named ellipsoid among the predefined ones.
   *
   * @param name  Name of ellipsoid to return. Non-null.
   * @return      Requested ellipsoid (or null if not found).
   * @throws IllegalArgumentException  If name is null.
   */
  public static Ellipsoid get(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("Ellipsoid name cannot be null");

    for (Ellipsoid ellipsoid : predefined_) {
      if (ellipsoid.getName().equals(name))
        return ellipsoid;
    }

    return null;
  }

  /**
   * Return all predefined ellipsoids.
   *
   * @return All predefined ellipsoids. Never null.
   */
  public static Ellipsoid[] getAll()
  {
    return Arrays.copyOf(predefined_, predefined_.length);
  }
}
