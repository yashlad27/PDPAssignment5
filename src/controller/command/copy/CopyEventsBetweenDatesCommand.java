package controller.command.copy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;
import utilities.TimezoneConverter;

public class CopyEventsBetweenDatesCommand implements CopyCommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  public CopyEventsBetweenDatesCommand(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
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
    return args.length >= 9 &&
            "events".equals(args[0]) &&
            "between".equals(args[1]) &&
            "and".equals(args[3]) &&
            "--target".equals(args[5]) &&
            "to".equals(args[7]);
  }

  @Override
  public String execute(String[] args) throws Exception {
    if (args.length < 9) {
      return "Error: Insufficient arguments for copy events between dates command";
    }

    String startDateStr = args[2];
    String endDateStr = args[4];
    String targetCalName = args[6];
    String targetStartDateStr = args[8];

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalName)) {
      return "Error: Target calendar '" + targetCalName + "' does not exist";
    }

    try {
      // Parse dates
      LocalDate startDate = DateTimeUtil.parseDate(startDateStr);
      LocalDate endDate = DateTimeUtil.parseDate(endDateStr);
      LocalDate targetStartDate = DateTimeUtil.parseDate(targetStartDateStr);

      // Validate date range
      if (endDate.isBefore(startDate)) {
        return "Error: End date cannot be before start date";
      }

      // Get source calendar (active calendar)
      ICalendar sourceCalendar = calendarManager.getActiveCalendar();
      String sourceCalendarName = calendarManager.getActiveCalendarName();

      // Get events in the date range
      List<Event> eventsInRange = sourceCalendar.getEventsInRange(startDate, endDate);

      if (eventsInRange.isEmpty()) {
        return "No events found between " + startDate + " and " + endDate;
      }

      // Get the source and target timezones
      String sourceTimezone = ((model.calendar.Calendar) sourceCalendar).getTimezone();
      String targetTimezone = calendarManager.executeOnCalendar(targetCalName,
              calendar -> ((model.calendar.Calendar) calendar).getTimezone());

      // Create timezone converter
      TimezoneConverter converter = timezoneHandler.getConverter(sourceTimezone, targetTimezone);

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

            // Calculate the adjusted date relative to the source and target start dates
            long daysFromStart = ChronoUnit.DAYS.between(startDate, eventDate);
            LocalDate adjustedDate = targetStartDate.plusDays(daysFromStart);

            newEvent = Event.createAllDayEvent(
                    sourceEvent.getSubject(),
                    adjustedDate,
                    sourceEvent.getDescription(),
                    sourceEvent.getLocation(),
                    sourceEvent.isPublic()
            );
          } else {
            // Regular event

            // Calculate days from source start date
            long daysFromStart = ChronoUnit.DAYS.between(
                    startDate,
                    sourceEvent.getStartDateTime().toLocalDate());

            // Apply the same offset from the target start date
            java.time.LocalDateTime adjustedStart = targetStartDate.atTime(
                    sourceEvent.getStartDateTime().toLocalTime()).plusDays(daysFromStart);

            // Calculate the duration of the original event
            long durationSeconds = ChronoUnit.SECONDS.between(
                    sourceEvent.getStartDateTime(),
                    sourceEvent.getEndDateTime());

            java.time.LocalDateTime adjustedEnd = adjustedStart.plusSeconds(durationSeconds);

            // Convert to target timezone context
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
        return "Successfully copied " + successCount + " events from date range " + startDate +
                " to " + endDate + " in calendar '" + sourceCalendarName +
                "' to date range starting at " + targetStartDate +
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