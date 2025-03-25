package controller.command.calendar;

import java.util.HashMap;
import java.util.Map;

import controller.ICommandFactory;
import controller.command.CommandAdapter;
import controller.command.ICommand;
import controller.command.copy.CopyEventCommand;
import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;
import view.ICalendarView;

/**
 * Factory class for creating and managing calendar-related commands.
 *
 * <p>This factory implements the Command pattern to handle various calendar operations
 * such as creating calendars, editing calendar properties, switching between calendars,
 * and copying events between calendars.
 *
 * <p>The factory maintains a registry of command handlers and provides methods to:
 * - Create command instances
 * - Execute calendar operations
 * - Manage command registration
 * - Handle command execution errors
 *
 * @see ICommandFactory
 * @see CalendarManager
 * @see ICalendarView
 */
public class CalendarCommandFactory implements ICommandFactory {

  private final Map<String, CalendarCommandHandler> commands;
  private final CalendarManager calendarManager;
  private final ICalendarView view;

  /**
   * Constructs a new CalendarCommandFactory with the specified dependencies.
   *
   * <p>This factory manages calendar-level commands such as creating, editing,
   * and using calendars. It initializes command handlers and validates
   * required dependencies.
   *
   * @param calendarManager Manager for calendar operations, must not be null
   * @param view            View component for user interaction, must not be null
   * @throws IllegalArgumentException if calendarManager or view is null
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
    this.view = view;
    TimeZoneHandler timezoneHandler = calendarManager.getTimezoneHandler();
    CopyEventCommand copyEventCommand = new CopyEventCommand(calendarManager, timezoneHandler);

    registerCommands(copyEventCommand);
  }

  private void registerCommands(CopyEventCommand copyEventCommand) {
    commands.put("create", this::executeCreateCommand);
    commands.put("edit", this::executeEditCalendarCommand);
    commands.put("use", this::executeUseCalendarCommand);
    commands.put("copy", args -> {
      try {
        return copyEventCommand.execute(args);
      } catch (ConflictingEventException e) {
        throw new RuntimeException(e);
      } catch (InvalidEventException e) {
        throw new RuntimeException(e);
      } catch (EventNotFoundException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private String executeCreateCommand(String[] args) {
    if (args.length < 5) {
      return "Error: Insufficient arguments for create calendar command";
    }

    String calendarName = args[2];
    String timezone = args[4];

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
      TimeZoneHandler timezoneHandler = calendarManager.getTimezoneHandler();
      CopyEventCommand copyEventCommand = new CopyEventCommand(calendarManager, timezoneHandler);
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