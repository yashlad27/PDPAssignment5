package controller.command;

/**
 * Interface for command pattern implementation. Each command represents a specific operation in the
 * calendar application.
 */
public interface ICommand {

  /**
   * Executes the command with the given arguments.
   *
   * @param args the command arguments
   * @return a string representing the result of command execution
   */
  String execute(String[] args);

  /**
   * Gets the name of the command.
   *
   * @return the command name
   */
  String getName();
}