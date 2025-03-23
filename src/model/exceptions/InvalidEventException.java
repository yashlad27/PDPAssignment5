package model.exceptions;

/**
 * Exception thrown when an invalid event is created.
 */
public class InvalidEventException extends Exception {

  public InvalidEventException(String message) {
    super(message);
  }
}