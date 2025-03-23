package model.event;

import java.time.LocalDateTime;

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
