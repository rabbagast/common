package no.geosoft.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple manager for keeping track of undo/redo on a structure.
 * <p>
 * @param <T>   Type of instances representing the <em>state</em>.
 * <p>
 * This class is <em>not</em> thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class UndoManager<T>
{
  /**
   * The different states we keep track of.
   * Contains always at least the initial state element.
   */
  private final List<T> states_ = new ArrayList<>();

  /** Pointer to the current state within states_. */
  private int currentStateIndex_;

  /**
   * Create an undo manager instance with the specified
   * initial state.
   *
   * @param initialState  Initial state. Non-null.
   * @throws IllegalArgumentException  If initialState is null.
   */
  public UndoManager(T initialState)
  {
    if (initialState == null)
      throw new IllegalArgumentException("initialState cannot be null");

    saveState(initialState);
  }

  /**
   * Check if it is possible to do an <em>undo</em>.
   *
   * @return  True if it is possible to do an undo, false otherwise.
   */
  public boolean hasUndo()
  {
    return currentStateIndex_ > 0;
  }

  /**
   * Check if it is possible to do a <em>redo</em>.
   *
   * @return  True if it is possible to do a redo, false otherwise.
   */
  public boolean hasRedo()
  {
    return currentStateIndex_ < states_.size() - 1;
  }

  /**
   * Return description of the change that will be undone with the
   * next undo operation.
   *
   * @return  Description of the next undo. Null if N/A.
   */
  public String getUndoDescription()
  {
    return hasUndo() ? getCurrentState().toString() : null;
  }

  /**
   * Return description of the change that will be redone with the
   * next redo operation.
   *
   * @return  Description of the next redo. Null if N/A.
   */
  public String getRedoDescription()
  {
    return hasRedo() ? states_.get(currentStateIndex_ + 1).toString() : null;
  }

  /**
   * Perform an <em>undo</em> operation, i.e. move the current pointer to
   * the previous available state.
   *
   * @return The new current state. Never null.
   * @throws IllegalStateException  If the manager has no undo in the present state.
   */
  public T undo()
  {
    if (!hasUndo())
      throw new IllegalStateException("Invalid state for undo");

    currentStateIndex_--;
    return states_.get(currentStateIndex_);
  }

  /**
   * Perform a <em>redo</em> operation, i.e. move the current pointer to
   * the next available state.
   *
   * @return The new current state. Never null.
   * @throws IllegalStateException  If the manager has no redo in the present state.
   */
  public T redo()
  {
    if (!hasRedo())
      throw new IllegalStateException("Invalid state for redo");

    currentStateIndex_++;
    return states_.get(currentStateIndex_);
  }

  /**
   * Save the specified state and set it as the new current state.
   *
   * @param state  State to save. Non-null.
   * @throws IllegalArgumentException  If state is null.
   */
  public void saveState(T state)
  {
    if (state == null)
      throw new IllegalArgumentException("state cannot be null");

    // Remove all redo states
    if (hasRedo())
      states_.subList(currentStateIndex_ + 1, states_.size()).clear();

    states_.add(state);
    currentStateIndex_ = states_.size() - 1;
  }

  /**
   * Return the current state of this manager.
   *
   * @return  The current state of this manager. Never null.
   */
  public T getCurrentState()
  {
    return states_.get(currentStateIndex_);
  }

  /**
   * Remove all but the initial state from this manager.
   */
  public void clear()
  {
    T initialState = states_.get(0);

    states_.clear();
    states_.add(initialState);

    currentStateIndex_ = 0;
  }

  public static void main(String[] arguments)
  {
    java.util.Map<Integer,String> edits = new java.util.HashMap<>();
    UndoManager<java.util.Map<Integer,String>> undoManager = new UndoManager<>(new java.util.HashMap<>(edits));

    edits.put(1, "test 1");
    undoManager.saveState(new java.util.HashMap<>(edits));
    System.out.println("Saved: " + undoManager.getCurrentState().size());

    edits.put(2, "test 2");
    undoManager.saveState(new java.util.HashMap<>(edits));
    System.out.println("Saved: " + undoManager.getCurrentState().size());

    edits.put(3, "test 3");
    undoManager.saveState(new java.util.HashMap<>(edits));
    System.out.println("Saved: " + undoManager.getCurrentState().size());

    //edits.clear();
    //edits.putAll(undoManager.undo());

    while (undoManager.hasUndo()) {
      java.util.Map<Integer,String> state = undoManager.undo();
      System.out.println("Undo " + state.size());
    }
  }
}
