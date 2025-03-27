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

/**
 * Factory class responsible for creating and managing calendar-related commands.
 * This factory handles operations for creating, editing, selecting, and copying
 * calendars and their events. It implements the ICommandFactory interface to
 * provide a consistent way to retrieve calendar commands.
 */
public class CalendarCommandFactory implements ICommandFactory {

  private final Map<String, CalendarCommandHandler> commands;
  private final CalendarManager calendarManager;
  private final CopyEventCommand copyEventCommand;

  /**
   * Creates a CalendarCommandFactory with required dependencies.
   *
   * @param calendarManager Handles calendar operations, non-null
   * @param view            Calendar view interface, non-null
   * @throws IllegalArgumentException If any parameter is null
   */
  public CalendarCommandFactory(CalendarManager calendarManager, ICalendarView view) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }

    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commands = new HashMap<>();
    this.calendarManager = calendarManager;
    TimeZoneHandler timezoneHandler = calendarManager.getTimezoneHandler();
    this.copyEventCommand = new CopyEventCommand(calendarManager, timezoneHandler);

    registerCommands();
  }

  private void registerCommands() {
    commands.put("create", this::executeCreateCommand);
    commands.put("edit", this::executeEditCalendarCommand);
    commands.put("use", this::executeUseCalendarCommand);
    commands.put("copy", this::executeCopyCommand);
  }

  private String executeCreateCommand(String[] args) {
    if (args.length < 5) {
      return "Error: Insufficient arguments for create calendar command";
    }

    String calendarName = args[2];
    String timezone = args[4];

    // Validate calendar name length
    if (calendarName.length() > 100) {
      return "Error: Calendar name cannot exceed 100 characters";
    }

    try {
      calendarManager.createCalendar(calendarName, timezone);
      return "Calendar '" + calendarName + "' created successfully with timezone " + timezone;
    } catch (DuplicateCalendarException e) {
      return e.getMessage();
    } catch (InvalidTimezoneException e) {
      return e.getMessage();
    }
  }

  private String executeEditCalendarCommand(String[] args) {
    if (args.length < 6) {
      return "Error: Insufficient arguments for edit calendar command";
    }

    String calendarName = args[2];
    String property = args[4];
    String value = args[5];

    try {
      if (property.equals("timezone")) {
        calendarManager.editCalendarTimezone(calendarName, value);
        return "Timezone updated to " + value + " for calendar '" + calendarName + "'";
      } else {
        return "Error: Invalid property '" + property + "' for calendar edit";
      }
    } catch (CalendarNotFoundException e) {
      return e.getMessage();
    } catch (InvalidTimezoneException e) {
      return e.getMessage();
    }
  }

  private String executeUseCalendarCommand(String[] args) {
    if (args.length < 3) {
      return "Error: Insufficient arguments for use calendar command";
    }

    String calendarName = args[2];

    try {
      calendarManager.setActiveCalendar(calendarName);
      return "Now using calendar: '" + calendarName + "'";
    } catch (CalendarNotFoundException e) {
      return e.getMessage();
    }
  }

  private String executeCopyCommand(String[] args) {
    if (args.length < 3) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      return copyEventCommand.execute(args);
    } catch (Exception e) {
      return "Error: " + e.getMessage();
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
          return "Error: " + e.getMessage();
        }
      });
    }
    return null;
  }
}