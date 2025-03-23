package controller.command.copy.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
 * Strategy for copying events within a date range from one calendar to another.
 * Format: copy events between <dateString> and <dateString> --target <calendarName> to <dateString>
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
    // Validate format: copy events between <dateString> and <dateString> --target <calendarName> to <dateString>
    if (args.length < 10) {
      throw new InvalidEventException("Insufficient arguments for copy events between dates command");
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
      return copyEventsBetweenDates(sourceStartDate, sourceEndDate, targetCalendar, targetStartDate);
    } catch (Exception e) {
      throw new InvalidEventException("Error copying events: " + e.getMessage());
    }
  }

  @Override
  public boolean canHandle(String[] args) {
    if (args.length < 3) return false;

    return (args[0].equals("copy") && args[1].equals("events") && args[2].equals("between"));
  }

  /**
   * Copies events within a date range from the active calendar to a target calendar.
   */
  private String copyEventsBetweenDates(String startDateStr, String endDateStr, String targetCalendarName,
                                        String targetStartDateStr) throws Exception {
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
}