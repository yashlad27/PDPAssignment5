package controller.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.command.copy.CopyCommand;
import controller.command.copy.CopyEventsBetweenDatesCommand;
import controller.command.copy.CopyEventsOnDateCommand;
import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;
import view.ICalendarView;

public class CalendarCommandFactory {

  private final Map<String, CalendarCommandHandler> commands;
  private final CalendarManager calendarManager;
  private final ICalendarView view;
  private final List<CopyCommand> copyCommands;
  private final TimeZoneHandler timezoneHandler;


  public CalendarCommandFactory(CalendarManager calendarManager, ICalendarView view) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }

    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commands = new HashMap<>();
    this.calendarManager = calendarManager;
    this.view = view;

    this.timezoneHandler = calendarManager.getTimezoneHandler();

    this.copyCommands = new ArrayList<>();
    registerCopyCommands();

    registerCommands();
  }

  private void registerCommands() {
    commands.put("create", this::executeCreateCommand);
    commands.put("edit", this::executeEditCalendarCommand);
    commands.put("use", this::executeUseCalendarCommand);
    commands.put("copy", this::executeCopyCommand);
  }

  /**
   * Executes the create calendar command.
   */
  private String executeCreateCommand(String[] args) throws DuplicateCalendarException,
          InvalidTimezoneException {

    if (args.length < 4) {
      return "Error: Insufficient arguments for create calendar command";
    }

    if (!args[0].equals("calendar")) {
      return "Error: Expected 'calendar' argument";
    }

    if (!args[1].equals("--name")) {
      return "Error: Expected '--name' flag";
    }

    String calendarName = args[2];

    if (!args[3].equals("--timezone")) {
      return "Error: Expected '--timezone' flag";
    }

    String timezone = args.length > 4 ? args[4] : "America/New_York";

    calendarManager.createCalendar(calendarName, timezone);
    return "Calendar '" + calendarName + "' created with timezone '" + timezone + "'";
  }

  /**
   * Executes the edit calendar command.
   */
  private String executeEditCalendarCommand(String[] args)
          throws CalendarNotFoundException, DuplicateCalendarException, InvalidTimezoneException {
    if (args.length < 6) {
      return "Error: Insufficient arguments for edit calendar command";
    }

    if (!args[0].equals("calendar")) {
      return "Error: Expected 'calendar' argument";
    }

    if (!args[1].equals("--name")) {
      return "Error: Expected '--name' flag";
    }

    String calendarName = args[2];

    if (!args[3].equals("--property")) {
      return "Error: Expected '--property' flag";
    }

    String property = args[4];
    String newValue = args[5];

    switch (property.toLowerCase()) {
      case "name":
        calendarManager.editCalendarName(calendarName, newValue);
        return "Calendar name changed from '" + calendarName + "' to '" + newValue + "'";
      case "timezone":
        calendarManager.editCalendarTimezone(calendarName, newValue);
        return "Timezone for calendar '" + calendarName + "' changed to '" + newValue + "'";
      default:
        return "Error: Unsupported property '" + property + "'. Valid properties are 'name' and 'timezone'";
    }
  }

  /**
   * Executes the use calendar command.
   */

  private String executeUseCalendarCommand(String[] args) throws CalendarNotFoundException {
    if (args.length < 3) {
      return "Error: Insufficient arguments for use calendar command";
    }

    if (!args[0].equals("calendar")) {
      return "Error: Expected 'calendar' argument";
    }

    if (!args[1].equals("--name")) {
      return "Error: Expected '--name' flag";
    }

    String calendarName = args[2];

    calendarManager.setActiveCalendar(calendarName);
    return "Now using calendar: '" + calendarName + "'";
  }

  /**
   * Executes the copy event/events command.
   */
  private String executeCopyCommand(String[] args) {
    if (args.length < 1) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      // Find the appropriate copy command
      for (CopyCommand command : copyCommands) {
        if (command.canHandle(args)) {
          return command.execute(args);
        }
      }

      // If no command could handle it
      return "Error: Invalid copy command format";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Checks if a command is registered.
   *
   * @param commandName the name of the command
   * @return true if the command is registered, false otherwise
   */
  public boolean hasCommand(String commandName) {
    return commands.containsKey(commandName);
  }

  /**
   * Gets a command handler by name.
   *
   * @param commandName the name of the command
   * @return the command handler with exception handling, or null if not found
   */
  public CalendarCommandHandler getCommand(String commandName) {
    CalendarCommandHandler handler = commands.get(commandName);
    if (handler != null) {
      // Wrap the handler with exception handling
      return handler.withExceptionHandling();
    }
    return null;
  }

  private void registerCopyCommands() {
    copyCommands.add(new CopyEventCommand(calendarManager, timezoneHandler));
    copyCommands.add(new CopyEventsOnDateCommand(calendarManager, timezoneHandler));
    copyCommands.add(new CopyEventsBetweenDatesCommand(calendarManager, timezoneHandler));
  }

}
