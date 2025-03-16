package controller.command;

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
  private void registerCommands() {
    // Create event command
    commands.put("create", this::executeCreateCommand);

    // Edit event command
    commands.put("edit", this::executeEditCommand);

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

  // Other create event methods would be implemented similarly...

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
    // Implementation
    return "Not implemented yet";
  }

  private String editEventsFromDate(String[] args) throws InvalidEventException, ConflictingEventException {
    // Implementation
    return "Not implemented yet";
  }

  private String editAllEvents(String[] args) throws InvalidEventException, ConflictingEventException {
    // Implementation
    return "Not implemented yet";
  }

  /**
   * Executes the print events command.
   */
  private String executePrintCommand(String[] args) {
    // Implementation
    return "Not implemented yet";
  }

  /**
   * Executes the show status command.
   */
  private String executeShowStatusCommand(String[] args) {
    // Implementation
    return "Not implemented yet";
  }

  /**
   * Executes the export calendar command.
   */
  private String executeExportCommand(String[] args) {
    // Implementation
    return "Not implemented yet";
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