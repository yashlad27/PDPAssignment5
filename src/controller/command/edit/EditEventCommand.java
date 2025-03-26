package controller.command.edit;

import controller.command.ICommand;
import controller.command.edit.strategy.EventEditor;
import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Command for editing calendar events using the Strategy pattern.
 */
public class EditEventCommand implements ICommand {

  private static final int MIN_REQUIRED_ARGS = 3;
  private final ICalendar calendar;

  /**
   * Constructor that creates an EditEventCommand with a calendar reference.
   *
   * @param calendar the calendar to use for editing events
   * @throws IllegalArgumentException if calendar is null
   */
  public EditEventCommand(ICalendar calendar) {
    validateCalendar(calendar);
    this.calendar = calendar;
  }

  /**
   * Validates that the calendar is not null.
   *
   * @param calendar the calendar to validate
   * @throws IllegalArgumentException if calendar is null
   */
  private void validateCalendar(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
  }

  /**
   * Executes the edit event command with the provided arguments.
   * Uses the strategy pattern to delegate event editing to the appropriate editor.
   *
   * @param args command arguments, where args[0] is the edit type
   * @return a message indicating the result of the operation
   */
  @Override
  public String execute(String[] args) {
    if (!hasMinimumArgs(args, MIN_REQUIRED_ARGS)) {
      return "Error: Insufficient arguments for edit command";
    }

    String editType = args[0];

    try {
      // Get the appropriate editor for this edit type
      EventEditor editor = EventEditor.forType(editType, args);

      // Execute the edit operation
      return editor.executeEdit(calendar);

    } catch (EventNotFoundException e) {
      return formatExceptionMessage("Event not found", e);
    } catch (InvalidEventException e) {
      return formatExceptionMessage("Invalid property or value", e);
    } catch (ConflictingEventException e) {
      return formatExceptionMessage("Would create a conflict", e);
    } catch (IllegalArgumentException e) {
      return "Error in command arguments: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
  }

  /**
   * Checks if the args array has at least the minimum required arguments.
   *
   * @param args    the arguments array to check
   * @param minArgs the minimum number of required arguments
   * @return true if args has at least minArgs elements
   */
  private boolean hasMinimumArgs(String[] args, int minArgs) {
    return args != null && args.length >= minArgs;
  }

  /**
   * Formats an exception message with a consistent pattern.
   *
   * @param context the context of the failure
   * @param e       the exception that was thrown
   * @return a formatted error message
   */
  private String formatExceptionMessage(String context, Exception e) {
    return String.format("Failed to edit event: %s - %s", context, e.getMessage());
  }

  /**
   * Returns the name of this command.
   *
   * @return the string "edit" which identifies this command to the command factory
   */
  @Override
  public String getName() {
    return "edit";
  }
}