package controller.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Command for diagnosing issues with event finding and copying.
 */
public class DiagnosticCommand implements ICommand {

  private final CalendarManager calendarManager;

  /**
   * Constructs a new DiagnosticCommand.
   *
   * @param calendarManager the calendar manager
   */
  public DiagnosticCommand(CalendarManager calendarManager) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }
    this.calendarManager = calendarManager;
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    if (args.length < 1) {
      return "Error: Insufficient arguments for diagnostic command";
    }

    String subCommand = args[0];

    try {
      switch (subCommand) {
        case "list_all":
          return listAllEvents();
        case "find_event":
          if (args.length < 3) {
            return "Error: Insufficient arguments for find_event command";
          }
          return findEvent(args[1], args[2]);
        case "list_calendars":
          return listCalendars();
        default:
          return "Error: Unknown diagnostic command: " + subCommand;
      }
    } catch (CalendarNotFoundException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Lists all events in the active calendar with full details.
   */
  private String listAllEvents() throws CalendarNotFoundException {
    ICalendar activeCalendar = calendarManager.getActiveCalendar();
    List<Event> events = activeCalendar.getAllEvents();

    if (events.isEmpty()) {
      return "No events found in the active calendar.";
    }

    StringBuilder result = new StringBuilder();
    result.append("Events in active calendar (").append(calendarManager.getActiveCalendarName()).append("):\n");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    for (Event event : events) {
      result.append("- Subject: '").append(event.getSubject()).append("'\n");
      result.append("  ID: ").append(event.getId()).append("\n");
      result.append("  Start: ").append(event.getStartDateTime().format(formatter)).append("\n");
      result.append("  End: ").append(event.getEndDateTime().format(formatter)).append("\n");
      result.append("  Description: ").append(event.getDescription() != null ? "'" + event.getDescription() + "'" : "null").append("\n");
      result.append("  Location: ").append(event.getLocation() != null ? "'" + event.getLocation() + "'" : "null").append("\n");
      result.append("  Is Public: ").append(event.isPublic()).append("\n");
      result.append("  Is All Day: ").append(event.isAllDay()).append("\n");
      result.append("\n");
    }

    return result.toString();
  }

  /**
   * Tries to find an event by name and date-time.
   */
  private String findEvent(String eventName, String dateTimeStr) throws CalendarNotFoundException {
    ICalendar activeCalendar = calendarManager.getActiveCalendar();

    try {
      LocalDateTime dateTime = DateTimeUtil.parseDateTime(dateTimeStr);

      StringBuilder result = new StringBuilder();
      result.append("Searching for event:\n");
      result.append("- Name: '").append(eventName).append("'\n");
      result.append("- DateTime: ").append(dateTime).append("\n\n");

      Event event = activeCalendar.findEvent(eventName, dateTime);

      if (event == null) {
        result.append("No matching event found.\n\n");

        // List all events to help diagnose
        result.append("Available events in calendar:\n");
        List<Event> allEvents = activeCalendar.getAllEvents();

        if (allEvents.isEmpty()) {
          result.append("No events in this calendar.\n");
        } else {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
          for (Event e : allEvents) {
            result.append("- '").append(e.getSubject()).append("' at ")
                    .append(e.getStartDateTime().format(formatter)).append("\n");
          }
        }

        return result.toString();
      }

      result.append("Event found!\n");
      result.append("- Subject: '").append(event.getSubject()).append("'\n");
      result.append("- ID: ").append(event.getId()).append("\n");
      result.append("- Start: ").append(event.getStartDateTime()).append("\n");
      result.append("- End: ").append(event.getEndDateTime()).append("\n");

      return result.toString();

    } catch (Exception e) {
      return "Error parsing date/time: " + e.getMessage();
    }
  }

  /**
   * Lists all available calendars.
   */
  private String listCalendars() {
    Set<String> calendarNames = calendarManager.getCalendarNames();

    if (calendarNames.isEmpty()) {
      return "No calendars found.";
    }

    StringBuilder result = new StringBuilder();
    result.append("Available calendars:\n");

    String activeCalendarName = calendarManager.getActiveCalendarName();

    for (String name : calendarNames) {
      result.append("- ").append(name);
      if (name.equals(activeCalendarName)) {
        result.append(" (active)");
      }
      result.append("\n");
    }

    return result.toString();
  }

  @Override
  public String getName() {
    return "diagnose";
  }
}