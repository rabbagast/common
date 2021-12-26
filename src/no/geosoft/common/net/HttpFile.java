package no.geosoft.common.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.geosoft.common.io.FileUtil;

/**
 * A convenient front-end to a HTTP based file entry, much like
 * the java.io.File class. An instance can represent a file or a
 * folder.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class HttpFile
{
  /** Parent directory, or null if at top level. */
  private final HttpFile parent_;

  /** Name of file or directory. Non-null. */
  private final String name_;

  /** True if this is a directory, false if it is a file. */
  private final boolean isDirectory_;

  /**
   * Create a HttpFile root instance for an existing HTTP folder.
   *
   * @param url  URL to HTTP folder. Non-null
   * @throws IllegalArgumentException  If url is null.
   */
  public HttpFile(URL url)
  {
    if (url == null)
      throw new IllegalArgumentException("url cannot be null");

    parent_ = null;

    // Capture full name including http:// but excluding any last folder indicator
    String name = url.toExternalForm();
    if (name.endsWith("/"))
      name = name.substring(0, name.length() - 1);

    name_ = name;
    isDirectory_ = true;
  }

  /**
   * Create a HttpFile instance for an existing HTTP file or folder.
   *
   * @param parent       Parent file. Non-null.
   * @param name         File name. Non-null.
   * @param isDirectory  True if this is a directory, false otherwise.
   * @throws IllegalArgumentException  If parent or name is null.
   */
  private HttpFile(HttpFile parent, String name, boolean isDirectory)
  {
    assert parent != null : "pareent cannot be null";
    assert name != null : "name cannot be null";

    parent_ = parent;
    name_ = name;
    isDirectory_ = isDirectory;
  }

  /**
   * Return name of this file instance.
   *
   * @return  Name of this file instance. Never null.
   */
  public String getName()
  {
    return name_;
  }

  /**
   * Check if the present instance represents the root
   * of its hierarchy.
   *
   * @return  True if the file is root, false otherwise.
   */
  public boolean isRoot()
  {
    return parent_ == null;
  }

  /**
   * Return parent folder instance of this HTTP file.
   *
   * @return  Parent folder instance. Null if at root level.
   */
  public HttpFile getParentFile()
  {
    return parent_;
  }

  /**
   * Return children files of this file.
   *
   * @return  Children files of this file. Never null.
   * @throws IOException  If the operation cannot be completed for some reason.
   */
  public List<HttpFile> listFiles()
    throws IOException
  {
    // Return empty list if this is not a directory
    if (!isDirectory_)
      return new ArrayList<>();

    // Otherwise parse the server HTML for the path and
    // create associated file and folder entries
    List<HttpFile> files = findChildren(this);
    return files;
  }

  /**
   * Return full path of this file.
   *
   * @return  Full path of this file. Never null.
   */
  public String getPath()
  {
    if (parent_ == null)
      return getName();

    String parentPath = parent_.getPath();
    return parentPath + (parentPath.endsWith("/") ? "" : "/") + getName();
  }

  /**
   * Check if this instance is a file.
   *
   * @return  True if this instance is a file, false otherwise.
   */
  public boolean isFile()
  {
    return !isDirectory_;
  }

  /**
   * Check if this instance is a directory.
   *
   * @return  True if this instance is a directory, false otherwise.
   */
  public boolean isDirectory()
  {
    return isDirectory_;
  }

  /**
   * Parse the content of the specified file and return its
   * associated children entries.
   *
   * @param file  File to find children of. Non-null.
   * @return      List of children instance. Never null.
   */
  private static List<HttpFile> findChildren(HttpFile file)
    throws IOException
  {
    assert file != null : "file cannot be null";

    List<HttpFile> children = new ArrayList<>();

    try {
      URL url = new URL(file.getPath());
      String text = FileUtil.read(url);

      Pattern pattern = Pattern.compile("\\<a href=\"(?<link>([^\"]*))\"\\>(?<name>([^\"]*))\\</a\\>");
      Matcher matcher = pattern.matcher(text);

      boolean isMatching = matcher.find();
      while (isMatching) {
        String link = matcher.group("link");

        if (!link.startsWith("/") && !link.startsWith("?")) {
          boolean isDirectory = link.endsWith("/");
          String name = isDirectory ? link.substring(0, link.length() - 1) : link;

          HttpFile child = new HttpFile(file, name, isDirectory);
          children.add(child);
        }

        isMatching = matcher.find();
      }

      return children;
    }
    catch (MalformedURLException exception) {
      assert false : "Programming error " + exception;
      return new ArrayList<>();
    }
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return Objects.hash(getPath());
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object object)
  {
    if (object == null)
      return false;

    if (object == this)
      return true;

    if (!(object instanceof HttpFile))
      return false;

    HttpFile httpFile = (HttpFile) object;

    if (!getPath().equals(httpFile.getPath()))
      return false;

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return getName();
  }
}
