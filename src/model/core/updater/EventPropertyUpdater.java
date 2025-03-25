package model.core.updater;

import model.event.Event;

/**
 * Functional interface for updating event properties.
 * This interface provides a way to encapsulate property update logic.
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
