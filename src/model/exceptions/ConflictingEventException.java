package model.exceptions;

/**
 * Exception thrown when attempting to create or modify an event that conflicts
 * with existing events.
 * This exception is used to prevent scheduling conflicts by indicating that the requested
 * event's time slot overlaps with one or more existing events in the calendar.
 */
public class ConflictingEventException extends Exception {

  /**
   * Constructs a new ConflictingEventException with the specified error message.
   *
   * @param message the error message describing the event conflict
   */
  public ConflictingEventException(String message) {
    super(message);
  }
}