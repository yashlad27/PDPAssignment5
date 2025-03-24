package model.event;

import model.exceptions.InvalidEventException;

/**
 * Interface defining the strategy for creating different types of events.
 * Each implementation represents a specific event creation algorithm.
 */
public interface EventCreationStrategy {

  /**
   * Creates an event according to the strategy's algorithm.
   *
   * @return the created event
   * @throws InvalidEventException if the event parameters are invalid
   */
  Event createEvent() throws InvalidEventException;
}