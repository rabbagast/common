package no.geosoft.common.event;

/**
 * Interface for an event listener.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public interface EventListener
{
  /**
   * Called in response to a notify() call on the EventManager if the
   * present listener is registered to listen for the given event.
   *
   * @param eventName  Name of the event. Non-null.
   * @param source     The source the event (as defined by caller). May be null.
   * @param data       Additional data (as defined by caller). May be null.
   */
  void update(String eventName, Object source, Object data);
}


