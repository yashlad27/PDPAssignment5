package model.exceptions;

/**
 * Exception thrown when an invalid timezone is specified for a calendar.
 * This exception is used to indicate that a timezone string provided to the application
 * is not recognized or is not a valid timezone identifier.
 */
public class InvalidTimezoneException extends Exception {
  /**
   * Constructs a new InvalidTimezoneException with the specified error message.
   *
   * @param message the error message describing the invalid timezone
   */
  public InvalidTimezoneException(String message) {
    super(message);
  }
}
