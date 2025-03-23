package controller.command.create.strategy;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Abstract base class for event creation strategies.
 * Provides common functionality and default implementations for event creation strategies.
 */
public abstract class AbstractEventCreator implements EventCreator {

  /**
   * Validates common event parameters.
   *
   * @param eventName the name of the event
   * @throws InvalidEventException if required parameters are invalid
   */
  protected void validateEventParameters(String eventName) throws InvalidEventException {
    if (eventName == null || eventName.trim().isEmpty()) {
      throw new InvalidEventException("Event name cannot be empty");
    }
  }

  /**
   * Removes surrounding quotes from a string value if present.
   *
   * @param value the string value to process
   * @return the string without surrounding quotes, or the original string if no quotes
   */
  protected String removeQuotes(String value) {
    if (value != null && value.length() >= 2) {
      if ((value.startsWith("\"") && value.endsWith("\"")) ||
              (value.startsWith("'") && value.endsWith("'"))) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }

  /**
   * Default implementation of executeCreation that creates the event and adds it to the calendar.
   * Subclasses can override this if they need special handling.
   *
   * @param calendar the calendar in which to create the event
   * @return a success message
   * @throws ConflictingEventException if the event conflicts with existing events
   * @throws InvalidEventException     if event parameters are invalid
   */
  @Override
  public String executeCreation(ICalendar calendar) throws ConflictingEventException,
          InvalidEventException {
    Event event = createEvent();

    if (event instanceof RecurringEvent) {
      calendar.addRecurringEvent((RecurringEvent) event, getAutoDecline());
    } else {
      calendar.addEvent(event, getAutoDecline());
    }

    return getSuccessMessage(event);
  }

  /**
   * Gets whether automatic decline should be used for this event.
   * Default implementation returns false.
   *
   * @return true if conflicts should automatically lead to declining the event, false otherwise
   */
  protected boolean getAutoDecline() {
    return false;
  }

  /**
   * Gets a success message for the event creation.
   * Subclasses should override this to provide specific success messages.
   *
   * @param event the created event
   * @return a success message
   */
  protected String getSuccessMessage(Event event) {
    return "Event '" + event.getSubject() + "' created successfully.";
  }
}