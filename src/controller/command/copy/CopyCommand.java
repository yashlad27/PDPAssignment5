package controller.command.copy;

/**
 * Interface for copy commands.
 */
public interface CopyCommand {

  /**
   * Executes the copy command.
   *
   * @param args the command arguments
   * @return the result of execution
   * @throws Exception if an error occurs during execution
   */
  String execute(String[] args) throws Exception;

  /**
   * Checks if this command can handler the given arguments.
   *
   * @param args the command arguments passed.
   * @return true if this command can handle the arguments, false otherwise.
   */
  boolean canHandle(String[] args);
}
