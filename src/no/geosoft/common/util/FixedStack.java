package no.geosoft.common.util;

import java.util.Stack;

/**
 * A stack that can be pushed and popped at the top,
 * but that will loose it bottom-most elements if the
 * stack grows beyond the size limit.
 *
 * @param <T>  Stack element type.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class FixedStack<T> extends Stack<T>
{
  /** Maximum size of stack. &lt;1,&gt; */
  private final int maxSize_;

  /**
   * Create a fixed size element of the specified max size.
   *
   * @param maxSize  Max size of the stack. &lt;1,&gt;
   * @throws IllegalArgumentException  If maxSize &lt; 1.
   */
  public FixedStack(int maxSize)
  {
    if (maxSize <= 1)
      throw new IllegalArgumentException("Invalid maxSize: " + maxSize);

    maxSize_ = maxSize;
  }

  /** {@inheritDoc} */
  @Override
  public T push(T object)
  {
    if (size() == maxSize_)
      remove(0);

    return super.push(object);
  }

  public static void main(String[] arguments)
  {
    Stack<String> s = new FixedStack<>(5);
    s.push("111");
    s.push("222");
    s.push("333");
    s.push("444");
    s.push("555");
    s.push("666");

    System.out.println(s.pop());
    System.out.println(s.size());
  }
}