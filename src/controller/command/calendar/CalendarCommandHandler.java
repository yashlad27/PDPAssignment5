package controller.command.calendar;

import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;

/**
 * Functional interface for handling calendar-specific commands.
 * Used for operations that specifically target calendar management
 * rather than event management.
 */
@FunctionalInterface
public interface CalendarCommandHandler {

  /**
   * Executes a calendar command with the given arguments.
   *
   * @param args the command arguments
   * @return a string result representing the outcome of the command execution
   * @throws CalendarNotFoundException  if a referenced calendar cannot be found
   * @throws DuplicateCalendarException if attempting to create a calendar with a name
   *                                    that already exists
   * @throws InvalidTimezoneException   if an invalid timezone is specified
   */
  String execute(String[] args) throws CalendarNotFoundException,
          DuplicateCalendarException,
          InvalidTimezoneException;

  /**
   * Wraps this handler to catch and report exceptions.
   *
   * @return a new handler that handles exceptions
   */
  default CalendarCommandHandler withExceptionHandling() {
    return args -> {
      try {
        return execute(args);
      } catch (CalendarNotFoundException e) {
        return "Error: " + e.getMessage();
      } catch (DuplicateCalendarException e) {
        return "Error: " + e.getMessage();
      } catch (InvalidTimezoneException e) {
        return "Error: " + e.getMessage();
      } catch (Exception e) {
        return "Unexpected error: " + e.getMessage();
      }
    };
  }
}