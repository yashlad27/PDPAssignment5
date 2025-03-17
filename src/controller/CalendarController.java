package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import controller.command.CalendarCommandFactory;
import controller.command.CommandFactory;
import controller.parser.CommandParser;
import model.calendar.ICalendar;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import view.ICalendarView;

/**
 * A controller that handles both event and calendar management commands.
 */
public class CalendarController {

  private final ICalendarView view;
  private final CommandParser parser;
  private final CalendarCommandFactory calendarCommandFactory;
  private static final String EXIT_COMMAND = "exit";

  /**
   * Constructs a new CalendarController.
   *
   * @param commandFactory         the command factory for event commands
   * @param calendarCommandFactory the command factory for calendar commands
   * @param view                   the view for user interaction
   */
  public CalendarController(CommandFactory commandFactory,
                            CalendarCommandFactory calendarCommandFactory,
                            ICalendarView view) {
    if (commandFactory == null) {
      throw new IllegalArgumentException("CommandFactory cannot be null");
    }
    if (calendarCommandFactory == null) {
      throw new IllegalArgumentException("CalendarCommandFactory cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.view = view;
    this.calendarCommandFactory = calendarCommandFactory;
    ICalendar calendar = commandFactory.getCalendar();
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
      if (trimmedCommand.startsWith("create calendar") ||
              trimmedCommand.startsWith("edit calendar") ||
              trimmedCommand.startsWith("use calendar") ||
              trimmedCommand.startsWith("copy event") ||
              trimmedCommand.startsWith("copy events")) {

        // Handle calendar-specific commands
        return processCalendarCommand(trimmedCommand);
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
   * Processes a calendar-specific command.
   *
   * @param commandStr the calendar command string
   * @return the result of command execution
   */
  private String processCalendarCommand(String commandStr) throws CalendarNotFoundException,
          InvalidTimezoneException, DuplicateCalendarException {
    // For commands like "create calendar --name myCalendar --timezone America/New_York"
    // parts[0] = "create", parts[1] = "calendar"
    String[] parts = commandStr.split("\\s+", 3);
    if (parts.length < 2) {
      return "Error: Invalid calendar command format";
    }

    String action = parts[0]; // e.g., "create", "edit", "use"

    // Prepare args - the first arg should be "calendar" for most commands
    String[] args;
    if (parts.length > 2) {
      // Create args array starting with "calendar" followed by the rest of the command
      String restOfCommand = parts[2].trim();
      args = restOfCommand.split("\\s+");
    } else {
      args = new String[0];
    }

    if (calendarCommandFactory.hasCommand(action)) {
      return calendarCommandFactory.getCommand(action).execute(args);
    } else {
      return "Error: Unknown calendar command: " + action;
    }
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