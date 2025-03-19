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
 * Command for copying events between calendars.
 */
public class CopyEventCommand implements ICommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new CopyEventCommand.
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

    String subCommand = args[0];

    try {
      switch (subCommand) {
        case "event":
          return copyEvent(args);
        case "events_on_date":
          return copyEventsOnDate(args);
        case "events_between_dates":
          return copyEventsBetweenDates(args);
        default:
          return "Error: Unknown copy command: " + subCommand;
      }
    } catch (CalendarNotFoundException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Copies a single event from one calendar to another.
   */
  private String copyEvent(String[] args) throws Exception {
    if (args.length < 6) {
      return "Error: Insufficient arguments for copy event command";
    }

    String eventName = args[1];
    LocalDateTime sourceDateTime = DateTimeUtil.parseDateTime(args[2]);
    String targetCalendarName = args[3];
    LocalDateTime targetDateTime = DateTimeUtil.parseDateTime(args[4]);
    boolean autoDecline = args.length > 5 ? Boolean.parseBoolean(args[5]) : true;

    // Get source calendar (active calendar)
    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    // Find the event
    Event sourceEvent = sourceCalendar.findEvent(eventName, sourceDateTime);
    if (sourceEvent == null) {
      throw new EventNotFoundException("Event not found: " + eventName + " at " + sourceDateTime);
    }

    // Determine time shift
    long timeShiftMillis = targetDateTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli() -
            sourceDateTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();

    // Get the source and target timezones
    String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
    String targetTimezone = calendarManager.executeOnCalendar(targetCalendarName,
            calendar -> ((model.calendar.Calendar) calendar).getTimezone());

    // Create timezone converter
    TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

    // Create a new event with the adjusted time
    Event newEvent = new Event(
            sourceEvent.getSubject(),
            converter.convert(targetDateTime),
            converter.convert(targetDateTime.plusSeconds(
                    sourceEvent.getEndDateTime().toEpochSecond(java.time.ZoneOffset.UTC) -
                            sourceEvent.getStartDateTime().toEpochSecond(java.time.ZoneOffset.UTC))),
            sourceEvent.getDescription(),
            sourceEvent.getLocation(),
            sourceEvent.isPublic()
    );

    // Add the event to the target calendar
    boolean added = calendarManager.executeOnCalendar(targetCalendarName,
            calendar -> calendar.addEvent(newEvent, autoDecline));

    if (added) {
      return "Event '" + eventName + "' copied successfully to calendar '" + targetCalendarName + "'.";
    } else {
      return "Failed to copy event due to conflicts.";
    }
  }

  /**
   * Copies all events on a specific date from one calendar to another.
   */
  private String copyEventsOnDate(String[] args) throws Exception {
    if (args.length < 4) {
      return "Error: Insufficient arguments for copy events on date command";
    }

    LocalDate sourceDate = DateTimeUtil.parseDate(args[1]);
    String targetCalendarName = args[2];
    LocalDate targetDate = DateTimeUtil.parseDate(args[3]);
    boolean autoDecline = args.length > 4 ? Boolean.parseBoolean(args[4]) : true;

    // Get source calendar (active calendar)
    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    // Get events on the source date
    List<Event> eventsOnDate = sourceCalendar.getEventsOnDate(sourceDate);

    if (eventsOnDate.isEmpty()) {
      return "No events found on date " + sourceDate;
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
                calendar -> calendar.addEvent(newEvent, autoDecline));

        successCount++;
      } catch (ConflictingEventException e) {
        failCount++;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (failCount == 0) {
      return "Successfully copied " + successCount + " events from " + sourceDate + " to " + targetDate +
              " in calendar '" + targetCalendarName + "'.";
    } else {
      return "Copied " + successCount + " events, but " + failCount + " events could not be copied due to conflicts.";
    }
  }

  /**
   * Copies events within a date range from one calendar to another.
   */
  private String copyEventsBetweenDates(String[] args) throws Exception {
    if (args.length < 5) {
      return "Error: Insufficient arguments for copy events between dates command";
    }

    LocalDate sourceStartDate = DateTimeUtil.parseDate(args[1]);
    LocalDate sourceEndDate = DateTimeUtil.parseDate(args[2]);
    String targetCalendarName = args[3];
    LocalDate targetStartDate = DateTimeUtil.parseDate(args[4]);
    boolean autoDecline = args.length > 5 ? Boolean.parseBoolean(args[5]) : true;

    // Get source calendar (active calendar)
    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    // Get events in the date range
    List<Event> eventsInRange = sourceCalendar.getEventsInRange(sourceStartDate, sourceEndDate);

    if (eventsInRange.isEmpty()) {
      return "No events found between " + sourceStartDate + " and " + sourceEndDate;
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
                calendar -> calendar.addEvent(newEvent, autoDecline));

        successCount++;
      } catch (ConflictingEventException e) {
        failCount++;
      }
    }

    if (failCount == 0) {
      return "Successfully copied " + successCount + " events from date range " + sourceStartDate +
              " to " + sourceEndDate + " in calendar '" + targetCalendarName + "'.";
    } else {
      return "Copied " + successCount + " events, but " + failCount + " events could not be copied due to conflicts.";
    }
  }

  @Override
  public String getName() {
    return "copy";
  }
}