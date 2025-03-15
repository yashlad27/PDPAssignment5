package model.exceptions;

/**
 * Exception thrown when an invalid event is created.
 */
public class InvalidEventException extends RuntimeException {

  public InvalidEventException(String message) {
    super(message);
  }
}