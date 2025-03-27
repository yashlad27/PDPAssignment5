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
 * Strategy for copying events within a date range from one calendar to another. Format: copy events
 * between {@code dateString} and {@code dateString} --target {@code calendarName} to
 * {@code dateString}
 */
public class RangeEventsCopyStrategy implements CopyStrategy {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new RangeEventsCopyStrategy.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   */
  public RangeEventsCopyStrategy(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    this.calendarManager = calendarManager;
    this.timezoneHandler = timezoneHandler;
  }

  @Override
  public String execute(String[] args) throws CalendarNotFoundException, InvalidEventException {
    if (args.length < 10) {
      throw new InvalidEventException(
          "Insufficient arguments for copy events between " + "dates command");
    }

    if (!args[0].equals("copy") || !args[1].equals("events") || !args[2].equals("between")) {
      throw new InvalidEventException("Invalid command format");
    }

    String sourceStartDate = args[3];

    if (!args[4].equals("and")) {
      throw new InvalidEventException("Expected 'and' keyword");
    }

    String sourceEndDate = args[5];

    if (!args[6].equals("--target")) {
      throw new InvalidEventException("Expected '--target' flag");
    }

    String targetCalendar = args[7];

    if (!args[8].equals("to")) {
      throw new InvalidEventException("Expected 'to' keyword");
    }

    String targetStartDate = args[9];

    try {
      return copyEventsBetweenDates(sourceStartDate, sourceEndDate, targetCalendar,
          targetStartDate);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  @Override
  public boolean canHandle(String[] args) {
    if (args.length < 3) {
      return false;
    }

    return (args[0].equals("copy") && args[1].equals("events") && args[2].equals("between"));
  }

  /**
   * Copies events within a date range from the active calendar to a target calendar.
   */
  private String copyEventsBetweenDates(String startDateStr, String endDateStr,
      String targetCalendarName, String targetStartDateStr) throws Exception {
    LocalDate sourceStartDate = DateTimeUtil.parseDate(startDateStr);
    LocalDate sourceEndDate = DateTimeUtil.parseDate(endDateStr);
    LocalDate targetStartDate = DateTimeUtil.parseDate(targetStartDateStr);

    if (!calendarManager.hasCalendar(targetCalendarName)) {
      throw new CalendarNotFoundException(
          "Target calendar '" + targetCalendarName + "' does not exist");
    }

    ICalendar sourceCalendar = calendarManager.getActiveCalendar();
    List<Event> eventsInRange = sourceCalendar.getEventsInRange(sourceStartDate, sourceEndDate);

    if (eventsInRange.isEmpty()) {
      return "No events found between " + sourceStartDate + " and " + sourceEndDate + " to copy.";
    }

    String sourceTimezone = ((Calendar) sourceCalendar).getTimezone();
    String targetTimezone = calendarManager.executeOnCalendar(targetCalendarName,
        calendar -> ((model.calendar.Calendar) calendar).getTimezone());

    TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

    long daysDifference = targetStartDate.toEpochDay() - sourceStartDate.toEpochDay();

    int successCount = 0;
    int failCount = 0;

    for (Event sourceEvent : eventsInRange) {
      try {
        Event newEvent;

        if (sourceEvent.isAllDay()) {
          LocalDate eventDate = sourceEvent.getDate();
          if (eventDate == null) {
            eventDate = sourceEvent.getStartDateTime().toLocalDate();
          }

          LocalDate adjustedDate = eventDate.plusDays(daysDifference);

          newEvent = Event.createAllDayEvent(sourceEvent.getSubject(), adjustedDate,
              sourceEvent.getDescription(), sourceEvent.getLocation(), sourceEvent.isPublic());
        } else {
          LocalDateTime adjustedStart = sourceEvent.getStartDateTime().plusDays(daysDifference);
          LocalDateTime adjustedEnd = sourceEvent.getEndDateTime().plusDays(daysDifference);

          newEvent = new Event(sourceEvent.getSubject(), converter.convert(adjustedStart),
              converter.convert(adjustedEnd), sourceEvent.getDescription(),
              sourceEvent.getLocation(), sourceEvent.isPublic());
        }

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
      return "Successfully copied " + successCount + " events from date range " + sourceStartDate
          + " to " + sourceEndDate + " in calendar '" + targetCalendarName + "'.";
    } else {
      return "Copied " + successCount + " events, but " + failCount
          + " events could not be copied due to conflicts.";
    }
  }
}