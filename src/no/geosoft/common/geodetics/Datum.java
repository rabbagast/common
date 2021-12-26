package no.geosoft.common.geodetics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * See http://www.colorado.edu/geography/gcraft/notes/datum/datum_f.html
 * and http://www.colorado.edu/geography/gcraft/notes/datum/edlist.html
 * <p>
 * Predefined datums are located in datum.txt and made available through
 * this class.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Datum
{
  /** Datum name. Non-null. */
  private final String name_;

  /** Ellipsoid associated with this datum. Non-null. */
  private final Ellipsoid ellipsoid_;

  /** Region where the datum is used. Null if unknown or N/A. */
  private final String region_;

  /** Location relative to the WGS 84 datum. */
  private final double dx_, dy_, dz_;

  /** Error estimates. TODO: Figure out this. */
  private final double errorX_, errorY_, errorZ_;

  /** Number of satellites. TODO: Figure out this. */
  private final int nSatellites_;

  /** An array of predefined datums. */
  private static final Datum[] predefined_;

  static {
    predefined_ = Datum.loadDatums();
  }

  /**
   * Create a new datum.
   *
   * @param name        Name of this datum. Non-null.
   * @param ellipsoid   Associated ellipsoid. Non-null.
   * @param region      Region where the datum is used. Make be null.
   * @param dx          X position relative to WGS 84.
   * @param dy          Y position relative to WGS 84.
   * @param dz          Z position relative to WGS 84.
   * @param errorX      Error measure in X direction.
   * @param errorY      Error measure in Y direction.
   * @param errorZ      Error measure in Z direction.
   * @param nSatellites Not sure. [0,&gt;.
   * @throws IllegalArgumentException  If name or ellipsoiod is null, or
   *                    if nSatellites is out of bounds.
   */
  public Datum(String name, Ellipsoid ellipsoid,
               String region,
               double dx, double dy, double dz,
               double errorX, double errorY, double errorZ,
               int nSatellites)
  {
    if (name == null)
      throw new IllegalArgumentException("Datum name cannot be null");

    if (ellipsoid == null)
      throw new IllegalArgumentException("Ellipsoid cannot be null");

    if (nSatellites < 0)
      throw new IllegalArgumentException("Invalid nSatellites: " + nSatellites);

    ellipsoid_ = ellipsoid;
    name_ = name;
    region_ = region;
    dx_ = dx;
    dy_ = dy;
    dz_ = dz;
    errorX_ = errorX;
    errorY_ = errorY;
    errorZ_ = errorZ;
    nSatellites_ = nSatellites;
  }

  /**
   * Return the name of this datum.
   *
   * @return Name of this datum.
   */
  public String getName()
  {
    return name_;
  }

  /**
   * Return associated ellipsoid of this datum.
   *
   * @return  Associated ellipsoid of this datum. Never null.
   */
  public Ellipsoid getEllipsoid()
  {
    return ellipsoid_;
  }

  /**
   * Return region of this datum.
   *
   * @return  Region of this datum. Null if unknown or N/A.
   */
  public String getRegion()
  {
    return region_;
  }

  /**
   * Return X position relative to the WGS 84.
   *
   * @return  X position relative to the WGS 84.
   */
  public double getDx()
  {
    return dx_;
  }

  /**
   * Return Y position relative to the WGS 84.
   *
   * @return  Y position relative to the WGS 84.
   */
  public double getDy()
  {
    return dy_;
  }

  /**
   * Return Z position relative to the WGS 84.
   *
   * @return  Z position relative to the WGS 84.
   */
  public double getDz()
  {
    return dz_;
  }

  /**
   * Return a predefined datum of the specified name.
   * Note that datum names are typcailly not unique, but that
   * datums with the same name share the same ellipsoid.
   *
   * @param name  Name of datum to return. Non-null.
   * @return      First found datum of the specified name, or null if none are found.
   * @throws IllegalArgumentException  If name is null.
   */
  public static Datum get(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("Name cannot be null");

    for (Datum datum : predefined_) {
      if (datum.getName().equals(name))
        return datum;
    }

    // Not found
    return null;
  }

  /**
   * Return all the predefined datums.
   *
   * @return  All the predefined datums.
   */
  public static Datum[] getAll()
  {
    return predefined_; // TODO: Copy this.
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return name_ + " Region=" + region_ +
      " Ellipsoid=" + ellipsoid_.getName() +
      " dx=" + dx_ + " dy=" + dy_ + " dz=" + dz_ +
      " ex=" + errorX_ + " ey=" + errorY_ + " ez=" + errorZ_ +
      " # sat=" + nSatellites_;
  }

  /**
   * Load datums from the datum.txt file.
   *
   * @return Datums loaded from the datum.txt file. Never null.
   */
  private static Datum[] loadDatums()
  {
    List<Datum> datums = new ArrayList<>();

    String packageName = Datum.class.getPackage().getName();
    String packageLocation = packageName.replace('.', '/');
    String fileName = "/" + packageLocation + "/" + "datum.txt";

    InputStream stream = Datum.class.getResourceAsStream(fileName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

    try {
      while (reader.ready()) {
        String line = reader.readLine();
        if (line == null)
          continue;

        line = line.trim();

        if (line.startsWith ("#"))
          continue; // Comments

        if (line.length() == 0)
          continue;

        String[] tokens = line.split(",");

        String datumName     = tokens[0].trim();
        String ellipsoidName = tokens[1].trim();

        double dx = Double.parseDouble(tokens[2].trim());
        double dy = Double.parseDouble(tokens[3].trim());
        double dz = Double.parseDouble(tokens[4].trim());

        String region = tokens[5];

        double ex = Double.parseDouble(tokens[6].trim());
        double ey = Double.parseDouble(tokens[7].trim());
        double ez = Double.parseDouble(tokens[8].trim());

        int nSatellites = Integer.parseInt(tokens[9].trim());

        Ellipsoid ellipsoid = Ellipsoid.get(ellipsoidName);
        if (ellipsoid == null)
          ellipsoid = Ellipsoid.WGS_84;

        Datum datum = new Datum(datumName, ellipsoid,
                                region, dx, dy, dz,
                                ex, ey, ez, nSatellites);

        datums.add(datum);
      }
    }
    catch (IOException exception) {
      assert false;
    }

    return datums.toArray(new Datum[datums.size()]);
  }
}
