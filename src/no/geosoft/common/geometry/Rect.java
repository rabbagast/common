package no.geosoft.common.geometry;

/**
 * A integer based rectangle. The strange name is to avoid name
 * clash with java.awt.Rectangle.
 * <p>
 * Rect and Box represents the same concept, but their different
 * definition makes them suitable for use in different situations.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Rect
{
  /** X coordinate of upper left corner. */
  public int x;

  /** Y coordinate of upper left corner. */
  public int y;

  /** Rectangle width. [0,&gt;. */
  public int height;

  /** Rectangle height. [0,&gt;. */
  public int width;

  /**
   * Create a rectangle.
   *
   * @param x       X coordinate of upper left corner.
   * @param y       Y coordinate of upper left corner.
   * @param width   Rectangle width. [0,&gt;.
   * @param height  Rectangle height. [0,&gt;.
   * @throws IllegalArgumentException  If width or height is less than 0.
   */
  public Rect(int x, int y, int width, int height)
  {
    if (width < 0)
      throw new IllegalArgumentException("Invalid width: " + width);

    if (height < 0)
      throw new IllegalArgumentException("Invalid height: " + height);

    set(x, y, width, height);
  }

  /**
   * Create a default (empty) rectangle.
   */
  public Rect()
  {
    this(0, 0, 0, 0);
  }

  /**
   * Create a rectangle as a copy of the specified rectangle.
   *
   * @param rectangle  Rectangle to copy. Non-null.
   * @throws IllegalArgumentException  If rectangle is null.
   */
  public Rect(Rect rectangle)
  {
    if (rectangle == null)
      throw new IllegalArgumentException("rectangle cannot be null");

    set(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }

  /**
   * Create a rectangle based on the specified box.
   *
   * @param box  Box to create rectangle from. Non-null.
   * @throws IllegalArgumentException  If box is null.
   */
  public Rect(Box box)
  {
    if (box == null)
      throw new IllegalArgumentException("box cannot be null");

    set(box.x1, box.y1, box.x2 - box.x1, box.y2 - box.y1);
  }

  /**
   * Copy the specified rectangle.
   *
   * @param rectangle  Rectangle to copy. Non-null.
   * @throws IllegalArgumentException  If rectangle is null.
   */
  public void copy(Rect rectangle)
  {
    if (rectangle == null)
      throw new IllegalArgumentException("rectangle cannot be null");

    this.x = rectangle.x;
    this.y = rectangle.y;
    this.width = rectangle.width;
    this.height = rectangle.height;
  }

  /**
   * Return true if this rectangle is empty.
   *
   * @return  True if this rectangle is empty, false otherwise.
   */
  public boolean isEmpty()
  {
    return width <= 0 || height <= 0;
  }

  /**
   * Expand this rectangle the specified amount in each direction.
   *
   * @param dx  Amount to expand to left and right.
   * @param dy  Amount to expand on top and botton.
   */
  public void expand(int dx, int dy)
  {
    x -= dx;
    y -= dy;
    width += dx + dx;
    height += dy + dy;
  }

  /**
   * Set the geometry of this rectangle.
   *
   * @param x       X coordinate of upper left corner.
   * @param y       Y coordinate of upper left corner.
   * @param width   Width of rectangle. [0,&gt;.
   * @param height  Height of rectangle. [0,&gt;.
   */
  public void set(int x, int y, int width, int height)
  {
    if (width < 0)
      throw new IllegalArgumentException("Invalid width: " + width);

    if (height < 0)
      throw new IllegalArgumentException("Invalid width: " + height);

    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * Set this rectangle as extent of specified polyline.
   *
   * @param xArray  X coordinates of polyline. Non-null.
   * @param yArray  Y coordinates of polyline. Non-null.
   * @throws IllegalArgumentException  If xArray or yArray is null or of different length.
   */
  public void set(int xArray[], int yArray[])
  {
    if (xArray == null)
      throw new IllegalArgumentException("xArray cannot be null");

    if (yArray == null)
      throw new IllegalArgumentException("yArray cannot be null");

    if (yArray.length != xArray.length)
      throw new IllegalArgumentException("Invalid yArray length: " + yArray.length);

    int  minX = Integer.MAX_VALUE;
    int  maxX = Integer.MIN_VALUE;

    int  minY = Integer.MAX_VALUE;
    int  maxY = Integer.MIN_VALUE;

    for (int i = 0; i < xArray.length; i++) {
      if (xArray[i] < minX) minX = xArray[i];
      if (xArray[i] > maxX) maxX = xArray[i];

      if (yArray[i] < minY) minY = yArray[i];
      if (yArray[i] > maxY) maxY = yArray[i];
    }

    x = minX;
    y = minY;

    width  = maxX - minX + 1;
    height = maxY - minY + 1;
  }

  /**
   * Return X coordinate of center of this rectangle.
   *
   * @return  X coordinate of center of this rectangle.
   */
  public int getCenterX()
  {
    return x + (int) Math.floor(width / 2.0);
  }

  /**
   * Return Y coordinate of center of this rectangle.
   *
   * @return  Y coordinate of center of this rectangle.
   */
  public int getCenterY()
  {
    return y + (int) Math.floor(height / 2.0);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return x * 7 + y * 11 + width * 13 + height * 17;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals (Object object)
  {
    if (object == this)
      return true;

    if (object == null)
      return false;

    if (!(object instanceof Rect))
      return false;

    Rect rectangle = (Rect) object;

    return this.x == rectangle.x &&
           this.y == rectangle.y &&
           this.width  == rectangle.width &&
           this.height == rectangle.height;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return "x= " + x + " y=" + y + " width=" + width + " height=" + height;
  }
}
