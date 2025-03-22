package controller.command.edit;

import controller.command.ICommand;
import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Command for editing calendar events using the Strategy pattern.
 */
public class EditEventCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Constructor that creates an EditEventCommand with a calendar reference.
   *
   * @param calendar the calendar to use for editing events
   * @throws IllegalArgumentException if calendar is null
   */
  public EditEventCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
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
    if (args.length < 3) {
      return "Error: Insufficient arguments for edit command";
    }

    String editType = args[0];

    try {
      // Get the appropriate editor for this edit type
      EventEditor editor = EventEditor.forType(editType, args);

      // Execute the edit operation
      return editor.executeEdit(calendar);

    } catch (EventNotFoundException e) {
      return "Failed to edit event: Event not found - " + e.getMessage();
    } catch (InvalidEventException e) {
      return "Failed to edit event: Invalid property or value - " + e.getMessage();
    } catch (ConflictingEventException e) {
      return "Failed to edit event: Would create a conflict - " + e.getMessage();
    } catch (IllegalArgumentException e) {
      return "Error in command arguments: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
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