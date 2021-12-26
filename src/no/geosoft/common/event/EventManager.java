package no.geosoft.common.event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Generic event manager class. Events are string based rather than being
 * enumerations which would have implied a central repository of events.
 * When events are strings, only the negotiating classes needs to know
 * about the events existence. It is transparent also to the EventManager
 * who only delegates the events.
 * <p>
 * A potential problem with string based events is that there might be
 * naming conflicts. In a large system, one might consider prefixing the
 * event names to avoid conflicts. If this seems to become a problem, the
 * system is probably bad designed anyway; The number of different event
 * types should be kept low.
 * <p>
 * The listener class must implement the EventListener interface which
 * implies the update() method. The listener class register itself in
 * the EventManager by:
 * <pre>
 * EventManager.getInstance().addListener("EventName", this);
 * </pre>
 * The source class of the event will call:
 * <pre>
 * EventManager.getInstance().notify("EventName", source, data);
 * </pre>
 * and the EventManager will then call the update() method of every listener.
 * <p>
 * The definition of <em>source</em> and <em>data</em> is purely up to
 * the involved classes. Typically <em>source</em> will be the source of
 * the event (the <em>created</em> object for a "Create" event, the
 * <em>deleted</em> object for a "Delete" event etc.) The additional
 * <em>data</em> object is for convenience only and will often be null.
 *
 * <p>
 * <b>Synchronization:</b>
 * <p>
 * This class is thread-safe.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class EventManager
{
  /** The singleton instance. */
  private static final EventManager instance_ = new EventManager();

  /** Listeners. */
  private final HashMap<String, Collection<WeakReference<EventListener>>> listeners_ = new HashMap<>();

  /**
   * Private constructor to prevent client instantiation.
   */
  private EventManager()
  {
    // Nothing
  }

  /**
   * Return the sole instance of the class.
   *
   * @return  The EventManager singleton.
   */
  public static EventManager getInstance()
  {
    return instance_;
  }

  /**
   * Add a listener.
   *
   * @param eventName      The event the listener will listen to. Non-null.
   * @param eventListener  The event listener object itself. Non-null.
   * @throws IllegalArgumentException  If eventName or eventListener is null.
   */
  public void addListener(String eventName, EventListener eventListener)
  {
    if (eventName == null)
      throw new IllegalArgumentException("eventName cannot be null");

    if (eventListener == null)
      throw new IllegalArgumentException("eventListener cannot be null");

    // Get the listener list for the specific event
    // Create it if not there
    synchronized (listeners_) {
      Collection<WeakReference<EventListener>> listeners = listeners_.get(eventName);
      if (listeners == null) {
        listeners = new ArrayList<WeakReference<EventListener>>();
        listeners_.put(eventName, listeners);
      }

      // Check to see if the listener is already there
      for (WeakReference<EventListener> reference : listeners) {
        EventListener listener = reference.get();
        if (listener == eventListener)
          return;
      }

      // Add the listener
      listeners.add(new WeakReference<EventListener>(eventListener));
    }
  }

  /**
   * Remove listener from specific event.
   *
   * @param eventName      Event to remove listener from. Non-null.
   * @param eventListener  Listener to remove. Non-null.
   * @throws IllegalArgumentException  If eventName or eventListener is null.
   */
  public void removeListener(String eventName, EventListener eventListener)
  {
    if (eventName == null)
      throw new IllegalArgumentException("eventName cannot be null");

    if (eventListener == null)
      throw new IllegalArgumentException("eventListener cannot be null");

    synchronized (listeners_) {
      // Find the listeners for the specified event
      Collection<WeakReference<EventListener>> listeners =
        listeners_.get(eventName);
      if (listeners == null)
        return;

      // Remove the listener
      for (Iterator<WeakReference<EventListener>> i = listeners.iterator();
           i.hasNext(); ) {
        WeakReference<EventListener> reference = i.next();
        EventListener listener = reference.get();
        if (listener == eventListener) {
          i.remove();
          break;
        }
      }

      // Remove the event as such if this was the last listener for this event
      if (listeners.isEmpty())
        listeners_.remove(eventName);
    }
  }

  /**
   * Remove listener from all events it is registered by. Convenient
   * way of cleaning up a listener object being destroyed.
   *
   * @param eventListener  Event listener to remove. Non-null.
   * @throws IllegalArgumentException  If eventListener is null.
   */
  public void removeListener(EventListener eventListener)
  {
    if (eventListener == null)
      throw new IllegalArgumentException("eventListener cannot be null");

    //
    // Loop over all registered events and remove the specified listener
    // Loop over a copy in case the removeListener() call wants to
    // remove the entire event from the hash map.
    //
    Collection<String> eventNames;
    synchronized (listeners_) {
      eventNames = new ArrayList<String>(listeners_.keySet());
    }

    for (String eventName : eventNames)
      removeListener(eventName, eventListener);
  }

  /**
   * Remove dead (GC'ed) listeners from the listeners list of the
   * specified event.
   *
   * @param eventName  Name of event of listener list to clean. Non-null.
   */
  private void cleanListenerList(String eventName)
  {
    assert eventName != null : "eventName cannot be null";

    Collection<WeakReference<EventListener>> listeners = listeners_.get(eventName);
    Collection<WeakReference<EventListener>> toDelete = new ArrayList<>();

    for (WeakReference<EventListener> reference : listeners) {
      EventListener listener = reference.get();
      if (listener == null)
        toDelete.add(reference);
    }

    listeners.removeAll(toDelete);
  }

  /**
   * Call listeners. The definition of <em>source</em> and <em>data</em>
   * is purely up to the communicating classes.
   *
   * @param eventName  Name of the event. Non-null.
   * @param source     Source of the event. May be null.
   * @param data       Additional data of the event. May be null.
   * @throws IllegalArgumentException  If eventName is null.
   */
  public void notify(String eventName, Object source, Object data)
  {
    if (eventName == null)
      throw new IllegalArgumentException("eventName cannot be null");

    Collection<WeakReference<EventListener>> copy;

    synchronized (listeners_) {

      // Find all listeners of this event
      Collection<WeakReference<EventListener>> listeners =
        listeners_.get(eventName);
      if (listeners == null)
        return;

      // Loop over a copy of the list in case it is altered by listener
      copy = new ArrayList<WeakReference<EventListener>>(listeners);

      for (WeakReference<EventListener> reference : copy) {
        EventListener listener = reference.get();
        if (listener != null)
          listener.update(eventName, source, data);
      }

      cleanListenerList(eventName);
    }
  }

  /**
   * Convenience front-end where the additional data parameter
   * is null.
   *
   * @param eventName  Name of the event. Non-null.
   * @param source     Source of the event. May be null.
   * @throws IllegalArgumentException  If eventName is null.
   */
  public void notify(String eventName, Object source)
  {
    notify(eventName, source, null);
  }
}
