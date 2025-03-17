package controller.command;

import java.util.HashMap;
import java.util.Map;

import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import view.ICalendarView;

public class CalendarCommandFactory {

  private final Map<String, CalendarCommandHandler> commands;
  private final CalendarManager calendarManager;
  private final ICalendarView view;

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

    if(!args[3].equals("--property")) {
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

  private String executeUseCalendarCommand(String[] args) {
    return "";
  }

  private String executeCopyCommand(String[] args) {
    return "";
  }
}
