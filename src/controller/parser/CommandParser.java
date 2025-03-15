package controller.parser;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.command.CommandFactory;
import controller.command.ICommand;
import utilities.DateTimeUtil;

/**
 * Improved parser for command-line input that uses a CommandFactory.
 */
public class CommandParser {

  private final CommandFactory commandFactory;

  private static final Pattern CREATE_EVENT_PATTERN = Pattern.compile(
      "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) "
          + "from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?");

  // Recurring event pattern with occurrences
  private static final Pattern CREATE_RECURRING_EVENT_PATTERN = Pattern.compile(
      "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) from "
          + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) repeats"
          + " ([MTWRFSU]+) for (\\d+) times(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?"
          + "(?:\\s+(private))?");

  // Recurring event pattern with end date
  private static final Pattern CREATE_RECURRING_UNTIL_PATTERN = Pattern.compile(
      "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) from "
          + "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) "
          + "repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})(?:\\s+desc\\s+\"([^\"]+)\")?"
          + "(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?");

  // All-day event pattern
  private static final Pattern CREATE_ALL_DAY_EVENT_PATTERN = Pattern.compile(
      "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) on (\\d{4}-\\d{2}-\\d{2})"
          + "(?:\\s+desc\\s+\"([^\"]+)\")?(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?");

  // All-day recurring event pattern with occurrences
  private static final Pattern CREATE_ALL_DAY_RECURRING_PATTERN = Pattern.compile(
      "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) on (\\d{4}-\\d{2}-\\d{2}) "
          + "repeats ([MTWRFSU]+) for (\\d+) times(?:\\s+desc\\s+\"([^\"]+)\")?"
          + "(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?");

  // All-day recurring event pattern with end date
  private static final Pattern CREATE_ALL_DAY_RECURRING_UNTIL_PATTERN = Pattern.compile(
      "create event (--autoDecline )?([\"']?[^\"']+[\"']?|[^\\s]+) on (\\d{4}-\\d{2}-\\d{2})"
          + " repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})(?:\\s+desc\\s+\"([^\"]+)\")?"
          + "(?:\\s+at\\s+\"([^\"]+)\")?(?:\\s+(private))?");

  // Display event patterns
  private static final Pattern PRINT_EVENTS_PATTERN = Pattern.compile(
      "print events on (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern PRINT_EVENTS_RANGE_PATTERN = Pattern.compile(
      "print events from (\\d{4}-\\d{2}-\\d{2}) to (\\d{4}-\\d{2}-\\d{2})");

  // Status check pattern
  private static final Pattern SHOW_STATUS_PATTERN = Pattern.compile(
      "show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");

  // Export calendar pattern
  private static final Pattern EXPORT_CALENDAR_PATTERN = Pattern.compile(
      "export cal (.+)");

  // Edit event patterns
  private static final Pattern EDIT_SINGLE_EVENT_PATTERN = Pattern.compile(
      "edit event (\\w+) \"([^\"]+)\" from (\\S+T\\S+) to (\\S+T\\S+) with \"?([^\"]+)\"?");

  private static final Pattern EDIT_EVENTS_FROM_DATE_PATTERN = Pattern.compile(
      "edit events (\\w+) \"([^\"]+)\" from (\\S+T\\S+) with \"?([^\"]+)\"?");

  private static final Pattern EDIT_ALL_EVENTS_PATTERN = Pattern.compile(
      "edit events (\\w+) \"([^\"]+)\" with \"?([^\"]+)\"?");

  private static final Pattern EDIT_ALL_DAY_EVENT_PATTERN = Pattern.compile(
      "edit event (\\w+) \"([^\"]+)\" on (\\d{4}-\\d{2}-\\d{2}) with \"?([^\"]+)\"?");

  /**
   * Constructs a new CommandParser.
   *
   * @param commandFactory the factory for creating commands
   */
  public CommandParser(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
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
      ICommand exitCommand = commandFactory.getCommand("exit");
      return new CommandWithArgs(exitCommand, new String[0]);
    }

    // Parse create event commands
    if (commandString.startsWith("create event")) {
      return parseCreateEventCommand(commandString);
    }

    // Parse edit commands
    if (commandString.startsWith("edit event") || commandString.startsWith("edit events")) {
      return parseEditEventCommand(commandString);
    }

    // Parse print commands
    if (commandString.startsWith("print events")) {
      return parsePrintEventsCommand(commandString);
    }

    // Parse show status command
    if (commandString.startsWith("show status")) {
      return parseShowStatusCommand(commandString);
    }

    // Parse export command
    if (commandString.startsWith("export cal")) {
      return parseExportCommand(commandString);
    }

    throw new IllegalArgumentException("Unrecognized or unsupported command: " + commandString);
  }

  /**
   * Parses a create event command.
   *
   * @param commandString the command string to parse
   * @return a CommandWithArgs for creating an event
   * @throws IllegalArgumentException if the command is invalid
   */
  private CommandWithArgs parseCreateEventCommand(String commandString) {
    ICommand createCommand = commandFactory.getCommand("create");
    Matcher matcher;

    matcher = CREATE_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
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

    matcher = CREATE_RECURRING_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;

      String eventName = matcher.group(2);
      if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
        eventName = eventName.substring(1, eventName.length() - 1);
      }

      String[] args = {
          "recurring",
          eventName,  // event name
          matcher.group(3),  // start date/time
          matcher.group(4),  // end date/time
          matcher.group(5),  // weekdays
          matcher.group(6),  // occurrences
          String.valueOf(autoDecline),
          matcher.group(7),  // description
          matcher.group(8),  // location
          matcher.group(9) != null ? "false" : "true"  // isPublic
      };
      return new CommandWithArgs(createCommand, args);
    }

    matcher = CREATE_RECURRING_UNTIL_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;

      String eventName = matcher.group(2);
      if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
        eventName = eventName.substring(1, eventName.length() - 1);
      }

      String[] args = {
          "recurring-until",
          eventName,  // event name
          matcher.group(3),  // start date/time
          matcher.group(4),  // end date/time
          matcher.group(5),  // weekdays
          matcher.group(6),  // until date
          String.valueOf(autoDecline),
          matcher.group(7),  // description
          matcher.group(8),  // location
          matcher.group(9) != null ? "false" : "true"  // isPublic
      };
      return new CommandWithArgs(createCommand, args);
    }

    matcher = CREATE_ALL_DAY_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;

      String eventName = matcher.group(2);
      if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
        eventName = eventName.substring(1, eventName.length() - 1);
      }

      String[] args = {
          "allday",
          eventName,  // event name
          matcher.group(3),  // date
          String.valueOf(autoDecline),
          matcher.group(4),  // description
          matcher.group(5),  // location
          matcher.group(6) != null ? "false" : "true"  // isPublic
      };
      return new CommandWithArgs(createCommand, args);
    }

    matcher = CREATE_ALL_DAY_RECURRING_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;

      String eventName = matcher.group(2);
      if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
        eventName = eventName.substring(1, eventName.length() - 1);
      }

      String[] args = {
          "allday-recurring",
          eventName,  // event name
          matcher.group(3),  // date
          matcher.group(4),  // weekdays
          matcher.group(5),  // occurrences
          String.valueOf(autoDecline),
          matcher.group(6),  // description
          matcher.group(7),  // location
          matcher.group(8) != null ? "false" : "true"  // isPublic
      };
      return new CommandWithArgs(createCommand, args);
    }

    matcher = CREATE_ALL_DAY_RECURRING_UNTIL_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;

      String eventName = matcher.group(2);
      if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
        eventName = eventName.substring(1, eventName.length() - 1);
      }

      String[] args = {
          "allday-recurring-until",
          eventName,  // event name
          matcher.group(3),  // date
          matcher.group(4),  // weekdays
          matcher.group(5),  // until date
          String.valueOf(autoDecline),
          matcher.group(6),  // description
          matcher.group(7),  // location
          matcher.group(8) != null ? "false" : "true"  // isPublic
      };
      return new CommandWithArgs(createCommand, args);
    }

    throw new IllegalArgumentException("Invalid create event command format");
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

    public String execute() {
      return command.execute(args);
    }
  }

  private CommandWithArgs parseEditEventCommand(String commandString) {
    ICommand editCommand = commandFactory.getCommand("edit");
    Matcher matcher;

    matcher = EDIT_SINGLE_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = matcher.group(2);
      if (subject.startsWith("\"") && subject.endsWith("\"")) {
        subject = subject.substring(1, subject.length() - 1);
      }

      String[] args = {
          "single",
          property,
          subject,
          matcher.group(3),
          matcher.group(5)
      };
      return new CommandWithArgs(editCommand, args);
    }

    matcher = EDIT_EVENTS_FROM_DATE_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = matcher.group(2);
      if (subject.startsWith("\"") && subject.endsWith("\"")) {
        subject = subject.substring(1, subject.length() - 1);
      }

      String[] args = {
          "series_from_date",
          property,
          subject,
          matcher.group(3),  // startDateTime
          matcher.group(4)   // newValue
      };
      return new CommandWithArgs(editCommand, args);
    }

    matcher = EDIT_ALL_EVENTS_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = matcher.group(2);
      if (subject.startsWith("\"") && subject.endsWith("\"")) {
        subject = subject.substring(1, subject.length() - 1);
      }

      String[] args = {
          "all",
          property,
          subject,
          matcher.group(3)
      };
      return new CommandWithArgs(editCommand, args);
    }

    matcher = EDIT_ALL_DAY_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = matcher.group(2);
      String dateStr = matcher.group(3);
      String newValue = matcher.group(4);

      if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
        newValue = newValue.substring(1, newValue.length() - 1);
      }

      LocalDateTime startDateTime = LocalDateTime.of(
          DateTimeUtil.parseDate(dateStr),
          LocalTime.of(0, 0));

      String[] args = {
          "single",
          property,
          subject,
          startDateTime.toString(),
          newValue
      };
      return new CommandWithArgs(editCommand, args);
    }

    throw new IllegalArgumentException("Invalid edit command format: " + commandString);
  }

  private CommandWithArgs parsePrintEventsCommand(String commandString) {
    ICommand printCommand = commandFactory.getCommand("print");
    Matcher matcher;

    matcher = PRINT_EVENTS_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String[] args = {
          "on_date",
          matcher.group(1)  // date
      };
      return new CommandWithArgs(printCommand, args);
    }

    matcher = PRINT_EVENTS_RANGE_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String[] args = {
          "date_range",
          matcher.group(1),
          matcher.group(2)
      };
      return new CommandWithArgs(printCommand, args);
    }

    throw new IllegalArgumentException("Invalid print command format: " + commandString);
  }

  private CommandWithArgs parseShowStatusCommand(String commandString) {
    ICommand statusCommand = commandFactory.getCommand("show");
    Matcher matcher = SHOW_STATUS_PATTERN.matcher(commandString);

    if (matcher.matches()) {
      String[] args = {
          matcher.group(1)  // dateTime
      };
      return new CommandWithArgs(statusCommand, args);
    }

    throw new IllegalArgumentException("Invalid status command format: " + commandString);
  }

  private CommandWithArgs parseExportCommand(String commandString) {
    ICommand exportCommand = commandFactory.getCommand("export");
    Matcher matcher = EXPORT_CALENDAR_PATTERN.matcher(commandString);

    if (matcher.matches()) {
      String[] args = {
          matcher.group(1)  // filename
      };
      return new CommandWithArgs(exportCommand, args);
    }

    throw new IllegalArgumentException("Invalid export command format: " + commandString);
  }
}