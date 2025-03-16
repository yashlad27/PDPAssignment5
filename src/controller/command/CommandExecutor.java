package controller.command;

import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;


/**
 * Functional interface for executing calendar commands.
 * This interface simplifies the command pattern by using a functional approach.
 */
@FunctionalInterface
public interface CommandExecutor {

  /**
   * Executes a command with the given arguments.
   *
   * @param args the command arguments
   * @return a string result representing the outcome of the command execution
   * @throws ConflictingEventException if the command would create a conflicting event
   * @throws InvalidEventException     if the command parameters are invalid
   * @throws EventNotFoundException    if an event required by the command cannot be found
   */
  String execute(String[] args) throws ConflictingEventException,
          InvalidEventException, EventNotFoundException;
}
