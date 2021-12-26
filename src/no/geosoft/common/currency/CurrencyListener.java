package no.geosoft.common.currency;

/**
 * Interface for entities interested in changes in
 * display currency.
 *
 * @see CurrencyManager
 *
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public interface CurrencyListener
{
  /**
   * A notification that the current display currency has changed.
   *
   */
  public void currencyChanged();
}
