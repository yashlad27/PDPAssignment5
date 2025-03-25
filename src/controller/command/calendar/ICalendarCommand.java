package controller.command.calendar;

import controller.CalendarController;

/**
 * Interface for calendar-related commands.
 * This interface defines the contract for all calendar-level operations.
 *
 * <p>The command pattern is used here to encapsulate calendar operations as objects,
 * allowing for better separation of concerns and easier addition of new calendar operations.
 * Each command represents a single calendar operation and can be executed independently.
 *
 * <p>This interface is part of the Command pattern implementation, where:
 * - Each command is a separate class implementing this interface
 * - Commands are created by the CalendarCommandFactory
 * - Commands are executed by the CalendarController
 *
 * @see CalendarCommandFactory
 * @see CalendarController
 */
public interface ICalendarCommand {
  /**
   * Executes the calendar command.
   * This method performs the actual calendar operation and returns a result message.
   *
   * <p>The execution should:
   * - Handle all necessary validation
   * - Perform the required calendar operation
   * - Return a descriptive message about the operation's result
   *
   * @return A string message describing the result of the command execution
   */
  String execute();
} 