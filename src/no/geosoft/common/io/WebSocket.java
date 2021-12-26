package no.geosoft.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.Properties;
import java.util.Map.Entry;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * Class representing a web socket a client can communicate through.
 * <p>
 * Usage:
 *    <pre>
 *    WebSocket webSocket = new WebSocket(url);
 *
 *    webSocket.send(request);
 *    String response = webSocket.receive();
 *    </pre>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public class WebSocket
{
  /** The back-end socket. */
  private final Socket socket_;

  /**
   * Creates a new WebSocket targeting the specified URL.
   *
   * @param url The URL for the socket.
   * @param headerProperties  Optional properties that should go
   *        into the hand-shaking request. Null for none.
   * @throws IllegalArgumentException  If url is null.
   * @throws IOException  If the socket cannot be created for some reason.
   */
  public WebSocket(URI url, Properties headerProperties)
    throws IOException
  {
    if (url == null)
      throw new IllegalArgumentException("url cannot be null");

    String scheme = url.getScheme();
    if (!scheme.equals("ws") && !scheme.equals("wss"))
      throw new IllegalArgumentException("Unsupported protocol: " + scheme);

    socket_ = createSocket(url);

    connect(url, headerProperties);
  }

  /**
   * Return port number of the specified URL.
   *
   * @param url  URL to get port number of. Non-null.
   * @return     Requested port number.
   */
  private static int getPort(URI url)
  {
    assert url != null : "url cannot be null";

    int port = url.getPort();

    if (port == -1) {
      String scheme = url.getScheme();
      if (scheme.equals("wss"))
        port = 443;
      else
        port = 80;
    }

    return port;
  }

  /**
   * Create a web socket.
   *
   * @param url  URL for socket destination. Non-null.
   * @return     The created URL. Never null.
   * @throws IOException  If the creation fails for some reason.
   */
  private static Socket createSocket(URI url)
    throws IOException
  {
    assert url != null : "url cannot be null";

    String scheme = url.getScheme();
    String host = url.getHost();
    int port = getPort(url);

    if (scheme.equals("wss")) {
      SocketFactory factory = SSLSocketFactory.getDefault();
      return factory.createSocket(host, port);
    }
    else {
      return new Socket(host, port);
    }
  }

  /**
   * Perform a hand-shaking with the server.
   *
   * @param url  Address to the server. Non-null.
   * @param headerProperties  Optional properties that should go
   *        into the hand-shaking request. Null for none.
   * @throws IOException  If the hand-shake fails for some reason.
   */
  private void connect(URI url, Properties headerProperties)
    throws IOException
  {
    //
    // Create handshake request
    //
    String host = url.getHost() + ":" + getPort(url);

    String origin = "http://" + url.getHost();

    String path = url.getPath();
    if (path == null || path.trim().isEmpty())
      path = "/";
    String query = url.getQuery();
    if (query != null)
      path = path + "?" + query;

    StringBuilder arguments = new StringBuilder();
    if (headerProperties != null) {
      for (Entry<Object, Object> entry : headerProperties.entrySet())
        arguments.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
    }

    String request = "GET " + path + " HTTP/1.1\r\n" +
                     "Upgrade: WebSocket\r\n" +
                     "Connection: Upgrade\r\n" +
                     "Host: " + host + "\r\n" +
                     "Origin: " + origin + "\r\n" +
                     arguments.toString() + "\r\n";

    System.out.println("REQUEST: ");
    System.out.println(request);

    //
    // Send handshake request
    //
    OutputStream outputStream = socket_.getOutputStream();
    outputStream.write(request.getBytes());
    outputStream.flush();

    //
    // Receive handshake response
    //
    InputStream inputStream = socket_.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    StringBuilder response = new StringBuilder();
    String line;
    do {
      line = reader.readLine();
      response.append(line + "\n");
    } while (!line.isEmpty());

    /*

      String header = reader.readLine();

      if (!header.equals("HTTP/1.1 101 Web Socket Protocol Handshake"))
      throw new IOException("Invalid handshake response");

      header = reader.readLine();
      if (!header.equals("Upgrade: WebSocket"))
      throw new IOException("Invalid handshake response");

      header = reader.readLine();
      if (!header.equals("Connection: Upgrade"))
      throw new IOException("Invalid handshake response");

    */

    System.out.println("RESPONSE: ");
    System.out.println(response);
  }

  /**
   * Sends the specified request to the server.
   *
   * @param request  The request string to send. Non-null.
   * @throws IllegalArgumentException  If string is null.
   * @throws IOException  If the sending fails for some reason.
   */
  public void send(String request)
    throws IOException
  {
    if (request == null)
      throw new IllegalArgumentException("request cannot be null");

    OutputStream outputStream = socket_.getOutputStream();
    outputStream.write(0x00);
    outputStream.write(request.getBytes("UTF-8"));
    outputStream.write(0xff);
    outputStream.flush();
  }

  /**
   * Receive response from the server.
   *
   * @return The received data.
   * @throws IOException  If the receive failed for some reason.
   */
  public String receive()
    throws IOException
  {
    InputStream inputStream = socket_.getInputStream();

    StringBuilder s = new StringBuilder();

    int b = inputStream.read();
    if ((b & 0x80) == 0x80) {
      // Skip data frame
      int len = 0;
      do {
        b = inputStream.read() & 0x7f;
        len = len * 128 + b;
      } while ((b & 0x80) != 0x80);

      for (int i = 0; i < len; i++) {
        inputStream.read();
      }
    }

    // Read the message
    while (true) {
      b = inputStream.read();
      if (b == 0xff)
        break;

      s.append((char) b);
    }

    return new String(s.toString().getBytes(), "UTF8");
  }

  /**
   * Clos this socket.
   *
   * @throws IOException  If the close operation fails for some reason.
   */
  public void close()
    throws IOException
  {
    socket_.close();
  }

  /**
   * Testing this class.
   *
   * @param args  Not used.
   */
  public static void main(String[] args)
  {
    try {
      WebSocket webSocket = new WebSocket(new URI("ws://echo.websocket.org"), null);

      //WebSocket webSocket = new WebSocket(new URI("ws://52.47.205.204:8025"), null);

      String request = "Balle Klorin";
      System.out.println("Send: " + request);
      webSocket.send(request);

      String response = webSocket.receive();
      System.out.println("Receive: " + request);

      System.out.println("Done.");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
