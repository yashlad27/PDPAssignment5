package controller.parser.commands;

import controller.ICommandFactory;
import controller.parser.AbstractCommandParser;
import controller.parser.CommandParser;

public class PrintCommandParser extends AbstractCommandParser {
  public PrintCommandParser(ICommandFactory commandFactory) {
    super(commandFactory);
  }

  @Override
  public CommandParser.CommandWithArgs parseCommand(String commandString) {
    String[] parts = commandString.trim().split("\\s+");

    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid print command format");
    }

    String subCommand = parts[1].toLowerCase();
    if (!subCommand.equals("events")) {
      throw new IllegalArgumentException("Invalid print command format");
    }

    if (parts.length < 3) {
      throw new IllegalArgumentException("Invalid print command format");
    }

    String dateArg = parts[2].toLowerCase();
    if (dateArg.equals("on")) {
      if (parts.length < 4) {
        throw new IllegalArgumentException("Date cannot be empty");
      }
      return new CommandParser.CommandWithArgs(commandFactory.getCommand("print_events_date"),
              new String[]{parts[3]});
    } else if (dateArg.equals("from")) {
      if (parts.length < 6 || !parts[4].equals("to")) {
        throw new IllegalArgumentException("Invalid date range format");
      }
      return new CommandParser.CommandWithArgs(commandFactory.getCommand("print_events_range"),
              new String[]{parts[3], parts[5]});
    } else {
      throw new IllegalArgumentException("Invalid print command format");
    }
  }
}
