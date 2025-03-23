package model.event;

import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Interface defining the strategy for creating different types of events.
 * Each implementation represents a specific event creation algorithm.
 */
public interface EventCreationStrategy {

  /**
   * Creates an event according to the strategy's algorithm.
   *
   * @return the created event
   * @throws InvalidEventException if the event parameters are invalid
   */
  Event createEvent() throws InvalidEventException;

  /**
   * Executes the event creation strategy by creating an event and adding it to the calendar.
   *
   * @param calendar the calendar in which to create the event
   * @return a result message indicating success or failure
   * @throws ConflictingEventException if the event conflicts with existing events
   * @throws InvalidEventException     if the event parameters are invalid
   */
  String executeCreation(ICalendar calendar) throws ConflictingEventException, InvalidEventException;


  /**
   * Factory method to select the appropriate strategy based on event type.
   *
   * @param eventType the type of event to create
   * @param args      the arguments for event creation
   * @return the appropriate strategy for the specified event type
   */
  static EventCreationStrategy forType(String eventType, String[] args) {
    // We'll implement this in the controller layer
    throw new UnsupportedOperationException("Not implemented at model layer");
  }
}