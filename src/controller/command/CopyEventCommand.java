package controller.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;
import utilities.TimezoneConverter;

/**
 * Consolidated command for copying events between calendars.
 * Uses the Strategy pattern internally to handle different copy operations.
 */
public class CopyEventCommand implements ICommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new CopyEventsCommand.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   */
  public CopyEventCommand(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }
    if (timezoneHandler == null) {
      throw new IllegalArgumentException("TimeZoneHandler cannot be null");
    }
    this.calendarManager = calendarManager;
    this.timezoneHandler = timezoneHandler;
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    System.out.println("*** Debug: Executing copy command with args:");
    for (int i = 0; i < args.length; i++) {
      System.out.println("Args[" + i + "]: '" + args[i] + "'");
    }

    if (args.length < 1) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      // Create a context object to hold parsed command data
      CopyCommandContext context = parseCommandArguments(args);

      // Execute the appropriate copy strategy based on the command type
      switch (context.commandType) {
        case SINGLE_EVENT:
          return copyEvent(context.eventName, context.sourceDateTime,
                  context.targetCalendar, context.targetDateTime);
        case EVENTS_ON_DATE:
          return copyEventsOnDate(context.sourceDate,
                  context.targetCalendar, context.targetDate);
        case EVENTS_BETWEEN_DATES:
          return copyEventsBetweenDates(context.sourceStartDate,
                  context.sourceEndDate,
                  context.targetCalendar,
                  context.targetStartDate);
        case UNKNOWN:
        default:
          return "Error: Unknown copy command format";
      }
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (CalendarNotFoundException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Command type enumeration to represent different copy operations
   */
  private enum CommandType {
    SINGLE_EVENT, EVENTS_ON_DATE, EVENTS_BETWEEN_DATES, UNKNOWN
  }

  /**
   * Context object to hold parsed command parameters
   */
  private static class CopyCommandContext {
    CommandType commandType = CommandType.UNKNOWN;
    String eventName;
    String sourceDateTime;
    String targetDateTime;
    String sourceDate;
    String targetDate;
    String sourceStartDate;
    String sourceEndDate;
    String targetStartDate;
    String targetCalendar;
  }

  /**
   * Parses command arguments into a context object
   */
  private CopyCommandContext parseCommandArguments(String[] args) {
    CopyCommandContext context = new CopyCommandContext();

    if (args.length > 2 && "events".equals(args[1]) && "between".equals(args[2])) {
      System.out.println("DEBUG: Parsing 'copy events between' command");
      parseEventsBetweenDatesCommand(args, context);
    }

    if (args[0].equals("copy")) {
      // Parse controller-format commands
      if (args.length < 2) {
        throw new IllegalArgumentException("Insufficient arguments for copy command");
      }

      if (args[1].equals("event")) {
        // Handle "copy event <name> on <date> --target <cal> to <date>"
        parseEventCopyCommand(args, context);
      } else if (args[1].equals("events")) {
        if (args.length < 3) {
          throw new IllegalArgumentException("Insufficient arguments for copy events command");
        }

        if (args[2].equals("on")) {
          // Handle "copy events on <date> --target <cal> to <date>"
          parseEventsOnDateCommand(args, context);
        } else if (args[2].equals("between")) {
          // Handle "copy events between <date> and <date> --target <cal> to <date>"
          parseEventsBetweenDatesCommand(args, context);
        }
      }
    } else if (args[0].equals("event")) {
      // Parse internal format: [event, name, dateTime, targetCal, targetDateTime]
      context.commandType = CommandType.SINGLE_EVENT;
      context.eventName = args[1];
      context.sourceDateTime = args[2];
      context.targetCalendar = args[3];
      context.targetDateTime = args[4];
    } else if (args[0].equals("events_on_date")) {
      // Parse internal format: [events_on_date, date, targetCal, targetDate]
      context.commandType = CommandType.EVENTS_ON_DATE;
      context.sourceDate = args[1];
      context.targetCalendar = args[2];
      context.targetDate = args[3];
    } else if (args[0].equals("events_between_dates")) {
      // Parse internal format: [events_between_dates, startDate, endDate, targetCal, targetStartDate]
      context.commandType = CommandType.EVENTS_BETWEEN_DATES;
      context.sourceStartDate = args[1];
      context.sourceEndDate = args[2];
      context.targetCalendar = args[3];
      context.targetStartDate = args[4];
    }

    return context;
  }

  /**
   * Parse the "copy event" command format
   */
  private void parseEventCopyCommand(String[] args, CopyCommandContext context) {
    System.out.println("DEBUG: Parsing copy event command");
    for (int i = 0; i < args.length; i++) {
      System.out.println("Args[" + i + "]: '" + args[i] + "'");
    }

    if (args.length < 8) {
      throw new IllegalArgumentException("Insufficient arguments for copy event command");
    }

    context.commandType = CommandType.SINGLE_EVENT;
    context.eventName = args[2];

    // Remove quotes if present
    if (context.eventName.startsWith("\"") && context.eventName.endsWith("\"")) {
      context.eventName = context.eventName.substring(1, context.eventName.length() - 1);
    }

    if (!args[3].equals("on")) {
      throw new IllegalArgumentException("Expected 'on' keyword after event name");
    }

    context.sourceDateTime = args[4];

    if (!args[5].equals("--target")) {
      throw new IllegalArgumentException("Expected '--target' flag");
    }

    context.targetCalendar = args[6];

    if (!args[7].equals("to")) {
      throw new IllegalArgumentException("Expected 'to' keyword");
    }

    context.targetDateTime = args[8];
  }

  /**
   * Parse the "copy events on date" command format
   */
  private void parseEventsOnDateCommand(String[] args, CopyCommandContext context) {
    if (args.length < 8) {
      throw new IllegalArgumentException("Insufficient arguments for copy events on date command");
    }

    context.commandType = CommandType.EVENTS_ON_DATE;
    context.sourceDate = args[3];

    if (!args[4].equals("--target")) {
      throw new IllegalArgumentException("Expected '--target' flag");
    }

    context.targetCalendar = args[5];

    if (!args[6].equals("to")) {
      throw new IllegalArgumentException("Expected 'to' keyword");
    }

    context.targetDate = args[7];
  }

  /**
   * Parse the "copy events between dates" command format
   */
  private void parseEventsBetweenDatesCommand(String[] args, CopyCommandContext context) {


//    if (args.length < 10) {
//      throw new IllegalArgumentException("Insufficient arguments for copy events between dates command");
//    }
//
//    context.commandType = CommandType.EVENTS_BETWEEN_DATES;
//    context.sourceStartDate = args[3];
//
//    if (!args[4].equals("and")) {
//      throw new IllegalArgumentException("Expected 'and' keyword");
//    }
//
//    context.sourceEndDate = args[5];
//
//    if (!args[6].equals("--target")) {
//      throw new IllegalArgumentException("Expected '--target' flag");
//    }
//
//    context.targetCalendar = args[7];
//
//    if (!args[8].equals("to")) {
//      throw new IllegalArgumentException("Expected 'to' keyword");
//    }
//
//    context.targetStartDate = args[9];

    System.out.println("DEBUG: Entering parseEventsBetweenDatesCommand()");

    // Print all received arguments
    for (int i = 0; i < args.length; i++) {
      System.out.println("Args[" + i + "]: '" + args[i] + "'");
    }

    if (args.length < 9) {
      throw new IllegalArgumentException("Insufficient arguments for copy events between dates command. Expected 9, but got: " + args.length);
    }

    context.commandType = CommandType.EVENTS_BETWEEN_DATES;
    context.sourceStartDate = args[3];

    if (!args[4].equals("and")) {
      throw new IllegalArgumentException("Expected 'and' keyword, but got: " + args[4]);
    }

    context.sourceEndDate = args[5];

    if (!args[6].equals("--target")) {
      throw new IllegalArgumentException("Expected '--target' flag, but got: " + args[6]);
    }

    context.targetCalendar = args[7];

    if (!args[8].equals("to")) {
      throw new IllegalArgumentException("Expected 'to' keyword, but got: " + args[8]);
    }

    context.targetStartDate = args[9];
  }


  /**
   * Copies a single event from the active calendar to a target calendar.
   */
  private String copyEvent(String eventName, String dateTimeStr, String targetCalendarName, String targetDateTimeStr) throws Exception {
    // Parse the date/time
    LocalDateTime sourceDateTime = DateTimeUtil.parseDateTime(dateTimeStr);
    LocalDateTime targetDateTime = DateTimeUtil.parseDateTime(targetDateTimeStr);

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException("Target calendar '" + targetCalendarName + "' does not exist");
    }

    // Get source calendar (active calendar)
    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    // Find the event
    Event sourceEvent = sourceCalendar.findEvent(eventName, sourceDateTime);
    if (sourceEvent == null) {
      throw new EventNotFoundException("Event not found: " + eventName + " at " + sourceDateTime);
    }

    // Get the source and target timezones
    String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
    String targetTimezone = calendarManager.executeOnCalendar(targetCalendarName,
            calendar -> ((model.calendar.Calendar) calendar).getTimezone());

    // Create timezone converter
    TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

    // Calculate duration of the source event
    long durationSeconds = sourceEvent.getEndDateTime().toEpochSecond(java.time.ZoneOffset.UTC) -
            sourceEvent.getStartDateTime().toEpochSecond(java.time.ZoneOffset.UTC);

    // Create a new event with the adjusted time
    Event newEvent = new Event(
            sourceEvent.getSubject(),
            converter.convert(targetDateTime),
            converter.convert(targetDateTime.plusSeconds(durationSeconds)),
            sourceEvent.getDescription(),
            sourceEvent.getLocation(),
            sourceEvent.isPublic()
    );

    // Add the event to the target calendar
    boolean success = calendarManager.executeOnCalendar(targetCalendarName,
            calendar -> calendar.addEvent(newEvent, true));

    if (success) {
      return "Event '" + eventName + "' copied successfully to calendar '" + targetCalendarName + "'.";
    } else {
      return "Failed to copy event due to conflicts.";
    }
  }

  /**
   * Copies all events on a specific date from the active calendar to a target calendar.
   */
  private String copyEventsOnDate(String dateStr, String targetCalendarName, String targetDateStr) throws Exception {
    // Parse the dates
    LocalDate sourceDate = DateTimeUtil.parseDate(dateStr);
    LocalDate targetDate = DateTimeUtil.parseDate(targetDateStr);

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException("Target calendar '" + targetCalendarName + "' does not exist");
    }

    // Get source calendar (active calendar)
    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    // Get events on the source date
    List<Event> eventsOnDate = sourceCalendar.getEventsOnDate(sourceDate);

    if (eventsOnDate.isEmpty()) {
      return "No events found on " + sourceDate + " to copy.";
    }

    // Get the source and target timezones
    String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
    String targetTimezone = calendarManager.executeOnCalendar(targetCalendarName,
            calendar -> ((model.calendar.Calendar) calendar).getTimezone());

    // Create timezone converter
    TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

    // Calculate date difference in days
    long daysDifference = targetDate.toEpochDay() - sourceDate.toEpochDay();

    int successCount = 0;
    int failCount = 0;

    // Copy each event
    for (Event sourceEvent : eventsOnDate) {
      try {
        // Create a new event with the adjusted date
        Event newEvent;

        if (sourceEvent.isAllDay()) {
          // All-day event
          newEvent = Event.createAllDayEvent(
                  sourceEvent.getSubject(),
                  targetDate,
                  sourceEvent.getDescription(),
                  sourceEvent.getLocation(),
                  sourceEvent.isPublic()
          );
        } else {
          // Regular event - adjust date and convert timezone
          LocalDateTime adjustedStart = sourceEvent.getStartDateTime().plusDays(daysDifference);
          LocalDateTime adjustedEnd = sourceEvent.getEndDateTime().plusDays(daysDifference);

          newEvent = new Event(
                  sourceEvent.getSubject(),
                  converter.convert(adjustedStart),
                  converter.convert(adjustedEnd),
                  sourceEvent.getDescription(),
                  sourceEvent.getLocation(),
                  sourceEvent.isPublic()
          );
        }

        // Add the event to the target calendar
        calendarManager.executeOnCalendar(targetCalendarName,
                calendar -> calendar.addEvent(newEvent, true));

        successCount++;
      } catch (ConflictingEventException e) {
        failCount++;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (failCount == 0) {
      return "Successfully copied " + successCount + " events from " + sourceDate +
              " to " + targetDate + " in calendar '" + targetCalendarName + "'.";
    } else {
      return "Copied " + successCount + " events, but " + failCount +
              " events could not be copied due to conflicts.";
    }
  }

  /**
   * Copies events within a date range from the active calendar to a target calendar.
   */
  private String copyEventsBetweenDates(String startDateStr, String endDateStr, String targetCalendarName, String targetStartDateStr) throws Exception {
    // Parse the dates
    LocalDate sourceStartDate = DateTimeUtil.parseDate(startDateStr);
    LocalDate sourceEndDate = DateTimeUtil.parseDate(endDateStr);
    LocalDate targetStartDate = DateTimeUtil.parseDate(targetStartDateStr);

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException("Target calendar '" + targetCalendarName + "' does not exist");
    }

    // Get source calendar (active calendar)
    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    // Get events in the date range
    List<Event> eventsInRange = sourceCalendar.getEventsInRange(sourceStartDate, sourceEndDate);

    if (eventsInRange.isEmpty()) {
      return "No events found between " + sourceStartDate + " and " + sourceEndDate + " to copy.";
    }

    // Get the source and target timezones
    String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
    String targetTimezone = calendarManager.executeOnCalendar(targetCalendarName,
            calendar -> ((model.calendar.Calendar) calendar).getTimezone());

    // Create timezone converter
    TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

    // Calculate date difference in days
    long daysDifference = targetStartDate.toEpochDay() - sourceStartDate.toEpochDay();

    int successCount = 0;
    int failCount = 0;

    // Copy each event
    for (Event sourceEvent : eventsInRange) {
      try {
        // Create a new event with the adjusted date
        Event newEvent;

        if (sourceEvent.isAllDay()) {
          // All-day event
          LocalDate eventDate = sourceEvent.getDate();
          if (eventDate == null) {
            eventDate = sourceEvent.getStartDateTime().toLocalDate();
          }

          // Calculate the adjusted date
          LocalDate adjustedDate = eventDate.plusDays(daysDifference);

          newEvent = Event.createAllDayEvent(
                  sourceEvent.getSubject(),
                  adjustedDate,
                  sourceEvent.getDescription(),
                  sourceEvent.getLocation(),
                  sourceEvent.isPublic()
          );
        } else {
          // Regular event - adjust date and convert timezone
          LocalDateTime adjustedStart = sourceEvent.getStartDateTime().plusDays(daysDifference);
          LocalDateTime adjustedEnd = sourceEvent.getEndDateTime().plusDays(daysDifference);

          newEvent = new Event(
                  sourceEvent.getSubject(),
                  converter.convert(adjustedStart),
                  converter.convert(adjustedEnd),
                  sourceEvent.getDescription(),
                  sourceEvent.getLocation(),
                  sourceEvent.isPublic()
          );
        }

        // Add the event to the target calendar
        calendarManager.executeOnCalendar(targetCalendarName,
                calendar -> calendar.addEvent(newEvent, true));

        successCount++;
      } catch (ConflictingEventException e) {
        failCount++;
      } catch (Exception e) {
        throw new RuntimeException("Error copying event: " + e.getMessage(), e);
      }
    }

    if (failCount == 0) {
      return "Successfully copied " + successCount + " events from date range " +
              sourceStartDate + " to " + sourceEndDate + " in calendar '" + targetCalendarName + "'.";
    } else {
      return "Copied " + successCount + " events, but " + failCount +
              " events could not be copied due to conflicts.";
    }
  }

  @Override
  public String getName() {
    return "copy";
  }
}