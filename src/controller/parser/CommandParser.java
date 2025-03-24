package controller.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
import java.util.List;

import controller.ICommandFactory;
import controller.command.ICommand;

/**
 * Improved parser for command-line input with extensible command pattern support.
 * Now accepts the ICommandFactory interface rather than the concrete implementation.
 */
public class CommandParser {

  private final ICommandFactory commandFactory;
  private final Map<String, CommandPattern> commandPatterns;
  private static final List<String> VALID_COMMANDS = Arrays.asList(
    "create", "use", "show", "edit", "copy", "exit", "print", "export"
  );
  private static final Set<String> VALID_COMMANDS_SET = new HashSet<>(VALID_COMMANDS);

  /**
   * Constructs a new CommandParser.
   *
   * @param commandFactory the factory for creating commands (using the interface)
   */
  public CommandParser(ICommandFactory commandFactory) {
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

    registerPattern("create_all_day_event",
            Pattern.compile("create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                    + "on (\\d{4}-\\d{2}-\\d{2})"
                    + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateAllDayEventCommand);

    registerPattern("create_recurring_event",
            Pattern.compile("create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) from "
                    + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                    + "repeats ([MTWRFSU]+) for (\\d+) times(?:\\s+desc\\s+\"([^\"]+)\")?"
                    + "(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateRecurringEventCommand);

    registerPattern("create_recurring_until_event",
            Pattern.compile("create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) from "
                    + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                    + "repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})"
                    + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateRecurringUntilEventCommand);

    registerPattern("create_all_day_recurring_event",
            Pattern.compile("create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                    + "on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times"
                    + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateAllDayRecurringEventCommand);

    registerPattern("create_all_day_recurring_until_event",
            Pattern.compile("create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                    + "on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})"
                    + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateAllDayRecurringUntilEventCommand);

    registerPattern("edit_single_event",
            Pattern.compile("edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) "
                    + "with \"?([^\"]+)\"?"), this::parseEditSingleEventCommand);

    registerPattern("edit_event_time",
            Pattern.compile("edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) "
                    + "to (\\S+T\\S+) with \"?([^\"]+)\"?"), this::parseEditEventTimeCommand);

    registerPattern("print_events_date",
            Pattern.compile("print events on (\\d{4}-\\d{2}-\\d{2})"),
            this::parsePrintEventsDateCommand);

    registerPattern("print_events_range",
            Pattern.compile("print events from (\\d{4}-\\d{2}-\\d{2}) "
                    + "to (\\d{4}-\\d{2}-\\d{2})"),
            this::parsePrintEventsRangeCommand);

    registerPattern("create_calendar",
            Pattern.compile("create calendar --name ([\\w-]+) --timezone ([\\w/]+)"),
            this::parseCreateCalendarCommand);

    // Edit calendar pattern
    registerPattern("edit_calendar",
            Pattern.compile("edit calendar --name ([\\w-]+) --property (\\w+) ([\\w/]+)"),
            this::parseEditCalendarCommand);

    // Use calendar pattern
    registerPattern("use_calendar",
            Pattern.compile("use calendar --name ([\\w-]+)"),
            this::parseUseCalendarCommand);

    // Copy single event pattern
    registerPattern("copy_event",
            Pattern.compile("copy event \"?([^\"]*)\"? "
                    + "on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                    + "--target ([\\w-]+) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"),
            this::parseCopyEventCommand);

    // Copy events on date pattern
    registerPattern("copy_events_on_date",
            Pattern.compile("copy events on (\\d{4}-\\d{2}-\\d{2}) --target ([\\w-]+) "
                    + "to (\\d{4}-\\d{2}-\\d{2})"),
            this::parseCopyEventsOnDateCommand);

    // Copy events between dates pattern
    registerPattern("copy_events_between_dates",
            Pattern.compile("copy events between (\\d{4}-\\d{2}-\\d{2}) "
                    + "and (\\d{4}-\\d{2}-\\d{2}) "
                    + "--target ([\\w-]+) to (\\d{4}-\\d{2}-\\d{2})"),
            this::parseCopyEventsBetweenDatesCommand);

    registerPattern("show_status",
            Pattern.compile("show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"),
            this::parseShowStatusCommand

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
      throw new IllegalArgumentException("Command cannot be empty");
    }

    String[] parts = commandString.trim().split("\\s+");
    if (parts.length == 0) {
      throw new IllegalArgumentException("Invalid command format");
    }

    String command = parts[0].toLowerCase();

    // Check if the command is valid
    if (!VALID_COMMANDS_SET.contains(command)) {
      throw new IllegalArgumentException("Invalid command: " + command + ". Valid commands are: " +
        String.join(", ", VALID_COMMANDS));
    }

    try {
      if (command.equals("exit")) {
        return new CommandWithArgs(commandFactory.getCommand("exit"), new String[0]);
      } else if (command.equals("create")) {
        return handleCreateCommand(parts);
      } else if (command.equals("use")) {
        return handleUseCommand(parts);
      } else if (command.equals("show")) {
        return handleShowCommand(parts);
      } else if (command.equals("edit")) {
        return handleEditCommand(parts);
      } else if (command.equals("copy")) {
        return handleCopyCommand(parts);
      } else if (command.equals("print")) {
        return handlePrintCommand(parts);
      } else if (command.equals("export")) {
        return handleExportCommand(parts);
      } else {
        throw new IllegalArgumentException("Unsupported command: " + command);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing command: " + e.getMessage());
    }
  }

  /**
   * Parse create event command.
   */
  private CommandWithArgs parseCreateEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

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
   * Parse create all day event command.
   */
  private CommandWithArgs parseCreateAllDayEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = matcher.group(2);
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    String[] args = {
            "allday",
            eventName,
            matcher.group(3),
            String.valueOf(autoDecline),
            matcher.group(4),
            matcher.group(5),
            matcher.group(6) != null ? "false" : "true"
    };
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse create recurring event command.
   */
  private CommandWithArgs parseCreateRecurringEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

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
   * Parse create recurring until event command.
   */
  private CommandWithArgs parseCreateRecurringUntilEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = matcher.group(2);
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    String[] args = {
            "recurring-until",
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
   * Parse create all day recurring event command.
   */
  private CommandWithArgs parseCreateAllDayRecurringEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = matcher.group(2);
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    String[] args = {
            "allday-recurring",
            eventName,
            matcher.group(3),
            matcher.group(4),
            matcher.group(5),
            String.valueOf(autoDecline),
            matcher.group(6),
            matcher.group(7),
            matcher.group(8) != null ? "false" : "true"
    };
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse create all day recurring until event command.
   */
  private CommandWithArgs parseCreateAllDayRecurringUntilEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = matcher.group(2);
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    String[] args = {
            "allday-recurring-until",
            eventName,
            matcher.group(3),
            matcher.group(4),
            matcher.group(5),
            String.valueOf(autoDecline),
            matcher.group(6),
            matcher.group(7),
            matcher.group(8) != null ? "false" : "true"
    };
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse edit single event command.
   */
  private CommandWithArgs parseEditSingleEventCommand(Matcher matcher) {
    ICommand editCommand = commandFactory.getCommand("edit");

    String property = matcher.group(1);
    String subject = matcher.group(2);
    String startDateTime = matcher.group(3);
    String newValue = matcher.group(4);

    String[] args = {
            "single",
            property,
            subject,
            startDateTime,
            newValue
    };
    return new CommandWithArgs(editCommand, args);
  }

  /**
   * Parse print events on date command.
   */
  private CommandWithArgs parsePrintEventsDateCommand(Matcher matcher) {
    ICommand printCommand = commandFactory.getCommand("print");

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
    ICommand printCommand = commandFactory.getCommand("print");

    String[] args = {
            "date_range",
            matcher.group(1),
            matcher.group(2)
    };
    return new CommandWithArgs(printCommand, args);
  }

  private CommandWithArgs parseCreateCalendarCommand(Matcher matcher) {
    ICommand calendarCommand = commandFactory.getCommand("create");

    String[] args = {
            "calendar",
            "--name",
            matcher.group(1),
            "--timezone",
            matcher.group(2)
    };
    return new CommandWithArgs(calendarCommand, args);
  }

  private CommandWithArgs parseEditCalendarCommand(Matcher matcher) {
    ICommand calendarCommand = commandFactory.getCommand("edit");

    String[] args = {
            "calendar",
            "--name",
            matcher.group(1),
            "--property",
            matcher.group(2),
            matcher.group(3)
    };
    return new CommandWithArgs(calendarCommand, args);
  }

  private CommandWithArgs parseUseCalendarCommand(Matcher matcher) {
    ICommand calendarCommand = commandFactory.getCommand("use");

    String[] args = {
            "calendar",
            "--name",
            matcher.group(1)
    };
    return new CommandWithArgs(calendarCommand, args);
  }

  private CommandWithArgs parseCopyEventCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String[] args = {
            "copy",
            "event",
            matcher.group(1),
            "on",
            matcher.group(2),
            "--target",
            matcher.group(3),
            "to",
            matcher.group(4)
    };
    return new CommandWithArgs(copyCommand, args);
  }

  private CommandWithArgs parseCopyEventsOnDateCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String[] args = {
            "copy",
            "events",
            "on",
            matcher.group(1),
            "--target",
            matcher.group(2),
            "to",
            matcher.group(3)
    };
    return new CommandWithArgs(copyCommand, args);
  }

  private CommandWithArgs parseCopyEventsBetweenDatesCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String[] args = {
            "copy",
            "events",
            "between",
            matcher.group(1),
            "and",
            matcher.group(2),
            "--target",
            matcher.group(3),
            "to",
            matcher.group(4)
    };
    return new CommandWithArgs(copyCommand, args);
  }

  /**
   * Parse show status command.
   */
  private CommandWithArgs parseShowStatusCommand(Matcher matcher) {
    ICommand statusCommand = commandFactory.getCommand("show");

    String[] args = {
            matcher.group(1)
    };
    return new CommandWithArgs(statusCommand, args);
  }

  /**
   * Parse export command.
   */
  private CommandWithArgs parseExportCommand(Matcher matcher) {
    ICommand exportCommand = commandFactory.getCommand("export");

    String[] args = {
            matcher.group(1)
    };
    return new CommandWithArgs(exportCommand, args);
  }

  private CommandWithArgs parseEditEventTimeCommand(Matcher matcher) {
    ICommand editCommand = commandFactory.getCommand("edit");

    String property = matcher.group(1);
    String subject = matcher.group(2);
    String startDateTime = matcher.group(3);
    String endDateTime = matcher.group(4);
    String newValue = matcher.group(5);

    String[] args = {
            "single",
            property,
            subject,
            startDateTime,
            newValue
    };
    return new CommandWithArgs(editCommand, args);
  }

  private CommandWithArgs handleCreateCommand(String[] parts) {
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
    return new CommandWithArgs(commandFactory.getCommand("create"), new String[]{calendarName});
  }

  private CommandWithArgs handleUseCommand(String[] parts) {
    if (parts.length < 3) {
        throw new IllegalArgumentException("Invalid use command format");
    }

    if (!parts[1].equals("calendar")) {
        throw new IllegalArgumentException("Invalid use command format");
    }

    if (!parts[2].equals("--name")) {
        throw new IllegalArgumentException("Invalid use command format");
    }

    if (parts.length < 4) {
        throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    String calendarName = parts[3];
    return new CommandWithArgs(commandFactory.getCommand("use"), new String[]{calendarName});
  }

  private CommandWithArgs handleShowCommand(String[] parts) {
    if (parts.length < 2) {
        throw new IllegalArgumentException("Invalid show command format");
    }

    String subCommand = parts[1].toLowerCase();
    if (!subCommand.equals("status")) {
        throw new IllegalArgumentException("Invalid show command format");
    }

    return new CommandWithArgs(commandFactory.getCommand("show"), new String[0]);
  }

  private CommandWithArgs handleEditCommand(String[] parts) {
    if (parts.length < 2) {
        throw new IllegalArgumentException("Invalid edit command format");
    }

    String subCommand = parts[1].toLowerCase();
    if (!subCommand.equals("event")) {
        throw new IllegalArgumentException("Invalid edit command format");
    }

    if (parts.length < 3) {
        throw new IllegalArgumentException("Event name cannot be empty");
    }

    String eventName = parts[2];
    return new CommandWithArgs(commandFactory.getCommand("edit"), new String[]{eventName});
  }

  private CommandWithArgs handleCopyCommand(String[] parts) {
    if (parts.length < 2) {
        throw new IllegalArgumentException("Invalid copy command format");
    }

    String subCommand = parts[1].toLowerCase();
    if (!subCommand.equals("event")) {
        throw new IllegalArgumentException("Invalid copy command format");
    }

    if (parts.length < 3) {
        throw new IllegalArgumentException("Event name cannot be empty");
    }

    String eventName = parts[2];
    return new CommandWithArgs(commandFactory.getCommand("copy"), new String[]{eventName});
  }

  private CommandWithArgs handlePrintCommand(String[] parts) {
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
        return new CommandWithArgs(commandFactory.getCommand("print_events_date"),
            new String[]{parts[3]});
    } else if (dateArg.equals("from")) {
        if (parts.length < 6 || !parts[4].equals("to")) {
            throw new IllegalArgumentException("Invalid date range format");
        }
        return new CommandWithArgs(commandFactory.getCommand("print_events_range"),
            new String[]{parts[3], parts[5]});
    } else {
        throw new IllegalArgumentException("Invalid print command format");
    }
  }

  private CommandWithArgs handleExportCommand(String[] parts) {
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
    return new CommandWithArgs(commandFactory.getCommand("export"), new String[]{fileName});
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