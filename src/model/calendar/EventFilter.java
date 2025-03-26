package model.calendar;

import java.util.ArrayList;
import java.util.List;

import model.event.Event;

/**
 * Functional interface for filtering events based on specified criteria.
 * This interface is used to implement various filtering strategies for calendar events.
 */
@FunctionalInterface
public interface EventFilter {

  /**
   * Tests if the provided event matches the filter criteria.
   *
   * @param event the event to test
   * @return true if the event matches the criteria, false otherwise
   */
  boolean matches(Event event);

  /**
   * Returns a composed filter that represents a logical AND of this filter and another.
   * This is a default method allowing filters to be chained together.
   *
   * @param other another EventFilter
   * @return a composed filter that represents the logical AND of this filter and the other
   */
  default EventFilter and(EventFilter other) {
    return event -> matches(event) && other.matches(event);
  }

  /**
   * Returns a filter that represents the logical negation of this filter.
   * This is a default method providing a convenient way to negate a filter.
   *
   * @return a filter that represents the logical negation of this filter
   */
  default EventFilter negate() {
    return event -> !matches(event);
  }

  /**
   * Filters a list of events based on the filter criteria.
   * This is a default method that uses the matches method to filter events.
   *
   * @param events the list of events to filter
   * @return a list of events that match the filter criteria
   */
  default List<Event> filterEvents(List<Event> events) {
    List<Event> filteredEvents = new ArrayList<>();
    for (Event event : events) {
      if (matches(event)) {
        filteredEvents.add(event);
      }
    }
    return filteredEvents;
  }
}
