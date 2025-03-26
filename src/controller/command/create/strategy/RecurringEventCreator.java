package controller.command.create.strategy;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Strategy for creating a recurring event with a specific number of occurrences.
 * Extends AbstractEventCreator to inherit common functionality.
 */
public class RecurringEventCreator extends AbstractEventCreator {

  private String eventName;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Set<DayOfWeek> repeatDays;
  private int occurrences;
  private boolean autoDecline;
  private String description;
  private String location;
  private boolean isPublic;

  /**
   * Constructs a strategy for creating a recurring event.
   *
   * @param args the arguments for event creation
   * @throws InvalidEventException if event parameters are invalid
   */
  public RecurringEventCreator(String[] args) throws InvalidEventException {
    validateInputArguments(args);
    initializeFields(args);
  }

  /**
   * Validates the input arguments array.
   *
   * @param args the arguments to validate
   * @throws IllegalArgumentException if arguments are invalid
   */
  private void validateInputArguments(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 7) {
      throw new IllegalArgumentException("Insufficient arguments for creating a recurring event");
    }
  }

  /**
   * Initializes all fields from the input arguments.
   *
   * @param args the arguments to parse
   * @throws InvalidEventException if event parameters are invalid
   */
  private void initializeFields(String[] args) throws InvalidEventException {
    try {
      initializeRequiredFields(args);
      initializeOptionalFields(args);
    } catch (InvalidEventException e) {
      throw e;  // Re-throw InvalidEventException as is
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  /**
   * Initializes the required fields from the input arguments.
   *
   * @param args the arguments to parse
   * @throws InvalidEventException if required parameters are invalid
   */
  private void initializeRequiredFields(String[] args) throws InvalidEventException {
    this.eventName = args[1];
    this.startDateTime = DateTimeUtil.parseDateTime(args[2]);
    this.endDateTime = DateTimeUtil.parseDateTime(args[3]);

    String weekdays = args[4];
    if (weekdays == null || weekdays.trim().isEmpty()) {
      throw new InvalidEventException("Repeat days cannot be empty");
    }

    this.repeatDays = DateTimeUtil.parseWeekdays(weekdays);
    if (this.repeatDays.isEmpty()) {
      throw new InvalidEventException("Repeat days cannot be empty");
    }

    this.occurrences = Integer.parseInt(args[5]);
    this.autoDecline = Boolean.parseBoolean(args[6]);
  }

  /**
   * Initializes the optional fields from the input arguments.
   *
   * @param args the arguments to parse
   */
  private void initializeOptionalFields(String[] args) {
    this.description = args.length > 7 ? removeQuotes(args[7]) : null;
    this.location = args.length > 8 ? removeQuotes(args[8]) : null;
    this.isPublic = args.length > 9 ? Boolean.parseBoolean(args[9]) : true;
  }

  @Override
  public Event createEvent() throws InvalidEventException {
    validateEventParameters();
    return buildRecurringEvent();
  }

  /**
   * Validates all event parameters before creation.
   *
   * @throws InvalidEventException if any parameter is invalid
   */
  private void validateEventParameters() throws InvalidEventException {
    validateEventParameters(eventName);

    // Validate date/time parameters
    if (startDateTime == null) {
      throw new InvalidEventException("Start date/time cannot be null");
    }
    if (endDateTime == null) {
      throw new InvalidEventException("End date/time cannot be null");
    }
    if (endDateTime.isBefore(startDateTime)) {
      throw new InvalidEventException("End date/time cannot be before start date/time");
    }

    // Validate recurrence parameters
    if (repeatDays == null || repeatDays.isEmpty()) {
      throw new InvalidEventException("Repeat days cannot be empty");
    }
    if (occurrences <= 0) {
      throw new InvalidEventException("Occurrences must be positive");
    }
  }

  /**
   * Builds and returns the recurring event.
   *
   * @return the created recurring event
   * @throws InvalidEventException if event creation fails
   */
  private Event buildRecurringEvent() throws InvalidEventException {
    try {
      return new RecurringEvent.Builder(eventName, startDateTime, endDateTime, repeatDays)
              .description(description)
              .location(location)
              .isPublic(isPublic)
              .occurrences(occurrences)
              .build();
    } catch (IllegalArgumentException e) {
      throw new InvalidEventException(e.getMessage());
    }
  }

  @Override
  protected boolean getAutoDecline() {
    return autoDecline;
  }

  @Override
  protected String getSuccessMessage(Event event) {
    return String.format("Recurring event '%s' created successfully with %d occurrences on %s",
            eventName,
            occurrences,
            DateTimeUtil.formatWeekdays(repeatDays));
  }
}