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
    if (args.length < 1) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      // The first arg is either "copy" or a specific format from CalendarCommandFactory
      if (args[0].equals("copy")) {
        // This is the direct format from the controller
        // Format: [copy, event/events, ...] or [copy, events, between/on, ...]
        if (args.length < 2) {
          return "Error: Insufficient arguments for copy command";
        }

        String type = args[1];

        if (type.equals("event")) {
          // copy event <eventName> on <dateTimeStr> --target <targetCalendarName> to <targetDateTimeStr>
          if (args.length < 9) {
            return "Error: Insufficient arguments for copy event command";
          }

          String eventName = args[2];
          // Remove quotes if present
          if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
            eventName = eventName.substring(1, eventName.length() - 1);
          }

          if (!args[3].equals("on")) {
            return "Error: Expected 'on' keyword after event name";
          }

          String dateTimeStr = args[4];

          if (!args[5].equals("--target")) {
            return "Error: Expected '--target' flag";
          }

          String targetCalendarName = args[6];

          if (!args[7].equals("to")) {
            return "Error: Expected 'to' keyword";
          }

          String targetDateTimeStr = args[8];

          return copyEvent(eventName, dateTimeStr, targetCalendarName, targetDateTimeStr);
        } else if (type.equals("events")) {
          if (args.length < 3) {
            return "Error: Insufficient arguments for copy events command";
          }

          String subType = args[2];

          if (subType.equals("on")) {
            // copy events on <dateStr> --target <targetCalendarName> to <targetDateStr>
            if (args.length < 8) {
              return "Error: Insufficient arguments for copy events on date command";
            }

            String dateStr = args[3];

            if (!args[4].equals("--target")) {
              return "Error: Expected '--target' flag";
            }

            String targetCalendarName = args[5];

            if (!args[6].equals("to")) {
              return "Error: Expected 'to' keyword";
            }

            String targetDateStr = args[7];

            return copyEventsOnDate(dateStr, targetCalendarName, targetDateStr);
          } else if (subType.equals("between")) {
            // copy events between <startDateStr> and <endDateStr> --target <targetCalendarName> to <targetStartDateStr>
            if (args.length < 10) {
              return "Error: Insufficient arguments for copy events between dates command";
            }

            String startDateStr = args[3];

            if (!args[4].equals("and")) {
              return "Error: Expected 'and' keyword";
            }

            String endDateStr = args[5];

            if (!args[6].equals("--target")) {
              return "Error: Expected '--target' flag";
            }

            String targetCalendarName = args[7];

            if (!args[8].equals("to")) {
              return "Error: Expected 'to' keyword";
            }

            String targetStartDateStr = args[9];

            return copyEventsBetweenDates(startDateStr, endDateStr, targetCalendarName, targetStartDateStr);
          }
        }

        return "Error: Unknown copy command format";
      } else if (args[0].equals("event")) {
        // This is the internal format from CalendarCommandFactory
        return copyEvent(args[1], args[2], args[3], args[4]);
      } else if (args[0].equals("events_on_date")) {
        // This is the internal format from CalendarCommandFactory
        return copyEventsOnDate(args[1], args[2], args[3]);
      } else if (args[0].equals("events_between_dates")) {
        // This is the internal format from CalendarCommandFactory
        return copyEventsBetweenDates(args[1], args[2], args[3], args[4]);
      } else {
        return "Error: Unknown copy command: " + args[0];
      }
    } catch (CalendarNotFoundException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
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