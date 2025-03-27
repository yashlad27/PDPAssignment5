package model.exceptions;

/**
 * Exception thrown when attempting to access or modify an event that does not exist in
 * the calendar.
 * This exception is used to indicate that a requested event could not be found,
 * either because it was deleted or never created.
 */
public class EventNotFoundException extends Exception {

  /**
   * Constructs a new EventNotFoundException with the specified error message.
   *
   * @param message the error message describing the missing event
   */
  public EventNotFoundException(String message) {
    super(message);
  }
}
