package model.exceptions;

/**
 * Exception is thrown when conflicting events are detected.
 */
public class ConflictingEventException extends RuntimeException {

  public ConflictingEventException(String message) {
    super(message);
  }
}