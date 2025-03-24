package controller.parser.commands;

import controller.ICommandFactory;
import controller.parser.AbstractCommandParser;
import controller.parser.CommandParser;

public class CreateCommandParser extends AbstractCommandParser {
  public CreateCommandParser(ICommandFactory commandFactory) {
    super(commandFactory);
  }

  @Override
  public CommandParser.CommandWithArgs parseCommand(String commandString) {
    String[] parts = commandString.trim().split("\\s+");

    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid create command format");
    }

    String subCommand = parts[1].toLowerCase();
    if (!subCommand.equals("calendar")) {
      throw new IllegalArgumentException("Invalid create command format");
    }

    if (parts.length < 3) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    String calendarName = parts[2];
    return new CommandParser.CommandWithArgs(commandFactory.getCommand("create"), new String[]{calendarName});
  }
}
