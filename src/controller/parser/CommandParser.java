package controller.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.ICommandFactory;
import controller.command.ICommand;

/**
 * Improved parser for command-line input with extensible command pattern support. Now accepts the
 * ICommandFactory interface rather than the concrete implementation.
 */
public class CommandParser {

  private final ICommandFactory commandFactory;
  private final Map<String, CommandPattern> commandPatterns;
  private static final List<String> VALID_COMMANDS = Arrays.asList("create", "use", "show", "edit",
          "copy", "exit", "print", "export");
  private static final Set<String> VALID_COMMANDS_SET = new HashSet<>(VALID_COMMANDS);

  /**
   * Constructs a new CommandParser.
   *
   * @param commandFactory the factory for creating commands (using the interface)
   */
  public CommandParser(ICommandFactory commandFactory) {
    this.commandFactory = commandFactory;
    this.commandPatterns = new HashMap<>();
    initializePatterns();
  }

  /**
   * Registers default command patterns.
   */
  private void initializePatterns() {
    // Create calendar pattern
    registerPattern("create_calendar", Pattern.compile(
                    "create calendar --name ([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "--timezone ([\\w/]+)"),
            this::parseCreateCalendarCommand);

    // Create event pattern
    registerPattern("create_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")"
                            + "?(?:\\s+(private))?"),
            this::parseCreateEventCommand);

    // Create recurring event pattern
    registerPattern("create_recurring_event", Pattern.compile(
                    "create event ([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "repeats ([MTWRFSU]+) for (\\d+) times"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?"),
            this::parseCreateRecurringEventCommand);

    // Create all-day event pattern
    registerPattern("create_all_day_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) on"
                            + " (\\d{4}-\\d{2}-\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")"
                            + "?(?:\\s+(private))?"),
            this::parseCreateAllDayEventCommand);

    // Create event patterns
    registerPattern("create_recurring_until_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) from "
                            + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to "
                            + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s"
                            + "+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateRecurringUntilEventCommand);

    registerPattern("create_all_day_recurring_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s"
                            + "+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateAllDayRecurringEventCommand);

    registerPattern("create_all_day_recurring_until_event", Pattern.compile(
                    "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
                            + "on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+)"
                            + " until (\\d{4}-\\d{2}-\\d{2})"
                            + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s"
                            + "+\"([^\"]+)\")?(?:\\s+(private))?"),
            this::parseCreateAllDayRecurringUntilEventCommand);

    registerPattern("edit_single_event",
            Pattern.compile("edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) "
                    + "with \"?([^\"]+)\"?"),
            this::parseEditSingleEventCommand);

    registerPattern("edit_event_time", Pattern.compile(
                    "edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) "
                            + "to (\\S+T\\S+) with \"?([^\"]+)\"?"),
            this::parseEditEventTimeCommand);

    registerPattern("print_events_date", Pattern.compile("print events "
                    + "on (\\d{4}-\\d{2}-\\d{2})"),
            this::parsePrintEventsDateCommand);

    registerPattern("print_events_range",
            Pattern.compile("print events from (\\d{4}-\\d{2}-\\d{2}) "
                    + "to (\\d{4}-\\d{2}-\\d{2})"),
            this::parsePrintEventsRangeCommand);

    // Edit calendar pattern
    registerPattern("edit_calendar",
            Pattern.compile("edit calendar --name ([\\w-]+) --property (\\w+) ([\\w/]+)"),
            this::parseEditCalendarCommand);

    // Use calendar pattern
    registerPattern("use_calendar",
            Pattern.compile("use calendar --name ([\"']?[^\"']+[\"']?|[^\\s]+)"),
            this::parseUseCalendarCommand);

    // Copy single event pattern
    registerPattern("copy_event", Pattern.compile(
                    "copy event \"?([^\"]*)\"? " + "on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
                            + "--target ([\\w-]+) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"),
            this::parseCopyEventCommand);

    // Copy events on date pattern
    registerPattern("copy_events_on_date", Pattern.compile(
                    "copy events on (\\d{4}-\\d{2}-\\d{2}) --target ([\\w-]+) "
                            + "to (\\d{4}-\\d{2}-\\d{2})"),
            this::parseCopyEventsOnDateCommand);

    // Copy events between dates pattern
    registerPattern("copy_events_between_dates", Pattern.compile(
                    "copy events between (\\d{4}-\\d{2}-\\d{2}) "
                            + "and (\\d{4}-\\d{2}-\\d{2}) "
                            + "--target ([\\w-]+) to (\\d{4}-\\d{2}-\\d{2})"),
            this::parseCopyEventsBetweenDatesCommand);

    registerPattern("show_status",
            Pattern.compile("show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"),
            this::parseShowStatusCommand);

    registerPattern("export_calendar", Pattern.compile("export cal (.+)"),
            this::parseExportCommand);

    registerPattern("exit", Pattern.compile("exit"), this::parseExitCommand);
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

    commandString = commandString.trim().replaceAll("\\s+", " ");

    String[] parts = commandString.split("\\s+", 2);
    if (parts.length == 0) {
      throw new IllegalArgumentException("Invalid command format");
    }

    String command = parts[0].toLowerCase();

    if (!VALID_COMMANDS_SET.contains(command)) {
      throw new IllegalArgumentException(
              "Invalid command: " + command + ". Valid commands are: " + String.join(", ",
                      VALID_COMMANDS));
    }

    for (Map.Entry<String, CommandPattern> entry : commandPatterns.entrySet()) {
      Matcher matcher = entry.getValue().getPattern().matcher(commandString);
      if (matcher.matches()) {
        return entry.getValue().getParser().parse(matcher);
      }
    }

    throw new IllegalArgumentException("Invalid command format");
  }

  /**
   * Helper method to remove surrounding quotes (both single and double) from a string.
   *
   * @param value the string that might have quotes
   * @return the string with quotes removed, or the original string if no quotes
   */
  private String removeQuotes(String value) {
    if (value == null) {
      return null;
    }

    if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
      return value.substring(1, value.length() - 1);
    }

    return value;
  }

  /**
   * Parse create event command.
   */
  private CommandWithArgs parseCreateEventCommand(Matcher matcher) {
    boolean autoDecline = matcher.group(1) != null;
    String eventName = removeQuotes(matcher.group(2));
    String startTime = matcher.group(3);
    String endTime = matcher.group(4);
    String description = matcher.group(5);
    String location = matcher.group(6);
    boolean isPrivate = matcher.group(7) != null;

    String[] args = {"single", eventName, startTime, endTime, description, location,
            String.valueOf(!isPrivate),
            String.valueOf(autoDecline)
    };
    return new CommandWithArgs(commandFactory.getCommand("create"), args);
  }

  /**
   * Parse create all day event command.
   */
  private CommandWithArgs parseCreateAllDayEventCommand(Matcher matcher) {
    boolean autoDecline = matcher.group(1) != null;
    String eventName = removeQuotes(matcher.group(2));
    String date = matcher.group(3);
    String description = matcher.group(4);
    String location = matcher.group(5);
    boolean isPrivate = matcher.group(6) != null;

    String[] args = {"allday", eventName, date, description, location, String.valueOf(!isPrivate),
            String.valueOf(autoDecline)
    };
    return new CommandWithArgs(commandFactory.getCommand("create"), args);
  }

  /**
   * Parse create recurring event command.
   */
  private CommandWithArgs parseCreateRecurringEventCommand(Matcher matcher) {
    String eventName = removeQuotes(matcher.group(1));
    String startTime = matcher.group(2);
    String endTime = matcher.group(3);
    String weekdays = matcher.group(4);
    String occurrences = matcher.group(5);
    String description = matcher.group(6);
    String location = matcher.group(7);

    String[] args = {"recurring", eventName, startTime, endTime, weekdays, occurrences,
                        description != null ? description : "", location != null ?
                        location : "", "true",
                        "false"
    };
    return new CommandWithArgs(commandFactory.getCommand("create"), args);
  }

  /**
   * Parse create recurring until event command.
   */
  private CommandWithArgs parseCreateRecurringUntilEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = removeQuotes(matcher.group(2));

    String[] args = {"recurring-until", eventName, matcher.group(3), matcher.group(4),
            matcher.group(5), matcher.group(6), String.valueOf(autoDecline), matcher.group(7),
            matcher.group(8), matcher.group(9) != null ? "false" : "true"};
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse create all day recurring event command.
   */
  private CommandWithArgs parseCreateAllDayRecurringEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = removeQuotes(matcher.group(2));

    String[] args = {"allday-recurring", eventName, matcher.group(3), matcher.group(4),
            matcher.group(5), String.valueOf(autoDecline), matcher.group(6), matcher.group(7),
            matcher.group(8) != null ? "false" : "true"};
    return new CommandWithArgs(createCommand, args);
  }

  /**
   * Parse create all day recurring until event command.
   */
  private CommandWithArgs parseCreateAllDayRecurringUntilEventCommand(Matcher matcher) {
    ICommand createCommand = commandFactory.getCommand("create");

    boolean autoDecline = matcher.group(1) != null;

    String eventName = removeQuotes(matcher.group(2));

    String[] args = {"allday-recurring-until", eventName, matcher.group(3), matcher.group(4),
            matcher.group(5), String.valueOf(autoDecline), matcher.group(6), matcher.group(7),
            matcher.group(8) != null ? "false" : "true"};
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

    String[] args = {"single", property, subject, startDateTime, newValue};
    return new CommandWithArgs(editCommand, args);
  }

  /**
   * Parse print events on date command.
   */
  private CommandWithArgs parsePrintEventsDateCommand(Matcher matcher) {
    ICommand printCommand = commandFactory.getCommand("print");

    String[] args = {"on_date", matcher.group(1)};
    return new CommandWithArgs(printCommand, args);
  }

  /**
   * Parse print events range command.
   */
  private CommandWithArgs parsePrintEventsRangeCommand(Matcher matcher) {
    ICommand printCommand = commandFactory.getCommand("print");

    String[] args = {"date_range", matcher.group(1), matcher.group(2)};
    return new CommandWithArgs(printCommand, args);
  }

  private CommandWithArgs parseCreateCalendarCommand(Matcher matcher) {
    String calendarName = removeQuotes(matcher.group(1));
    String timezone = matcher.group(2);
    return new CommandWithArgs(commandFactory.getCommand("create"),
            new String[]{"calendar", "--name", calendarName, "--timezone", timezone});
  }

  private CommandWithArgs parseEditCalendarCommand(Matcher matcher) {
    ICommand calendarCommand = commandFactory.getCommand("edit");

    String[] args = {"calendar", "--name", matcher.group(1), "--property", matcher.group(2),
            matcher.group(3)};
    return new CommandWithArgs(calendarCommand, args);
  }

  private CommandWithArgs parseUseCalendarCommand(Matcher matcher) {
    ICommand calendarCommand = commandFactory.getCommand("use");

    String calendarName = matcher.group(1);
    if (calendarName == null || calendarName.equals("null")) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }

    calendarName = removeQuotes(calendarName);

    String[] args = {"calendar", "--name", calendarName};
    return new CommandWithArgs(calendarCommand, args);
  }

  private CommandWithArgs parseCopyEventCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String eventName = removeQuotes(matcher.group(1));

    String[] args = {"copy", "event", eventName, "on", matcher.group(2), "--target",
            matcher.group(3), "to", matcher.group(4)};
    return new CommandWithArgs(copyCommand, args);
  }

  private CommandWithArgs parseCopyEventsOnDateCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String[] args = {"copy", "events", "on", matcher.group(1), "--target", matcher.group(2), "to",
            matcher.group(3)};
    return new CommandWithArgs(copyCommand, args);
  }

  private CommandWithArgs parseCopyEventsBetweenDatesCommand(Matcher matcher) {
    ICommand copyCommand = commandFactory.getCommand("copy");

    String[] args = {"copy", "events", "between", matcher.group(1), "and", matcher.group(2),
                        "--target", matcher.group(3), "to", matcher.group(4)};
    return new CommandWithArgs(copyCommand, args);
  }

  /**
   * Parse show status command.
   */
  private CommandWithArgs parseShowStatusCommand(Matcher matcher) {
    ICommand statusCommand = commandFactory.getCommand("show");

    String dateTime = matcher.group(1);
    if (dateTime == null || dateTime.trim().isEmpty()) {
      throw new IllegalArgumentException(
              "Invalid date time format. Expected format: YYYY-MM-DDThh:mm");
    }

    if (!dateTime.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
      throw new IllegalArgumentException(
              "Invalid date time format. Expected format: YYYY-MM-DDThh:mm");
    }

    String[] args = {dateTime.trim()};
    return new CommandWithArgs(statusCommand, args);
  }

  /**
   * Parse export command.
   */
  private CommandWithArgs parseExportCommand(Matcher matcher) {
    ICommand exportCommand = commandFactory.getCommand("export");

    String filePath = matcher.group(1);
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("Export file path cannot be empty");
    }

    String[] args = {filePath.trim()};
    return new CommandWithArgs(exportCommand, args);
  }

  /**
   * Parse exit command.
   */
  private CommandWithArgs parseExitCommand(Matcher matcher) {
    ICommand exitCommand = commandFactory.getCommand("exit");
    return new CommandWithArgs(exitCommand, new String[0]);
  }

  private CommandWithArgs parseEditEventTimeCommand(Matcher matcher) {
    ICommand editCommand = commandFactory.getCommand("edit");

    String property = matcher.group(1);
    String subject = matcher.group(2);
    String startDateTime = matcher.group(3);
    String endDateTime = matcher.group(4);
    String newValue = matcher.group(5);

    String[] args = {"single", property, subject, startDateTime, newValue};
    return new CommandWithArgs(editCommand, args);
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