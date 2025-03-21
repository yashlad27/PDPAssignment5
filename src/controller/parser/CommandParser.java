package controller.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.command.CommandAdapter;
import controller.command.CommandExecutor;
import controller.command.CommandFactory;
import controller.command.ICommand;

/**
 * Improved parser for command-line input with extensible command pattern support.
 */
public class CommandParser {

  private final CommandFactory commandFactory;
  private final Map<String, CommandPattern> commandPatterns;

  /**
   * Constructs a new CommandParser.
   *
   * @param commandFactory the factory for creating commands
   */
  public CommandParser(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
    this.commandPatterns = new HashMap<>();
    registerDefaultCommandPatterns();
  }

  /**
   * Registers default command patterns.
   */
  private void registerDefaultCommandPatterns() {
    // Create event patterns
    registerPattern("create_event",
            Pattern.compile("create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                    + "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                    + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
                    + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateEventCommand);

    // Recurring event pattern with occurrences
    registerPattern("create_recurring_event",
            Pattern.compile("create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) from "
                    + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                    + "repeats ([MTWRFSU]+) for (\\d+) times(?:\\s+desc\\s+\"([^\"]+)\")?"
                    + "(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateRecurringEventCommand);

    // Add other command patterns...
    registerPattern("edit_single_event",
            Pattern.compile("edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) to "
                    + "(\\S+T\\S+) with \"?([^\"]+)\"?"),
            this::parseEditSingleEventCommand);

    registerPattern("print_events_date",
            Pattern.compile("print events on (\\d{4}-\\d{2}-\\d{2})"),
            this::parsePrintEventsDateCommand);

    registerPattern("print_events_range",
            Pattern.compile("print events from (\\d{4}-\\d{2}-\\d{2}) "
                    + "to (\\d{4}-\\d{2}-\\d{2})"),
            this::parsePrintEventsRangeCommand);

    registerPattern("show_status",
            Pattern.compile("show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"),
            this::parseShowStatusCommand);

    registerPattern("export_calendar",
            Pattern.compile("export cal (.+)"),
            this::parseExportCommand);
  }

  /**
   * Registers a new command pattern.
   *
   * @param name    the name of the pattern
   * @param pattern the regex pattern
   * @param parser  the parser function for the pattern
   */
  public void registerPattern(String name, Pattern pattern, CommandPatternParser parser) {
    commandPatterns.put(name, new CommandPattern(pattern, parser));
  }

  /**
   * Parses a command string and returns the appropriate Command object with arguments.
   *
   * @param commandString the command string to parse
   * @return a Command object that can execute the requested operation
   * @throws IllegalArgumentException if the command is invalid or unsupported
   */
  public CommandWithArgs parseCommand(String commandString) {
    if (commandString == null || commandString.trim().isEmpty()) {
      throw new IllegalArgumentException("Command string cannot be null or empty");
    }

    commandString = commandString.trim();

    // Check for exit command
    if (commandString.equalsIgnoreCase("exit")) {
      CommandExecutor exitExecutor = commandFactory.getCommandExecutor("exit");
      ICommand exitCommand = new CommandAdapter("exit", exitExecutor);
      return new CommandWithArgs(exitCommand, new String[0]);
    }

    // Try all registered patterns
    for (CommandPattern commandPattern : commandPatterns.values()) {
      Matcher matcher = commandPattern.getPattern().matcher(commandString);
      if (matcher.matches()) {
        return commandPattern.getParser().parse(matcher);
      }
    }

    throw new IllegalArgumentException("Unrecognized or unsupported command: " + commandString);
  }

  /**
   * Parse create event command.
   */
  private CommandWithArgs parseCreateEventCommand(Matcher matcher) {
    CommandExecutor createExecutor = commandFactory.getCommandExecutor("create");
    ICommand createCommand = new CommandAdapter("create", createExecutor);

    boolean autoDecline = matcher.group(1) != null;

    String eventName = matcher.group(2);
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    String[] args = {
            "single",
            eventName,
            matcher.group(3),
            matcher.group(4),
            matcher.group(5),
            matcher.group(6),
            matcher.group(7) != null ? "false" : "true",
            String.valueOf(autoDecline)
    };
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse create recurring event command.
   */
  private CommandWithArgs parseCreateRecurringEventCommand(Matcher matcher) {
    CommandExecutor createExecutor = commandFactory.getCommandExecutor("create");
    ICommand createCommand = new CommandAdapter("create", createExecutor);

    boolean autoDecline = matcher.group(1) != null;

    String eventName = matcher.group(2);
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    String[] args = {
            "recurring",
            eventName,
            matcher.group(3),
            matcher.group(4),
            matcher.group(5),
            matcher.group(6),
            String.valueOf(autoDecline),
            matcher.group(7),
            matcher.group(8),
            matcher.group(9) != null ? "false" : "true"
    };
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse edit single event command.
   */
  private CommandWithArgs parseEditSingleEventCommand(Matcher matcher) {
    CommandExecutor editExecutor = commandFactory.getCommandExecutor("edit");
    ICommand editCommand = new CommandAdapter("edit", editExecutor);

    String property = matcher.group(1);
    String subject = matcher.group(2);

    String[] args = {
            "single",
            property,
            subject,
            matcher.group(3),
            matcher.group(5)
    };
    return new CommandWithArgs(editCommand, args);
  }

  /**
   * Parse print events on date command.
   */
  private CommandWithArgs parsePrintEventsDateCommand(Matcher matcher) {
    CommandExecutor printExecutor = commandFactory.getCommandExecutor("print");
    ICommand printCommand = new CommandAdapter("print", printExecutor);

    String[] args = {
            "on_date",
            matcher.group(1)
    };
    return new CommandWithArgs(printCommand, args);
  }

  /**
   * Parse print events range command.
   */
  private CommandWithArgs parsePrintEventsRangeCommand(Matcher matcher) {
    CommandExecutor printExecutor = commandFactory.getCommandExecutor("print");
    ICommand printCommand = new CommandAdapter("print", printExecutor);

    String[] args = {
            "date_range",
            matcher.group(1),
            matcher.group(2)
    };
    return new CommandWithArgs(printCommand, args);
  }

  /**
   * Parse show status command.
   */
  private CommandWithArgs parseShowStatusCommand(Matcher matcher) {
    CommandExecutor statusExecutor = commandFactory.getCommandExecutor("show");
    ICommand statusCommand = new CommandAdapter("show", statusExecutor);

    String[] args = {
            matcher.group(1)
    };
    return new CommandWithArgs(statusCommand, args);
  }

  /**
   * Parse export command.
   */
  private CommandWithArgs parseExportCommand(Matcher matcher) {
    CommandExecutor exportExecutor = commandFactory.getCommandExecutor("export");
    ICommand exportCommand = new CommandAdapter("export", exportExecutor);

    String[] args = {
            matcher.group(1)
    };
    return new CommandWithArgs(exportCommand, args);
  }

  /**
   * Helper class to hold a command and its arguments.
   */
  public static class CommandWithArgs {
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

    public String execute() throws Exception {
      return command.execute(args);
    }
  }

  /**
   * Class that represents a command pattern with its regex and parser.
   */
  private static class CommandPattern {
    private final Pattern pattern;
    private final CommandPatternParser parser;

    public CommandPattern(Pattern pattern, CommandPatternParser parser) {
      this.pattern = pattern;
      this.parser = parser;
    }

    public Pattern getPattern() {
      return pattern;
    }

    public CommandPatternParser getParser() {
      return parser;
    }
  }
}