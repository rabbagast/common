package no.geosoft.common.io;

import java.io.File;

/**
 * Interface for listening to disk file changes.
 * @see FileMonitor
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public interface FileListener
{
  /**
   * Called when a monitored file is created.
   *
   * @param file  File that has been created. Non-null.
   */
  void fileCreated(File file);

  /**
   * Called when a monitored file is modified.
   *
   * @param file  File that has been modified. Non-null.
   */
  void fileModified(File file);

  /**
   * Called when a monitored file is deleted.
   *
   * @param file  File that has been modified. Non-null.
   */
  void fileDeleted(File file);
}
