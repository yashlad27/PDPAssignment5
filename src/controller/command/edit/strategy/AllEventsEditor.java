package controller.command.edit.strategy;

import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Editor for editing all events with a specific subject.
 */
public class AllEventsEditor extends AbstractEventEditor {

  private final String property;
  private final String subject;
  private final String newValue;

  /**
   * Creates a new AllEventsEditor.
   *
   * @param args the edit arguments
   */
  public AllEventsEditor(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 4) {
      throw new IllegalArgumentException("Insufficient arguments for editing all events");
    }

    this.property = args[1];
    this.subject = args[2];
    this.newValue = removeQuotes(args[3]);
  }

  @Override
  public String executeEdit(ICalendar calendar) throws InvalidEventException,
          ConflictingEventException {
    validateParameters(subject, property);

    int count = calendar.editAllEvents(subject, property, newValue);

    if (count > 0) {
      return "Successfully edited " + count + " events.";
    } else {
      return "No events found with the subject '" + subject + "'.";
    }
  }
}