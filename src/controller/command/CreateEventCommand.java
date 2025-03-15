package controller.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import utilities.DateTimeUtil;

/**
 * Command for creating calendar events (both single and recurring events).
 */
public class CreateEventCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Constructs a new CreateEventCommand with the specified calendar.
   *
   * @param calendar the calendar in which to create events
   */
  public CreateEventCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  /**
   * Creates a single event in the calendar.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param autoDecline   if true, event creation will be declined if it conflicts
   * @param description   the description of the event
   * @param location      the location of the event
   * @param isPublic      whether the event is public
   * @return a response indicating success or failure
   */
  private String createEvent(String eventName, LocalDateTime startDateTime,
      LocalDateTime endDateTime, boolean autoDecline, String description, String location,
      boolean isPublic) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (startDateTime == null) {
      return "Error: Start date/time cannot be null";
    }

    Event event = new Event(eventName, startDateTime, endDateTime, description, location, isPublic);

    boolean success = calendar.addEvent(event, autoDecline);

    if (success) {
      return "Event '" + eventName + "' created successfully.";
    } else {
      return "Failed to create event due to conflicts.";
    }
  }

  /**
   * Creates an all-day event in the calendar.
   *
   * @param eventName   the name of the event
   * @param date        the date of the event
   * @param autoDecline if true, event creation will be declined if it conflicts
   * @param description the description of the event
   * @param location    the location of the event
   * @param isPublic    whether the event is public
   * @return a response indicating success or failure
   */
  private String createAllDayEvent(String eventName, LocalDate date, boolean autoDecline,
      String description, String location, boolean isPublic) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (date == null) {
      return "Error: Date cannot be null";
    }

    Event event = Event.createAllDayEvent(eventName, date, description, location, isPublic);

    boolean success = calendar.addEvent(event, autoDecline);

    if (success) {
      return "All-day event '" + eventName + "' created successfully.";
    } else {
      return "Failed to create all-day event due to conflicts.";
    }
  }

  /**
   * Creates a recurring event in the calendar with a specified number of occurrences.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the first occurrence
   * @param endDateTime   the end date and time of the first occurrence
   * @param weekdays      the days of the week on which the event repeats, e.g., "MWF"
   * @param occurrences   the number of times the event repeats
   * @param autoDecline   if true, event creation will be declined if it conflicts
   * @param description   the description of the event
   * @param location      the location of the event
   * @param isPublic      whether the event is public
   * @return a response indicating success or failure
   */
  private String createRecurringEvent(String eventName, LocalDateTime startDateTime,
      LocalDateTime endDateTime, String weekdays, int occurrences, boolean autoDecline,
      String description, String location, boolean isPublic) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (startDateTime == null) {
      return "Error: Start date/time cannot be null";
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      return "Error: Weekdays cannot be empty";
    }
    if (occurrences <= 0) {
      return "Error: Occurrences must be positive";
    }

    try {
      Set<java.time.DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent(eventName, startDateTime, endDateTime,
          description, location, isPublic, repeatDays, occurrences);

      boolean success = calendar.addRecurringEvent(recurringEvent, autoDecline);

      if (success) {
        return "Recurring event '" + eventName + "' created successfully with " + occurrences
            + " occurrences.";
      } else {
        return "Failed to create recurring event due to conflicts.";
      }
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Creates a recurring event in the calendar that repeats until a specified end date.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the first occurrence
   * @param endDateTime   the end date and time of the first occurrence
   * @param weekdays      the days of the week on which the event repeats, e.g., "MWF"
   * @param untilDate     the date after which the event stops repeating
   * @param autoDecline   if true, event creation will be declined if it conflicts
   * @param description   the description of the event
   * @param location      the location of the event
   * @param isPublic      whether the event is public
   * @return a response indicating success or failure
   */
  private String createRecurringEventUntil(String eventName, LocalDateTime startDateTime,
      LocalDateTime endDateTime, String weekdays, LocalDate untilDate, boolean autoDecline,
      String description, String location, boolean isPublic) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (startDateTime == null) {
      return "Error: Start date/time cannot be null";
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      return "Error: Weekdays cannot be empty";
    }
    if (untilDate == null) {
      return "Error: Until date cannot be null";
    }

    try {
      Set<java.time.DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent(eventName, startDateTime, endDateTime,
          description, location, isPublic, repeatDays, untilDate);

      boolean success = calendar.addRecurringEvent(recurringEvent, autoDecline);

      if (success) {
        return "Recurring event '" + eventName + "' created successfully, repeating until "
            + DateTimeUtil.formatDate(untilDate) + ".";
      } else {
        return "Failed to create recurring event due to conflicts.";
      }
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Executes the create event command with the provided arguments. Handles different types of event
   * creation including single events, recurring events, all-day events, and their variants.
   *
   * @param args an array of arguments for the command: - args[0]: the type of event to create
   *             (single, recurring, allday, etc.) - remaining args: parameters specific to each
   *             event type
   * @return a string message indicating the result of the command execution
   */
  @Override
  public String execute(String[] args) {
    if (args.length < 1) {
      return "Error: Insufficient arguments for create command";
    }

    switch (args[0]) {
      case "single":
        /*
        Example: single "Meeting" 2023-05-15T10:00 2023-05-15T11:00 "Description"
        "Location" true true
         */
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

          return createEvent(name, start, end, autoDecline, description, location, isPublic);
        } catch (Exception e) {
          return "Error parsing arguments: " + e.getMessage();
        }

      case "recurring":
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

          return createRecurringEvent(name, start, end, weekdays, occurrences, autoDecline,
              description, location, isPublic);
        } catch (Exception e) {
          return "Error parsing arguments: " + e.getMessage();
        }

      case "allday":
        // Example: allday "Holiday" 2023-05-15 true "Description" "Location" true
        if (args.length < 4) {
          return "Error: Insufficient arguments for creating an all-day event";
        }
        try {
          String name = args[1];
          LocalDate date = DateTimeUtil.parseDate(args[2]);
          boolean autoDecline = Boolean.parseBoolean(args[3]);

          String description = args.length > 4 ? args[4] : null;
          String location = args.length > 5 ? args[5] : null;
          boolean isPublic = args.length > 6 ? Boolean.parseBoolean(args[6]) : true;

          return createAllDayEvent(name, date, autoDecline, description, location, isPublic);
        } catch (Exception e) {
          return "Error parsing arguments: " + e.getMessage();
        }

      case "recurring-until":
        if (args.length < 7) {
          return "Error: Insufficient arguments for recurring event until date";
        }
        try {
          String name = args[1];
          LocalDateTime start = DateTimeUtil.parseDateTime(args[2]);
          LocalDateTime end = DateTimeUtil.parseDateTime(args[3]);
          String weekdays = args[4];
          LocalDate untilDate = DateTimeUtil.parseDate(args[5]);
          boolean autoDecline = Boolean.parseBoolean(args[6]);

          String description = args.length > 7 ? args[7] : null;
          String location = args.length > 8 ? args[8] : null;
          boolean isPublic = args.length > 9 ? Boolean.parseBoolean(args[9]) : true;

          return createRecurringEventUntil(name, start, end, weekdays, untilDate, autoDecline,
              description, location, isPublic);
        } catch (Exception e) {
          return "Error creating recurring event: " + e.getMessage();
        }

      case "allday-recurring":
        if (args.length < 6) {
          return "Error: Insufficient arguments for all-day recurring event";
        }
        try {
          String name = args[1];
          LocalDate date = DateTimeUtil.parseDate(args[2]);
          String weekdays = args[3];
          int occurrences = Integer.parseInt(args[4]);
          boolean autoDecline = Boolean.parseBoolean(args[5]);

          String description = args.length > 6 ? args[6] : null;
          String location = args.length > 7 ? args[7] : null;
          boolean isPublic = args.length > 8 ? Boolean.parseBoolean(args[8]) : true;

          boolean success = calendar.createAllDayRecurringEvent(name, date, weekdays, occurrences,
              autoDecline, description, location, isPublic);

          if (success) {
            return "All-day recurring event '" + name + "' created successfully with " + occurrences
                + " occurrences.";
          } else {
            return "Failed to create all-day recurring event due to conflicts.";
          }
        } catch (Exception e) {
          return "Error creating all-day recurring event: " + e.getMessage();
        }

      case "allday-recurring-until":
        if (args.length < 6) {
          return "Error: Insufficient arguments for all-day recurring event until date";
        }
        try {
          String name = args[1];
          LocalDate date = DateTimeUtil.parseDate(args[2]);
          String weekdays = args[3];
          LocalDate untilDate = DateTimeUtil.parseDate(args[4]);
          boolean autoDecline = Boolean.parseBoolean(args[5]);

          String description = args.length > 6 ? args[6] : null;
          String location = args.length > 7 ? args[7] : null;
          boolean isPublic = args.length > 8 ? Boolean.parseBoolean(args[8]) : true;

          boolean success = calendar.createAllDayRecurringEventUntil(name, date, weekdays,
              untilDate, autoDecline, description, location, isPublic);

          if (success) {
            return "All-day recurring event '" + name + "' created successfully until " + untilDate
                + ".";
          } else {
            return "Failed to create all-day recurring event due to conflicts.";
          }
        } catch (Exception e) {
          return "Error creating all-day recurring event: " + e.getMessage();
        }

      default:
        return "Error: Unknown create event type: " + args[0];
    }
  }

  /**
   * Returns the name of this command.
   *
   * @return the string "create" which identifies this command to the command factory
   */
  @Override
  public String getName() {
    return "create";
  }
}