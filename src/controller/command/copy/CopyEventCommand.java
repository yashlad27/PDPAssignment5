package controller.command.copy;

import java.time.LocalDateTime;

import model.calendar.CalendarManager;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;

public class CopyEventCommand implements CopyCommand {

  private final CalendarManager calendarManager;
  private final TimeZoneHandler timezoneHandler;

  public CopyEventCommand(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
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
    return args.length >= 8 && "event".equals(args[0]) &&
            "on".equals(args[2]) &&
            "--target".equals(args[4]) &&
            "to".equals(args[6]);
  }


  @Override
  public String execute(String[] args) throws Exception {
    if (args.length < 8) {
      return "Error: Insufficient arguments for copy event command";
    }
    String eventName = args[1];
    String dateTimeStr = args[3];
    String targetCalName = args[5];
    String targetDateTimeStr = args[7];

    // Validate target calendar exists
    if (!calendarManager.hasCalendar(targetCalName)) {
      return "Error: Target calendar '" + targetCalName + "' does not exist";
    }

    try {
      LocalDateTime sourceDateTime = DateTimeUtil.parseDateTime(dateTimeStr);
      LocalDateTime targetDateTime = DateTimeUtil.parseDateTime(targetDateTimeStr);

      // Create and execute a copy command
      controller.command.CopyEventCommand copyCommand =
              new controller.command.CopyEventCommand(calendarManager, timezoneHandler);

      return copyCommand.execute(new String[]{
              "event", eventName, dateTimeStr, targetCalName, targetDateTimeStr, "true"
      });

    } catch (Exception e) {
      throw new Exception("Failed to copy event: " + e.getMessage(), e);
    }
  }
}
