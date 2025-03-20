package controller.command.copy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.EventNotFoundException;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;

public class CopyEventCommand implements CopyCommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

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
  public boolean canHandle(String[] args) {
    return args.length >= 8 &&
            "event".equals(args[0]) &&
            "on".equals(args[2]) &&
            "--target".equals(args[4]) &&
            "to".equals(args[6]);
  }


  @Override
  public String execute(String[] args) throws Exception {
    if (args.length < 8) {
      return "Error: Insufficient arguments for copy event command";
    }

    String eventName = args[1];
    String dateTimeStr = args[3];
    String targetCalName = args[5];
    String targetDateTimeStr = args[7];

    // Remove quotes from event name if present
    if (eventName.startsWith("\"") && eventName.endsWith("\"")) {
      eventName = eventName.substring(1, eventName.length() - 1);
    }

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalName)) {
      return "Error: Target calendar '" + targetCalName + "' does not exist";
    }

    try {
      LocalDateTime sourceDateTime = DateTimeUtil.parseDateTime(dateTimeStr);
      LocalDateTime targetDateTime = DateTimeUtil.parseDateTime(targetDateTimeStr);

      // Get source calendar (active calendar)
      ICalendar sourceCalendar = calendarManager.getActiveCalendar();
      String sourceCalendarName = calendarManager.getActiveCalendarName();

      // Find the event
      Event sourceEvent = sourceCalendar.findEvent(eventName, sourceDateTime);
      if (sourceEvent == null) {
        throw new EventNotFoundException("Event not found: " + eventName + " at " + sourceDateTime);
      }

      // Calculate duration of the source event in seconds
      long durationSeconds = ChronoUnit.SECONDS.between(
              sourceEvent.getStartDateTime(),
              sourceEvent.getEndDateTime());

      // Create a new event with the provided target time
      Event newEvent = new Event(
              sourceEvent.getSubject(),
              targetDateTime,
              targetDateTime.plusSeconds(durationSeconds),
              sourceEvent.getDescription(),
              sourceEvent.getLocation(),
              sourceEvent.isPublic()
      );

      if (sourceEvent.isAllDay()) {
        newEvent.setAllDay(true);
      }

      // Add the event to the target calendar
      final Event eventToAdd = newEvent;
      boolean added = calendarManager.executeOnCalendar(targetCalName,
              calendar -> {
                try {
                  return calendar.addEvent(eventToAdd, true);
                } catch (Exception e) {
                  return false;
                }
              });

      if (added) {
        return "Event '" + eventName + "' copied successfully from calendar '" +
                sourceCalendarName + "' to calendar '" + targetCalName +
                "' at " + DateTimeUtil.formatDateTime(targetDateTime) + ".";
      } else {
        return "Failed to copy event due to conflicts.";
      }

    } catch (EventNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new Exception("Failed to copy event: " + e.getMessage(), e);
    }
  }
}