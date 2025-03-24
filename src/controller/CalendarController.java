package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import controller.command.event.CommandFactory;
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
  private static final Set<String> VALID_COMMANDS = new HashSet<>(Arrays.asList(
      "create", "use", "show", "edit", "copy", "exit"
  ));

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

    String[] parts = commandStr.trim().split("\\s+");
    if (parts.length == 0) {
      return "Error: Invalid command format";
    }

    String command = parts[0].toLowerCase();
    
    // Check if the command is valid
    if (!VALID_COMMANDS.contains(command)) {
      return "Error: Invalid command: " + command + ". Valid commands are: " + 
          String.join(", ", VALID_COMMANDS);
    }

    try {
      // First, check if it's a calendar management command
      if (isCalendarCommand(commandStr)) {
        // Handle calendar-specific commands
        String result = processCalendarCommand(commandStr);

        // If we changed the active calendar, we need to update the parser with a new CommandFactory
        if (commandStr.startsWith("use calendar")) {
          String calendarName = extractCalendarName(commandStr);
          if (calendarName != null) {
            // Update the command factory with the new active calendar
            updateCommandFactory();
          }
        }

        return result;
      }

      // Otherwise, treat it as a regular event command
      CommandParser.CommandWithArgs commandWithArgs = parser.parseCommand(commandStr);
      return commandWithArgs.execute();
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
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
        // Create a new CommandFactory with the active calendar
        this.commandFactory = new CommandFactory(activeCalendar, view);
        // Create a new parser with the updated factory
        this.parser = new CommandParser(this.commandFactory);
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

    // For copy commands, just pass the entire parts array to the copy command handler
    if (action.equals("copy")) {
      return calendarCommandFactory.getCommand("copy").execute(parts);
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
        tokens.add(matcher.group(1));
      } else if (matcher.group(2) != null) {
        tokens.add(matcher.group(2));
      } else {
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
                "Error: Command file is empty. "
                        + "At least one command (exit) is required.");
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