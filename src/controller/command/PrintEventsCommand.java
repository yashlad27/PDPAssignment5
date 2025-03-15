package controller.command;

import model.calendar.ICalendar;
import model.event.Event;
import utilities.CSVExporter;
import utilities.DateTimeUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Command for printing events on a specific date or within a date range.
 */
public class PrintEventsCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Creates a PrintEventsCommand with the given calendar.
   *
   * @param calendar the calendar to query
   */
  public PrintEventsCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  /**
   * Executes the command to print events based on arguments provided.
   *
   * @param args the command arguments.
   * @return a string containing the list of events or error message.
   */
  @Override
  public String execute(String[] args) {
    if (args.length < 2) {
      return "Error: Insufficient arguments for print command";
    }

    String type = args[0];

    if (type.equals("on_date")) {
      if (args.length < 2) {
        return "Error: Missing date for 'print events on' command";
      }

      LocalDate date;
      try {
        date = DateTimeUtil.parseDate(args[1]);
      } catch (Exception e) {
        return "Error parsing date: " + e.getMessage();
      }

      return printEventsOnDate(date);

    } else if (type.equals("date_range") || type.equals("from_range")) {
      if (args.length < 3) {
        return "Error: Missing dates for 'print events from...to' command";
      }

      LocalDate startDate;
      LocalDate endDate;

      try {
        startDate = DateTimeUtil.parseDate(args[1]);
        endDate = DateTimeUtil.parseDate(args[2]);
      } catch (Exception e) {
        return "Error parsing dates: " + e.getMessage();
      }

      return printEventsInRange(startDate, endDate);

    } else {
      return "Unknown print command type: " + type;
    }
  }

  private String printEventsOnDate(LocalDate date) {
    List<Event> events = calendar.getEventsOnDate(date);

    if (events.isEmpty()) {
      return "No events on " + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    StringBuilder result = new StringBuilder();
    result.append("Events on ").append(date.format(DateTimeFormatter.ISO_LOCAL_DATE)).append(":\n");
    result.append(CSVExporter.formatEventsForDisplay(events, true));

    return result.toString();
  }

  private String printEventsInRange(LocalDate startDate, LocalDate endDate) {
    List<Event> events = calendar.getEventsInRange(startDate, endDate);

    if (events.isEmpty()) {
      return "No events from " + startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " to "
          + endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    StringBuilder result = new StringBuilder();
    result.append("Events from ").append(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        .append(" to ").append(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).append(":\n");
    result.append(CSVExporter.formatEventsForDisplay(events, false));

    return result.toString();
  }

  /**
   * fetches the name of the command.
   *
   * @return the name of command as String.
   */
  @Override
  public String getName() {
    return "print";
  }
}