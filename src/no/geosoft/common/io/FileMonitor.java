package no.geosoft.common.io;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import no.geosoft.common.util.CopyOnWriteHashMap;

/**
 * Class for monitoring changes in disk files.
 * <p>
 * Usage:
 * <ol>
 *    <li>Implement the FileListener interface.</li>
 *    <li>Create a FileMonitor instance.</li>
 *    <li>Add the file(s)/directory(directories) to listen for</li>
 * </ol>
 * fileChanged() will be called when a monitored file is created,
 * deleted or its modified time changes.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class FileMonitor
{
  /** Task scheduler */
  private final Timer timer_ = new Timer(true);

  /** Map of monitored files and their last modified time. */
  private final Map<File,Long> files_ = new CopyOnWriteHashMap<>();

  /** Listeners */
  private final Collection<WeakReference<FileListener>> listeners_ = new ArrayList<>();

  /**
   * Create a file monitor instance with specified polling interval.
   *
   * @param pollingInterval  Polling interval in milliseconds. &lt;0,&gt;.
   * @throws IllegalArgumentException  If polling interval is out of bounds.
   */
  public FileMonitor(long pollingInterval)
  {
    if (pollingInterval <= 0)
      throw new IllegalArgumentException("Illegal polling interval");

    timer_.schedule(new FileMonitorNotifier(), 0, pollingInterval);
  }

  /**
   * Stop the file monitor polling.
   */
  public void stop()
  {
    timer_.cancel();
  }

  /**
   * Add file to listen for. File may be any java.io.File (including a
   * directory) and may well be a non-existing file in the case where the
   * creating of the file is to be trapped.
   * <p>
   * More than one file can be listened for. When the specified file is
   * created, modified or deleted, listeners are notified.
   *
   * @param file  File to listen for. Non-null.
   * @throws IllegalArgumentException If file is null.
   */
  public void addFile(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    // Do nothing if the file is monitored already
    if (files_.containsKey(file))
      return;

    // Add to the set of monitored files
    long modifiedTime = file.exists() ? file.lastModified() : -1;
    files_.put(file, modifiedTime);
  }

  /**
   * Remove the specified file for listening. If the file is not
   * monitored, this call has no effect.
   *
   * @param file  File to remove. Non-null.
   * @throws IllegalArgumentException If file is null.
   */
  public void removeFile(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (!files_.containsKey(file))
      return;

    files_.remove(file);
  }

  /**
   * Return all files being currently monitored.
   *
   * @return All files being currently monitored. Never null.
   */
  public Set<File> getFiles()
  {
    return Collections.unmodifiableSet(files_.keySet());
  }

  /**
   * Return the file being monitored (or the first one if there
   * are more than one). This is a convenience method in case
   * the client knows that exactly one file is being monitored.
   *
   * @return  File being monitored. Null if none.
   */
  public File getFile()
  {
    if (files_.isEmpty())
      return null;

    for (File file : files_.keySet())
      return file;

    return null;
  }

  /**
   * Add listener to this file monitor.
   *
   * @param fileListener  Listener to add. Non-null.
   * @throws IllegalArgumentException  If fileListener is null.
   */
  public void addListener(FileListener fileListener)
  {
    if (fileListener == null)
      throw new IllegalArgumentException("fileListener cannot be null");

    removeListener(fileListener);
    listeners_.add(new WeakReference<>(fileListener));
  }

  /**
   * Remove listener from this file monitor.
   *
   * @param fileListener  Listener to remove. Non-null.
   * @throws IllegalArgumentException  If fileListener is null.
   */
  public void removeListener(FileListener fileListener)
  {
    if (fileListener == null)
      throw new IllegalArgumentException("fileListener cannot be null");

    // Loop with iterator to get the remove ability
    for (Iterator<WeakReference<FileListener>> i = listeners_.iterator();
         i.hasNext(); ) {
      WeakReference<FileListener> reference = i.next();
      FileListener listener = reference.get();
      if (listener == fileListener) {
        i.remove();
        return;
      }
    }
  }

  /**
   * This is the timer thread which is executed every n milliseconds
   * according to the setting of the file monitor. It investigates the
   * files in question and notify listeners if changed.
   *
   * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
   */
  private class FileMonitorNotifier extends TimerTask
  {
    /** {@inheritDoc} */
    @Override
    public void run()
    {
      // Loop over the registered files and see which have changed.
      // Use a copy of the list in case listener wants to alter the
      // list within its fileChanged method.
      Collection<File> files = new ArrayList<>(files_.keySet());

      for (File file : files) {
        long lastModifiedTime = files_.get(file);
        long newModifiedTime = file.exists() ? file.lastModified() : -1;

        boolean isCreated = lastModifiedTime == -1;
        boolean isDeleted = newModifiedTime == -1;

        // Check if file has changed
        if (newModifiedTime != lastModifiedTime) {

          // Register new modified time
          files_.put(file, newModifiedTime);

          // Notify listeners
          for (Iterator<WeakReference<FileListener>> i = listeners_.iterator();
               i.hasNext(); ) {
            WeakReference<FileListener> reference = i.next();
            FileListener listener = reference.get();

            // Remove from list if the back-end object has been GC'd
            if (listener == null) {
              i.remove();
            }
            else {
              if (isCreated)
                listener.fileCreated(file);
              else if (isDeleted)
                listener.fileDeleted(file);
              else
                listener.fileModified(file);
            }
          }
        }
      }
    }
  }
}
