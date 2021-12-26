package no.geosoft.common.util;

// http://images.google.com/images/q=Ulriken&tbm=isch

import java.awt.Image;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * Class for finding images through Google.
 * <p>
 * NOTE: Currently not working.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class GoogleImage
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(GoogleImage.class.getName());

  /** Base URL for the Google API image search. */
  private final static String baseUrl = "https://www.google.com/search?q=<QUERY>&num=10&tbm=isch&tbs=isz&imgsz=l";

  public static String getLargeUrl(String searchTerm)
  {
    if (searchTerm == null)
      throw new IllegalArgumentException("serachTerm cannot be null");

    String urlString = null;

    try {
      urlString = baseUrl.replace("<QUERY>", URLEncoder.encode(searchTerm, "utf-8"));

      URL searchUrl = new URL(urlString);

      java.net.URLConnection connection = searchUrl.openConnection();
      connection.addRequestProperty("User-Agent", "Mozilla/5.0");


      String all = no.geosoft.common.io.FileUtil.read(connection.getInputStream());

      // System.out.println(all);

      // all = "httpimgurl=http://geosoft.no/test.jpgBALLE.jpg";

      Pattern pattern = Pattern.compile("...jpg"); //(.+?)jpg");
      Matcher matcher = pattern.matcher(all);

      while (matcher.find()) {
        System.out.println(matcher.group(0));
      }





      /*

      Scanner scanner = new Scanner(connection.getInputStream());

      Pattern pattern = Pattern.compile("imgurl=(.*)jpg");

      String match = scanner.findWithinHorizon(pattern, 0);
      scanner.close();

      System.out.println("-- MATCH: " + match);

      if (match != null) {
        Matcher matcher = pattern.matcher(match);
        while (true) {
          boolean isFound = matcher.find();
          if (!isFound)
            break;

          if (matcher.groupCount() > 0) {
            System.out.println("-----#" + matcher.group(0) + "#-----");

            //urlString = matcher.group(1);
            //return urlString;
          }
        }
      }

      */

      return null;
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Programming error: " + exception;
    }
    catch (MalformedURLException exception) {
      logger_.log(Level.WARNING, "Invalid URL: " + urlString, exception);
      return null;
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to access URL: " + urlString, exception);
      return null;
    }

    return null;
  }

  /**
   * Return a large image for the specified search term.
   *
   * @param searchTerm  Serach term to get image for. Non-null.
   * @return            Requested image, or null if none found.
   * @throws IllegalArgumentException  If searchTerm is null.
   */
  public static Image getLarge(String searchTerm)
  {
    if (searchTerm == null)
      throw new IllegalArgumentException("serachTerm cannot be null");

    String urlString = null;

    try {
      urlString = getLargeUrl(searchTerm);

      if (urlString != null) {
        logger_.log(Level.INFO, "Image URL: " + urlString);

        URL imageUrl = new URL(urlString);
        return ImageIO.read(imageUrl);
      }

      return null;
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Programming error: " + exception;
    }
    catch (MalformedURLException exception) {
      logger_.log(Level.WARNING, "Invalid URL: " + urlString, exception);
      return null;
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to read image: " + urlString, exception);
      return null;
    }

    return null;
  }


  public static void main(String[] arguments)
  {
    javax.swing.JFrame f = new javax.swing.JFrame();
    f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    Image image = GoogleImage.getLarge("bergen");

    javax.swing.JLabel l = new javax.swing.JLabel();
    l.setIcon(new javax.swing.ImageIcon(image));
    f.getContentPane().add(l);

    f.pack();
    f.setVisible(true);
  }
}
