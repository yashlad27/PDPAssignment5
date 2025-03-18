package controller.command;

import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Adapter class that converts a CommandExecutor (functional interface) to an ICommand.
 * This allows lambda-based commands to be used where ICommand objects are expected.
 */
public class CommandAdapter implements ICommand {



  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    return "";
  }

  @Override
  public String getName() {
    return "";
  }
}
