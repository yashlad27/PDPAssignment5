package model.event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

@FunctionalInterface
public interface EventCreationStrategy {

  Event createEvent() throws ConflictingEventException, InvalidEventException;

  /**
   * Returns a composed EventCreationStrategy that applies the given action to the created event.
   *
   * @param action the action to apply
   * @return a composed EventCreationStrategy
   */
  default EventCreationStrategy andThen(EventAction action) {
    return () -> {
      Event event = createEvent();
      action.apply(event);
      return event;
    };
  }

  /**
   * Returns an EventCreationStrategy that creates a timed event.
   *
   * @param subject the subject of the event
   * @param start   the start date and time
   * @param end     the end date and time
   * @return an EventCreationStrategy that creates a timed event
   */
  static EventCreationStrategy timedEvent(String subject, LocalDateTime start, LocalDateTime end) {
    return () -> new Event(subject, start, end, null, null, true);
  }

  static EventCreationStrategy allDayEvent(String subject, LocalDate date) {
    return () -> Event.createAllDayEvent(subject, date, null, null, true);
  }

  static EventCreationStrategy recurringEvent(String subject, LocalDateTime start, LocalDateTime end,
                                              Set<DayOfWeek> repeatDays, int occurrences) {
    return () -> new RecurringEvent.Builder(subject, start, end, repeatDays)
            .occurrences(occurrences)
            .build();
  }

  static EventCreationStrategy recurringEventUntil(String subject, LocalDateTime start, LocalDateTime end,
                                                   Set<DayOfWeek> repeatDays, LocalDate endDate) {
    return () -> new RecurringEvent.Builder(subject, start, end, repeatDays)
            .endDate(endDate)
            .build();
  }


  /**
   * Returns a composed EventCreationStrategy with the given description.
   *
   * @param strategy    the base strategy
   * @param description the description to set
   * @return a composed EventCreationStrategy with the given description
   */

  static EventCreationStrategy withDescription(EventCreationStrategy strategy, String description) {
    return strategy.andThen(event -> event.setDescription(description));
  }

  /**
   * Returns a composed EventCreationStrategy with the given location.
   *
   * @param strategy the base strategy
   * @param location the location to set
   * @return a composed EventCreationStrategy with the given location
   */
  static EventCreationStrategy withLocation(EventCreationStrategy strategy, String location) {
    return strategy.andThen(event -> event.setLocation(location));
  }

  /**
   * Returns a composed EventCreationStrategy with the given visibility.
   *
   * @param strategy the base strategy
   * @param isPublic whether the event is public
   * @return a composed EventCreationStrategy with the given visibility
   */
  static EventCreationStrategy withVisibility(EventCreationStrategy strategy, boolean isPublic) {
    return strategy.andThen(event -> event.setPublic(isPublic));
  }

  /**
   * Returns a composed EventCreationStrategy with the all-day setting.
   *
   * @param strategy the base strategy
   * @param isAllDay whether the event is an all-day event
   * @return a composed EventCreationStrategy with the all-day setting
   */
  static EventCreationStrategy withAllDay(EventCreationStrategy strategy, boolean isAllDay) {
    return strategy.andThen(event -> event.setAllDay(isAllDay));
  }

}
