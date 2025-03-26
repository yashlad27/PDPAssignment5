package model.exceptions;

/**
 * Exception thrown when an event is created with invalid parameters or properties.
 * This exception is used to indicate that an event's data violates the application's
 * business rules or constraints, such as invalid dates, missing required fields,
 * or invalid property values.
 */
public class InvalidEventException extends Exception {

  /**
   * Constructs a new InvalidEventException with the specified error message.
   *
   * @param message the error message describing the invalid event parameters
   */
  public InvalidEventException(String message) {
    super(message);
  }
}