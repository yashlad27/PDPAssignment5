package controller.command.copy.strategy;

import java.time.LocalDateTime;

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
 * Strategy for copying a single event from one calendar to another. Format: copy event
 * {@code eventName} on {@code dateStringTtimeString} --target {@code calendarName} to
 * {@code dateStringTtimeString}
 */
public class SingleEventCopyStrategy implements CopyStrategy {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new SingleEventCopyStrategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   * @throws IllegalArgumentException if either calendarManager or timezoneHandler is null
   */
  public SingleEventCopyStrategy(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
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
  public String execute(String[] args)
      throws CalendarNotFoundException, EventNotFoundException, ConflictingEventException
      , InvalidEventException {
    if (args.length < 9) {
      throw new InvalidEventException("Insufficient arguments for copy event command");
    }

    if (!args[0].equals("copy") || !args[1].equals("event")) {
      throw new InvalidEventException("Invalid command format");
    }

    String eventName = args[2];
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    if (!args[3].equals("on")) {
      throw new InvalidEventException("Expected 'on' keyword after event name");
    }

    String sourceDateTime = args[4];

    if (!args[5].equals("--target")) {
      throw new InvalidEventException("Expected '--target' flag");
    }

    String targetCalendar = args[6];

    if (!args[7].equals("to")) {
      throw new InvalidEventException("Expected 'to' keyword");
    }

    String targetDateTime = args[8];

    try {
      return copyEvent(eventName, sourceDateTime, targetCalendar, targetDateTime);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  @Override
  public boolean canHandle(String[] args) {
    if (args.length < 2) {
      return false;
    }

    return (args[0].equals("copy") && args[1].equals("event"));
  }

  /**
   * Copies a single event from the active calendar to a target calendar.
   */
  private String copyEvent(String eventName, String dateTimeStr, String targetCalendarName,
      String targetDateTimeStr) throws Exception {
    LocalDateTime sourceDateTime = DateTimeUtil.parseDateTime(dateTimeStr);
    LocalDateTime targetDateTime = DateTimeUtil.parseDateTime(targetDateTimeStr);

    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException(
          "Target calendar '" + targetCalendarName + "' does not " + "exist");
    }

    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    Event sourceEvent = sourceCalendar.findEvent(eventName, sourceDateTime);
    if (sourceEvent == null) {
      throw new EventNotFoundException("Event not found: " + eventName + " at " + sourceDateTime);
    }

    String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
    String targetTimezone = calendarManager.executeOnCalendar(targetCalendarName,
        calendar -> ((model.calendar.Calendar) calendar).getTimezone());

    TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

    long durationSeconds = sourceEvent.getEndDateTime().toEpochSecond(java.time.ZoneOffset.UTC)
        - sourceEvent.getStartDateTime().toEpochSecond(java.time.ZoneOffset.UTC);

    Event newEvent = new Event(sourceEvent.getSubject(), converter.convert(targetDateTime),
        converter.convert(targetDateTime.plusSeconds(durationSeconds)),
        sourceEvent.getDescription(), sourceEvent.getLocation(), sourceEvent.isPublic());

    boolean success = calendarManager.executeOnCalendar(targetCalendarName,
        calendar -> calendar.addEvent(newEvent, true));

    if (success) {
      return "Event '" + eventName + "' copied successfully to calendar '" + targetCalendarName
          + "'.";
    } else {
      return "Failed to copy event due to conflicts.";
    }
  }
}