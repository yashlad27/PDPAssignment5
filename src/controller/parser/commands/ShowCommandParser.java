package controller.parser.commands;

import controller.ICommandFactory;
import controller.parser.AbstractCommandParser;
import controller.parser.CommandParser;

public class ShowCommandParser extends AbstractCommandParser {
  public ShowCommandParser(ICommandFactory commandFactory) {
    super(commandFactory);
  }

  @Override
  public CommandParser.CommandWithArgs parseCommand(String commandString) {
    String[] parts = commandString.trim().split("\\s+");

    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid show command format");
    }

    String subCommand = parts[1].toLowerCase();
    if (!subCommand.equals("status")) {
      throw new IllegalArgumentException("Invalid show command format");
    }

    return new CommandParser.CommandWithArgs(commandFactory.getCommand("show"), new String[0]);
  }
}
