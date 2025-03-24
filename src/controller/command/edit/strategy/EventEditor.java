package controller.command.edit.strategy;

import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

/**
 * Strategy interface for different event editing methods.
 * Each concrete implementation handles a specific type of event editing operation.
 */
public interface EventEditor {

  /**
   * Executes the edit operation on the calendar.
   *
   * @param calendar the calendar containing the events to edit
   * @return a result message indicating success or failure
   * @throws EventNotFoundException    if the event to edit cannot be found
   * @throws InvalidEventException     if the edit parameters are invalid
   * @throws ConflictingEventException if the edit would create a conflict
   */
  String executeEdit(ICalendar calendar) throws EventNotFoundException,
          InvalidEventException, ConflictingEventException;

  /**
   * Factory method to create the appropriate editor based on edit type.
   *
   * @param type the type of edit operation
   * @param args the arguments for the edit operation
   * @return the appropriate editor for the specified edit type
   * @throws IllegalArgumentException if the edit type is unknown or arguments are invalid
   */
  static EventEditor forType(String type, String[] args) {
    switch (type) {
      case "single":
        return new SingleEventEditor(args);
      case "series_from_date":
        return new SeriesFromDateEditor(args);
      case "all":
        return new AllEventsEditor(args);
      default:
        throw new IllegalArgumentException("Unknown edit type: " + type);
    }
  }
}