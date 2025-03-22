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
  public String execute(String[] args) throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    System.out.println("*** Debug: Executing copy command with args:");
    for (int i = 0; i < args.length; i++) {
      System.out.println("Args[" + i + "]: '" + args[i] + "'");
    }

    if (args[0].equals("copy") && args[1].equals("events")) {
      System.out.println("DEBUG: Found 'copy events' command");
      System.out.println("args[2]: '" + args[2] + "'");
      if (args[2].equals("on")) {
        System.out.println("DEBUG: args[2] is 'on'");
        // Debug other positions
        if (args.length >= 5) System.out.println("args[4]: '" + args[4] + "'");
        if (args.length >= 7) System.out.println("args[6]: '" + args[6] + "'");
      }
    }

    if (args.length < 1) {
      return "Error: Insufficient arguments for copy command";
    }

    try {
      // Get the appropriate strategy for the command
      CopyStrategy strategy = strategyFactory.getStrategy(args);

      if (strategy == null) {
        return "Error: Unknown copy command format";
      }

      // Execute the strategy
      return strategy.execute(args);

    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "copy";
  }
}