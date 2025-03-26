package controller.command.copy.strategy;

import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Interface for copy strategies. Each strategy implements a specific
 * type of event copying operation.
 */
public interface CopyStrategy {

  /**
   * Execute the copy strategy with the given arguments.
   *
   * @param args The command arguments
   * @return Result message of the copy operation
   * @throws CalendarNotFoundException if the target calendar is not found
   * @throws EventNotFoundException    if the source event is not found
   * @throws ConflictingEventException if there's a conflict in the target calendar
   * @throws InvalidEventException     if the event parameters are invalid
   */
  String execute(String[] args) throws CalendarNotFoundException,
          EventNotFoundException,
          ConflictingEventException,
          InvalidEventException;

  /**
   * Checks if this strategy can handle the given command args.
   *
   * @param args The command arguments
   * @return true if this strategy can handle the command, false otherwise
   */
  boolean canHandle(String[] args);
}