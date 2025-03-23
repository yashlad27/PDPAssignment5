package controller.command.calendar;

import java.util.HashMap;
import java.util.Map;

import controller.ICommandFactory;
import controller.command.CommandAdapter;
import controller.command.ICommand;
import controller.command.copy.CopyEventCommand;
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
  private final TimeZoneHandler timezoneHandler;
  private final CopyEventCommand copyEventCommand;

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
    this.copyEventCommand = new CopyEventCommand(calendarManager, timezoneHandler);

    registerCommands();
  }

  private void registerCommands() {
    commands.put("create", this::executeCreateCommand);
    commands.put("edit", this::executeEditCalendarCommand);
    commands.put("use", this::executeUseCalendarCommand);
    commands.put("copy", this::executeCopyCommand);
  }

  private String executeCreateCommand(String[] args) throws DuplicateCalendarException,
          InvalidTimezoneException {
    if (args.length < 4) {
      String error = "Error: Insufficient arguments for create calendar command";
      view.displayError(error);
      return error;
    }

    if (!args[0].equals("calendar")) {
      String error = "Error: Expected 'calendar' argument";
      view.displayError(error);
      return error;
    }

    if (!args[1].equals("--name")) {
      String error = "Error: Expected '--name' flag";
      view.displayError(error);
      return error;
    }

    String calendarName = args[2];

    if (!args[3].equals("--timezone")) {
      String error = "Error: Expected '--timezone' flag";
      view.displayError(error);
      return error;
    }

    String timezone = args.length > 4 ? args[4] : "America/New_York";

    try {
      calendarManager.createCalendar(calendarName, timezone);
      String success = "Calendar '" + calendarName + "' created with timezone '" + timezone + "'";
      view.displayMessage(success);
      return success;
    } catch (DuplicateCalendarException | InvalidTimezoneException e) {
      String error = "Error: " + e.getMessage();
      view.displayError(error);
      return error;
    }
  }

  private String executeEditCalendarCommand(String[] args) throws CalendarNotFoundException,
          DuplicateCalendarException, InvalidTimezoneException {
    if (args.length < 6) {
      String error = "Error: Insufficient arguments for edit calendar command";
      view.displayError(error);
      return error;
    }

    if (!args[0].equals("calendar")) {
      String error = "Error: Expected 'calendar' argument";
      view.displayError(error);
      return error;
    }

    if (!args[1].equals("--name")) {
      String error = "Error: Expected '--name' flag";
      view.displayError(error);
      return error;
    }

    String calendarName = args[2];

    if (!args[3].equals("--property")) {
      String error = "Error: Expected '--property' flag";
      view.displayError(error);
      return error;
    }

    String property = args[4];
    String newValue = args[5];

    try {
      switch (property.toLowerCase()) {
        case "name":
          calendarManager.editCalendarName(calendarName, newValue);
          String success = "Calendar name changed from '" + calendarName + "' to '" + newValue + "'";
          view.displayMessage(success);
          return success;
        case "timezone":
          calendarManager.editCalendarTimezone(calendarName, newValue);
          success = "Timezone for calendar '" + calendarName + "' changed to '" + newValue + "'";
          view.displayMessage(success);
          return success;
        default:
          String error = "Error: Unsupported property '" + property
                  + "'. Valid properties are 'name' "
                  + "and 'timezone'";
          view.displayError(error);
          return error;
      }
    } catch (CalendarNotFoundException | DuplicateCalendarException | InvalidTimezoneException e) {
      String error = "Error: " + e.getMessage();
      view.displayError(error);
      return error;
    }
  }

  private String executeUseCalendarCommand(String[] args) throws CalendarNotFoundException {
    if (args.length < 3) {
      String error = "Error: Insufficient arguments for use calendar command";
      view.displayError(error);
      return error;
    }

    if (!args[0].equals("calendar")) {
      String error = "Error: Expected 'calendar' argument";
      view.displayError(error);
      return error;
    }

    if (!args[1].equals("--name")) {
      String error = "Error: Expected '--name' flag";
      view.displayError(error);
      return error;
    }

    String calendarName = args[2];

    try {
      calendarManager.setActiveCalendar(calendarName);
      String success = "Now using calendar: '" + calendarName + "'";
      view.displayMessage(success);
      return success;
    } catch (CalendarNotFoundException e) {
      String error = "Error: " + e.getMessage();
      view.displayError(error);
      return error;
    }
  }

  private String executeCopyCommand(String[] args) {
    if (args.length < 3) {
      String error = "Error: Insufficient arguments for copy command";
      view.displayError(error);
      return error;
    }

    try {
      String result = copyEventCommand.execute(args);
      if (result.startsWith("Error:")) {
        view.displayError(result);
      } else {
        view.displayMessage(result);
      }
      return result;
    } catch (Exception e) {
      String error = "Error: " + e.getMessage();
      view.displayError(error);
      return error;
    }
  }

  public boolean hasCommand(String commandName) {
    return commands.containsKey(commandName);
  }

  @Override
  public ICommand getCommand(String commandName) {
    if (commandName == null) {
      return null;
    }

    if (commandName.equals("copy")) {
      return copyEventCommand;
    }

    CalendarCommandHandler handler = commands.get(commandName);
    if (handler != null) {
      return new CommandAdapter(commandName, args -> {
        try {
          return handler.execute(args);
        } catch (Exception e) {
          String error = "Error: " + e.getMessage();
          view.displayError(error);
          return error;
        }
      });
    }
    return null;
  }
}