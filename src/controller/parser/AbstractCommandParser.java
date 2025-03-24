package controller.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import controller.ICommandFactory;

public abstract class AbstractCommandParser {
  protected final ICommandFactory commandFactory;
  protected static final List<String> VALID_COMMANDS = Arrays.asList(
          "create", "use", "show", "edit", "copy", "exit", "print", "export"
  );
  protected static final Set<String> VALID_COMMANDS_SET = new HashSet<>(VALID_COMMANDS);

  protected AbstractCommandParser(ICommandFactory commandFactory) {
    this.commandFactory = commandFactory;
  }

  public abstract CommandParser.CommandWithArgs parseCommand(String commandString);

  protected void validateCommand(String commandString) {
    if (commandString == null || commandString.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    String[] parts = commandString.trim().split("\\s+");
    if (parts.length == 0) {
      throw new IllegalArgumentException("Invalid command format");
    }

    String command = parts[0].toLowerCase();
    if (!VALID_COMMANDS_SET.contains(command)) {
      throw new IllegalArgumentException("Invalid command: " + command + ". Valid commands are: " +
              String.join(", ", VALID_COMMANDS));
    }
  }
}
