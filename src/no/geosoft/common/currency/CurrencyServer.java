package no.geosoft.common.currency;

import java.util.Collection;
import java.util.Currency;
import java.util.Date;

/**
 * Interface for entities acting as a currency srver.
 *
 * @author <a href="mailto:info@geosoft.no">GeoSoft</a>
 */
public interface CurrencyServer
{
  /**
   * Get name of this currency server.
   *
   * @return  Name of this currency server.
   */
  String getName();

  /**
   * Get URL of this currency server.
   *
   * @return  URL of this currency server.
   */
  String getUrl();

  /**
   * Return all currencies supported by this server.
   *
   * @return Collection of currencies (java.util.Currency)
   */
  Collection<Currency> getCurrencies();


  /**
   * Return a specfic currency (or null if this currency
   * is not supported).
   *
   * @param isoCode  ISO code of the currency to get.
   * @return         Requested currency.
   */
  Currency getCurrency(String isoCode);


  /**
   * Get exchange rate between this currency and the base
   * currency (US$)
   *
   * @param currency  Currency to get exchange rate for.
   * @return          Exchange rate between this currency and US$
   */
  double getExchangeRate(Currency currency);

  /**
   * Get date of exchange rate information.
   *
   * @return  Date of exchange rate information.
   */
  Date getDate();
}
