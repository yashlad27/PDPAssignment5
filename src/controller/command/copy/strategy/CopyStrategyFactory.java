package controller.command.copy.strategy;

import java.util.ArrayList;
import java.util.List;

import model.calendar.CalendarManager;
import utilities.TimeZoneHandler;

/**
 * Factory for creating appropriate copy strategies based on command arguments.
 */
public class CopyStrategyFactory {

  private final List<CopyStrategy> strategies;

  /**
   * Constructs a new CopyStrategyFactory with all available strategies.
   *
   * @param calendarManager the calendar manager
   * @param timezoneHandler the timezone handler
   */
  public CopyStrategyFactory(CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
    strategies = new ArrayList<>();

    // Register all available copy strategies
    strategies.add(new SingleEventCopyStrategy(calendarManager, timezoneHandler));
    strategies.add(new DayEventsCopyStrategy(calendarManager, timezoneHandler));
    strategies.add(new RangeEventsCopyStrategy(calendarManager, timezoneHandler));
  }

  /**
   * Gets a strategy that can handle the given command arguments.
   *
   * @param args the command arguments
   * @return the appropriate copy strategy, or null if none found
   */
  public CopyStrategy getStrategy(String[] args) {
    // Check if we have enough arguments to determine command type
    if (args.length < 3) {
      return null;
    }

    // Determine command type based on argument pattern
    if (args[0].equals("copy")) {
      if (args[1].equals("event")) {
        // copy event <eventName> on <dateStringTtimeString> --target <calendarName> to <dateStringTtimeString>
        for (CopyStrategy strategy : strategies) {
          if (strategy instanceof SingleEventCopyStrategy) {
            return strategy;
          }
        }
      } else if (args[1].equals("events")) {
        if (args[2].equals("on")) {
          // copy events on <dateString> --target <calendarName> to <dateString>
          for (CopyStrategy strategy : strategies) {
            if (strategy instanceof DayEventsCopyStrategy) {
              return strategy;
            }
          }
        } else if (args[2].equals("between")) {
          // copy events between <dateString> and <dateString> --target <calendarName> to <dateString>
          for (CopyStrategy strategy : strategies) {
            if (strategy instanceof RangeEventsCopyStrategy) {
              return strategy;
            }
          }
        }
      }
    }

    // Fall back to the canHandle method for custom formats
    for (CopyStrategy strategy : strategies) {
      if (strategy.canHandle(args)) {
        return strategy;
      }
    }

    return null;
  }
}