package model.calendar;

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
   * Returns a composed filter that represents a logical OR of this filter and another.
   * This is a default method allowing filters to be chained together.
   *
   * @param other another EventFilter
   * @return a composed filter that represents the logical OR of this filter and the other
   */
  default EventFilter or(EventFilter other) {
    return event -> matches(event) || other.matches(event);
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
}
