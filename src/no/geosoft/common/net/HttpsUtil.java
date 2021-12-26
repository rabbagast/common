package no.geosoft.common.net;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * A class for turning off and on certificate checking before HTTPS access.
 * <p>
 * Such checking is by default on, and should probably be left on in most cases.
 * However, when accessing our own server, we get certification validation exception
 * unless we first turn off this checking. In these cases this should anyway be safe.
 * <p>
 * Usage should be as follows:
 * <pre>
 *   HttpsUtil.disableCertificateCheck();
 *   URLConnection connection = ...
 *   ...
 *   HttpsUtil.enableCertificateCheck();
 * </pre>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class HttpsUtil
{
  /** The default SSL socket factory that should normally apply. */
  private static final SSLSocketFactory defaultSocketFactory_ = HttpsURLConnection.getDefaultSSLSocketFactory();

  /** The non-checking SSL socket factory to use for HTTPS access against a trusted source. */
  private static final SSLSocketFactory noCheckSocketFactory_ = createNoCheckSocketFactory();

  /**
   * Private constructor to prevent client instantiation.
   */
  private HttpsUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Enable normal behavior, i.e certificate check on HTTP access.
   */
  public static void enableCertificateCheck()
  {
    HttpsURLConnection.setDefaultSSLSocketFactory(defaultSocketFactory_);
  }

  /**
   * Disable certificate checks on upcoming HTTPS accesses. The setting is valid
   * until enableCertificateCheck() is called.
   */
  public static void disableCertificateCheck()
  {
    if (noCheckSocketFactory_ != null)
      HttpsURLConnection.setDefaultSSLSocketFactory(noCheckSocketFactory_);
  }

  /**
   * Create a SLL socket factory that skips certificate checking.
   *
   * @return  The requested SSL socket factory. Null if not possible to create.
   */
  private static SSLSocketFactory createNoCheckSocketFactory()
  {
    TrustManager[] trustManagers = new TrustManager[] {
      new X509TrustManager() {

        /** {@inheritDoc} */
        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
          return null;
        }

        /** {@inheritDoc} */
        @Override
        public void checkClientTrusted(X509Certificate[] certificates, String authorizationType)
        {
          // Nothing
        }

        /** {@inheritDoc} */
        @Override
        public void checkServerTrusted(X509Certificate[] certificates, String authorizationType)
        {
          // Nothing
        }
      }
    };

    try {
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustManagers, new SecureRandom());
      return sslContext.getSocketFactory();
    }
    catch (NoSuchAlgorithmException exception) {
      assert false : exception;
      return null;
    }
    catch (KeyManagementException exception) {
      assert false : exception;
      return null;
    }
  }
}
