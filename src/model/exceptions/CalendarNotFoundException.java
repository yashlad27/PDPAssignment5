package model.exceptions;

/**
 * Exception thrown when attempting to access a calendar that does not exist in the system.
 * This exception is used to indicate that a requested calendar could not be found,
 * either because it was deleted or never created.
 */
public class CalendarNotFoundException extends Exception {
  /**
   * Constructs a new CalendarNotFoundException with the specified error message.
   *
   * @param message the error message describing the missing calendar
   */
  public CalendarNotFoundException(String message) {
    super(message);
  }
}
