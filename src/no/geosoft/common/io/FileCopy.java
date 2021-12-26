package no.geosoft.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for copying disk files.
 * <p>
 * <b>Synchronization:</b> This class is thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class FileCopy
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(FileCopy.class.getName());

  /**
   * Due to a bug in FileChannel.transferTo() on Windows XP, chunks
   * larger than approx. 1.5GB cannot be transferred without throwing an
   * IOException. Performance of the file copy depends a lot on the chunk
   * size and experiments shows that the present value is the best compromise.
   */
  private static final long CHUNK_SIZE = 80000000L;  // ~80MB

  /**
   * Private constructor to prevent client instantiation.
   */
  private FileCopy()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Copy specified source to specified destination. Source and destination
   * may be files or directories.
   *
   * @param source       Entry to copy. Non-null.
   * @param destination  Where to copy to. Non-null.
   * @return             True if copy operation was successful, false otherwise.
   * @throws IllegalArgumentException  If source of destination is null.
   */
  public static boolean copy(File source, File destination)
  {
    if (source == null)
      throw new IllegalArgumentException("source cannot be null");

    if (destination == null)
      throw new IllegalArgumentException("destination cannot be null");

    List<File> newFiles = new ArrayList<>();
    return copy(source, destination, newFiles);
  }

  /**
   * Copy specified source to specified destination. Source and destination
   * may be files or directories. The method will return all the created
   * file entries in the provided file list.
   *
   * @param source       Entry to copy. Non-null.
   * @param destination  Where to copy to. Non-null.
   * @param newFiles     List where new entries will be added. Non-null
   *                     and initially empty. The list will include every
   *                     entry created, also sub entries if source is a
   *                     directory. In this case the first element will
   *                     corresponds to the source element itself.
   * @return             True if copy operation was successful, false otherwise.
   * @throws IllegalArgumentException  If source, destination or newFiles is null.
   */
  public static boolean copy(File source, File destination, List<File> newFiles)
  {
    if (source == null)
      throw new IllegalArgumentException("source cannot be null");

    if (destination == null)
      throw new IllegalArgumentException("destination cannot be null");

    if (newFiles == null)
      throw new IllegalArgumentException("newFiles cannot be null");

    //
    // Case 1: File -> Directory
    //
    if (source.isFile() && destination.isDirectory()) {
      File destinationFile = new File(destination, source.getName());
      return copy(source, destinationFile, newFiles);
    }

    //
    // Case 2: File -> File (possibly non-existent)
    //
    else if (source.isFile()) {
      try {
        fileCopyByExec(source, destination);
        newFiles.add(destination);
        return true;
      }
      catch (IOException exception) {
        logger_.log(Level.WARNING,
                    "Unable to copy: " + source + " to " + destination,
                    exception);
        return false;
      }
    }

    //
    // Case 3: Directory -> Directory (possibly non-existent)
    //
    else if (source.isDirectory() && !destination.isFile()) {
      File newDirectory = new File(destination, source.getName());
      boolean isCreated = newDirectory.mkdirs();
      if (isCreated)
        newFiles.add(newDirectory);

      boolean isOk = true;
      for (File file : source.listFiles()) {
        // Make sure we don't recurse into something already created
        if (newFiles.contains(file))
          continue;

        boolean isFileOk = copy(file, newDirectory, newFiles);

        if (!isFileOk)
          isOk = false;
      }
      return isOk;
    }

    //
    // Case 4 : Something else (unsupported)
    //
    else  {
      throw new IllegalArgumentException("Invalid copy operation: " +
                                         source + " to " + destination);
    }
  }

  /**
   * Copy content of one file to another.
   *
   * @param inputFile   File to copy from. Non-null.
   * @param outputFile  File to copy to. Non-null.
   * @throws IllegalArgumentException  If inputFile or outputFile is null or input File is not a file.
   * @throws IOException If the operation failed for some reason.
   */
  static void fileCopy(File inputFile, File outputFile)
    throws IOException
  {
    if (inputFile == null)
      throw new IllegalArgumentException("inputFile annot be null");

    if (outputFile == null)
      throw new IllegalArgumentException("outputFile cannot be null");

    if (!inputFile.isFile())
      throw new IllegalArgumentException("Invalid file: " + inputFile);

    // Considered done
    if (outputFile.equals(inputFile))
      return;

    FileChannel inputChannel = null;
    FileChannel outputChannel = null;

    try {
      // Create channel on the source
      inputChannel = new FileInputStream(inputFile).getChannel();
      outputChannel = new FileOutputStream(outputFile).getChannel();

      long position = 0;
      long nBytesLeft = inputChannel.size();

      while (nBytesLeft > 0) {
        long chunkSize = Math.min(nBytesLeft, CHUNK_SIZE);
        long nBytesTransferred = inputChannel.transferTo(position, chunkSize,
                                                         outputChannel);

        position += nBytesTransferred;
        nBytesLeft -= nBytesTransferred;
      }
    }
    finally {
      if (inputChannel != null)
        inputChannel.close();

      if (outputChannel != null)
        outputChannel.close();
    }
  }

  /**
   * Copy content of one file to another.
   *
   * @param inputFile   File to copy from. Non-null.
   * @param outputFile  File to copy to. Non-null.
   * @throws IllegalArgumentException  If inputFile or outputFile is null,
   *                                   or if inputFile is not a file.
   * @throws IOException If the operation failed for some reason.
   */
  static void fileCopyByExec(File inputFile, File outputFile)
    throws IOException
  {
    if (inputFile == null)
      throw new IllegalArgumentException("inputFile annot be null");

    if (outputFile == null)
      throw new IllegalArgumentException("outputFile cannot be null");

    if (!inputFile.isFile())
      throw new IllegalArgumentException("Invalid file: " + inputFile);

    // Considered done
    if (outputFile.equals(inputFile))
      return;

    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    String c1;
    String c2;
    String c3;
    if (isWindows) {
      c1 = "cmd.exe";
      c2 = "/C";
      c3 = "copy " +
           "\"" + inputFile.getPath() + "\" " +
           "\"" + outputFile.getPath() + "\"";
    }
    else {
      c1 = "/bin/sh";
      c2 = "-c";
      c3 = "cp " +
           "\"" + inputFile.getPath() + "\" " +
           "\"" + outputFile.getPath() + "\"";
    }

    String[] command = {c1, c2, c3};
    logger_.log(Level.INFO, "Execute external command: " + c1 + " " + c2 + " " + c3);

    try {
      Process process = Runtime.getRuntime().exec(command);
      int returnCode = process.waitFor();
      logger_.log(Level.INFO, "External command completed with return code " + returnCode + ".");
    }
    catch (InterruptedException exception) {
      logger_.log(Level.WARNING, "Unable to complete external command", exception);
    }
  }
}
