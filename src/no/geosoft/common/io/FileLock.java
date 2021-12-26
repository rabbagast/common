package no.geosoft.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for locking files. This is not a deep OS lock, but it will
 * work through queries of this class.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class FileLock
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(FileLock.class.getName());

  /** The file being locked. Non-null. */
  private final File file_;

  /** Time of locking. ms from Epoch. */
  private long lockTime_;

  /**
   * Create a file lock instance for the specified file. The file is not
   * locked until the lock() method gets called.
   *
   * @param file  File to lock. Non-null.
   * @throws IllegalArgumentException  If file is null.
   */
  public FileLock(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    file_ = file;
  }

  /**
   * The lock is implemented by creating an identification "lock" file along
   * with the file being locked. This method returns this lock file instance.
   *
   * @return  Lock file instance. Never null.
   */
  private File getLockFile()
  {
    String fileName = file_.getName();
    String lockFileName = fileName + ".lock";

    return new File(file_.getParentFile(), lockFileName);
  }

  /**
   * Return owner of the lock.
   *
   * @return  The owner of the lock (or null if the lock file is
   *          not accessible for some reason).
   */
  public String lockedBy()
  {
    File lockFile = getLockFile();

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(lockFile));
      String owner = reader.readLine();
      return owner;
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to access lock file: " + lockFile, exception);
      return null;
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException exception) {
          logger_.log(Level.WARNING, "Unable to close: " + lockFile, exception);
        }
      }
    }
  }

  /**
   * Return time the lock was acquired.
   *
   * @return  The time this lock was acquired (or null if the lock file is
   *          not accessible for security reasons).
   */
  public Date lockedSince()
  {
    File lockFile = getLockFile();

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(lockFile));
      reader.readLine(); // owner

      String time = reader.readLine();
      try {
        return new Date(Long.parseLong(time));
      }
      catch (NumberFormatException  exception) {
        logger_.log(Level.WARNING, "Invalid time stamp format: " + time, exception);
        return null;
      }
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Unable to access lock file: " + lockFile, exception);
      return null;
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException exception) {
          logger_.log(Level.WARNING, "Unable to close: " + lockFile, exception);
        }
      }
    }
  }

  /**
   * Lock file. I.e. create the associated lock-file.
   *
   * @return True if the locking was successful, false otherwise.
   */
  public boolean lock()
  {
    // Can't lock if it is already locked
    if (isLocked()) {
      logger_.log(Level.WARNING, "File is already locked: " + file_);
      return false;
    }

    // Can't lock if the source file doesn't exist
    if (!file_.exists()) {
      logger_.log(Level.WARNING, "Unable to lock non-existing file: " + file_);
      return false;
    }

    // Owner and time stamp of lock file
    String owner = System.getProperty("user.name");
    lockTime_ = System.currentTimeMillis();

    // Find and write the lock file
    File lockFile = getLockFile();

    FileWriter writer = null;
    try {
      writer = new FileWriter(lockFile);
      writer.write(owner);
      writer.write(System.getProperty("line.separator"));
      writer.write("" + lockTime_);
      writer.close();

      // In a rear case with different processes writing the lock file,
      // it could happen that the file is not ours anyway.
      Date lockedSince = lockedSince();
      if (lockedSince != null && lockedSince.getTime() != lockTime_) {
        logger_.log(Level.WARNING, "File locking failed: " + file_);
        return false;
      }

      // All is fine. No-one can alter the lock file other than this
      // lock instance (or by hijacking it externally). We make sure
      // it disappears automatically when the JVM dies.
      lockFile.deleteOnExit();
      logger_.log(Level.INFO, "File sucessfully locked: " + file_);
      return true;
    }
    catch (IOException exception) {
      logger_.log(Level.WARNING, "Accessing lock file failed: " + lockFile, exception);
      boolean isDeleted = lockFile.delete();
      if (!isDeleted)
        logger_.log(Level.WARNING, "Deleting lock file failed: " + lockFile);
      return false;
    }
    finally {
      try {
        if (writer != null)
          writer.close();
      }
      catch (IOException exception) {
        logger_.log(Level.WARNING, "Unable to close " + file_, exception);
        return false;
      }
    }
  }

  /**
   * Check if file is locked.
   *
   * @return  True if the file is locked, false otherwise.
   */
  public boolean isLocked()
  {
    return getLockFile().exists();
  }

  /**
   * Check if the file is locked by this lock.
   *
   * @return  True if the file is locked by this lock. Otherwise it might
   *          be locked by some other lock, or not at all.
   */
  public boolean isMyLock()
  {
    Date lockedSince = lockedSince();
    return lockedSince != null && lockedSince.getTime() == lockTime_;
  }

  /**
   * Try to release the lock.
   *
   * @param force  The lock may be held by a different lock. If true, we
   *               force a release, if false we only release if this is
   *               actually our lock.
   * @return True if the lock was actually released, false otherwise.
   */
  public boolean release(boolean force)
  {
    //
    // Case 0: Attempt to release if file is not locked
    //
    if (!isLocked()) {
      logger_.log(Level.WARNING, "File is not locked");
      return false;
    }

    //
    // Case 1: Attempt to release someone else's lock.
    //
    if (!isMyLock() && !force) {
      logger_.log(Level.WARNING, "Cannot release someone elses lock: " + this);
      return false;
    }

    //
    // Case 2a: Force release someone else's lock
    //      2b: Release own lock
    //
    boolean isForced = !isMyLock();

    // Release lock by removing lock file
    File lockFile = getLockFile();
    boolean isDeleted = lockFile.delete();

    if (!isDeleted) {
      logger_.log(Level.WARNING, "Deleting lock file failed: " + lockFile);
      return false;
    }
    else if (isForced) {
      logger_.log(Level.INFO, "Lock released by force: " + lockFile);
      return true;
    }
    else {
      logger_.log(Level.INFO, "Lock successfully released: " + lockFile);
      return true;
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder text = new StringBuilder();
    text.append(file_.toString());
    text.append(": ");

    if (isLocked()) {
      text.append("Locked by: ");
      text.append(lockedBy());
      text.append(" Since: ");
      text.append(lockedSince());
    }
    else {
      text.append("Not locked");
    }

    return text.toString();
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main (String[] arguments)
  {
    File file = new File("C:/Users/jacob/tull.txt");
    FileLock fileLock1 = new FileLock(file);
    FileLock fileLock2 = new FileLock(file);

    System.out.println ("1:---------------------------");
    System.out.println (fileLock1);

    System.out.println ("Try to lock: " );
    boolean isOk = fileLock1.lock();
    System.out.println (isOk ? "Success" : "Failure");
    System.out.println (fileLock1);

    System.out.println ("2:---------------------------");
    System.out.println (fileLock2);

    System.out.println ("Try to lock: " );
    isOk = fileLock2.lock();
    System.out.println (isOk ? "Success" : "Failure");
    System.out.println (fileLock2);

    System.out.println ("Try to release: " );
    isOk = fileLock2.release(true);
    System.out.println (isOk ? "Success" : "Failure");
    System.out.println (fileLock2);

    System.out.println ("1:---------------------------");

    System.out.println ("Try to release: " );
    isOk = fileLock1.release(false);
    System.out.println (isOk ? "Success" : "Failure");
    System.out.println (fileLock1);
  }
}
