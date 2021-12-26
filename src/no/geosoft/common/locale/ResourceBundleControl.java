package no.geosoft.common.locale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * The class is downloaded from the net.
 * <p>
 * I think we use it in order to properly handle the full Unicode (UTF-8)
 * character set in our translation property files.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class ResourceBundleControl extends ResourceBundle.Control
{
  /** Character encoding used for the translation property files. */
  private final String encoding_;

  /**
   * This constructor allows to set encoding that will be used while reading resource bundle
   *
   * @param encoding  Encoding to use. Non-null.
   */
  public ResourceBundleControl(String encoding)
  {
    if (encoding == null)
      throw new IllegalArgumentException("encoding cannot be null");

    encoding_ = encoding;
  }

  /**
   * This code is just copy-paste with usage {@link java.io.Reader}
   * instead of {@link java.io.InputStream} to read properties.
   *
   * {@inheritDoc}
   */
  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                  ClassLoader loader, boolean reload)
    throws IllegalAccessException, InstantiationException, IOException
  {
    String bundleName = toBundleName(baseName, locale);

    ResourceBundle bundle = null;

    if (format.equals("java.class")) {
      try {
        @SuppressWarnings({"unchecked"})
        Class<? extends ResourceBundle> bundleClass = (Class<? extends ResourceBundle>) loader.loadClass(bundleName);

        // If the class isn't a ResourceBundle subclass, throw a
        // ClassCastException.
        if (ResourceBundle.class.isAssignableFrom(bundleClass))
          bundle = bundleClass.newInstance();
        else
          throw new ClassCastException(bundleClass.getName() + " cannot be cast to ResourceBundle");
      }
      catch (ClassNotFoundException exception) {
        // Ignore. This will happen.
      }
    }

    else if (format.equals("java.properties")) {
      final String resourceName = toResourceName(bundleName, "properties");
      final ClassLoader classLoader = loader;
      final boolean reloadFlag = reload;

      InputStreamReader reader = null;
      InputStream stream;

      try {
        stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>()
        {
          @Override
          public InputStream run() throws IOException
          {
            InputStream is = null;
            if (reloadFlag) {
              URL url = classLoader.getResource(resourceName);
              if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                  // Disable caches to get fresh data for reloading
                  connection.setUseCaches(false);
                  is = connection.getInputStream();
                }
              }
            }
            else {
              is = classLoader.getResourceAsStream(resourceName);
            }

            return is;
          }
        });

        if (stream != null)
          reader = new InputStreamReader(stream, encoding_);
      }
      catch (PrivilegedActionException exception) {
        throw (IOException) exception.getException();
      }

      if (reader != null) {
        try {
          bundle = new PropertyResourceBundle(reader);
        }
        finally {
          reader.close();
        }
      }
    }

    else {
      throw new IllegalArgumentException("unknown format: " + format);
    }

    return bundle;
  }
}
