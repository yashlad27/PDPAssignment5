package controller.command;

/**
 * Command for exiting the application.
 */
public class ExitCommand implements ICommand {

  @Override
  public String execute(String[] args) {
    return "Exiting application.";
  }

  @Override
  public String getName() {
    return "exit";
  }
}