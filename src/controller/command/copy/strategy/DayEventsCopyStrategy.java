package controller.command.copy.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;
import utilities.TimezoneConverter;

/**
 * Strategy for copying all events on a specific date from one calendar to another. Format: copy
 * events on {@code dateString} --target {@code calendarName} to {@code dateString}
 */
public class DayEventsCopyStrategy implements CopyStrategy {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new DayEventsCopyStrategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   */
  public DayEventsCopyStrategy(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    this.calendarManager = calendarManager;
    this.timezoneHandler = timezoneHandler;
  }

  @Override
  public String execute(String[] args) throws CalendarNotFoundException, InvalidEventException {
    // Validate format: copy events on <dateString> --target <calendarName> to <dateString>
    if (args.length < 8) {
      throw new InvalidEventException("Insufficient arguments for copy events on date command");
    }

    if (!args[0].equals("copy") || !args[1].equals("events") || !args[2].equals("on")) {
      throw new InvalidEventException("Invalid command format");
    }

    String sourceDate = args[3];

    if (!args[4].equals("--target")) {
      throw new InvalidEventException("Expected '--target' flag");
    }

    String targetCalendar = args[5];

    if (!args[6].equals("to")) {
      throw new InvalidEventException("Expected 'to' keyword");
    }

    String targetDate = args[7];

    try {
      return copyEventsOnDate(sourceDate, targetCalendar, targetDate);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  @Override
  public boolean canHandle(String[] args) {
    if (args.length < 3) {
      return false;
    }

    return (args[0].equals("copy") && args[1].equals("events") && args[2].equals("on"));
  }

  /**
   * Copies all events on a specific date from the active calendar to a target calendar.
   */
  private String copyEventsOnDate(String dateStr, String targetCalendarName, String targetDateStr)
      throws Exception {
    // Parse the dates
    LocalDate sourceDate = DateTimeUtil.parseDate(dateStr);
    LocalDate targetDate = DateTimeUtil.parseDate(targetDateStr);

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException(
          "Target calendar '" + targetCalendarName + "' does not exist");
    }

    // Get source calendar (active calendar)
    ICalendar sourceCalendar = calendarManager.getActiveCalendar();

    // Get events on the source date
    List<Event> eventsOnDate = sourceCalendar.getEventsOnDate(sourceDate);

    if (eventsOnDate.isEmpty()) {
      return "No events found on " + sourceDate + " to copy.";
    }

    // Get the source and target timezones
    String sourceTimezone = ((Calendar) sourceCalendar).getTimezone();
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
          newEvent = Event.createAllDayEvent(sourceEvent.getSubject(), targetDate,
              sourceEvent.getDescription(), sourceEvent.getLocation(), sourceEvent.isPublic());
        } else {
          // Regular event - adjust date and convert timezone
          LocalDateTime adjustedStart = sourceEvent.getStartDateTime().plusDays(daysDifference);
          LocalDateTime adjustedEnd = sourceEvent.getEndDateTime().plusDays(daysDifference);

          newEvent = new Event(sourceEvent.getSubject(), converter.convert(adjustedStart),
              converter.convert(adjustedEnd), sourceEvent.getDescription(),
              sourceEvent.getLocation(), sourceEvent.isPublic());
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
      return "Successfully copied " + successCount + " events from " + sourceDate + " to "
          + targetDate + " in calendar '" + targetCalendarName + "'.";
    } else {
      return "Copied " + successCount + " events, but " + failCount
          + " events could not be copied due to conflicts.";
    }
  }
}