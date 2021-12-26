package no.geosoft.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A collection of file utilities.
 * <p>
 * <b>Synchronization:</b> This class is thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class FileUtil
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(FileUtil.class.getName());

  /**
   * Private constructor to prevent client instantiation.
   */
  private FileUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Check if the specified file is opened, possibly by a different
   * process and thereby locked for access in the present program.
   *
   * @param file  File to check. Non-null.
   * @return      True if the file is already open, false otherwise.
   *              If the file doesn't exist, false is returned.
   * @throws IllegalArgumentException  If file is null.
   */
  public static boolean isLocked(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (!file.exists())
      return false;

    try {
      new RandomAccessFile(file, "rw");
      return false;
    }
    catch (Exception exception) {
      return true;
    }
  }

  /**
   * Create a new file extension that follow the case of the
   * source extension:
   *
   * <pre>
   *   "asc" =&gt; "las"
   *   "ASC" =&gt; "LAS"
   *   "Asc" =&gt; "Las"
   *   etc.
   * </pre>
   *
   * @param ext1  The actual source extension. Non-null.
   * @param ext2  The any-case version of the result extension. Non-null.
   * @return      The ext2 with casing resembling the source extension.
   * @throws IllegalArgumentException  If ext1 or ext2 is null.
   */
  public static String getCaseAwareExtension(String ext1, String ext2)
  {
    if (ext1 == null)
      throw new IllegalArgumentException("ext1 cannot be null");

    if (ext2 == null)
      throw new IllegalArgumentException("ext2 cannot be null");

    // If ext1 is all upper case, return upper case
    if (ext1.equals(ext1.toUpperCase(Locale.US)))
      return ext2.toUpperCase(Locale.US);

    // If ext1 is all lower case, return lower case
    if (ext1.equals(ext1.toLowerCase(Locale.US)))
      return ext2.toLowerCase(Locale.US);

    // It is mixed case. We copy the case of the first and second
    // character, and leves the rest with the case of the second.
    boolean isFirstUpper = ext1.length() > 0 && Character.isUpperCase(ext1.charAt(0));
    boolean isSecondUpper = ext1.length() > 1 && Character.isUpperCase(ext1.charAt(1));

    StringBuilder s = new StringBuilder();
    for (int i = 0; i < ext2.length(); i++) {
      char cIn = ext2.charAt(i);
      char cOut = i == 0 && isFirstUpper || i > 0 && isSecondUpper ?
                  Character.toUpperCase(cIn) : Character.toLowerCase(cIn);
      s.append(cOut);
    }

    return s.toString();
  }

  /**
   * Create a new non-existent file instance in the specified directory
   * and base the name on the specified file name.
   *
   * @param folder     Directory of new file. Non-null.
   * @param fileName   Full file name to base the new name on. Non-null.
   * @param suffix     Suffix to append to base name. Non-null.
   * @param extension  Extension. Should not include ".". Null to use extension from source file name.
   * @return           The requested file instance. Never null.
   * @throws IllegalArgumentException  If folder, fileName or suffix is null.
   */
  public static File newFile(File folder, String fileName, String suffix, String extension)
  {
    if (folder == null)
      throw new IllegalArgumentException("folder cannot be null");

    if (fileName == null)
      throw new IllegalArgumentException("fileName cannot be null");

    if (suffix == null)
      throw new IllegalArgumentException("suffix cannot be null");

    String sourceFileName = fileName;

    String baseName = getBaseName(sourceFileName);
    if (baseName.endsWith(suffix))
      suffix = "";

    // If we pass in fileName-8, we want the new name to be fileName-9,
    // not fileName-8-1, so we strip off the current counter.
    for (int i = 0; i < 100; i++) {
      if (baseName.endsWith("-" + i)) {
        baseName = baseName.substring(0, baseName.lastIndexOf("-"));
        break;
      }
    }

    String sourceExtension = getExtension(sourceFileName);
    String destinationExtension = extension != null ?
                                  getCaseAwareExtension(sourceExtension, extension) :
                                  sourceExtension;

    //
    // Loop until we find a non-existent file
    //
    for (int i = 0; true; i++) {

      String counter = i == 0 ? "" : "-" + i;

      // Build the file name
      StringBuilder s = new StringBuilder();
      s.append(baseName);
      s.append(suffix);
      s.append(counter);
      s.append(".");
      s.append(destinationExtension);

      String destinationFileName = s.toString();

      File destinationFile = new File(folder, destinationFileName);
      if (!destinationFile.exists())
        return destinationFile;
    }
  }

  /**
   * Create a new non-existent file instance in the same folder as the
   * source file, and with the following properties:
   *
   * <ol>
   *   <li>Base file name identical
   *   <li>Specified (optional) suffix appended to the base name
   *   <li>Counter added to ensure uniqueness
   *   <li>New extension as specified
   *   <li>Extension follows case of the source file.
   * </ol>
   *
   * Examples:
   *
   * <pre>
   *   &lt;name&gt;.&lt;ext&gt; =&gt; &lt;name&gt;&lt;suffix&gt;[-n].&lt;extension&gt;
   *   test.ASC =&gt; test_Converted-1.LAS
   *   input.las =&gt; input_conv.las
   * </pre>
   *
   * @param sourceFile  The source file. Non-null.
   * @param suffix      Optional suffix. "" if N/A. Non-null.
   * @param extension   Extension of new file. Null to use same as source.
   *                    Do not include dot character.
   * @return            The requested file instance. Never null.
   * @throws IllegalArgumentException  If sourceFile or suffix is null.
   */
  public static File newFile(File sourceFile, String suffix, String extension)
  {
    if (sourceFile == null)
      throw new IllegalArgumentException("suffix cannot be null");

    if (suffix == null)
      throw new IllegalArgumentException("suffix cannot be null");

    File folder = sourceFile.getParentFile();
    return newFile(folder, sourceFile.getName(), suffix, extension);
  }

  /**
   * For the specified file name, ensure that it has the specified extension
   * or otherwise add this extension.
   *
   * @param fileName         File name to consider. Non-null.
   * @param extension        Extension to ensure. Without dot character. Non-null.
   * @param isCaseSensitive  Indicate if the check on the file name should be case
   *                         sensitive or not.
   * @return                 A file name which has the specified extension for sure.
   *                         Never null.
   * @throws IllegalArgumentException  If fileName or extension is null.
   */
  public static String ensureExtension(String fileName, String extension, boolean isCaseSensitive)
  {
    if (fileName == null)
      throw new IllegalArgumentException("fileName cannot be null");

    if (extension == null)
      throw new IllegalArgumentException("extension cannot be null");

    // Strip off initial dot from the extension if it is there
    if (extension.startsWith("."))
      extension = extension.substring(1);

    //
    // Check if the file name is OK already
    //
    if (isCaseSensitive) {
      if (fileName.toLowerCase().endsWith(extension.toLowerCase()))
        return fileName;
    }
    else {
      if (fileName.endsWith(extension))
        return fileName;
    }

    // Add extension
    return fileName + "." + extension;
  }

  /**
   * Return the creation time for the specified file.
   *
   * @param file  File to get creation time for. Non-null.
   * @return      Creation time for the file, or null if this cannot be obtained.
   * @throws IllegalArgumentException  If file is null.
   */
  public static Date getCreationTime(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    try {
      BasicFileAttributes fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
      long creationTime = fileAttributes.creationTime().to(TimeUnit.MILLISECONDS);
      return new Date(creationTime);
    }
    catch (IOException exception) {
      // Creation time is not known, or not supported by the OS
      return null;
    }
  }

  /**
   * Return the base name of a file name.
   *
   * <pre>
   *   "fileName.ext" =&gt; "fileName"
   *   "fileName.a.b.c.d" =&gt; "fileName.a.b.c"
   *   "fileName" =&gt; "fileName"
   *   ".ext" =&gt; ""
   * </pre>
   *
   * @param fileName  File name to find base name of. Non-null.
   * @return          Requested base name. Never null.
   * @throws IllegalArgumentException  If fileName is null.
   */
  public static String getBaseName(String fileName)
  {
    if (fileName == null)
      throw new IllegalArgumentException("fileName cannot be null");

    int pos = fileName.lastIndexOf('.');
    return pos != -1 ? fileName.substring(0, pos) : fileName;
  }

  /**
   * Return the base name of a file name.
   *
   * <pre>
   *   "fileName.ext" =&gt; "fileName"
   *   "fileName.a.b.c.d" =&gt; "fileName.a.b.c"
   *   "fileName" =&gt; "fileName"
   *   ".ext" =&gt; ""
   * </pre>
   *
   * @param file  File to find base name of. Non-null.
   * @return      Requested base name. Never null.
   * @throws IllegalArgumentException  If file is null.
   */
  public static String getBaseName(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    return getBaseName(file.getName());
  }

  /**
   * Return the extension of a file name.
   *
   * <pre>
   *   "fileName.ext" =&gt; "ext"
   *   "fileName.a.b.c.d" =&gt; "d"
   *   "fileName" =&gt; ""
   *   ".ext" =&gt; "ext"
   * </pre>
   *
   * @param fileName  File name to return extension of. Non-null.
   * @return          Extension of the specified file name. Never null.
   * @throws IllegalArgumentException  If fileName is null.
   */
  public static String getExtension(String fileName)
  {
    if (fileName == null)
      throw new IllegalArgumentException("fileName cannot be null");

    int pos = fileName.lastIndexOf('.');
    return pos != -1 ? fileName.substring(pos + 1) : "";
  }

  /**
   * Return the extension of a file name.
   *
   * <pre>
   *   "fileName.ext" =&gt; "ext"
   *   "fileName.a.b.c.d" =&gt; "d"
   *   "fileName" =&gt; ""
   *   ".ext" =&gt; "ext"
   * </pre>
   *
   * @param file  File to return extension of. Non-null.
   * @return      Extension of the specified file. Never null.
   * @throws IllegalArgumentException  If file is null.
   */
  public static String getExtension(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    return getExtension(file.getName());
  }

  /**
   * Clear file content, but keep the file.
   * If the file doesn't exists, calling this method has no effect.
   *
   * @param file  File to clear. Non-null.
   * @throws IllegalArgumentException  If file is null.
   * @throws IOException  If the clear operations failed for some reason.
   */
  public static void clear(File file)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (!file.exists() || file.length() == 0L)
      return;

    PrintWriter printWriter = new PrintWriter(file);
    printWriter.close();
  }

  /**
   * Read a number of bytes from the start of the specified file.
   *
   * @param file    File to read. Non-null.
   * @param nBytes  Number of bytes to read, or 0 for all. [0,&gt;.
   * @return        The first (maximum) nBytes of the file, or null
   *                if reading is not possible.
   * @throws IllegalArgumentException  If file is null or nBytes &lt; 0.
   */
  public static byte[] read(File file, long nBytes)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (nBytes < 0)
      throw new IllegalArgumentException("Invalid nBytes: " + nBytes);

    if (nBytes == 0)
      nBytes = file.length();

    nBytes = Math.min(file.length(), nBytes);

    // Prepare return buffer
    byte[] content = new byte[(int) nBytes];

    // Read and return the requested number of bytes
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(file));
      stream.read(content, 0, content.length);
      return content;
    }
    catch (IOException exception) {
      return null;
    }
    finally {
      if (stream != null) {
        try {
          stream.close();
        }
        catch (IOException exception) {
          // Ignore.
        }
      }
    }
  }

  /**
   * <em>Touch</em> (i.e. create empty if non-existent or update time stamp of)
   * the specified file.
   *
   * @param file  File to touch. Non-null.
   * @throws IllegalArgumentException  If file is null.
   * @throws IOException  If the touch operation fails for some reason.
   */
  public static void touch(File file)
    throws IOException
  {
    if (!file.exists())
      (new FileOutputStream(file)).close();
    else
      file.setLastModified(System.currentTimeMillis());
  }

  /**
   * Convert the specified name to something that can be used as a file name.
   * <p>
   * NOTE: This topic is a <em>science</em>. There are many aspects to consider,
   * if the file name is readable, whether the process should be reversible, and
   * how to avoid collisions. There are many solutions available, but as none
   * seems universal, we start off with the simplest possible. It doesn't solve
   * the simple cases of empty string or "." or "..", but anyway.
   *
   * @param name  Name to convert. Non-null.
   * @return      Associated name that legally can be used as a file name. Never null.
   * @throws IllegalArgumentException  If name is null.
   */
  public static String getLegalFileName(String name)
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    return name.replaceAll("[:\\\\/*?|<>]", "_");
  }

  /**
   * Delete a file or (possibly non-empty) directory.
   *
   * @param file  File to delete. Non-null.
   * @throws IllegalArgumentException  If file is null.
   */
  public static void delete(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (file.isDirectory()) {
      for (File f : file.listFiles())
        delete(f);
    }

    Path path = file.toPath();
    try {
      Files.deleteIfExists(path);
      logger_.log(Level.INFO, "Deleted " + file + " OK.");
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to delete " + file, exception);
    }
  }

  /**
   * Check if a certain file <em>appears</em> to be a binary file.
   *
   * @param file  File to check. Non-null.
   * @return      True if the file seems to be binary, false if it is readable text.
   * @throws IllegalArgumentException  If file is null or if it is not a legal file.
   * @throws IOException  If accessing the file fails for some reason.
   */
  public static boolean isBinary(File file)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (!file.isFile())
      throw new IllegalArgumentException("Not a file: " + file);

    Reader reader = null;

    try {
      reader = new BufferedReader(new FileReader(file));
      char[] buffer = new char[4096];
      int n = reader.read(buffer);

      for (int i = 0; i < n; i++) {
        int c = buffer[i];

        boolean isAscii = c >= 32 && c <= 127 ||
                          c == (int) '\n' ||
                          c == (int) '\r' ||
                          c == (int) '\t';

        if (!isAscii)
          return true;
      }
    }
    finally {
      if (reader != null)
        reader.close();
    }

    return false;
  }

  /**
   * Check if a file is PDF.
   *
   * @param file  File to check. Non-null.
   * @return      True if the specified file is PDF, false otherwise.
   * @throws IllegalArgumentException  If file is null.
   */
  public static boolean isPdf(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    String fileName = file.getName().toLowerCase();
    return fileName.endsWith(".pdf");
  }

  /**
   * Write the specified text to the given file.
   *
   * @param file  File to write to. Non-null.
   * @param text  Text to write. Non-null.
   * @throws IllegalArgumentException  If file or text is null.
   * @throws IOException  If the write operation fails for some reason.
   */
  public static void write(File file, String text)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    FileOutputStream stream = new FileOutputStream(file);

    PrintWriter writer = null;

    try {
      writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"), true);
      writer.print(text);
    }
    catch (UnsupportedEncodingException exception) {
      assert false : "Unsupported encoding: UTF-8";
    }
    finally {
      if (writer != null)
        writer.close();
    }
  }

  /**
   * Write the content of the specified URL to the given file.
   *
   * @param url   URL to read. Non-null.
   * @param file  File to write. Non-null.
   * @throws IllegalArgumentException  If url or file is null.
   * @throws IOException  If the operation fails for some reason.
   */
  public static void write(URL url, File file)
    throws IOException
  {
    if (url == null)
      throw new IllegalArgumentException("url cannot be null");

    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    InputStream inputStream = url.openStream();
    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    inputStream.close();
  }

  /**
   * Rename the specified file <em>in place</em>.
   *
   * @param file     File to rename. Non-null.
   * @param newName  New name. Non-null.
   * @return         True if the operation was successful, false otherwise,
   * @throws IllegalArgumentException  If file or newName is null.
   */
  public static boolean rename(File file, String newName)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (newName == null)
      throw new IllegalArgumentException("newName cannot be null");

    File newFile = new File(file.getParentFile(), newName);

    if (newFile.exists())
      return false;

    return file.renameTo(newFile);
  }

  /**
   * Write the specified text to a temporary file.
   * The file will be deleted when the program exits.
   *
   * @param fileExtension  Extension (including dot if needed) to use for file name. Non-null.
   * @param text           Text to write. Non-null.
   * @return               The file instance. Never null.
   * @throws IllegalArgumentException  If fileExtension or text is null.
   * @throws IOException  If the write operation failed for some reason.
   */
  public static File writeToTempFile(String fileExtension, String text)
    throws IOException
  {
    if (fileExtension == null)
      throw new IllegalArgumentException("fileExtension cannot be null");

    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    File file = File.createTempFile("temp", fileExtension);
    file.deleteOnExit();

    FileUtil.write(file, text);

    return file;
  }

  /**
   * Write the specified text to a temporary text file.
   * The file will be deleted when the program exits.
   *
   * @param text  Text to write. Non-null.
   * @return      The file instance. Never null.
   * @throws IllegalArgumentException  If text is null.
   * @throws IOException  If the write operation failed for some reason.
   */
  public static File writeToTempFile(String text)
    throws IOException
  {
    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    File file = File.createTempFile("temp", ".txt");
    file.deleteOnExit();

    FileUtil.write(file, text);

    return file;
  }

  /**
   * Return the content of a stream as a string.
   *
   * @param stream  Stream to read. Non-null.
   * @return        Content of file as a string. Never null.
   * @throws IllegalArgumentException  If stream is null.
   * @throws IOException  If the operation failed for some reason.
   */
  public static String read(InputStream stream)
    throws IOException
  {
    if (stream == null)
      throw new IllegalArgumentException("stream cannot be null");

    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

    StringBuilder s = new StringBuilder();

    boolean isFirst = true;
    String line;

    try {
      while ((line = reader.readLine()) != null) {
        if (!isFirst)
          s.append("\n");

        s.append(line);
        isFirst =  false;
      }
    }
    finally {
      reader.close();
    }

    return s.toString();
  }

  /**
   * Count number of lines in the specified file.
   *
   * @param file  File to count lines of. Non-null.
   * @return      Number of lines in the file. [0,&gt;.
   * @throws IllegalArgumentException  If file is null.
   * @throws IOException If the operation failed for some reason.
   */
  public static int getNLines(File file)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    // Special case if the file is empty
    if (file.length() == 0L)
      return 0;

    LineNumberReader lineNumberReader = null;

    try {
      lineNumberReader = new LineNumberReader(new FileReader(file));
      lineNumberReader.skip(Long.MAX_VALUE);
      return lineNumberReader.getLineNumber() + 1; // As numbering starts at 0
    }
    finally {
      if (lineNumberReader != null)
        lineNumberReader.close();
    }
  }

  /**
   * Return the content of a file as a string.
   *
   * @param file  File to read. Non-null.
   * @return      Content of file as a string. Never null.
   * @throws IllegalArgumentException  If file is null.
   * @throws IOException  If the operation failed for some reason.
   */
  public static String read(File file)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    return read(new FileInputStream(file));
  }

  /**
   * Return the content of a URL as a string.
   *
   * @param url  URL to read. Non-null.
   * @return     Content of file as a string. Never null.
   * @throws IllegalArgumentException  If url is null.
   * @throws IOException  If the operation failed for some reason.
   */
  public static String read(URL url)
    throws IOException
  {
    if (url == null)
      throw new IllegalArgumentException("url cannot be null");

    return read(url.openStream());
  }

  /**
   * Read the n'th line of the specified file and return
   * it as a string
   *
   * @param file        File to read from. Non-null.
   * @param lineNumber  Line number of line to read. [0,&gt;.
   * @return            Line read. Null if the line doesn't exists.
   * @throws IllegalArgumentException  If file is null or lineNumber is out of bounds.
   * @throws IOException  If the read operation fails for some reason.
   */
  public static String readLine(File file, int lineNumber)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (lineNumber < 0)
      throw new IllegalArgumentException("Invalid lineNumber: " + lineNumber);

    BufferedReader reader = null;

    try {
      reader = new BufferedReader(new FileReader(file));

      String line = null;
      for (int i = 0; i <= lineNumber; i++) {
        line = reader.readLine();
        if (line == null)
          break;
      }

      return line;
    }
    finally {
      if (reader != null)
        reader.close();
    }
  }

  /**
   * Read the last line of the specified file.
   * <p>
   * Based on:
   * http://nunobrito1981.blogspot.no/2014/11/java-reading-last-line-on-large-text.html
   *
   * @param file  File to read. Non-null.
   * @return      Last line of the file. Null if the file is empty.
   * @throws IllegalArgumentException  If file is null.
   * @throws IOException               If the read operation fails for some reason.
   */
  public static String readLastLine(File file)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    final char NEW_LINE = '\n';

    RandomAccessFile randomAccessFile = null;

    try {
      randomAccessFile = new RandomAccessFile(file, "r");

      long blockStart = (file.length() - 1) / 4096 * 4096;

      byte[] currentBlock = new byte[(int) (file.length() - blockStart)];

      // Later (previously read) blocks
      List<byte[]> laterBlocks = new ArrayList<>();

      while (blockStart >= 0) {
        randomAccessFile.seek(blockStart);
        randomAccessFile.readFully(currentBlock);

        // Ignore the last 2 bytes of the block if it is the first one
        int lengthToScan = currentBlock.length - (laterBlocks.isEmpty() ? 2 : 0);
        for (int i = lengthToScan - 1; i >= 0; i--) {

          // We found our end of line. Create the last line.
          if (currentBlock[i] == NEW_LINE) {
            StringBuilder result = new StringBuilder();

            // RandomAccessFile#readLine uses ISO-8859-1, therefore we do here too
            result.append(new String(currentBlock, i + 1, currentBlock.length - (i + 1), "ISO-8859-1"));
            for (byte[] laterBlock : laterBlocks)
              result.append(new String(laterBlock, "ISO-8859-1"));

            // Maybe we had a newline at end of file? Strip it.
            if (result.charAt(result.length() - 1) == NEW_LINE) {
              // newline can be \r\n or \n, so check which one to strip
              int newlineLength = result.charAt(result.length() - 2) == '\r' ? 2 : 1;
              result.setLength(result.length() - newlineLength);
            }

            return result.toString();
          }
        }

        // No end of line found - we need to read more
        laterBlocks.add(0, currentBlock);
        blockStart -= 4096;
        currentBlock = new byte[4096];
      }
    }
    finally {
      if (randomAccessFile != null)
        randomAccessFile.close();
    }

    return null;
  }

  /**
   * Return a Microsoft UNC path for the specified file.
   * <p>
   * Note that UNC is not defined for removable disks.
   * Note that this method uses a Windows specific 3rd-party
   * command ("net use") in order to construct the UNC path.
   *
   * @param file  File to get UNC path of. Non-null.
   * @return      Requested UNC path. If it cannot be created, the
   *              file absolute path is returned instead.
   * @throws IllegalArgumentException  If file is null.
   */
  public static String toUnc(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    String hostName = "localhost";

    try {
      hostName = java.net.InetAddress.getLocalHost().getHostName();
    }
    catch (Exception exception) {
      // Nothing
    }

    String filePath = file.getAbsolutePath();

    Runtime runTime = Runtime.getRuntime();

    try {
      Process process = runTime.exec("net use");
      InputStream inStream = process.getInputStream();
      InputStreamReader inputStreamReader = new InputStreamReader(inStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

      String line = null;
      String[] components = null;
      while (null != (line = bufferedReader.readLine())) {
        components = line.split("\\s+");
        if ((components.length > 2) && (components[1].equals(filePath.substring(0, 2))))
          return filePath.replace(components[1], components[2]);
      }
    }
    catch (IOException exception) {
      return filePath;
    }

    if (filePath.startsWith("C:"))
      filePath = "\\\\" + hostName + "\\C$" + filePath.substring(2);

    return filePath;
  }

  /**
   * ZIP (combine and compress) the specified input files to the given
   * output file.
   *
   * @param zipFile  ZIP file to create. Non-null.
   * @param files    Files to ZIP. Non-null.
   * @throws IllegalArgumentException  If zipFile or files are null.
   * @throws IOException  If the ZIP operation fails for some reason.
   */
  public static void zip(File zipFile, List<File> files)
    throws IOException
  {
    if (zipFile == null)
      throw new IllegalArgumentException("zipFile cannot be null");

    if (files == null)
      throw new IllegalArgumentException("files cannot be null");

    //
    // Create the ZIP output file
    //
    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

    //
    // Process each input file in turn
    //
    for (File file : files) {

      // Add ZIP entry to the output
      ZipEntry zipEntry = new ZipEntry(file.getName());
      zipOutputStream.putNextEntry(zipEntry);

      // Read the entire input file and put content into ZIP
      FileInputStream inputStream = new FileInputStream(file);
      byte[] bytes = new byte[1024];
      int nBytes = inputStream.read(bytes);
      while (nBytes >= 0) {
        zipOutputStream.write(bytes, 0, nBytes);
        nBytes = inputStream.read(bytes);
      }
      inputStream.close();
    }

    zipOutputStream.close();
  }
}
