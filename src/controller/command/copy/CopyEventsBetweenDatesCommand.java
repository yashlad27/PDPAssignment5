// Create this class in a new file
package controller.command.copy;

import java.time.LocalDate;
import model.calendar.CalendarManager;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;

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
    return args.length >= 8 &&
            "events".equals(args[0]) &&
            "between".equals(args[1]) &&
            "and".equals(args[3]) &&
            "--target".equals(args[5]);
  }

  @Override
  public String execute(String[] args) throws Exception {
    String startDateStr = args[2];
    String endDateStr = args[4];
    String targetCalName = args[6];
    String targetStartDateStr = args[7];

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalName)) {
      return "Error: Target calendar '" + targetCalName + "' does not exist";
    }

    try {
      // Parse dates
      LocalDate startDate = DateTimeUtil.parseDate(startDateStr);
      LocalDate endDate = DateTimeUtil.parseDate(endDateStr);
      LocalDate targetStartDate = DateTimeUtil.parseDate(targetStartDateStr);

      // Create and execute copy command
      controller.command.CopyEventCommand copyCommand =
              new controller.command.CopyEventCommand(calendarManager, timezoneHandler);

      return copyCommand.execute(new String[] {
              "events_between_dates", startDateStr, endDateStr, targetCalName, targetStartDateStr, "true"
      });

    } catch (Exception e) {
      throw new Exception("Failed to copy events: " + e.getMessage(), e);
    }
  }
}