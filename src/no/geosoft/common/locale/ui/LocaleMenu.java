package no.geosoft.common.locale.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import no.geosoft.common.locale.LocaleListener;
import no.geosoft.common.locale.LocaleManager;

/**
 * A menu for locale selection. The menu updates and is updated
 * by the LocaleManager instance.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class LocaleMenu extends JMenu
{
  /**
   * Locale of the language tags in the messages files.
   */
  private final Locale ENGLISH = new Locale("en", "US");

  /**
   * Create a menu with the specified locales.
   *
   * @param locales  Supported locales. Non-null and non-empty.
   * @throws IllegalArgumentException  If locales is null or empty.
   */
  public LocaleMenu(Locale[] locales)
  {
    if (locales == null)
      throw new IllegalArgumentException("locales cannot be null");

    if (locales.length == 0)
      throw new IllegalArgumentException("locales cannot be empty");

    EventHandler eventHandler = new EventHandler();
    LocaleManager.getInstance().addLocaleListener(eventHandler);

    Locale currentLocale = LocaleManager.getInstance().getCurrentLocale();

    ButtonGroup buttonGroup = new ButtonGroup();

    // Loop over the locales and create radio button
    // menu entries for each
    for (Locale locale : locales) {
      JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
      menuItem.putClientProperty("Locale", locale);
      menuItem.setSelected(locale.equals(currentLocale));

      buttonGroup.add(menuItem);

      menuItem.addActionListener(eventHandler);
      super.add(menuItem);
    }

    refreshLocaleDependent();
  }

  /**
   * Refresh locale dependent part of the GUI.
   *
   * TODO: Re-sort.
   */
  private void refreshLocaleDependent()
  {
    LocaleManager localeManager = LocaleManager.getInstance();

    // Loop over the menu entries and update the language names
    for (int i = 0; i < getItemCount(); i++) {
      JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) getItem(i);
      Locale locale = (Locale) menuItem.getClientProperty("Locale");
      String language = locale.getDisplayLanguage(ENGLISH);

      String text = localeManager.getText(language, locale);
      String toolTipText = localeManager.getText(language);

      // Hack
      boolean isManual = locale.getLanguage().equals("en") ||
                         locale.getLanguage().equals("no");

      // We add "(Auto-translate)" to the ones that are auto-translated,
      if (!isManual) {
        toolTipText = toolTipText + " (" + localeManager.getText("AutoTranslated") + ")";
      }

      menuItem.setText(text);
      menuItem.setToolTipText(toolTipText);
    }
  }

  /**
   * Event handler for internal events.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private class EventHandler
    implements ActionListener, LocaleListener
  {
    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event)
    {
      JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) event.getSource();
      if (menuItem.isSelected()) {
        Locale locale = (Locale) menuItem.getClientProperty("Locale");
        LocaleManager.getInstance().setCurrentLocale(locale);
      }
    }

    /** {@inheritDoc} */
    @Override
    public void localeChanged()
    {
      LocaleManager localeManager = LocaleManager.getInstance();

      Locale newLocale = localeManager.getCurrentLocale();

      // Remove listener so we don't get callback from our own update
      removeActionListener(this);

      // Select the correct language button
      for (int i = 0; i < getItemCount(); i++) {
        JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) getItem(i);
        Locale locale = (Locale) menuItem.getClientProperty("Locale");
        if (locale == newLocale)
          menuItem.setSelected(true);

        menuItem.setText(localeManager.getText(locale.getDisplayLanguage(ENGLISH)));
      }

      addActionListener(this);

      // Update names on all entries
      refreshLocaleDependent();
    }
  }
}
