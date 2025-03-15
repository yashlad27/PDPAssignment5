package controller.command;

import java.time.LocalDateTime;

import model.calendar.ICalendar;
import utilities.DateTimeUtil;

/**
 * Command for editing calendar events (both single and recurring events).
 */
public class EditEventCommand implements ICommand {

  private final ICalendar calendar;
  private final String commandType;
  private final String subject;
  private final String property;
  private final String newValue;
  private final LocalDateTime startDateTime;

  /**
   * Private constructor used by the builder.
   */
  private EditEventCommand(Builder builder) {
    this.calendar = builder.calendar;
    this.commandType = builder.commandType;
    this.property = builder.property;
    this.subject = builder.subject;
    this.newValue = builder.newValue;
    this.startDateTime = builder.startDateTime;
  }

  /**
   * Constructor that creates a minimal EditEventCommand with just a calendar reference. This
   * constructor is used for registration with the command factory.
   *
   * @param calendar the calendar to use for editing events
   * @throws IllegalArgumentException if calendar is null
   */
  public EditEventCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
    this.commandType = null;
    this.property = null;
    this.subject = null;
    this.newValue = null;
    this.startDateTime = null;
  }

  /**
   * Builder class for EditEventCommand.
   */
  public static class Builder {

    private final ICalendar calendar;

    private String commandType = null;
    private String subject = null;
    private String property = null;
    private String newValue = null;
    private LocalDateTime startDateTime = null;

    /**
     * Creates a builder for EditEventCommand.
     *
     * @param calendar the calendar to use for editing events
     * @throws IllegalArgumentException if calendar is null
     */
    public Builder(ICalendar calendar) {
      if (calendar == null) {
        throw new IllegalArgumentException("Calendar cannot be null");
      }
      this.calendar = calendar;
    }

    /**
     * Sets the subject of the event(s) to edit.
     *
     * @param subject the subject of the event(s)
     * @return the builder instance
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the property to edit.
     *
     * @param property the property to edit (subject, description, etc.)
     * @return the builder instance
     */
    public Builder property(String property) {
      this.property = property;
      return this;
    }

    /**
     * Sets the start date/time for identifying the event.
     *
     * @param startDateTime the start date/time of the event
     * @return the builder instance
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

  }

  /**
   * Removes surrounding quotes from a string value if present.
   *
   * @param value the string value to process
   * @return the string without surrounding quotes
   */
  private String removeQuotes(String value) {
    if (value != null && value.length() >= 2) {
      if ((value.startsWith("\"") && value.endsWith("\"")) ||
              (value.startsWith("'") && value.endsWith("'"))) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }

  /**
   * Executes the edit event command with the provided arguments. Handles different types of event
   * editing including single events, recurring events, all-day events, and their variants.
   *
   * @param args an array of arguments for the command: - args[0]: the type of event to edit
   *             (single, recurring, allday, etc.) - remaining args: parameters specific to each
   *             event type
   * @return a string message indicating the result of the command execution
   */

  @Override
  public String execute(String[] args) {
    if (args.length < 3) {
      return "Error: Insufficient arguments for edit command";
    }

    String type = args[0];
    if (type.equals("single")) {
      if (args.length < 5) {
        return "Error: Insufficient arguments for editing a single event";
      }

      String property = args[1];
      String subject = args[2];
      LocalDateTime startDateTime;
      try {
        startDateTime = DateTimeUtil.parseDateTime(args[3]);
      } catch (Exception e) {
        return "Error parsing date/time: " + e.getMessage();
      }

      String newValue = args[4];
      newValue = removeQuotes(newValue);

      boolean success = calendar.editSingleEvent(subject, startDateTime, property, newValue);

      if (success) {
        return "Successfully edited event '" + subject + "'.";
      } else {
        return "Failed to edit event. Event not found or invalid property.";
      }
    } else if (type.equals("series_from_date")) {
      if (args.length < 5) {
        return "Error: Insufficient arguments for editing events from date";
      }

      String property = args[1];
      String subject = args[2];
      LocalDateTime startDateTime;
      try {
        startDateTime = DateTimeUtil.parseDateTime(args[3]);
      } catch (Exception e) {
        return "Error parsing date/time: " + e.getMessage();
      }

      String newValue = args[4];
      newValue = removeQuotes(newValue);

      int count = calendar.editEventsFromDate(subject, startDateTime, property, newValue);

      if (count > 0) {
        return "Successfully edited " + count + " events in the series.";
      } else {
        return "No matching events found to edit.";
      }
    } else if (type.equals("all")) {
      if (args.length < 4) {
        return "Error: Insufficient arguments for editing all events";
      }

      String property = args[1];
      String subject = args[2];
      String newValue = args[3];
      newValue = removeQuotes(newValue);

      int count = calendar.editAllEvents(subject, property, newValue);

      if (count > 0) {
        return "Successfully edited " + count + " events.";
      } else {
        return "No events found with the subject '" + subject + "'.";
      }
    } else {
      return "Unknown edit command type: " + type;
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