package controller.command.copy;

import controller.command.ICommand;
import controller.command.copy.strategy.CopyStrategy;
import controller.command.copy.strategy.CopyStrategyFactory;
import model.calendar.CalendarManager;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.TimeZoneHandler;

/**
 * Command for copying events between calendars.
 * Uses the Strategy pattern to handle different copy operations.
 */
public class CopyEventCommand implements ICommand {

  private final CopyStrategyFactory strategyFactory;

  /**
   * Constructs a new CopyEventCommand.
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

    this.strategyFactory = new CopyStrategyFactory(calendarManager, timezoneHandler);
  }

  @Override
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    if (args.length < 1) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      CopyStrategy strategy = strategyFactory.getStrategy(args);

      if (strategy == null) {
        return "Error: Unknown copy command format";
      }

      return strategy.execute(args);

    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "copy";
  }
}