package controller.command.edit.strategy;

import model.exceptions.InvalidEventException;

/**
 * Abstract base class for event editing strategies.
 * Provides common functionality for all event editors.
 */
public abstract class AbstractEventEditor implements EventEditor {

  /**
   * Removes surrounding quotes from a string value if present.
   *
   * @param value the string value to process
   * @return the string without surrounding quotes, or the original string if no quotes
   */
  protected String removeQuotes(String value) {
    if (value != null && value.length() >= 2) {
      if ((value.startsWith("\"") && value.endsWith("\"")) ||
              (value.startsWith("'") && value.endsWith("'"))) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }

  /**
   * Validates common event edit parameters.
   *
   * @param subject  the subject of the event to edit
   * @param property the property to edit
   * @throws InvalidEventException if required parameters are invalid
   */
  protected void validateParameters(String subject, String property) throws InvalidEventException {
    if (subject == null || subject.trim().isEmpty()) {
      throw new InvalidEventException("Event subject cannot be empty");
    }
    if (property == null || property.trim().isEmpty()) {
      throw new InvalidEventException("Property to edit cannot be empty");
    }
  }
}