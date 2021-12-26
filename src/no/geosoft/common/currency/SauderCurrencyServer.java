package no.geosoft.common.currency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Currency server based on online currency service at
 * Sauder School of Business, University of British Columbia:
 *
 * http://fx.sauder.ubc.ca/supplement.html
 *
 *
 * @author <a href="mailto:info@geosoft.no">GeoSoft</a>
 */
class SauderCurrencyServer
  implements CurrencyServer
{
  private final String URL = "https://fx.sauder.ubc.ca/supplement.html";

  private final Map<String,Currency> currencies_ = new HashMap<>();

  private final Map<Currency,Double> exchangeRates_ = new HashMap<>();

  private Date date_;

  /**
   * Construct a currency server.
   */
  SauderCurrencyServer()
  {
    date_ = null;
  }

  /**
   * Get name of this currency server.
   *
   * @return  Name of this currency server.
   */
  public String getName()
  {
    return "Saunder School of Business";
  }

  /**
   * Get URL of this currency server.
   *
   * @return  URL of this currency server.
   */
  public String getUrl()
  {
    return URL;
  }

  /**
   * Return all currencies supported by this server.
   *
   * @return Collection of currencies.
   */
  public Collection<Currency> getCurrencies()
  {
    if (currencies_.isEmpty()) {
      try {
        loadCurrencies();
      }
      catch (IOException exception) {
        exception.printStackTrace(); // TODO
      }
    }

    return currencies_.values();
  }

  /**
   * Return a specfic currency (or null if this currency
   * is not supported).
   *
   * @param isoCode  ISO code of the currency to get.
   * @return         Requested currency.
   */
  public Currency getCurrency(String isoCode)
  {
    getCurrencies();
    return currencies_.get(isoCode);
  }

  /**
   * Get exchange rate between this currency and the base
   * currency (US$)
   *
   * @param currency  Currency to get exchange rate for.
   * @return          Exchange rate between this currency and US$
   */
  public double getExchangeRate (Currency currency)
  {
    Double exchangeRate = exchangeRates_.get(currency);
    return exchangeRate != null ? exchangeRate.doubleValue() : 0.0;
  }

  /**
   * Get date of exchange rate information.
   *
   * @return
   */
  public Date getDate()
  {
    getCurrencies();
    return date_;
  }

  private void parseDate(BufferedReader reader)
    throws IOException
  {
    while (true) {
      String line = reader.readLine();

      if (line == null)
        break;

      if (line.indexOf ("FX Rates Supplement") != -1) {
        // TODO
        date_ = new Date();
        break;
      }
    }
  }

  private void skipPast(BufferedReader reader, String string)
    throws IOException
  {
    while (true) {
      String line = reader.readLine();

      if (line == null)
        break;

      if (line.indexOf(string) != -1)
        break;
    }
  }

  private boolean parseCurrency(BufferedReader reader)
    throws IOException
  {
    Currency currency = null;

    skipPast(reader, "<tr ");

    String line = reader.readLine();

    if (line == null)
      return true; // Done

    int pos = line.indexOf("<tt>");
    if (pos == -1)
      return true; // Done

    String isoCode = line.substring(pos+4, pos+7);

    try {
      currency = Currency.getInstance(isoCode);
    }
    catch (RuntimeException exception) {
      // System.out.println ("Unknown ISO code: " + isoCode);
    }

    skipPast(reader, "<td");
    skipPast(reader, "<td");

    line = reader.readLine();
    if (line == null) return true; // Done

    int pos1 = line.indexOf("right\">");
    int pos2 = line.indexOf("&nbsp;", pos1);
    String rateString = line.substring(pos1 + 7, pos2);

    double rate = 0.0;
    try {
      rate = Double.parseDouble (rateString);
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }

    if (currency != null) {
      currencies_.put(currency.getCurrencyCode(), currency);
      exchangeRates_.put (currency, new Double (rate));
    }

    return false; // !Done
  }

  private void loadCurrencies()
    throws IOException
  {
    URL url = new URL(getUrl());
    InputStream is = url.openStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    parseDate(reader);

    skipPast(reader, "Code");

    while (true) {
      boolean isDone = parseCurrency(reader);
      if (isDone)
        break;
    }

    reader.close();
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    getCurrencies();

    StringBuilder s = new StringBuilder();
    s.append("Name: " + getName() + "\n");
    s.append("URL: " + getUrl() + "\n");
    for (Map.Entry<Currency, Double> exchangeRate : exchangeRates_.entrySet())
      s.append("  " + exchangeRate.getKey().getCurrencyCode() + " " + exchangeRate.getKey().getDisplayName() + " " + exchangeRate.getValue() + "\n");
    return s.toString();
  }

  /**
   * Testing this class.
   *
   * @param args  Not used.
   */
  public static void main (String[] args)
  {
    CurrencyServer server = new SauderCurrencyServer();
    System.out.println(server);
  }
}
