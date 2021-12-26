package no.geosoft.common.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * A collection of common encoding and cryptography methods.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Crypto
{
  /** Pseudo-protected key. */
  private static final byte[] keyValue = new byte[] {'T', 'h', 'i', 's', 'I', 's', 'A', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

  /**
   * Private constructor to prevent client instantiation.
   */
  private Crypto()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Create a SHA-512 hash code (checksum) from the specified string.
   * <p>
   * SHA-512 is irreversible, but it can be used to verify correctness
   * of passwords, etc.
   *
   * @param text  Text to get SHA-512 hash code from.
   * @return      Associated SHA-512 hash code. Never null.
   * @throws IllegalArgumentException  If text is null.
   */
  public static String getSha512Hash(String text)
  {
    if (text == null)
      throw new IllegalArgumentException("plainText cannot be null");

    try {
      MessageDigest md5 = MessageDigest.getInstance("SHA-512");
      md5.update(text.getBytes());
      byte[] hashCode = md5.digest();

      return (new BigInteger(1, hashCode)).toString(16);
    }
    catch (NoSuchAlgorithmException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
  }

  /**
   * Create a MD-5 hash code (checksum) from the specified string.
   * <p>
   * MD-5 is irreversible, but it can be used to verify correctness
   * of passwords, etc.
   *
   * @param text  Text to get MD5 hash code from.
   * @return      Associated MD5 hash code. Never null.
   * @throws IllegalArgumentException  If text is null.
   */
  public static String getMd5Hash(String text)
  {
    if (text == null)
      throw new IllegalArgumentException("plainText cannot be null");

    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(text.getBytes());
      byte[] hashCode = md5.digest();

      return (new BigInteger(1, hashCode)).toString(16);
    }
    catch (NoSuchAlgorithmException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
  }

  /**
   * Encode the specified plain text according to the Base64
   * algorithm.
   *
   * @param plainText  Plain text to encode. Non-null.
   * @return           Encoded text. Never null.
   * @throws IllegalArgumentException  If plainText is null.
   */
  public static String base64Encode(String plainText)
  {
    if (plainText == null)
      throw new IllegalArgumentException("plainText cannot be null");

    try {
      return Base64.getEncoder().encodeToString(plainText.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
  }

  /**
   * Decode the specified base64 encoded text.
   *
   * @param encodedText  Text to decode. Non-null.
   * @return             Decoded text. Never null.
   * @throws IllegalArgumentException  If encodedText is null.
   */
  public static String base64Decode(String encodedText)
  {
    if (encodedText == null)
      throw new IllegalArgumentException("encodedText cannot be null");

    try {
      return new String(Base64.getDecoder().decode(encodedText), "UTF-8");
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
  }

  /**
   * A reasonable safe test for checking if a string is base64 encoded.
   *
   * @param text  Text to check. Non-null.
   * @return      True if text appears to be base64 encoded, false otherwise.
   */
  public static boolean isBase64(String text)
  {
    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    if (text.isEmpty())
      return false;

    if (text.length() % 4 != 0)
      return false;

    if (text.contains(" ") || text.contains("\t") || text.contains("\r") || text.contains("\n"))
      return false;

    try {
      new String(Base64.getDecoder().decode(text), "UTF-8");
    }
    catch (Exception exception) {
      return false;
    }

    return true;
  }

  /**
   * Generate "secret" key.
   *
   * @return  Requested key. Never null.
   */
  private static Key generateKey()
  {
    return new SecretKeySpec(keyValue, "AES");
  }

  /**
   * Encrypt the specified plain text according to the AES algorithm
   * and using the member key of this class.
   *
   * @param plainText  Plain text to encrypt. Non-null.
   * @return           Encrypted text. Never null.
   * @throws IllegalArgumentException  If plainText is null.
   */
  public static String aesEncrypt(String plainText)
  {
    if (plainText == null)
      throw new IllegalArgumentException("plainText cannot be null");

    Key key = generateKey();

    try {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, key);

      byte[] b = cipher.doFinal(plainText.getBytes());

      return Base64.getEncoder().encodeToString(b);
    }
    catch (NoSuchAlgorithmException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (NoSuchPaddingException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (InvalidKeyException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (IllegalBlockSizeException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (BadPaddingException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
  }

  /**
   * Decrypt the specified AES encrypted text
   * using the member key of this class.
   *
   * @param encryptedText  Plain text to decrypt. Non-null.
   * @return               Decrypted text. Never null.
   * @throws IllegalArgumentException  If encryptedText is null.
   */
  public static String aesDecrypt(String encryptedText)
  {
    if (encryptedText == null)
      throw new IllegalArgumentException("encryptedText cannot be null");

    Key key = generateKey();

    try {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, key);

      byte[] b = Base64.getDecoder().decode(encryptedText);

      return new String(cipher.doFinal(b));
    }
    catch (NoSuchAlgorithmException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (NoSuchPaddingException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (InvalidKeyException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (IllegalBlockSizeException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
    catch (BadPaddingException exception) {
      assert false : "Programming error: " + exception;
      return null;
    }
  }

  /**
   * This program is for "encrypting" (not properly, but anyway) a license file so that
   * it can possibly be installed with Log Studio at the client site in case the client
   * experience problems with Log Studio accessing the Internet.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    try {
      // Point to the file that should be encrypted

      // Archer:
      //java.io.File file = new java.io.File("C:/Users/jacob/Development/dev/geosoft.no/LogStudio/Licenses/7f41ef8d97495a01b43dc1d8503b608d.license");

      // Equinor:
      java.io.File file = new java.io.File("C:/Users/jacob/Development/dev/geosoft.no/LogStudio/Licenses/3e3bdda643a2274ac0be568f003dfa4b.license");

      System.out.println("Encrypting file: " + file.getPath());
      String text = no.geosoft.common.io.FileUtil.read(file);
      String encryptedText = Crypto.aesEncrypt(text);

      // This is the encrypted version
      java.io.File encryptedFile = new java.io.File(file.getParentFile(), file.getName() + ".aes");
      System.out.println("Writing: " + encryptedFile.getPath());

      no.geosoft.common.io.FileUtil.write(encryptedFile, encryptedText);

      System.out.println("Done.");

      System.out.println("Remove the .aes extension and send it to the client");
      System.out.println("with instruction of storing it along LogStudio.exe.");
    }
    catch (java.io.IOException exception) {
      exception.printStackTrace();
    }
  }
}
