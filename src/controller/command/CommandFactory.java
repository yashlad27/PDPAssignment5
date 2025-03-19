package controller.command;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;
import view.ICalendarView;

/**
 * Factory for creating and registering commands using functional interfaces.
 */
public class CommandFactory {

  private final Map<String, CommandExecutor> commands;
  private final ICalendar calendar;
  private final ICalendarView view;

  /**
   * Constructs a new CommandFactory and registers all available commands.
   *
   * @param calendar the calendar model
   * @param view     the view for user interaction
   */
  public CommandFactory(ICalendar calendar, ICalendarView view) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }

    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commands = new HashMap<>();
    this.calendar = calendar;
    this.view = view;

    // Register all available commands as functional interfaces
    registerCommands();
  }

  /**
   * Registers all command executors.
   */
  /**
   * Registers all command executors.
   */
  private void registerCommands() {
    // Create event command - using a lambda that captures 'this'
    commands.put("create", args -> {
      if (args.length < 1) {
        return "Error: Insufficient arguments for create command";
      }

      try {
        switch (args[0]) {
          case "single":
            return createSingleEvent(args);
          case "recurring":
            return createRecurringEvent(args);
          case "allday":
            return createAllDayEvent(args);
          case "recurring-until":
            return createRecurringEventUntil(args);
          case "allday-recurring":
            return createAllDayRecurringEvent(args);
          case "allday-recurring-until":
            return createAllDayRecurringEventUntil(args);
          default:
            return "Error: Unknown create event type: " + args[0];
        }
      } catch (ConflictingEventException e) {
        return "Error: Event conflicts with existing event - " + e.getMessage();
      } catch (Exception e) {
        return "Error creating event: " + e.getMessage();
      }
    });

    // Edit event command
    commands.put("edit", args -> {
      if (args.length < 3) {
        return "Error: Insufficient arguments for edit command";
      }

      try {
        String type = args[0];
        if (type.equals("single")) {
          return editSingleEvent(args);
        } else if (type.equals("series_from_date")) {
          return editEventsFromDate(args);
        } else if (type.equals("all")) {
          return editAllEvents(args);
        } else {
          return "Unknown edit command type: " + type;
        }
      } catch (EventNotFoundException e) {
        return "Error: Event not found - " + e.getMessage();
      } catch (InvalidEventException e) {
        return "Error: Invalid event parameters - " + e.getMessage();
      } catch (ConflictingEventException e) {
        return "Error: Edit would create a conflict - " + e.getMessage();
      } catch (Exception e) {
        return "Error editing event: " + e.getMessage();
      }
    });

    // Print events command
    commands.put("print", this::executePrintCommand);

    // Show status command
    commands.put("show", this::executeShowStatusCommand);

    // Export calendar command
    commands.put("export", this::executeExportCommand);

    // Exit command
    commands.put("exit", args -> "Exiting application.");
  }

  /**
   * Executes the create event command.
   */
  private String executeCreateCommand(String[] args) throws ConflictingEventException, InvalidEventException {
    if (args.length < 1) {
      return "Error: Insufficient arguments for create command";
    }

    switch (args[0]) {
      case "single":
        return createSingleEvent(args);
      case "recurring":
        return createRecurringEvent(args);
      case "allday":
        return createAllDayEvent(args);
      case "recurring-until":
        return createRecurringEventUntil(args);
      case "allday-recurring":
        return createAllDayRecurringEvent(args);
      case "allday-recurring-until":
        return createAllDayRecurringEventUntil(args);
      default:
        return "Error: Unknown create event type: " + args[0];
    }
  }

  /**
   * Creates a single event.
   */
  private String createSingleEvent(String[] args) throws ConflictingEventException {
    if (args.length < 4) {
      return "Error: Insufficient arguments for creating a single event";
    }

    try {
      String name = args[1];
      LocalDateTime start = DateTimeUtil.parseDateTime(args[2]);
      LocalDateTime end = DateTimeUtil.parseDateTime(args[3]);

      String description = args.length > 4 ? args[4] : null;
      String location = args.length > 5 ? args[5] : null;
      boolean isPublic = args.length > 6 ? Boolean.parseBoolean(args[6]) : true;
      boolean autoDecline = args.length > 7 ? Boolean.parseBoolean(args[7]) : false;

      Event event = new Event(name, start, end, description, location, isPublic);

      calendar.addEvent(event, autoDecline);
      return "Event '" + name + "' created successfully.";
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      return "Error creating event: " + e.getMessage();
    }
  }

  /**
   * Creates a recurring event.
   */
  private String createRecurringEvent(String[] args) throws ConflictingEventException {
    if (args.length < 7) {
      return "Error: Insufficient arguments for creating a recurring event";
    }

    try {
      String name = args[1];
      LocalDateTime start = DateTimeUtil.parseDateTime(args[2]);
      LocalDateTime end = DateTimeUtil.parseDateTime(args[3]);
      String weekdays = args[4];
      int occurrences = Integer.parseInt(args[5]);
      boolean autoDecline = Boolean.parseBoolean(args[6]);

      String description = args.length > 7 ? args[7] : null;
      String location = args.length > 8 ? args[8] : null;
      boolean isPublic = args.length > 9 ? Boolean.parseBoolean(args[9]) : true;

      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, start, end, repeatDays)
              .description(description)
              .location(location)
              .isPublic(isPublic)
              .occurrences(occurrences)
              .build();

      calendar.addRecurringEvent(recurringEvent, autoDecline);
      return "Recurring event '" + name + "' created successfully with " + occurrences + " occurrences.";
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      return "Error creating recurring event: " + e.getMessage();
    }
  }


  private String createAllDayEvent(String[] args) {
    // Implementation
    return "Not implemented yet";
  }

  private String createRecurringEventUntil(String[] args) {
    // Implementation
    return "Not implemented yet";
  }

  private String createAllDayRecurringEvent(String[] args) {
    // Implementation
    return "Not implemented yet";
  }

  private String createAllDayRecurringEventUntil(String[] args) {
    // Implementation
    return "Not implemented yet";
  }

  /**
   * Executes the edit event command.
   */
// Implementation for CommandFactory.java

  /**
   * Executes the edit event command.
   */
  private String executeEditCommand(String[] args) throws EventNotFoundException, InvalidEventException, ConflictingEventException {
    if (args.length < 3) {
      return "Error: Insufficient arguments for edit command";
    }

    String type = args[0];
    if (type.equals("single")) {
      return editSingleEvent(args);
    } else if (type.equals("series_from_date")) {
      return editEventsFromDate(args);
    } else if (type.equals("all")) {
      return editAllEvents(args);
    } else {
      return "Unknown edit command type: " + type;
    }
  }

  private String editSingleEvent(String[] args) throws EventNotFoundException, InvalidEventException, ConflictingEventException {
    if (args.length < 5) {
      return "Error: Insufficient arguments for editing a single event";
    }

    String property = args[1];
    String subject = args[2];
    LocalDateTime startDateTime;
    try {
      startDateTime = DateTimeUtil.parseDateTime(args[3]);
    } catch (Exception e) {
      return "Error parsing date/time: " + e.getMessage();
    }

    String newValue = args[4];
    if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
      newValue = newValue.substring(1, newValue.length() - 1);
    }

    try {
      boolean success = calendar.editSingleEvent(subject, startDateTime, property, newValue);
      if (success) {
        return "Successfully edited event '" + subject + "'.";
      } else {
        return "Failed to edit event. Event not found or property not editable.";
      }
    } catch (EventNotFoundException e) {
      throw e;
    } catch (InvalidEventException e) {
      throw e;
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      return "Unexpected error editing event: " + e.getMessage();
    }
  }

  private String editEventsFromDate(String[] args) throws InvalidEventException, ConflictingEventException {
    if (args.length < 5) {
      return "Error: Insufficient arguments for editing events from date";
    }

    String property = args[1];
    String subject = args[2];
    LocalDateTime startDateTime;

    try {
      startDateTime = DateTimeUtil.parseDateTime(args[3]);
    } catch (Exception e) {
      return "Error parsing date/time: " + e.getMessage();
    }

    String newValue = args[4];
    if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
      newValue = newValue.substring(1, newValue.length() - 1);
    }

    try {
      int count = calendar.editEventsFromDate(subject, startDateTime, property, newValue);
      if (count > 0) {
        return "Successfully edited " + count + " events.";
      } else {
        return "No matching events found to edit.";
      }
    } catch (InvalidEventException | ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      return "Unexpected error editing events: " + e.getMessage();
    }
  }

  private String editAllEvents(String[] args) throws InvalidEventException, ConflictingEventException {
    if (args.length < 4) {
      return "Error: Insufficient arguments for editing all events";
    }

    String property = args[1];
    String subject = args[2];
    String newValue = args[3];

    if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
      newValue = newValue.substring(1, newValue.length() - 1);
    }

    try {
      int count = calendar.editAllEvents(subject, property, newValue);
      if (count > 0) {
        return "Successfully edited " + count + " events.";
      } else {
        return "No events found with the subject '" + subject + "'.";
      }
    } catch (InvalidEventException | ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      return "Unexpected error editing events: " + e.getMessage();
    }
  }

  /**
   * Executes the print events command.
   */
  private String executePrintCommand(String[] args) {
    // Implementation
    return "Print command not fully implemented yet";
  }

  /**
   * Executes the show status command.
   */
  private String executeShowStatusCommand(String[] args) {
    // Implementation
    return "Show status command not fully implemented yet";
  }

  /**
   * Executes the export calendar command.
   */
  private String executeExportCommand(String[] args) {
    if (args.length < 1) {
      return "Error: Missing filename for export command";
    }

    String filePath = args[0];

    try {
      String absolutePath = calendar.exportToCSV(filePath);
      if (absolutePath != null) {
        return "Calendar exported successfully to: " + absolutePath;
      } else {
        return "Failed to export calendar. Check if the file path is valid and accessible.";
      }
    } catch (IOException e) {
      return "Failed to export calendar: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error during export: " + e.getMessage();
    }
  }


  /**
   * Gets a command executor by name.
   *
   * @param name the name of the command
   * @return the command executor, or null if not found
   */
  public CommandExecutor getCommand(String name) {
    return commands.get(name);
  }

  /**
   * Checks if a command is registered.
   *
   * @param name the name of the command
   * @return true if the command is registered, false otherwise
   */
  public boolean hasCommand(String name) {
    return commands.containsKey(name);
  }

  /**
   * Gets all available command names.
   *
   * @return a set of command names
   */
  public Iterable<String> getCommandNames() {
    return commands.keySet();
  }

  /**
   * Gets the calendar instance.
   *
   * @return the calendar instance
   */
  public ICalendar getCalendar() {
    return calendar;
  }

  /**
   * Gets the view instance.
   *
   * @return the view instance
   */
  public ICalendarView getView() {
    return view;
  }
}