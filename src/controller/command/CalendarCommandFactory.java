package controller.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.ICommandFactory;
import controller.command.copy.CopyCommand;
import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;
import view.ICalendarView;

public class CalendarCommandFactory implements ICommandFactory {

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
  private String executeCreateCommand(String[] args) throws DuplicateCalendarException, InvalidTimezoneException {

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
  private String executeEditCalendarCommand(String[] args) throws CalendarNotFoundException, DuplicateCalendarException, InvalidTimezoneException {
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
    if (args.length < 3) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      String subCommand = args[1];

      if (subCommand.equals("event")) {
        // Format from controller: [copy, event, Event Name, on, 2025-03-25T09:00, --target, Personal, to, 2025-03-26T10:00]
        if (args.length < 9) {
          return "Error: Insufficient arguments for copy event command";
        }

        String eventName = args[2];
        String dateTimeStr = args[4];
        String targetCalendarName = args[6];
        String targetDateTimeStr = args[8];

        controller.command.CopyEventCommand copyCommand = new controller.command.CopyEventCommand(calendarManager, timezoneHandler);

        return copyCommand.execute(new String[]{"event", eventName, dateTimeStr, targetCalendarName, targetDateTimeStr, "true"});
      } else if (subCommand.equals("events") && args.length > 2 && "on".equals(args[2])) {
        // Format: [copy, events, on, 2025-03-26, --target, Travel, to, 2025-04-16]
        if (args.length < 8) {
          return "Error: Insufficient arguments for copy events on date command";
        }

        String dateStr = args[3];
        String targetCalendarName = args[5];
        String targetDateStr = args[7];

        controller.command.CopyEventCommand copyCommand = new controller.command.CopyEventCommand(calendarManager, timezoneHandler);

        return copyCommand.execute(new String[]{"events_on_date", dateStr, targetCalendarName, targetDateStr, "true"});
      } else if (subCommand.equals("events") && args.length > 2 && "between".equals(args[2])) {
        // Format: [copy, events, between, 2025-03-25, and, 2025-03-28, --target, Personal, to, 2025-04-01]
        if (args.length < 10) {
          return "Error: Insufficient arguments for copy events between dates command";
        }

        String startDateStr = args[3];
        String endDateStr = args[5];
        String targetCalendarName = args[7];
        String targetStartDateStr = args[9];

        controller.command.CopyEventCommand copyCommand = new controller.command.CopyEventCommand(calendarManager, timezoneHandler);

        return copyCommand.execute(new String[]{"events_between_dates", startDateStr, endDateStr, targetCalendarName, targetStartDateStr, "true"});
      } else {
        for (CopyCommand command : copyCommands) {
          if (command.canHandle(args)) {
            return command.execute(args);
          }
        }
        return "Error: Invalid copy command format";
      }
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
  @Override
  public ICommand getCommand(String commandName) {
    CalendarCommandHandler handler = commands.get(commandName);
    if (handler != null) {
      return new CommandAdapter(commandName, args -> {
        try {
          return handler.execute(args);
        } catch (Exception e) {
          return "Error: " + e.getMessage();
        }
      });
    }
    return null;
  }

  private void registerCopyCommands() {
    copyCommands.add(new controller.command.copy.CopyEventCommand(calendarManager, timezoneHandler));
    copyCommands.add(new controller.command.copy.CopyEventsOnDateCommand(calendarManager, timezoneHandler));
    copyCommands.add(new controller.command.copy.CopyEventsBetweenDatesCommand(calendarManager, timezoneHandler));
  }
}