package no.geosoft.common.locale;

/**
 * Interface for classes interested in locale changes.
 * When notified, the new locale can be obtained by:
 *
 * <pre>
 *   LocaleManager.getInstance().getLocale()
 * </pre>
 *
 * A more typical approach (as the actual new locale is secondary)
 * is simply to update GUI text elements, like
 *
 * <pre>
 *   helpButton.setText(localeManager.getText("Help");
 * </pre>
 *
 * etc.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public interface LocaleListener
{
  /**
   * Called when the locale is changed.
   */
  void localeChanged();
}
