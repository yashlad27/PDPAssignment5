package controller.command;

import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Adapter class that converts a CommandExecutor (functional interface) to an ICommand.
 * This allows lambda-based commands to be used where ICommand objects are expected.
 */
public class CommandAdapter implements ICommand {

  private final CommandExecutor executor;
  private final String commandName;

  /**
   * Creates a new CommandAdapter.
   *
   * @param commandName the name of the command
   * @param executor    the command executor to adapt
   */
  public CommandAdapter(String commandName, CommandExecutor executor) {
    if (commandName == null || commandName.trim().isEmpty()) {
      throw new IllegalArgumentException("Command name cannot be null or empty");
    }
    if (executor == null) {
      throw new IllegalArgumentException("Command executor cannot be null");
    }
    this.commandName = commandName;
    this.executor = executor;
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    return executor.execute(args);
  }

  @Override
  public String getName() {
    return commandName;
  }
}
