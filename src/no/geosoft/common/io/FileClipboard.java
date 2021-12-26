package no.geosoft.common.io;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for putting and retrieving files on the system clipboard.
 * <p>
 * This class is thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class FileClipboard
  implements ClipboardOwner
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(FileClipboard.class.getName());

  /** The sole instance of this class. */
  private static final FileClipboard instance_ = new FileClipboard();

  /**
   * Private constructor to prevent client instantiation.
   */
  private FileClipboard()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return the sole instance of this class.
   *
   * @return  The sole instance of this class. Never null.
   */
  public static FileClipboard getInstance()
  {
    return instance_;
  }

  /**
   * Return all the files (java.io.File which also includes folders)
   * from the system clipboard.
   *
   * @return  All files from the system clipboard. The list may be empty, but
   *          is never null.
   */
  @SuppressWarnings("unchecked")
  private static Collection<File> getFiles()
  {
    // Create a default empty list
    List<File> files = new ArrayList<>();

    // Get system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    // Get list of java.util.File entries on the clipboard
    try {
      files = (List<File>) clipboard.getData(DataFlavor.javaFileListFlavor);
    }
    catch (UnsupportedFlavorException exception) {
      logger_.log(Level.INFO, "The system clipboard doesn't support files");
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to get files from clipboard",
                  exception);
    }

    return files;
  }

  /**
   * Return the number of files on the clipboard.
   *
   * @return  The number of files on the clipboard. [0,&gt;.
   */
  public static int getNFiles()
  {
    return getFiles().size();
  }

  /**
   * Check if the file clipboard is empty, i.e. contain no java.io.File
   * instances.
   *
   * @return True if the clipboard is empty, false otherwise.
   */
  public static boolean isEmpty()
  {
    return getNFiles() == 0;
  }

  /**
   * "Copy" the specified files by placing them on the clipboard.
   * The current clipboard content will be removed.
   *
   * @param files  Files to copy. Non-null.
   * @throws IllegalArgumentException  If files is null.
   */
  public void copy(Collection<File> files)
  {
    if (files == null)
      throw new IllegalArgumentException("files cannot be null");

    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable transferable = new FileTransferable(files);
    clipboard.setContents(transferable, this);

    logger_.info(files.size() + " file(s) put on clipboard.");
  }

  /**
   * "Copy" the specified file by placing it on the clipboard.
   * The current clipboard content will be removed.
   *
   * @param file  File to copy. Non-null.
   * @throws IllegalArgumentException  If file is null.
   */
  public void copy(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    Collection<File> files = new ArrayList<>();
    files.add(file);

    copy(files);
  }

  /**
   * "Cut" the specified files by making a temporary copy and putting
   * the copy on the clipboard.
   * <p>
   * NOTE: The actual delete part of the cut operation must be done by
   * the client, typically just after calling this method.
   *
   * @param files  Files to cut. Non-null.
   * @throws IllegalArgumentException  If files is null.
   * @throws IOException  If making a temporary file copy failed somehow.
   */
  public void cut(Collection<File> files)
    throws IOException
  {
    if (files == null)
      throw new IllegalArgumentException("files cannot be null");

    // Make a dummy temporary file in order to find the temporary directory
    File dummy = File.createTempFile("cut", "tmp");
    dummy.deleteOnExit();
    File tempFolder = dummy.getParentFile();

    // Loop over the files. As they soon will be deleted by the client,
    // we copy them to a temporary area.
    Collection<File> tempFiles = new ArrayList<>();
    for (File file : files) {
      List<File> newFiles = new ArrayList<>();
      FileCopy.copy(file, tempFolder, newFiles);
      tempFiles.add(newFiles.get(0));
    }

    // Put the temporary files on the clipboard
    copy(tempFiles);
  }

  /**
   * "Cut" the specified file by making a temporary copy and putting
   * the copy on the clipboard.
   * <p>
   * NOTE: The actual delete part of the cut operation must be done by
   * the client, typically just after calling this method.
   *
   * @param file  File to cut. Non-null.
   * @throws IllegalArgumentException  If file is null.
   * @throws IOException  If making a temporary file copy failed somehow.
   */
  public void cut(File file)
    throws IOException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    Collection<File> files = new ArrayList<>();
    files.add(file);

    cut(files);
  }

  /**
   * Paste the present clipboard contents into the specified destination folder.
   *
   * @param destinationFolder  Folder to paste into. Non-null.
   * @return                   List of new files created. Never null.
   * @throws IllegalArgumentException  If destinationFolder is null or not a folder.
   */
  public static Collection<File> paste(File destinationFolder)
  {
    if (destinationFolder == null)
      throw new IllegalArgumentException("destination cannot be null");

    if (!destinationFolder.isDirectory())
      throw new IllegalArgumentException("Invalid folder: " + destinationFolder);

    // Prepare return structure
    List<File> newFiles = new ArrayList<>();

    // Note that files in this list may also be directories
    // FileCopy will handle this
    Collection<File> files = getFiles();
    for (File sourceFile : files) {
      boolean isOk = FileCopy.copy(sourceFile, destinationFolder, newFiles);
      if (!isOk) {
        logger_.log(Level.WARNING, "Unable to paste: " + sourceFile);
      }
    }

    logger_.info(newFiles.size() + " file(s) pasted.");

    return newFiles;
  }

  /** {@inheritDoc} */
  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents)
  {
    logger_.info("Lost clipboard ownership: " + contents);
  }

  /**
   * A Transferable implementation for a set of java.io.File instances.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private static class FileTransferable implements Transferable
  {
    /** The files being transferred. */
    private final List<File> files_ = new ArrayList<>();

    /**
     * Create a transferable instance containing the specified files.
     *
     * @param files  The files that should be transferred. Non-null.
     * @throws IllegalArgumentException  If files is null.
     */
    private FileTransferable(Collection<File> files)
    {
      if (files == null)
        throw new IllegalArgumentException("files cannot be null");

      files_.addAll(files);
    }

    /** {@inheritDoc} */
    @Override
    public Object getTransferData(DataFlavor flavor)
    {
      return Collections.unmodifiableList(files_);
    }

    /** {@inheritDoc} */
    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
      return new DataFlavor[] {
        DataFlavor.javaFileListFlavor
      };
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
      return flavor == DataFlavor.javaFileListFlavor;
    }
  }
}
