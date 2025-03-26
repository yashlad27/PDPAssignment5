package utilities;

import model.event.Event;

/**
 * Functional interface for updating event properties.
 * This interface represents an operation that updates a specific property
 * of an Event with a new value.
 */
@FunctionalInterface
public interface EventPropertyUpdater {

  /**
   * Updates an event's property with the given value.
   *
   * @param event    the event to update
   * @param newValue the new value for the property
   * @return true if the update was successful, false otherwise
   */
  boolean update(Event event, String newValue);
}
