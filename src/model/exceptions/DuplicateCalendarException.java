package model.exceptions;

/**
 * Exception thrown when attempting to create a calendar with a name that already exists.
 * This exception is used to prevent duplicate calendar names in the system and maintain
 * unique identification of calendars.
 */
public class DuplicateCalendarException extends Exception {
  /**
   * Constructs a new DuplicateCalendarException with the specified error message.
   *
   * @param message the error message describing the duplicate calendar situation
   */
  public DuplicateCalendarException(String message) {
    super(message);
  }
}
