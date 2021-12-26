package no.geosoft.common.country;

/**
 * Class representing a country (according to ISO 3166-1). The class consist
 * of both ISO code and name, where name is a localized version of the
 * official country name.
 *
 * @see CountryManager
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Country
  implements Comparable<Country>
{
  /**
   * International ISO code of this country. Non-null.
   * ISO codes are case insensitive by the standard. We keep them all upper case
   * for consistency.
   */
  private final String isoCode_;

  /** Name of this country. Possibly localized. Non-null. */
  private final String name_;

  /**
   * Create a country instance with given ISO code.
   *
   * @param isoCode  ISO code of country. Case-insensitive. Non-null.
   * @param name     Name of country. Possibly localized. Non-null.
   */
  public Country(String isoCode, String name)
  {
    if (isoCode == null)
      throw new IllegalArgumentException("isoCode cannot be null");

    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    isoCode_ = isoCode.toUpperCase();
    name_ = name;
  }

  /**
   * Return ISO code of this country.
   *
   * @return ISO code of this country. Never null. Upper case.
   */
  public String getIsoCode()
  {
    return isoCode_;
  }

  /**
   * Return localized name of this country.
   *
   * @return  Localized name of this country. Never null.
   */
  public String getName()
  {
    return name_;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return isoCode_ + ":" + name_;
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(Country country)
  {
    return name_.compareTo(country.name_);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return isoCode_.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object object)
  {
    if (object == this)
      return true;

    else if (object == null)
      return false;

    else if (!(object instanceof Country))
      return false;

    Country country = (Country) object;
    return isoCode_.equals(country.isoCode_);
  }
}
