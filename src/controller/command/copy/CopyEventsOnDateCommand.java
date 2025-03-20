package controller.command.copy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;
import utilities.TimezoneConverter;

import java.util.List;

public class CopyEventsOnDateCommand implements CopyCommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  public CopyEventsOnDateCommand(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
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
    return args.length >= 7 &&
            "events".equals(args[0]) &&
            "on".equals(args[1]) &&
            "--target".equals(args[3]) &&
            "to".equals(args[5]);
  }

  @Override
  public String execute(String[] args) throws Exception {
    if (args.length < 7) {
      return "Error: Insufficient arguments for copy events on date command";
    }

    String sourceDateStr = args[2];
    String targetCalName = args[4];
    String targetDateStr = args[6];

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalName)) {
      return "Error: Target calendar '" + targetCalName + "' does not exist";
    }

    try {
      // Parse dates
      LocalDate sourceDate = DateTimeUtil.parseDate(sourceDateStr);
      LocalDate targetDate = DateTimeUtil.parseDate(targetDateStr);

      // Get source calendar (active calendar)
      ICalendar sourceCalendar = calendarManager.getActiveCalendar();
      String sourceCalendarName = calendarManager.getActiveCalendarName();

      // Get events on the source date
      List<Event> eventsOnDate = sourceCalendar.getEventsOnDate(sourceDate);

      if (eventsOnDate.isEmpty()) {
        return "No events found on date " + sourceDate;
      }

      // Get the source and target timezones
      String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
      String targetTimezone = calendarManager.executeOnCalendar(targetCalName,
              calendar -> ((model.calendar.Calendar) calendar).getTimezone());

      // Create timezone converter
      TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

      // Calculate date difference in days
      long daysDifference = ChronoUnit.DAYS.between(sourceDate, targetDate);

      int successCount = 0;
      int failCount = 0;

      // Copy each event
      for (Event sourceEvent : eventsOnDate) {
        try {
          // Create a new event with the adjusted date
          Event newEvent;

          if (sourceEvent.isAllDay()) {
            // All-day event - just use the target date
            newEvent = Event.createAllDayEvent(
                    sourceEvent.getSubject(),
                    targetDate,
                    sourceEvent.getDescription(),
                    sourceEvent.getLocation(),
                    sourceEvent.isPublic()
            );
          } else {
            // Regular event - adjust date and convert timezone
            java.time.LocalDateTime adjustedStart = sourceEvent.getStartDateTime().plusDays(daysDifference);
            java.time.LocalDateTime adjustedEnd = sourceEvent.getEndDateTime().plusDays(daysDifference);

            // Convert to target timezone
            java.time.LocalDateTime convertedStart = converter.convert(adjustedStart);
            java.time.LocalDateTime convertedEnd = converter.convert(adjustedEnd);

            newEvent = new Event(
                    sourceEvent.getSubject(),
                    convertedStart,
                    convertedEnd,
                    sourceEvent.getDescription(),
                    sourceEvent.getLocation(),
                    sourceEvent.isPublic()
            );
          }

          // Add the event to the target calendar
          final Event eventToAdd = newEvent;
          boolean added = calendarManager.executeOnCalendar(targetCalName,
                  calendar -> {
                    try {
                      return calendar.addEvent(eventToAdd, true);
                    } catch (ConflictingEventException e) {
                      return false;
                    }
                  });

          if (added) {
            successCount++;
          } else {
            failCount++;
          }
        } catch (Exception e) {
          failCount++;
        }
      }

      if (failCount == 0) {
        return "Successfully copied " + successCount + " events from " + sourceDate +
                " in calendar '" + sourceCalendarName + "' to " + targetDate +
                " in calendar '" + targetCalName + "'.";
      } else {
        return "Copied " + successCount + " events, but " + failCount +
                " events could not be copied due to conflicts.";
      }

    } catch (Exception e) {
      throw new Exception("Failed to copy events: " + e.getMessage(), e);
    }
  }
}