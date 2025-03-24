package controller.command.edit.strategy;

import java.time.LocalDateTime;

import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Editor for editing a single event.
 */
public class SingleEventEditor extends AbstractEventEditor {

  private final String property;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String newValue;

  /**
   * Creates a new SingleEventEditor.
   *
   * @param args the edit arguments
   */
  public SingleEventEditor(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 5) {
      throw new IllegalArgumentException("Insufficient arguments for editing a single event");
    }

    this.property = args[1];
    this.subject = args[2];

    try {
      this.startDateTime = DateTimeUtil.parseDateTime(args[3]);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing date/time: " + e.getMessage(), e);
    }

    this.newValue = removeQuotes(args[4]);
  }

  @Override
  public String executeEdit(ICalendar calendar) throws EventNotFoundException,
          InvalidEventException, ConflictingEventException {
    validateParameters(subject, property);

    boolean success = calendar.editSingleEvent(subject, startDateTime, property, newValue);

    if (success) {
      return "Successfully edited event '" + subject + "'.";
    } else {
      return "Failed to edit event '" + subject + "'.";
    }
  }
}