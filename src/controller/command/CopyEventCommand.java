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
 * Consolidated implementation that handles all copying operations with a strategy pattern.
 */
public class CopyEventCommand implements ICommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;
  private final EventCopyService copyService;

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
    this.copyService = new EventCopyService(calendarManager, timezoneHandler);
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    if (args.length < 1) {
      return "Error: Insufficient arguments for copy command";
    }

    String subCommand = args[0];

    try {
      // Choose the appropriate copy strategy based on the subcommand
      CopyStrategy strategy = getCopyStrategy(subCommand);

      if (strategy == null) {
        return "Error: Unknown copy command: " + subCommand;
      }

      return strategy.execute(args);
    } catch (CalendarNotFoundException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Returns the appropriate copy strategy based on the subcommand.
   */
  private CopyStrategy getCopyStrategy(String subCommand) {
    switch (subCommand) {
      case "event":
        return new SingleEventCopyStrategy(copyService);
      case "events_on_date":
        return new EventsOnDateCopyStrategy(copyService);
      case "events_between_dates":
        return new EventsBetweenDatesCopyStrategy(copyService);
      default:
        return null;
    }
  }

  @Override
  public String getName() {
    return "copy";
  }

  /**
   * Interface defining the contract for copy strategies.
   */
  private interface CopyStrategy {
    String execute(String[] args) throws Exception;
  }

  /**
   * Strategy for copying a single event.
   */
  private static class SingleEventCopyStrategy implements CopyStrategy {
    private final EventCopyService copyService;

    public SingleEventCopyStrategy(EventCopyService copyService) {
      this.copyService = copyService;
    }

    @Override
    public String execute(String[] args) throws Exception {
      if (args.length < 6) {
        return "Error: Insufficient arguments for copy event command";
      }

      String eventName = args[1];
      LocalDateTime sourceDateTime = DateTimeUtil.parseDateTime(args[2]);
      String targetCalendarName = args[3];
      LocalDateTime targetDateTime = DateTimeUtil.parseDateTime(args[4]);
      boolean autoDecline = args.length > 5 ? Boolean.parseBoolean(args[5]) : true;

      try {
        boolean success = copyService.copyEvent(
                eventName, sourceDateTime, targetCalendarName, targetDateTime, autoDecline);

        if (success) {
          return "Event '" + eventName + "' copied successfully to calendar '" + targetCalendarName + "'.";
        } else {
          return "Failed to copy event due to conflicts.";
        }
      } catch (EventNotFoundException e) {
        return "Error: Event not found: " + e.getMessage();
      } catch (CalendarNotFoundException e) {
        return "Error: Calendar not found: " + e.getMessage();
      } catch (ConflictingEventException e) {
        return "Error: Event conflicts with existing event: " + e.getMessage();
      }
    }
  }

  /**
   * Strategy for copying events on a specific date.
   */
  private static class EventsOnDateCopyStrategy implements CopyStrategy {
    private final EventCopyService copyService;

    public EventsOnDateCopyStrategy(EventCopyService copyService) {
      this.copyService = copyService;
    }

    @Override
    public String execute(String[] args) throws Exception {
      if (args.length < 4) {
        return "Error: Insufficient arguments for copy events on date command";
      }

      LocalDate sourceDate = DateTimeUtil.parseDate(args[1]);
      String targetCalendarName = args[2];
      LocalDate targetDate = DateTimeUtil.parseDate(args[3]);
      boolean autoDecline = args.length > 4 ? Boolean.parseBoolean(args[4]) : true;

      try {
        CopyResult result = copyService.copyEventsOnDate(
                sourceDate, targetCalendarName, targetDate, autoDecline);

        if (result.getFailCount() == 0) {
          return "Successfully copied " + result.getSuccessCount() + " events from " + sourceDate +
                  " to " + targetDate + " in calendar '" + targetCalendarName + "'.";
        } else {
          return "Copied " + result.getSuccessCount() + " events, but " + result.getFailCount() +
                  " events could not be copied due to conflicts.";
        }
      } catch (CalendarNotFoundException e) {
        return "Error: Calendar not found: " + e.getMessage();
      }
    }
  }

  /**
   * Strategy for copying events between dates.
   */
  private static class EventsBetweenDatesCopyStrategy implements CopyStrategy {
    private final EventCopyService copyService;

    public EventsBetweenDatesCopyStrategy(EventCopyService copyService) {
      this.copyService = copyService;
    }

    @Override
    public String execute(String[] args) throws Exception {
      if (args.length < 5) {
        return "Error: Insufficient arguments for copy events between dates command";
      }

      LocalDate sourceStartDate = DateTimeUtil.parseDate(args[1]);
      LocalDate sourceEndDate = DateTimeUtil.parseDate(args[2]);
      String targetCalendarName = args[3];
      LocalDate targetStartDate = DateTimeUtil.parseDate(args[4]);
      boolean autoDecline = args.length > 5 ? Boolean.parseBoolean(args[5]) : true;

      try {
        CopyResult result = copyService.copyEventsBetweenDates(
                sourceStartDate, sourceEndDate, targetCalendarName, targetStartDate, autoDecline);

        if (result.getFailCount() == 0) {
          return "Successfully copied " + result.getSuccessCount() + " events from date range " +
                  sourceStartDate + " to " + sourceEndDate + " in calendar '" + targetCalendarName + "'.";
        } else {
          return "Copied " + result.getSuccessCount() + " events, but " + result.getFailCount() +
                  " events could not be copied due to conflicts.";
        }
      } catch (CalendarNotFoundException e) {
        return "Error: Calendar not found: " + e.getMessage();
      }
    }
  }

  /**
   * Service class for copying events, extracted to follow SRP.
   */
  private static class EventCopyService {
    private final CalendarManager calendarManager;
    private final TimeZoneHandler timezoneHandler;

    public EventCopyService(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
      this.calendarManager = calendarManager;
      this.timezoneHandler = timezoneHandler;
    }

    /**
     * Copies a single event from the active calendar to a target calendar.
     */
    public boolean copyEvent(String eventName, LocalDateTime sourceDateTime,
                             String targetCalendarName, LocalDateTime targetDateTime,
                             boolean autoDecline)
            throws Exception {

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
      return calendarManager.executeOnCalendar(targetCalendarName,
              calendar -> calendar.addEvent(newEvent, autoDecline));
    }

    /**
     * Copies all events on a specific date from the active calendar to a target calendar.
     */
    public CopyResult copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                                       LocalDate targetDate, boolean autoDecline)
            throws Exception {

      // Validate target calendar exists
      if (!calendarManager.hasCalendar(targetCalendarName)) {
        throw new CalendarNotFoundException("Target calendar '" + targetCalendarName + "' does not exist");
      }

      // Get source calendar (active calendar)
      ICalendar sourceCalendar = calendarManager.getActiveCalendar();

      // Get events on the source date
      List<Event> eventsOnDate = sourceCalendar.getEventsOnDate(sourceDate);

      if (eventsOnDate.isEmpty()) {
        return new CopyResult(0, 0);
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

      return new CopyResult(successCount, failCount);
    }

    /**
     * Copies events within a date range from the active calendar to a target calendar.
     */
    public CopyResult copyEventsBetweenDates(LocalDate sourceStartDate, LocalDate sourceEndDate,
                                             String targetCalendarName, LocalDate targetStartDate,
                                             boolean autoDecline)
            throws Exception {

      // Validate target calendar exists
      if (!calendarManager.hasCalendar(targetCalendarName)) {
        throw new CalendarNotFoundException("Target calendar '" + targetCalendarName + "' does not exist");
      }

      // Get source calendar (active calendar)
      ICalendar sourceCalendar = calendarManager.getActiveCalendar();

      // Get events in the date range
      List<Event> eventsInRange = sourceCalendar.getEventsInRange(sourceStartDate, sourceEndDate);

      if (eventsInRange.isEmpty()) {
        return new CopyResult(0, 0);
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
        } catch (Exception e) {
          throw new RuntimeException("Error copying event: " + e.getMessage(), e);
        }
      }

      return new CopyResult(successCount, failCount);
    }
  }

  /**
   * DTO class to hold the result of a copy operation.
   */
  private static class CopyResult {
    private final int successCount;
    private final int failCount;

    public CopyResult(int successCount, int failCount) {
      this.successCount = successCount;
      this.failCount = failCount;
    }

    public int getSuccessCount() {
      return successCount;
    }

    public int getFailCount() {
      return failCount;
    }
  }

  /**
   * Factory method to create an instance that can parse the provided CLI arguments.
   * This is useful for command-line parsing integration.
   */
  public static CopyEventCommand fromArgs(String[] args, CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    return new CopyEventCommand(calendarManager, timezoneHandler);
  }

  /**
   * Determines if this command can handle the given command-line arguments.
   * Useful for command routing in a CLI application.
   */
  public static boolean canHandle(String[] args) {
    if (args.length < 1) return false;

    String command = args[0];
    return command.equals("copy") || command.equals("event") ||
            command.equals("events_on_date") || command.equals("events_between_dates");
  }
}