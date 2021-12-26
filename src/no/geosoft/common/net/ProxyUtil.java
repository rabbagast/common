package no.geosoft.common.net;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utilities for working with Internet Proxy servers.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class ProxyUtil
{
  /** Sample URL used when accessing Internet. */
  private final static String SAMPLE_URL = "http://www.google.com/";

  /**
   * Private constructor to prevent client instantiation.
   */
  private ProxyUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return the host name of the present proxy server.
   *
   * @return  Name of proxy server, or null if we are directly (or not at all)
   *          connected to the Internet.
   */
  public static String getProxyHostName()
  {
    try {
      for (Proxy proxy : ProxySelector.getDefault().select(new URI(SAMPLE_URL))) {
        InetSocketAddress socketAddress = (InetSocketAddress) proxy.address();
        return socketAddress != null ? socketAddress.getHostName() : null;
      }
    }

    // This will not happen
    catch (URISyntaxException exception) {
      assert false : exception.getMessage();
    }

    return null;
  }

  /**
   * Return the port number for the specified proxy server.
   *
   * @param proxyHostName  Name of proxy server to find port number of. Non-null.
   * @return Requested port number, or -1 if the specified proxy server is not found.
   * @throws IllegalArgumentException  If proxyHostName is null
   */
  public static int getProxyPort(String proxyHostName)
  {
    if (proxyHostName == null)
      throw new IllegalArgumentException("proxyHostName cannot be null");

    try {
      for (Proxy proxy : ProxySelector.getDefault().select(new URI(SAMPLE_URL))) {
        InetSocketAddress socketAddress = (InetSocketAddress) proxy.address();
        if (socketAddress != null && socketAddress.getHostName().equals(proxyHostName))
          return socketAddress.getPort();
      }
    }

    // This will not happen
    catch (URISyntaxException exception) {
      assert false : exception.getMessage();
    }

    return -1;
  }
}
