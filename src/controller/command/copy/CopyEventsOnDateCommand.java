package controller.command.copy;

import java.time.LocalDate;

import model.calendar.CalendarManager;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;

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

      // Create and execute copy command
      controller.command.CopyEventCommand copyCommand =
              new controller.command.CopyEventCommand(calendarManager, timezoneHandler);

      return copyCommand.execute(new String[]{
              "events_on_date", sourceDateStr, targetCalName, targetDateStr, "true"
      });

    } catch (Exception e) {
      throw new Exception("Failed to copy events: " + e.getMessage(), e);
    }
  }
}