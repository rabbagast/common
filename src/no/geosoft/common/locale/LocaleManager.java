package no.geosoft.common.locale;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for holding current locale information, and notify client
 * application about locale changes. Used for dynamic (or static)
 * locale updates and language translations during a program session.
 * <p>
 * For language dependent user interfaces, put translation files into
 * the packages where required. The files are named as follows:
 * <tt>Messages_xx_XX.properties</tt> where <em>xx</em> is the language
 * identifier and <em>XX</em> is the country identifier, for instance
 *
 * <pre>
 *   Messages_en_US.properties  // american
 *   Messages_fr_FR.properties  // french
 *   Messages_ch_CH.properties  // chineese
 * </pre>
 *
 * and so on. These are ordinary properties files containing language
 * translations, for instance (French):
 *
 * <pre>
 *   Close = Fermer
 *   New   = Nouveau
 *   Help  = Aide
 * </pre>
 *
 * and (American):
 *
 * <pre>
 *   Close = Close
 *   New   = New
 *   Help  = Help
 * </pre>
 *
 * <p>
 * A locale aware GUI will use the locale manager like this:
 *
 * <pre>
 *   JButton helpButton = new JButton();
 *   :
 *   helpButton.setText(localeManager.getText("Help"));
 * </pre>
 *
 * If the latter is done inside a localeChanged callback, the GUI will
 * update itself dynamically if locale is changed during a session.
 *
 * <p>
 * <b>Synchronization:</b>
 * <p>
 * This class is thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class LocaleManager
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(LocaleManager.class.getName());

  /** The sole instance of this class. */
  private static final LocaleManager instance_ = new LocaleManager();

  /** Text resources. */
  private final HashMap<String, ResourceBundle> resourceBundles_ = new HashMap<>();

  /** List of locale listeners. */
  private final Collection<WeakReference<LocaleListener>> listeners_ = new ArrayList<>();

  /** Default locale. Used when no mapping is found for a specific text. */
  private Locale defaultLocale_ = new Locale("en", "US");

  /** The current locale. */
  private Locale currentLocale_ = defaultLocale_;

  /**
   * Private constructor to prevent client instantiation.
   */
  private LocaleManager()
  {
    // Nothing
  }

  /**
   * Return the sole instance of this class.
   *
   * @return  The sole locale manager instance.
   */
  public static LocaleManager getInstance()
  {
    return instance_;
  }

  /**
   * Set the default locale. If a translation is not found for a key
   * in the current locale, the default locale is used instead.
   *
   * @param locale  Default locale.
   * @throws IllegalArgumentException  If locale is null.
   */
  public void setDefaultLocale(Locale locale)
  {
    if (locale == null)
      throw new IllegalArgumentException("locale cannot be null");

    synchronized (this) {
      defaultLocale_ = locale;
    }
  }

  /**
   * Return current locale.
   *
   * @return  Current locale.
   */
  public Locale getCurrentLocale()
  {
    synchronized (this) {
      return currentLocale_;
    }
  }

  /**
   * Set the current locale, and notify listeners about the change.
   *
   * @param locale  New local to set. Non null.
   * @throws IllegalArgumentException  If locale is null.
   */
  public void setCurrentLocale(Locale locale)
  {
    if (locale == null)
      throw new IllegalArgumentException("locale cannot be null");

    synchronized (this) {
      // Leave here if no change
      if (locale.equals(currentLocale_))
        return;

      // Create the new locale
      currentLocale_ = locale;

      // Clear resource bundles
      resourceBundles_.clear();

      // Call listeners
      for (Iterator<WeakReference<LocaleListener>> i = listeners_.iterator();
           i.hasNext(); ) {
        WeakReference<LocaleListener> reference = i.next();
        LocaleListener listener = reference.get();
        if (listener == null)
          i.remove();
        else
          listener.localeChanged();
      }
    }

    logger_.info("Current locale set to: " + locale);
  }

  /**
   * Returned localized text for specified text. Search resource within
   * specified package.
   *
   * @param packageName  Name of package to locate resource. Non-null.
   * @param tag          Text tag. Non-null.
   * @param locale       Locale to return text for. Non-null.
   * @return  Localized text.
   */
  private String getText(String packageName, String tag, Locale locale)
  {
    assert packageName != null : "packageName cannot be null";
    assert tag != null : "tag cannot be null";
    assert locale != null : "locale cannot be null";

    try {
      ResourceBundle resourceBundle;

      synchronized (this) {
        resourceBundle = resourceBundles_.get(packageName);

        if (resourceBundle == null || locale != currentLocale_) {
          String bundleName = packageName + ".Messages";

          // Use custom ResourceBundle.Control that handles Unicode (UTF-8)
          resourceBundle = ResourceBundle.getBundle(bundleName, locale, new ResourceBundleControl("UTF-8"));

          if (locale == currentLocale_)
            resourceBundles_.put(packageName, resourceBundle);
        }
      }

      return resourceBundle.getString(tag);
    }
    catch (MissingResourceException exception) {
      logger_.log(Level.WARNING, "Resource not found for tag: " + tag +
                  " in " + packageName,
                  exception);
      return tag;
    }
  }


  private String getCallerPackage(int level)
  {
    String packageName = null;

    //
    // Approach 1: Java 9 and later
    //
    /*
    List<StackWalker.StackFrame> stack = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.limit(level).collect(java.util.stream.Collectors.toList()));
    Class<?> callingClass = stack.get(level - 1).getDeclaringClass();
    packageName = callingClass.getPackage().getName();
    */

    //
    // Approach 2: Java < 9
    //
    @SuppressWarnings("deprecation")
    Class<?> callingClass = sun.reflect.Reflection.getCallerClass(level);
    packageName = callingClass.getPackage().getName();

    //
    // Approach 3: Fall-back
    //
    /*
    StringWriter stringWriter = new StringWriter();
    new Throwable().printStackTrace(new PrintWriter(stringWriter));
    String callStack = stringWriter.toString();

    int pos1 = callStack.indexOf("at ");
    for (int i = 1; i < level; i++)
      pos1 = callStack.indexOf("at ", pos1 + 2) + 3;

    int pos2 = callStack.indexOf("(", pos1);
    String line = callStack.substring(pos1, pos2);
    line = line.substring(0, line.lastIndexOf("."));
    packageName = line.substring(0, line.lastIndexOf("."));
    */

    return packageName;
  }


  /**
   * Return localized text for a specified tag. The text is looked up
   * in the resource bundle of the package of the caller.
   *
   * @param tag  Tag of text to locate. Non-null.
   * @return     Localized text of tag. Never null.
   * @throws IllegalArgumentException  If tag is null.
   */
  public String getText(String tag)
  {
    if (tag == null)
      throw new IllegalArgumentException("tag cannot be null");

    String packageName = getCallerPackage(3);
    return getText(packageName, tag, currentLocale_);
  }

  /**
   * Get text of tag for the specified locale.
   *
   * @param tag     Tag of text to get. Non-null.
   * @param locale  Locale to get text in. Non-null.
   * @return        Requested text. Never null.
   * @throws IllegalArgumentException  If tag of locale is null.
   */
  public String getText(String tag, Locale locale)
  {
    if (tag == null)
      throw new IllegalArgumentException("tag cannot be null");

    if (locale == null)
      throw new IllegalArgumentException("locale cannot be null");

    String packageName = getCallerPackage(3);
    return getText(packageName, tag, locale);
  }

  /**
   * Check if text for the specified specified locale tag exists.
   *
   * @param packageName  Name of package to locate resource. Non-null.
   * @param tag          Text tag. Non-null.
   * @return             True if text exists, false otherwise.
   */
  private boolean hasText(String packageName, String tag)
  {
    assert packageName != null : "packageName cannot be null";
    assert tag != null : "tag cannot be null";

    synchronized (this) {
      ResourceBundle resourceBundle = resourceBundles_.get(packageName);

      if (resourceBundle == null) {
        String bundleName = packageName + ".Messages";
        resourceBundle = ResourceBundle.getBundle(bundleName, currentLocale_);
        resourceBundles_.put(packageName, resourceBundle);
      }

      return resourceBundle.containsKey(tag);
    }
  }

  /**
   * Check if text for the specified locale tag exists.
   *
   * @param tag  Text tag to check. Non-null.
   * @return     True if text exists, false otherwise.
   * @throws IllegalArgumentException  If tag is null.
   */
  public boolean hasText(String tag)
  {
    if (tag == null)
      throw new IllegalArgumentException("tag cannot be null");

    String packageName = getCallerPackage(3);
    return hasText(packageName, tag);
  }

  /**
   * Add a locale listener. The listener is notified when the current
   * locale of the LocaleManager is changed.
   *
   * @param localeListener  Listener to add. Non-null.
   * @throws IllegalArgumentException  If localeListener is null.
   */
  public void addLocaleListener(LocaleListener localeListener)
  {
    if (localeListener == null)
      throw new IllegalArgumentException("listener cannot be null");

    synchronized (listeners_) {

      // Check to see if it is there already
      for (WeakReference<LocaleListener> listenerRef : listeners_) {
        LocaleListener listener = listenerRef.get();
        if (listener == localeListener)
          return;
      }

      // Add the listener
      listeners_.add(new WeakReference<>(localeListener));
    }
  }

  /**
   * Remove a locale listener.
   *
   * @param localeListener  Locale listener to remove. Non-null.
   * @throws IllegalArgumentException  If localeListener is null.
   */
  public void removeLocaleListener(LocaleListener localeListener)
  {
    if (localeListener == null)
      throw new IllegalArgumentException("localeListener cannot be null");

    WeakReference<LocaleListener> toRemove = null;

    synchronized (this) {
      for (WeakReference<LocaleListener> listenerRef : listeners_) {
        LocaleListener listener = listenerRef.get();
        if (listener == localeListener) {
          toRemove = listenerRef;
          break;
        }
      }

      if (toRemove != null)
        listeners_.remove(toRemove);
    }
  }

  /**
   * Get default date format for the current locale.
   *
   * @param style  One of DateFormat.SHORT, MEDIUM or LONG.
   * @return       Date format for current locale.
   */
  public DateFormat getDateFormat(int style)
  {
    DateFormat dateFormat;

    // TODO: Cannot sync on non-final variable!!
    synchronized (currentLocale_) {
      dateFormat = DateFormat.getDateInstance(style, currentLocale_);
    }

    //
    // The default SHORT date format is like 19.09.15.
    // We want it with full year specification: 19.09.2015.
    //
    // In some locales the SHORT date format is like 9/19/15.
    // We'd like to pad this zeroes for readability in
    // tabular views.
    //
    if (style == DateFormat.SHORT && dateFormat instanceof SimpleDateFormat) {
      String pattern = ((SimpleDateFormat) dateFormat).toPattern();
      pattern = pattern.replaceAll("y+","yyyy");
      pattern = pattern.replaceAll("d+", "dd");
      pattern = pattern.replaceAll("M+", "MM");

      ((SimpleDateFormat) dateFormat).applyPattern(pattern);
    }

    return dateFormat;
  }

  /**
   * Get default time format for the current locale.
   *
   * @param style  One of DateFormat.SHORT, MEDIUM or LONG.
   * @return       Time format for current locale.
   */
  public DateFormat getTimeFormat(int style)
  {
    DateFormat dateFormat = null;

    synchronized (currentLocale_) {
      dateFormat = DateFormat.getTimeInstance(style, currentLocale_);
    }

    //
    // The default LONG format is without milliseconds.
    // We want these as well.
    //
    String pattern = ((SimpleDateFormat) dateFormat).toPattern();
    if (style == DateFormat.LONG) {
      int ind = pattern.lastIndexOf('s');
      String newPattern = null;
      if (ind > 0) {
        newPattern = pattern.substring(0, ind + 1) + ".SSS";
        if (ind + 1 < pattern.length()) {
          newPattern += pattern.substring(ind + 1, pattern.length());
        }
      }
      pattern = newPattern;
    }

    //
    // Force 24h
    //
    if ((pattern.indexOf('h') == pattern.lastIndexOf('h')) &&
        (pattern.indexOf('h') != -1)) {
      pattern = pattern.replaceAll("h", "HH");
    }
    else {
      pattern = pattern.replaceAll("h", "H");
    }

    pattern = pattern.replaceAll("a", "");
    pattern = pattern.replaceAll("z", "");
    pattern = pattern.trim();

    ((SimpleDateFormat) dateFormat).applyPattern(pattern);

    return dateFormat;
  }

  /**
   * Get defaut date/time format for current locale.
   *
   * @param dateStyle  One of DateFormat.SHORT, MEDIUM or LONG.
   * @param timeStyle  One of DateFormat.SHORT, MEDIUM or LONG.
   * @return           Date/time format for the current locale.
   */
  public DateFormat getDateTimeFormat(int dateStyle, int timeStyle)
  {
    DateFormat dateFormat = null;
    DateFormat timeFormat = null;

    synchronized (currentLocale_) {
      dateFormat = getDateFormat(dateStyle);
      timeFormat = getTimeFormat(timeStyle);
    }

    String datePattern = ((SimpleDateFormat) dateFormat).toPattern();
    String timePattern = ((SimpleDateFormat) timeFormat).toPattern();

    ((SimpleDateFormat) dateFormat).applyPattern(datePattern + ' ' + timePattern);
    return dateFormat;
  }

  /**
   * Return default localized text representation of the specified date.
   *
   * @param date  Date to format.
   *              May be null, in case an empty string is returned.
   * @return String representation of the specified date.
   */
  public String getDate(Date date)
  {
    return getDate(date, DateFormat.SHORT);
  }

  /**
   * Return default localized text reprentation of the specified date.
   *
   * @param date   Date to format.
   * @param style  DateFormat.SHORT, MEDIUM or LONG
   * @return       String representation of specified date.
   */
  public String getDate(Date date, int style)
  {
    if (date == null)
      return "";

    DateFormat dateFormat = getDateFormat(style);
    return dateFormat.format(date);
  }

  /**
   * Return default localized text representation of the specified time.
   *
   * @param date   Time to format.
   * @param style  DateFormat.SHORT, MEDIUM or LONG
   * @return       String representation of specified time.
   */
  public String getTime(Date date, int style)
  {
    if (date == null) return "";
    DateFormat dateFormat = getTimeFormat(style);
    return dateFormat.format(date);
  }

  /**
   * Return default localized text representation of the specified time.
   *
   * @param date  Time to format.
   * @return      String representation of specified time.
   */
  public String getTime(Date date)
  {
    return getTime(date, DateFormat.SHORT);
  }

  /**
   * Return default localized text representation of the specified date/time.
   *
   * @param date       Date/time to format.
   * @param dateStyle  One of DateFormat.SHORT, MEDIUM or LONG.
   * @param timeStyle  One of DateFormat.SHORT, MEDIUM or LONG.
   * @return           String representation of specified date/time.
   */
  public String getDateTime(Date date, int dateStyle, int timeStyle)
  {
    if (date == null)
      return "";

    DateFormat dateFormat = getDateTimeFormat(dateStyle, timeStyle);
    return dateFormat.format(date);
  }

  /**
   * Return default localized text representation of the specified date/time.
   *
   * @param date  Date/time to format.
   * @return      String representation of specified date/time.
   */
  public String getDateTime(Date date)
  {
    return getDateTime(date, DateFormat.SHORT, DateFormat.SHORT);
  }
}
