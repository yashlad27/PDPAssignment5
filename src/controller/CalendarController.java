package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.command.CalendarCommandFactory;
import controller.command.CommandFactory;
import controller.parser.CommandParser;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.exceptions.CalendarNotFoundException;
import view.ICalendarView;

/**
 * A controller that handles both event and calendar management commands.
 * Follows the Dependency Inversion Principle by depending on abstractions instead of
 * concrete implementations where possible.
 */
public class CalendarController {

  private final ICalendarView view;
  private CommandParser parser;
  private final ICommandFactory calendarCommandFactory;
  private final CalendarManager calendarManager;
  private ICommandFactory commandFactory;
  private static final String EXIT_COMMAND = "exit";

  /**
   * Constructs a new CalendarController.
   *
   * @param commandFactory         the command factory for event commands
   * @param calendarCommandFactory the command factory for calendar commands
   * @param calendarManager        the calendar manager
   * @param view                   the view for user interaction
   */
  public CalendarController(ICommandFactory commandFactory,
                            ICommandFactory calendarCommandFactory,
                            CalendarManager calendarManager,
                            ICalendarView view) {
    if (commandFactory == null) {
      throw new IllegalArgumentException("CommandFactory cannot be null");
    }
    if (calendarCommandFactory == null) {
      throw new IllegalArgumentException("CalendarCommandFactory cannot be null");
    }
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.view = view;
    this.calendarCommandFactory = calendarCommandFactory;
    this.calendarManager = calendarManager;
    this.commandFactory = commandFactory;
    this.parser = new CommandParser(commandFactory);
  }

  /**
   * Processes a command and returns the result.
   *
   * @param commandStr the command string to process
   * @return the result of command execution
   */
  public String processCommand(String commandStr) {
    if (commandStr == null || commandStr.trim().isEmpty()) {
      return "Error: Command cannot be empty";
    }

    String trimmedCommand = commandStr.trim();

    // Check for exit command
    if (trimmedCommand.equalsIgnoreCase(EXIT_COMMAND)) {
      return "Exiting application.";
    }

    try {
      // First, check if it's a calendar management command
      if (isCalendarCommand(trimmedCommand)) {
        // Handle calendar-specific commands
        String result = processCalendarCommand(trimmedCommand);

        // If we changed the active calendar, we need to update the parser with a new CommandFactory
        if (trimmedCommand.startsWith("use calendar")) {
          String calendarName = extractCalendarName(trimmedCommand);
          if (calendarName != null) {
            // Update the command factory with the new active calendar
            updateCommandFactory();
          }
        }

        return result;
      }

      // Otherwise, treat it as a regular event command
      CommandParser.CommandWithArgs commandWithArgs = parser.parseCommand(trimmedCommand);
      return commandWithArgs.execute();
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
  }

  /**
   * Checks if the command is a calendar management command.
   *
   * @param command the command to check
   * @return true if it's a calendar command, false otherwise
   */
  private boolean isCalendarCommand(String command) {
    return command.startsWith("create calendar") ||
            command.startsWith("edit calendar") ||
            command.startsWith("use calendar") ||
            command.startsWith("copy event") ||
            command.startsWith("copy events");
  }

  /**
   * Updates the command factory with the current active calendar.
   */
  private void updateCommandFactory() {
    try {
      ICalendar activeCalendar = calendarManager.getActiveCalendar();
      if (this.commandFactory instanceof CommandFactory) {
        this.commandFactory = new CommandFactory(activeCalendar, view);
        this.parser = new CommandParser((CommandFactory)this.commandFactory);
      }
    } catch (CalendarNotFoundException e) {
      view.displayError("Error updating command factory: " + e.getMessage());
    }
  }

  /**
   * Processes a calendar-specific command.
   *
   * @param commandStr the calendar command string
   * @return the result of command execution
   */
  private String processCalendarCommand(String commandStr) throws Exception {
    String[] parts = parseCommand(commandStr);
    if (parts.length < 2) {
      return "Error: Invalid calendar command format";
    }

    String action = parts[0]; // e.g., "create", "edit", "use", "copy"
    String targetType = parts[1]; // e.g., "calendar", "event", "events"

    // Handle copy commands specially due to their more complex structure
    if (action.equals("copy")) {
      if (targetType.equals("event")) {
        // Format: copy event "Event Name" on DATE --target CAL to DATE
        if (parts.length < 9) {
          return "Error: Insufficient arguments for copy event command";
        }

        // First, validate the command structure
        if (!parts[3].equals("on") || !parts[5].equals("--target") || !parts[7].equals("to")) {
          return "Error: Invalid copy event command format";
        }

        return calendarCommandFactory.getCommand("copy").execute(parts);
      } else if (targetType.equals("events")) {
        // Two formats:
        // 1. copy events on DATE --target CAL to DATE
        // 2. copy events between DATE and DATE --target CAL DATE
        if (parts.length < 2) {
          return "Error: Insufficient arguments for copy events command";
        }

        String subtype = parts[2]; // "on" or "between"

        if (subtype.equals("on")) {
          if (parts.length < 8) {
            return "Error: Insufficient arguments for copy events on date command";
          }
          if (!parts[3].equals("on") || !parts[5].equals("--target") || !parts[7].equals("to")) {
            return "Error: Invalid copy events on date command format";
          }
        } else if (subtype.equals("between")) {
          if (parts.length < 10) {
            return "Error: Insufficient arguments for copy events between dates command";
          }
          if (!parts[3].equals("between") || !parts[5].equals("and") ||
                  !parts[7].equals("--target")) {
            return "Error: Invalid copy events between dates command format";
          }
        } else {
          return "Error: Unknown copy events subcommand: " + subtype;
        }

        return calendarCommandFactory.getCommand("copy").execute(parts);
      } else {
        return "Error: Unknown copy target: " + targetType;
      }
    }

    // For other calendar commands
    String[] args;
    if (targetType.equals("calendar")) {
      args = new String[parts.length - 1];
      args[0] = "calendar";
      System.arraycopy(parts, 2, args, 1, parts.length - 2);
    } else {
      return "Error: Expected 'calendar' after '" + action + "'";
    }

    if (calendarCommandFactory.hasCommand(action)) {
      return calendarCommandFactory.getCommand(action).execute(args);
    } else {
      return "Error: Unknown calendar command: " + action;
    }
  }

  /**
   * Parses a command string into an array of tokens, properly handling quoted strings.
   *
   * @param commandStr the command string to parse
   * @return an array of command tokens
   */
  private String[] parseCommand(String commandStr) {
    List<String> tokens = new ArrayList<>();
    Pattern pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    Matcher matcher = pattern.matcher(commandStr);

    while (matcher.find()) {
      if (matcher.group(1) != null) {
        // Add double-quoted string without the quotes
        tokens.add(matcher.group(1));
      } else if (matcher.group(2) != null) {
        // Add single-quoted string without the quotes
        tokens.add(matcher.group(2));
      } else {
        // Add unquoted word
        tokens.add(matcher.group());
      }
    }

    return tokens.toArray(new String[0]);
  }

  /**
   * Helper method to extract calendar name from "use calendar" command
   */
  private String extractCalendarName(String command) {
    Pattern pattern = Pattern.compile("use calendar --name ([\\w-]+)");
    Matcher matcher = pattern.matcher(command);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  /**
   * Starts the controller in interactive mode.
   */
  public void startInteractiveMode() {
    view.displayMessage("Calendar Application Started");
    view.displayMessage("Enter commands (type 'exit' to quit):");

    String command;
    while (!(command = view.readCommand()).equalsIgnoreCase(EXIT_COMMAND)) {
      String result = processCommand(command);
      view.displayMessage(result);
    }

    view.displayMessage("Calendar Application Terminated");
  }

  /**
   * Starts the controller in headless mode with commands from a file.
   *
   * @param commandsFilePath the path to the file containing commands
   * @return true if all commands were executed successfully, false otherwise
   */
  public boolean startHeadlessMode(String commandsFilePath) {
    if (commandsFilePath == null || commandsFilePath.trim().isEmpty()) {
      view.displayError("Error: File path cannot be empty");
      return false;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(commandsFilePath))) {
      String line;
      String lastCommand = null;
      boolean fileHasCommands = false;

      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty()) {
          continue;
        }

        fileHasCommands = true;
        lastCommand = line;

        String result = processCommand(line);
        view.displayMessage(result);

        if (line.equalsIgnoreCase(EXIT_COMMAND)) {
          break;
        }

        if (result.startsWith("Error")) {
          view.displayError("Command failed, stopping execution: " + result);
          return false;
        }
      }

      // Check if file was empty
      if (!fileHasCommands) {
        view.displayError(
                "Error: Command file is empty. " + "At least one command (exit) is required.");
        return false;
      }

      // Check if the last command was an exit command
      if (!lastCommand.equalsIgnoreCase(EXIT_COMMAND)) {
        view.displayError("Headless mode requires the last command to be 'exit'");
        return false;
      }

      return true;
    } catch (IOException e) {
      view.displayError("Error reading command file: " + e.getMessage());
      return false;
    }
  }
}
