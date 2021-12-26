package no.geosoft.common.util;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Class for accessing GoogleEarth from java.
 *
 * @author <a href="mailto:jdre@statoil.com">Jacob Dreyer</a>
 */
public final class GoogleEarth
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(GoogleEarth.class.getName());

  /**
   * Private constructor to prevent client instantiation.
   */
  private GoogleEarth() {
    assert false : "this ctor should never be called";
  }

  /**
   * Return the center from a number of placemarks.
   *
   * @param placemarks  List of placemarks to find center of.
   *                    Non-null and non-empty.
   * @return            Center location among the arguments as a
   *                    double[2] of latitude, longitude in radians.
   */
  private static double[] findCenter(List<Placemark> placemarks)
  {
    assert placemarks != null && !placemarks.isEmpty() :
      "placemarks cannot be null or empty";

    double cLatitude = 0.0;
    double cLongitude = 0.0;

    for (Placemark placemark : placemarks) {
      cLatitude += placemark.latitude;
      cLongitude += placemark.longitude;
    }

    int nPlacemarks = placemarks.size();

    cLatitude /= nPlacemarks;
    cLongitude /= nPlacemarks;

    return new double[] {cLatitude, cLongitude};
  }

  /**
   *
   *
   * @return
   */
  private static String getFactGlobeKml() {
    StringBuilder s = new StringBuilder();

    s.append("<NetworkLink>");
    s.append("  <name>North Sea</name>");
    s.append("  <visibility>1</visibility>");
    s.append("  <open>1</open>");
    s.append("  <Link>");
    s.append("    <href>http://www.npd.no/engelsk/cwi/pbl/en/aFactGlobe/NPDsFactGlobe.kml</href>");
    s.append("    <refreshMode>onChange</refreshMode>");
    s.append("    <refreshInterval>5</refreshInterval>");
    s.append("  </Link>");
    s.append(" </NetworkLink>");

    return s.toString();
  }

  /**
   * Open GoogleEarth with the specified placemarks inserted.
   *
   * @param folderName  Folder name.
   * @param placemarks  Placemarks to insert. Non-null and non-empty.
   * @throws IllegalArgumentException  If placemarks is null or empty.
   */
  public static void show(String folderName, List<Placemark> placemarks) {
    if (placemarks == null)
      throw new IllegalArgumentException("placemarks cannot be null");

    if (placemarks.isEmpty())
      throw new IllegalArgumentException("placemarks cannot be empty: "
                                         + placemarks);

    double[] center = findCenter(placemarks);

    double centerLatitudeDegrees = center[0] * 180.0 / Math.PI;
    double centerLongitudeDegrees = center[1] * 180.0 / Math.PI;

    StringBuilder s = new StringBuilder();
    s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    s.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
    s.append("<Folder>");
    s.append("  <name>GeoSoft</name>");
    s.append("  <visibility>1</visibility>");
    s.append("  <open>1</open>");

    s.append(getFactGlobeKml());

    s.append("  <LookAt>");
    s.append("    <longitude>" + centerLongitudeDegrees + "</longitude>");
    s.append("    <latitude>" + centerLatitudeDegrees + "</latitude>");
    s.append("    <range>1000000</range>");
    s.append("    <heading>0</heading>");
    s.append("    <tilt>0</tilt>");
    s.append("    <roll>0</roll>");
    s.append("  </LookAt>");

    s.append("  <Folder>");
    s.append("    <name>" + folderName + "</name>");

    s.append("    <Document>");


    for (Placemark placemark : placemarks) {
      s.append(placemark.toKml());
    }

    s.append("    </Document>");
    s.append("  </Folder>");
    s.append("</Folder>");
    s.append("</kml>");

    //
    // Write content to a temporary file and launch this file.
    // If proper mapped on the desktop this will launch GoogleEarth.
    //
    try {
      File tempFile = File.createTempFile("Well", ".kml");
      tempFile.deleteOnExit();

      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
      writer.write(s.toString());
      writer.close();

      Desktop desktop = Desktop.getDesktop();
      desktop.open(tempFile);
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to show in Google Earth: " + s, exception);
    }
  }

  /**
   * Open GoogleEarth with the specified placemark inserted.
   *
   * @param folderName  Folder name.
   * @param placemark   Placemark to show. Non-null.
   * @throws IllegalArgumentException  If placemark is null.
   */
  public static void show(String folderName, GoogleEarth.Placemark placemark)
  {
    List<Placemark> placemarks = new ArrayList<Placemark>();
    placemarks.add(placemark);

    show(folderName, placemarks);
  }

  /**
   * Class representing a GoogleEarth placemark.
   *
   * @author <a href="mailto:jdre@statoil.com">Jacob Dreyer</a>
   */
  public static class Placemark {

    /** Placemark name. May be empty. Never null. */
    private String text;

    /** Location latitude in radians. */
    private double latitude;

    /** Location longitude in radians. */
    private double longitude;

    /** Name of image to decorate placemark with. May be null. */
    private String imageName;

    /**
     * Create a new GoogleEarth placemark.
     *
     * @param text       Text to follow the placemark.
     *                   May be null to indicate none.
     * @param latitude   Latitude of location in radians.
     * @param longitude  Longitude of location in radians.
     * @param imageName  Image name (like "icons/well.png") that will
     *                   decorate the placemark.
     *                   May be null to indicate none.
     */
    public Placemark(String text, double latitude, double longitude,
                     String imageName) {
      this.text = text != null ? text : "";
      this.latitude = latitude;
      this.longitude = longitude;
      this.imageName = imageName;
    }

    /**
     * Convert this placemark to a KML string.
     *
     * @return  This placemark as a KML string. Never null.
     */
    public String toKml()
    {
      double latitudeDegrees = latitude * 180.0 / Math.PI;
      double longitudeDegrees = longitude * 180.0 / Math.PI;

      StringBuilder s = new StringBuilder();

      String styleName = null;

      if (imageName != null) {
        // TODO
        File file = null; // CommonIcon.get(imageName);
        String iconReference = "";// file.getAbsolutePath();

        // This is just some unique tag referred to from below
        // styleName = file.getName();

        s.append("<Style id=\"" + styleName + "\">");
        s.append("  <IconStyle>");
        s.append("     <Icon>");
        s.append("        <href>" + iconReference + "</href>");
        s.append("     </Icon>");
        s.append("  </IconStyle>");
        s.append("</Style>");
      }

      s.append("<Placemark>");
      s.append("  <name>" + text + "</name>");
      s.append("  <Point>");
      s.append("    <coordinates>" + longitudeDegrees + "," + latitudeDegrees + "</coordinates>");
      s.append("  </Point>");

      //if (styleName != null)
      //  s.append("  <styleUrl>#" + styleName + "</styleUrl>");

      s.append("</Placemark>");

      return s.toString();
    }
  }
}
