package no.geosoft.common.net;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

/**
 * A collection of useful TCP/IP <em>port</em> methods.
 * <p>
 * A computer <em>port</em> in TCP/IP terminology denotes the unique
 * identifier associated with a <em>service</em> running on that computer.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Port
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private Port()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return a port number that is not currently in use.
   *
   * @return  The requested port number, or -1 if the operation
   *          fails for some reason.
   */
  public static int get()
  {
    ServerSocket socket = null;

    try {
      socket = new ServerSocket(0);
      return socket.getLocalPort();
    }
    catch (IOException exception) {
      return 0;
    }
    finally {
      if (socket != null) {
        try {
          socket.close();
        }
        catch (IOException exception) {
          // Ignore. Nothing we can do anyway.
        }
      }
    }
  }

  /**
   * Check if the specified port number is available.
   *
   * @param portNumber  Port number to check.
   * @return            True if the port number is available, false otherwise,
   */
  public static boolean isAvailable(int portNumber)
  {
    Socket socket = null;

    try {
      socket = new Socket("localhost", portNumber);

      // If we get this far without an exception it means a service
      // is using the port and has responded
      return false;
    }
    catch (IOException exception) {
      return true;
    }
    finally {
      if (socket != null){
        try {
          socket.close();
        }
        catch (IOException exception) {
          // Ignore. Nothing we can do anyway.
        }
      }
    }
  }
}
