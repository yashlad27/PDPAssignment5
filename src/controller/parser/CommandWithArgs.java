package controller.parser;

import controller.command.ICommand;

public class CommandWithArgs {
  private final ICommand command;
  private final String[] args;

  public CommandWithArgs(ICommand command, String[] args) {
    this.command = command;
    this.args = args;
  }

  public ICommand getCommand() {
    return command;
  }

  public String[] getArgs() {
    return args;
  }
}