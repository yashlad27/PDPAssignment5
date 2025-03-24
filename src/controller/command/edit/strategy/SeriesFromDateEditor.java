package controller.command.edit.strategy;

import java.time.LocalDateTime;

import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Editor for editing events in a series from a specific date.
 */
public class SeriesFromDateEditor extends AbstractEventEditor {

  private final String property;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String newValue;

  /**
   * Creates a new SeriesFromDateEditor.
   *
   * @param args the edit arguments
   */
  public SeriesFromDateEditor(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 5) {
      throw new IllegalArgumentException("Insufficient arguments for editing events from date");
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
  public String executeEdit(ICalendar calendar) throws InvalidEventException,
          ConflictingEventException {
    validateParameters(subject, property);

    int count = calendar.editEventsFromDate(subject, startDateTime, property, newValue);

    if (count > 0) {
      return "Successfully edited " + count + " events in the series.";
    } else {
      return "No matching events found to edit.";
    }
  }
}