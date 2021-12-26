package no.geosoft.common.locale.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import no.geosoft.common.locale.LocaleListener;
import no.geosoft.common.locale.LocaleManager;

/**
 * Combo box for locale selection. The combo box updates and is updated
 * by the LocaleManager instance.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class LocaleComboBox extends JComboBox<Locale>
{
  /**
   * Create a local combo box with the specified locales.
   *
   * @param locales  Supported locales. Non-null and non-empty.
   */
  public LocaleComboBox(Locale[] locales)
  {
    super(locales);

    setSelectedItem(LocaleManager.getInstance().getCurrentLocale());

    setRenderer(new Renderer());

    EventHandler eventHandler = new EventHandler();
    addActionListener(eventHandler);
    LocaleManager.getInstance().addLocaleListener(eventHandler);
  }

  /**
   * Combo box renderer. Make sure each language is written in the
   * language of the current locale.
   *
   * TODO: Sort correctly according to current locale.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private static final class Renderer extends DefaultListCellRenderer
  {
    private final Locale englishLocale = new Locale("en", "US");

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object object,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean hasFocus)
    {
      JLabel label = (JLabel) super.getListCellRendererComponent(list,
                                                                 object,
                                                                 index,
                                                                 isSelected,
                                                                 hasFocus);
      Locale locale = (Locale) object;

      String text = LocaleManager.getInstance().
                    getText(locale.getDisplayLanguage(englishLocale));
      label.setText(text);

      return label;
    }
  }

  /**
   * Event handler for internal events.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private final class EventHandler
    implements ActionListener, LocaleListener
  {
    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event)
    {
      Locale locale = (Locale) getSelectedItem();
      LocaleManager.getInstance().setCurrentLocale(locale);
    }

    /** {@inheritDoc} */
    @Override
    public void localeChanged()
    {
      removeActionListener(this);
      setSelectedItem(LocaleManager.getInstance().getCurrentLocale());
      addActionListener(this);
    }
  }
}
