package no.geosoft.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for pinging a host.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Ping
{
  /**
   * Private constructor to prevent client instantiation.
   *
   * @return
   */
  private Ping() {
    assert false : "This constructor should never be called";
  }

  /**
   * Run ping using the JDK InetAddress feature. It is not safe on Windows
   * where ping2 below should be considered.
   *
   * @param ipAddress  IP address to ping. Non-null.
   * @param timeout    Timeout time in milliseconds. &gt;= 0.
   * @return           Roundtrip time on success, null on failure.
   * @throws IllegalArgumentException  If ipAddress is null or timeout &lt; 0.
   */
  public static Long ping(String ipAddress, long timeout)
  {
    if (ipAddress == null)
      throw new IllegalArgumentException("ipAddress cannot be null");

    if (timeout < 0)
      throw new IllegalArgumentException("Invalid timeout: " + timeout);

    long now = System.currentTimeMillis();

    try {
      boolean isReachable = InetAddress.getByName(ipAddress).isReachable((int) timeout);
      long roundtripTime = System.currentTimeMillis() - now;
      return isReachable ? roundtripTime : null;
    }
    catch (IOException exception) {
      return null;
    }
  }

  /**
   * Run ping as an external command.
   *
   * The following command is executed:
   *
   * <pre>
   *        ping -n 1 -w &lt;timeout&gt; &lt;ipAddress&gt;;
   * </pre>
   *
   * It will give the response on success:
   *
   * <pre>
   *        Pinging 194.248.60.129 with 100 bytes of data:
   *
   *        Reply from 194.248.60.129: bytes=100 time=11ms TTL=246
   *
   *        Ping statistics for 194.248.60.129:
   *            Packets: Sent = 1, Received = 1, Lost = 0 (0% loss),
   *        Approximate round trip times in milli-seconds:
   *            Minimum = 11ms, Maximum = 11ms, Average = 11ms
   * </pre>
   *
   * And failure:
   *
   * <pre>
   *        Pinging 194.248.60.128 with 100 bytes of data:
   *
   *        Request timed out.
   *
   *        Ping statistics for 194.248.60.128:
   *            Packets: Sent = 1, Received = 0, Lost = 1 (100% loss),
   * </pre>
   *
   * We capture the "Average = &lt;time&gt;ms" part and return as the roundtrip time.
   *
   * @param ipAddress  Ip address to ping. Non-null.
   * @param timeout    Timeout time in milliseconds. &gt;= 0.
   * @return           Roundtrip time on success, null on failur.
   * @throws IllegalArgumentException if ipAddress is null or timeout &lt; 0.
   */
  public static Long ping2(String ipAddress, long timeout)
  {
    if (ipAddress == null)
      throw new IllegalArgumentException("ipAddress cannot be null");

    if (timeout < 0)
      throw new IllegalArgumentException("Invalid timeout: " + timeout);

    String pingCommand = "ping -n 1 -w " + timeout + " " + ipAddress;

    StringBuilder s = new StringBuilder();
    Long roundtripTime = null;

    BufferedReader reader = null;
    try {
      Process process = Runtime.getRuntime().exec(pingCommand);

      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = reader.readLine();
      while (line != null) {
        s.append(line);
        line = reader.readLine();
      }
    }
    catch (IOException exception) {
      return null;
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException exception) {
          // Nothing we can do.
        }
      }
    }

    String response = s.toString().toLowerCase();

    Pattern p1 = Pattern.compile("average = \\d+ms");
    Matcher matcher = p1.matcher(response);
    if (matcher.find()) {
      Pattern p2 = Pattern.compile("\\d+");
      matcher = p2.matcher(matcher.group());
      matcher.find();
      String timeString = matcher.group();
      try {
        roundtripTime = Long.parseLong(timeString);
      }
      catch (NumberFormatException exception) {
        assert false : "programming error";
      }
    }

    return roundtripTime;
  }

  public static void main(String[] arguments)
  {
    System.out.println(Ping.ping2("66.249.64.2", 10000));
  }
}
