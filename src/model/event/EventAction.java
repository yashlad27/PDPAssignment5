package model.event;

import java.time.LocalDateTime;

/**
 * A functional interface representing an action to be performed on an Event object.
 * This interface provides a flexible way to define and compose event modifications
 * through a chain of operations.
 *
 * <p> The interface includes:
 * <ul>
 *   <li>A single abstract method {@code apply(Event event)} for executing the action</li>
 *   <li>A default method {@code andThen(EventAction after)} for composing multiple actions</li>
 *   <li>Static factory methods for common event modifications</li>
 * </ul>
 * <p>
 */
@FunctionalInterface
public interface EventAction {

  void apply(Event event);

  default EventAction andThen(EventAction after) {
    return event -> {
      apply(event);
      after.apply(event);
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

  static EventAction setVisibility(boolean isPublic) {
    return event -> event.setPublic(isPublic);
  }

  static EventAction setAllDay(boolean isAllDay) {
    return event -> event.setAllDay(isAllDay);
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

  /**
   * Returns an EventAction that performs no action.
   *
   * @return an EventAction that performs no action
   */
  static EventAction doNothing() {
    return event -> {
    };
  }
}
