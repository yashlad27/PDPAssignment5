package controller.command.create.strategy;

import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Strategy interface for different event creation methods. Following the Strategy Pattern to
 * encapsulate different event creation algorithms.
 *
 * <p>This interface defines the contract for creating and adding different types of events to a
 * calendar.
 * Each concrete implementation handles a specific type of event creation (single, recurring,
 * all-day, etc.).
 */
public interface EventCreator {

  /**
   * Creates an event without adding it to a calendar. This method handles the creation logic
   * specific to each type of event.
   *
   * @return the created event
   * @throws InvalidEventException if the event parameters are invalid
   */
  Event createEvent() throws InvalidEventException;

  /**
   * Executes the event creation strategy by creating an event and adding it to the calendar. This
   * is the main method that will be called by clients to perform the complete event creation
   * operation.
   *
   * @param calendar the calendar in which to create the event
   * @return a result message indicating success or failure
   * @throws ConflictingEventException if the event conflicts with existing events
   * @throws InvalidEventException     if the event parameters are invalid
   */
  String executeCreation(ICalendar calendar)
      throws ConflictingEventException, InvalidEventException;

  /**
   * Factory method to create the appropriate strategy based on event type. This method encapsulates
   * the logic for selecting the right strategy implementation.
   *
   * @param type the type of event to create
   * @param args the arguments for event creation
   * @return the appropriate strategy for the specified event type
   * @throws InvalidEventException if the event type is unknown or the arguments are invalid
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

  /**
   * Validates the basic event parameters. This method can be used by concrete implementations to
   * validate common parameters.
   *
   * @param eventName the name of the event
   * @throws InvalidEventException if the event name is invalid
   */
  default void validateEventName(String eventName) throws InvalidEventException {
    if (eventName == null || eventName.trim().isEmpty()) {
      throw new InvalidEventException("Event name cannot be empty");
    }
  }
}