package model.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A functional interface representing an action to be performed on an Event object.
 * This interface provides a flexible way to define and compose event modifications
 * through a chain of operations.
 *
 * <p>The interface includes:
 * <ul>
 *   <li>A single abstract method {@code apply(Event event)} for executing the action</li>
 *   <li>A default method {@code andThen(EventAction after)} for composing multiple actions</li>
 *   <li>Static factory methods for common event modifications</li>
 * </ul>
 * </p>
 */
@FunctionalInterface
public interface EventAction {

  /**
   * Executes an action on the provided event.
   *
   * @param event the event to perform the action on
   */
  void execute(Event event);

  /**
   * Executes the action on a list of events.
   * This is a default method that applies the action to each event in the list.
   *
   * @param events the list of events to perform the action on
   */
  default void executeOnList(List<Event> events) {
    for (Event event : events) {
      execute(event);
    }
  }

  /**
   * Returns a composed action that executes this action followed by another.
   * This is a default method allowing actions to be chained together.
   *
   * @param other another EventAction
   * @return a composed action that executes this action followed by the other
   */
  default EventAction andThen(EventAction other) {
    return event -> {
      execute(event);
      other.execute(event);
    };
  }

  static EventAction setSubject(String subject) {
    return event -> event.setSubject(subject);
  }

  static EventAction setDescription(String description) {
    return event -> event.setDescription(description);
  }

  static EventAction setLocation(String location) {
    return event -> event.setLocation(location);
  }

  /**
   * Returns an EventAction that sets the start date and time of an event.
   *
   * @param startDateTime the start date and time to set
   * @return an EventAction that sets the start date and time
   */
  static EventAction setStartDateTime(LocalDateTime startDateTime) {
    return event -> event.setStartDateTime(startDateTime);
  }

  /**
   * Returns an EventAction that sets the end date and time of an event.
   *
   * @param endDateTime the end date and time to set
   * @return an EventAction that sets the end date and time
   */
  static EventAction setEndDateTime(LocalDateTime endDateTime) {
    return event -> event.setEndDateTime(endDateTime);
  }

}
