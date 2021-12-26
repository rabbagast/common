package no.geosoft.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A collection of I/O utilities.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class IoUtil
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private IoUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Serialize the specified object.
   *
   * @param object  Object to serialize. May be null.
   * @return        The byte serialized version of object.
   *                An empty array if object is null.
   */
  public static byte[] serialize(Serializable object)
  {
    if (object == null)
      return new byte[0];

    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    try {
      ObjectOutput objectOutput = new ObjectOutputStream(byteStream);
      objectOutput.writeObject(object);
      objectOutput.flush();

      return byteStream.toByteArray();
    }
    catch (IOException exception) {
      // Don't think this can happen
      return new byte[0];
    }
    finally {
      try {
        byteStream.close();
      }
      catch (IOException exception) {
        // Ignore close exception
      }
    }
  }

  /**
   * Deserialize the specified byte array.
   *
   * @param bytes  Bytes to deserialize. Non-null.
   * @return       The associated object. Null if bytes is empty or it
   *               cannot be deserialized.
   * @throws IllegalArgumentException  If bytes is null.
   */
  public static Object deserialize(byte[] bytes)
  {
    if (bytes == null)
      throw new IllegalArgumentException("bytes cannot be null");

    if (bytes.length == 0)
      return null;

    ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);

    try {
      ObjectInput objectInput = new ObjectInputStream(byteStream);

      return objectInput.readObject();
    }
    catch (ClassNotFoundException exception) {
      return null;
    }
    catch (IOException exception) {
      return null;
    }
    finally {
      try {
        byteStream.close();
      }
      catch (IOException exception) {
        // Ignore close exception
      }
    }
  }
}
