package controller.parser;

import java.util.HashMap;
import java.util.Map;

import controller.ICommandFactory;

public class BaseCommandParser extends AbstractCommandParser {
  private final Map<String, AbstractCommandParser> commandParsers;
  private final CommandParserFactory parserFactory;

  public BaseCommandParser(ICommandFactory commandFactory) {
    super(commandFactory);
    this.commandParsers = new HashMap<>();
    this.parserFactory = new CommandParserFactory(commandFactory);
    initializeParsers();
  }

  private void initializeParsers() {
    for (String command : VALID_COMMANDS) {
      if (!command.equals("exit")) {  // Skip exit as it's handled separately
        commandParsers.put(command, parserFactory.createParser(command));
      }
    }
  }

  @Override
  public CommandParser.CommandWithArgs parseCommand(String commandString) {
    validateCommand(commandString);

    String[] parts = commandString.trim().split("\\s+");
    String command = parts[0].toLowerCase();

    AbstractCommandParser parser = commandParsers.get(command);
    if (parser == null) {
      throw new IllegalArgumentException("Unsupported command: " + command);
    }

    return parser.parseCommand(commandString);
  }
}
