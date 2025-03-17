package model.event;

import java.time.LocalDateTime;

import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

@FunctionalInterface
public interface EventCreationStrategy {

  Event createEvent() throws ConflictingEventException, InvalidEventException;

  default EventCreationStrategy andThen(EventAction action) {
    return () -> {
      Event event = createEvent();
      action.apply(event);
      return event;
    };
  }

  static EventCreationStrategy timedEvent(String subject, LocalDateTime start, LocalDateTime end) {
    return () -> new Event(subject, start, end, null, null, true);
  }

  static EventCreationStrategy withDescription(EventCreationStrategy strategy, String description) {
    return strategy.andThen(event -> event.setDescription(description));
  }

  static EventCreationStrategy withLocation(EventCreationStrategy strategy, String location) {
    return strategy.andThen(event -> event.setLocation(location));
  }

  static EventCreationStrategy withVisibility(EventCreationStrategy strategy, boolean isPublic) {
    return strategy.andThen(event -> event.setPublic(isPublic));
  }

}
