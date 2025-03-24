package controller.parser.commands;

import controller.ICommandFactory;
import controller.parser.AbstractCommandParser;
import controller.parser.CommandParser;

public class ExportCommandParser extends AbstractCommandParser {
  public ExportCommandParser(ICommandFactory commandFactory) {
    super(commandFactory);
  }

  @Override
  public CommandParser.CommandWithArgs parseCommand(String commandString) {
    String[] parts = commandString.trim().split("\\s+");

    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid export command format");
    }

    String subCommand = parts[1].toLowerCase();
    if (!subCommand.equals("cal")) {
      throw new IllegalArgumentException("Invalid export command format");
    }

    if (parts.length < 3) {
      throw new IllegalArgumentException("File name cannot be empty");
    }

    String fileName = parts[2];
    return new CommandParser.CommandWithArgs(commandFactory.getCommand("export"), new String[]{fileName});
  }
}
