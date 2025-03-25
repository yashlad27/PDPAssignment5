package controller.command.create.strategy;

import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Strategy interface for creating different types of calendar events (single, recurring, all-day).
 * Each implementation handles a specific event creation algorithm.
 */
public interface EventCreator {

  /**
   * Creates an event without adding it to a calendar.
   * This method handles the creation logic specific to each type of event.
   *
   * @return the created event
   * @throws InvalidEventException if the event parameters are invalid
   */
  Event createEvent() throws InvalidEventException;

  /**
   * Executes the event creation strategy by creating an event and adding it to the calendar.
   * This is the main method that will be called by clients to perform the complete event
   * creation operation.
   *
   * @param calendar the calendar in which to create the event
   * @return a result message indicating success or failure
   * @throws ConflictingEventException if the event conflicts with existing events
   * @throws InvalidEventException     if the event parameters are invalid
   */
  String executeCreation(ICalendar calendar) throws ConflictingEventException, InvalidEventException;

  /**
   * Creates an appropriate event creation strategy based on the event type.
   *
   * @param type the type of event (single, recurring, allday, etc.)
   * @param args the arguments for event creation
   * @return the appropriate EventCreator implementation
   * @throws InvalidEventException if the event type is unknown or arguments are invalid
   */
  static EventCreator forType(String type, String[] args) throws InvalidEventException {
    switch (type) {
      case "single":
        return new SingleEventCreator(args);
      case "recurring":
        return new RecurringEventCreator(args);
      case "allday":
        return new AllDayEventCreator(args);
      case "recurring-until":
        return new RecurringUntilEventCreator(args);
      case "allday-recurring":
        return new AllDayRecurringEventCreator(args);
      case "allday-recurring-until":
        return new AllDayRecurringUntilEventCreator(args);
      default:
        throw new IllegalArgumentException("Unknown event type: " + type);
    }
  }
}