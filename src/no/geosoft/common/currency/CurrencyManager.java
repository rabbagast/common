package no.geosoft.common.currency;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Collection;
import java.util.Iterator;

/**
 * A currency manager object. The client machine must be online in order
 * for dynamic exchange rates to work.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class CurrencyManager
{
  private final Collection<WeakReference<CurrencyListener>> listeners_ = new ArrayList<>();

  private final CurrencyServer currencyServer_;

  private Currency displayCurrency_;

  /**
   * Create currency manager with the specified currency server.
   *
   * @param currencyServer  Currency server.
   */
  public CurrencyManager(CurrencyServer currencyServer)
  {
    currencyServer_ = currencyServer;
    displayCurrency_ = null;
  }

  /**
   * Create a currency manager with default currency server.
   */
  public CurrencyManager()
  {
    this (new SauderCurrencyServer());
  }

  /**
   * Set the current "display currency". The "dipslay currency" feature is
   * optional, but useful for GUI clients that display money amounts
   * in a user defined currency.
   * @see   #addCurrencyListener
   *
   * @param currency  New display currency.
   */
  public void setDisplayCurrency(Currency currency)
  {
    if (currency.equals (displayCurrency_))
      return;

    displayCurrency_ = currency;

    notifyListeners();
  }

  /**
   * Get current "display currency".
   *
   * @return  Current display currency.
   */
  public Currency getDisplayCurrency()
  {
    if (displayCurrency_ == null)
      displayCurrency_ = currencyServer_.getCurrency ("USD");

    return displayCurrency_;
  }

  /**
   * Notify listeners about change in display currency.
   */
  private void notifyListeners()
  {
    for (Iterator<WeakReference<CurrencyListener>> i = listeners_.iterator(); i.hasNext(); ) {
      WeakReference<CurrencyListener> reference = i.next();
      CurrencyListener listener = reference.get();
      if (listener == null)
        i.remove();
      else
        listener.currencyChanged();
    }
  }

  /**
   * Get currency for specified ISO code. The present currency server
   * defines which currencies are defined.
   *
   * @param isoCode  ISO code of currency to get.
   * @return         Requested currency (or null if not supported by the
   *                 current currency server.
   */
  public Currency getCurrency(String isoCode)
  {
    return currencyServer_.getCurrency(isoCode);
  }

  /**
   * Convert an amount in a given currency to base currency (US$).
   *
   * @param amount    Amount to covert.
   * @param currency  Currency to convert from.
   * @return          Equivalent in base currency (US$) according to
   *                  exchange rates given by current currency server.
   */
  public double convertToBase (double amount, Currency currency)
  {
    return amount / currencyServer_.getExchangeRate (currency);
  }



  /**
   * Convert an amount in current display currency to base currency (US$).
   *
   * @param amount  Amount to covert.
   * @return        Equivalent in base currency (US$) according to
   *                exchange rates given by current currency server.
   */
  public double getBaseAmount (double amount)
  {
    return convertToBase (amount, getDisplayCurrency());
  }



  /**
   * Convert an amount in base currency (US$) to the given currency.
   *
   * @param amount    Amount to covert.
   * @param currency  Currency to convert to.
   * @return          Equivalent in given currency according to
   *                  exchange rates given by current currency server.
   */
  public double convertFromBase (double amount, Currency currency)
  {
    return amount * currencyServer_.getExchangeRate (currency);
  }



  /**
   * Convert a base amount (US$) to current display currency.
   *
   * @param amount  Amount to covert.
   * @return        Equivalent in display currency according to
   *                exchange rates given by current currency server.
   */
  public double getDisplayAmount (double amount)
  {
    return convertFromBase (amount, getDisplayCurrency());
  }



  /**
   * Convert an amount between two currencies.
   *
   * @param amount        Amount to convert.
   * @param fromCurrency  Currency to convert from.
   * @param toCurrency    Currency to convert to.
   * @return              Converted amount according to exchange
   *                      rates given by current currency server.
   */
  public double convert (double amount, Currency fromCurrency, Currency toCurrency)
  {
    double baseAmount = convertToBase (amount, fromCurrency);
    return convertFromBase (baseAmount, toCurrency);
  }

  /**
   * Add a display currency listener. The listener is notified when the
   * display currency changes.
   *
   * @param currencyListener  Listener to add.
   */
  public void addCurrencyListener(CurrencyListener currencyListener)
  {
    // Check to see if it is there already
    for (Iterator<WeakReference<CurrencyListener>> i = listeners_.iterator(); i.hasNext(); ) {
      WeakReference<CurrencyListener> reference = i.next();
      CurrencyListener listener = reference.get();
      if (listener == currencyListener)
        return;
    }

    // Add the listener
    listeners_.add(new WeakReference<CurrencyListener>(currencyListener));
  }

  /**
   * Remove a display currency listener.
   *
   * @param currencyListener  Listener to remove.
   */
  public void removeCurrencyListener (CurrencyListener currencyListener)
  {
    for (Iterator<WeakReference<CurrencyListener>> i = listeners_.iterator(); i.hasNext(); ) {
      WeakReference<CurrencyListener> reference = i.next();
      CurrencyListener listener = reference.get();
      if (listener == currencyListener) {
        i.remove();
        break;
      }
    }
  }

  /**
   * Testing this class.
   *
   * @param args  Not used.
   */
  public static void main (String[] args)
  {
    double usdAmount = 100.0;

    CurrencyManager currencyManager = new CurrencyManager();
    Currency euro = currencyManager.getCurrency ("EUR");

    double euroAmount = currencyManager.convertFromBase (usdAmount, euro);

    System.out.println (usdAmount + "US$ == " + euroAmount + "EURO");
  }
}
