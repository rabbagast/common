package no.geosoft.common.graphics;

import java.awt.Font;

import no.geosoft.common.geometry.Rect;

/**
 * Class for representing text within graphics.
 * <p>
 * Text is not added directly to coordinates in the canvas, but
 * associated with geometric elements of the scene and positioned
 * through rendering hints. <tt>GText</tt> objects can have their own
 * style, but will inherit unset style elements from their segment owner.
 * <p>
 * Typical usage:
 *
 * <pre>
 *    GSegment segment = new GSegment();
 *    segment.setText(new GText("Revenue", GPosition.TOP | GPosition.WEST));
 * </pre>
 *
 * Note that a text is typically associated with a GSegment at a time
 * where the GSegment is without geometry. The text position is
 * not determined until the rendering step anyway.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class GText extends GPositional
{
  // TODO
  public enum Alignment
  {
    LEFT,
    CENTER,
    RIGHT
  };

  private static final int DEFAULT_POSITION_HINT = GPosition.CENTER |
                                                   GPosition.DYNAMIC;

  private String text_;

  /**
   * Create text object with specified position hint.
   * @see #setPositionHint(int)
   *
   * @param text          Text string to associate with text. Can be null
   *                      or empty.
   * @param positionHint  Position preferences.
   */
  public GText(String text, int positionHint)
  {
    super(positionHint, false);
    text_ = text;
  }

  /**
   * Create text object with default position hint.
   *
   * @param text  Text string to associate with text. Can be null or empty.
   */
  public GText(String text)
  {
    this(text, DEFAULT_POSITION_HINT);
  }

  /**
   * Create empty text object with default position hint.
   */
  public GText()
  {
    this("");
  }

  /**
   * Set text content of this text element.
   *
   * @param text  Text content. Can be null or empty.
   */
  public void setText(String text)
  {
    // Do nothing if no change
    if (text == null && text_ == null ||
        text != null && text.equals(text_))
      return;

    text_ = text;

    // Flag owner region and scene annotation as invalid
    GObject object = getObject();
    GScene scene  = object != null ? object.getScene() : null;

    if (object != null)
      object.flagRegionValid(false);

    if (scene != null)
      scene.setAnnotationValid(false);

    updateDamage();
  }

  /**
   * Return text content of this text element.
   *
   * @return  Text content of this text element.
   */
  public String getText()
  {
    return text_;
  }

  /**
   * Return margin for this text element. The margin defines the distance
   * between the computed position of the text and its actual position.
   * The margin is > 0 to make the location seem natural and visually
   * pleasent. The margin is font relative.
   *
   * @return  Margin of this text element.
   */
  int getMargin()
  {
    // At this point we know the rectangle (i.e. font) height, so we
    // set margin relative to it. A half height seems reasonable.
    return rectangle_.height / 2;
  }

  int getPadding()
  {
    Font font = getActualStyle().getFont();
    int fontSize = font.getSize();

    return (int) Math.round(fontSize * 0.3);
  }

  /**
   * Compute the size of this text element and set in rectangle.
   */
  void computeSize()
  {
    // HACK: We must go to the canvas since it is the only object in
    // the system that knows about rendering stuff.
    // TODO: Make canvas some interface we can get as input at ctor-time.
    GScene  scene  = segment_.getScene();
    GCanvas canvas = (GCanvas) scene.getWindow().getCanvas();

    int width  = 0;
    int height = 0;

    int padding = getPadding();

    Font font = getActualStyle().getFont();

    boolean isFilled = getActualStyle().getBackgroundColor() != null;
    int hMargin = isFilled ? padding : 2;
    int vMargin = isFilled ? padding : 2;

    int vSpacing = 0;

    if (text_ != null && text_.length() > 0) {
      String[] tokens = text_.split("\n");
      for (String token : tokens) {
        Rect rectangle = canvas.getStringBox(token, font);
        int tokenWidth = rectangle.width;
        if (tokenWidth > width)
          width = tokenWidth;
        height += rectangle.height;
      }

      vSpacing = (tokens.length - 1) * padding;
    }

    rectangle_.width  = width + hMargin;
    rectangle_.height = height + vSpacing + vMargin;
  }
}
