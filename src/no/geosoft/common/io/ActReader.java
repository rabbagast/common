package no.geosoft.common.io;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ActReader
{
  private final InputStream inputStream_;

  public ActReader(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    try {
      inputStream_ = new FileInputStream(file);
    }
    catch (FileNotFoundException exception) {
      throw new IllegalArgumentException("Invalid file: " + file, exception);
    }
  }

  public ActReader(InputStream inputStream)
  {
    if (inputStream == null)
      throw new IllegalArgumentException("inputStream cannot be null");

    inputStream_ = inputStream;
  }

  public List<Color> read()
    throws IOException
  {
    List<Color> colors = new ArrayList<>();

    try {
      while (true) {
        int r = inputStream_.read();
        int g = inputStream_.read();
        int b = inputStream_.read();

        if (r == -1 || g == -1 || b == -1)
          break;

        if (r == 0 && g == 0 && b == 0)
          break;

        colors.add(new Color(r, g, b));
      }

      inputStream_.close();

      return colors;
    }
    catch (FileNotFoundException exception) {
      throw new IOException("Unable to read: " + inputStream_, exception);
    }
  }
}
