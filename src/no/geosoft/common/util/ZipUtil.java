package no.geosoft.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Collection of utilities for handling compression.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class ZipUtil
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private ZipUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * GZIP compress the specified byte array.
   *
   * @param bytes  Bytes to compress. Non-null.
   * @return       Compressed sequence. Never null.
   * @throws IllegalArgumentException  If bytes is null.
   */
  public static byte[] compress(byte[] bytes)
  {
    if (bytes == null)
      throw new IllegalArgumentException("bytes cannot be null");

    ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length);

    try {
      GZIPOutputStream gzip = new GZIPOutputStream(stream);
      gzip.write(bytes);
      gzip.close();
      byte[] compressedText = stream.toByteArray();
      return compressedText;
    }
    catch (IOException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    finally {
      try {
        stream.close();
      }
      catch (IOException exception) {
        assert false : "Programming error";
      }
    }
  }

  /**
   * GZIP compress the specified text
   *
   * @param text  Text to compress. Non-null.
   * @return      Compressed sequence. Never null.
   * @throws IllegalArgumentException  If text is null.
   */
  public static byte[] compress(String text)
  {
    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    try {
      return compress(text.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Programming error";
      return null;
    }
  }

  /**
   * Decompress the specified array according to the GZIP procedure.
   *
   * @param bytes  Bytes to decompress. Non-null.
   * @return       Decompressed version of bytes as a string. Never null.
   * @throws IllegalArgumentException  If bytes is null.
   */
  public static byte[] decompress(byte[] bytes)
  {
    if (bytes == null)
      throw new IllegalArgumentException("bytes cannot be null");

    if (bytes.length == 0)
      return new byte[0];

    GZIPInputStream stream = null;

    try {
      stream = new GZIPInputStream(new ByteArrayInputStream(bytes));
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      byte[] data = new byte[16384];
      while (true) {
        int nRead = stream.read(data, 0, data.length);
        if (nRead == -1)
          break;

        buffer.write(data, 0, nRead);
      }

      return buffer.toByteArray();
    }
    catch (IOException exception) {
      assert false : "Programming error " + exception.toString();
      return null;
    }
    finally {
      if (stream != null) {
        try {
          stream.close();
        }
        catch (IOException exception) {
          // Nothing
        }
      }
    }
  }
}
