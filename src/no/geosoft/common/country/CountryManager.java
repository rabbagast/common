package no.geosoft.common.country;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;

import no.geosoft.common.locale.ResourceBundleControl;

/**
 * A country management singelton module. Useful for presentation of
 * sorted list of countries in GUI like in country combo boxes etc.
 * <p>
 * The country manager instantiate country objects for the countries
 * listed in the <tt>countries.txt</tt> resource of this package. The file
 * lists ISO codes and the official english names for countries of the world.
 * The countries.txt file has the following layout:
 *
 * <pre>
 *    AF; AFGHANISTAN
 *    AL; ALBANIA
 *    DZ; ALGERIA
 *    AS; AMERICAN SAMOA
 *    AD; ANDORRA
 *    AO; ANGOLA
 *    :
 * </pre>
 *
 * The country name is the default name used if a localized mapping is
 * not found, or the client asks for countries independent of locale.
 * <p>
 * TODO: Connect to an online resource to pick country ISO codes.
 * <p>
 * For internal application usage, only ISO codes for countries should
 * be used (i.e. for persistent storage store the ISO code rather than
 * a country name).
 * <p>
 * In user interfaces, localized names should be used according
 * to the locale of the application or preferred locale selected by the
 * user. Localized names are maintained in resource bundles picked from
 * property files with name <tt>Messages_xx_XX.properties</tt> where
 * <tt>xx</tt> is the language specifier and <tt>XX</tt> is the country
 * specifier. Provide localized country name mappings for all locales
 * supported by the client application. The properties files should be
 * located in the same package as the CountryManager. The properties
 * files has the following format (Messages_en_US.properties):
 *
 * <pre>
 *    AF = Afghanistan
 *    AL = Albania
 *    DZ = Algeria
 *    AS = American Samoa
 *    AD = Andorra
 *    AO = Angola
 *    :
 * </pre>
 *
 * <p>
 * Typical usage:
 * <pre>
 *    // Get a specific country
 *    Country country = CountryManager.getInstance().getCountry ("AF", french);
 *
 *    // Get world countries localized to french (assuming
 *    // Messages_fr_FR.properties is available).
 *    Locale french = new Locale ("fr", "FR");
 *    Collection countries = CountryManager.getInstance.getCountries (french);
 *
 *    // Put countries (sorted according to locale) in a combo box
 *    TreeSet sorted = new TreeSet (countries);
 *    JComboBox countriesCombo = new JComboBox (new Vector (sorted));
 * </pre>
 *
 * @see <a href="http://www.iso.org">ISO standard</a>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class CountryManager
{
  /** The sole instance of this class. */
  private static final CountryManager  instance_ = new CountryManager();

  /** List of countries hashed on ISO codes. */
  private final Map<String, Country> countries_ = loadCountries();

  /** List of synonym country names, hashed on ISO codes. */
  private final Map<String, List<String>> synonyms_ = loadSynonyms();

  /**
   * Create country manager.
   * Private to prevent instantiation.
   */
  private CountryManager()
  {
    // Nothing
  }

  /**
   * Return sole instance of this class.
   *
   * @return  CountryManager singleton instance. Never null.
   */
  public static CountryManager getInstance()
  {
    return instance_;
  }

  /**
   * Return countries of the world (as specified in countries.txt) using
   * the default (official ISO name) country names.
   *
   * @return  Countries of the world. Never null.
   */
  public Collection<Country> getCountries()
  {
    return Collections.unmodifiableCollection(countries_.values());
  }

  /**
   * Return countries of the world (as specified in countries.txt) with
   * names according  to the specified locale. The name mappings must be
   * available in a Messages_xx_XX.properties file where xx and XX match
   * the language and country of the specified locale respectively.
   * <p>
   * If the associated properties file is not found, the country names
   * are set according to the default ISO standard.
   *
   * @param locale  Locale to setting country names. Null for default.
   * @return        Countries of the world. Never null.
   */
  public Collection<Country> getCountries(Locale locale)
  {
    Collection<Country> sorted = new TreeSet<>();

    for (Country country : countries_.values()) {
      String isoCode = country.getIsoCode();
      Country localizedCountry = getCountry(isoCode, locale);
      if (localizedCountry != null)
        sorted.add (localizedCountry);
    }

    return sorted;
  }

  /**
   * Get country for specified ISO code.
   *
   * @param isoCode  ISO code of country to find. Non-null.
   * @param locale   Locale to control name of requested country. Null for default.
   * @return         Requested country (or null if ISO code is unknown).
   */
  public Country getCountry(String isoCode, Locale locale)
  {
    if (isoCode == null)
      throw new IllegalArgumentException("isoCode cannot be null");

    Country country = countries_.get(isoCode.toUpperCase());

    if (locale == null)
      return country;

    // Country for ISO code not found
    if (country == null)
      return null;

    // Default name
    String name = country.getName();

    // Localized name
    String bundleName = getClass().getPackage().getName() + ".Messages";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, locale, new ResourceBundleControl("UTF-8"));

    try {
      name = resourceBundle.getString(country.getIsoCode());
    }
    catch (Exception exception) {
      // No localized name found, keep the default name
    }

    return new Country(country.getIsoCode(), name);
  }

  /**
   * Get country for specified ISO code.
   *
   * @param isoCode  ISO code of country to find. Case-insensitive. Non-null.
   * @return         Requested country (with default ISO name) (or null if
   *                 ISO code is unknown).
   */
  public Country getCountry(String isoCode)
  {
    if (isoCode == null)
      throw new IllegalArgumentException("isoCode cannot be null");

    return getCountry(isoCode, null);
  }

  /**
   * Return country for specified (default ISO) name.
   *
   * @param name  Name of country to find. Non-null. Case-insensitive.
   * @return      Associated country instance. Null if not found.
   */
  public Country getCountryByName(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    //
    // Search the countries list
    //
    for (Country country : countries_.values()) {
      if (country.getName().toLowerCase().equals(name.toLowerCase()))
        return country;
    }

    //
    // Search the synonyms list
    //
    for (Map.Entry<String, List<String>> entry : synonyms_.entrySet()) {
      String isoCode = entry.getKey();
      List<String> synonyms = entry.getValue();

      for (String synonym : synonyms) {
        if (synonym.toLowerCase().equals(name.toLowerCase()))
          return getCountry(isoCode);
      }
    }

    // Not found
    return null;
  }

  /**
   * Load country synonyms.
   */
  private static Map<String, List<String>> loadSynonyms()
  {
    Map<String, List<String>> synonyms = new HashMap<>();

    String fileName = "synonyms.txt";
    String packageName = CountryManager.class.getPackage().getName();
    String packageLocation = packageName.replace('.', '/');
    String filePath = "/" + packageLocation + "/" + fileName;

    InputStream stream = CountryManager.class.getResourceAsStream(filePath);
    BufferedReader reader;

    try {
      reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Check encoding";
      return synonyms;
    }

    try {
      String line = reader.readLine();
      while (line != null) {
        String[] tokens = line.split(";");
        String isoCode = tokens[0].trim();

        List<String> countryNames = synonyms.get(isoCode);
        if (countryNames == null)
          countryNames = new ArrayList<String>();

        for (int i = 1; i < tokens.length; i++)
          countryNames.add(tokens[i].trim());

        synonyms.put(isoCode, countryNames);

        line = reader.readLine();
      }
    }
    catch (IOException exception) {
      // TODO: Logging
    }
    finally {
      try {
        reader.close();
      }
      catch (IOException exception) {
        // TODO: Logging
      }
    }

    return synonyms;
  }

  /**
   * Load all countries from countries.txt file.
   */
  private static Map<String, Country> loadCountries()
  {
    Map<String, Country> countries = new HashMap<>();

    String fileName = "countries.txt";
    String packageName = CountryManager.class.getPackage().getName();
    String packageLocation = packageName.replace('.', '/');
    String filePath = "/" + packageLocation + "/" + fileName;

    InputStream stream = CountryManager.class.getResourceAsStream(filePath);
    BufferedReader reader;

    try {
      reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Check encoding";
      return countries;
    }

    try {
      String line = reader.readLine();
      while (line != null) {
        String[] tokens = line.split(";");
        String isoCode = tokens[0].trim().toUpperCase();
        String name = tokens[1].trim();

        Country country = new Country(isoCode, name);
        countries.put(isoCode, country);

        line = reader.readLine();
      }
    }
    catch (IOException exception) {
      // TODO: Logging
    }
    finally {
      try {
        reader.close();
      }
      catch (IOException exception) {
        // TODO: Logging
      }
    }

    return countries;
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    CountryManager countryManager = CountryManager.getInstance();

    System.out.println(countryManager.getCountryByName("canada"));
    System.out.println(countryManager.getCountry("no", new Locale("no", "NO")));
  }
}
