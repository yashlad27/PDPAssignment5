package controller.command.create.strategy;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Strategy for creating a recurring event with a specific number of occurrences. Extends
 * AbstractEventCreator to inherit common functionality.
 */
public class RecurringEventCreator extends AbstractEventCreator {

  private static final int MIN_REQUIRED_ARGS = 7;
  private static final int MAX_OCCURRENCES = 999;

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
    if (!hasMinimumLength(args, MIN_REQUIRED_ARGS)) {
      throw new IllegalArgumentException("Insufficient arguments for creating a recurring event");
    }
  }

  /**
   * Checks if an array has at least the minimum required length.
   *
   * @param array     the array to check
   * @param minLength the minimum required length
   * @return true if the array has at least the minimum length
   */
  private static boolean hasMinimumLength(Object[] array, int minLength) {
    return array != null && array.length >= minLength;
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
      throw e;
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
    if (isNullOrEmpty(weekdays)) {
      throw new InvalidEventException("Repeat days cannot be empty");
    }

    try {
      this.repeatDays = DateTimeUtil.parseWeekdays(weekdays);
      if (isCollectionEmpty(this.repeatDays)) {
        throw new InvalidEventException("Invalid weekday combination");
      }
    } catch (IllegalArgumentException e) {
      throw new InvalidEventException("Invalid weekday combination");
    }

    this.occurrences = Integer.parseInt(args[5]);
    validateOccurrences(this.occurrences);

    this.autoDecline = Boolean.parseBoolean(args[6]);
  }

  /**
   * Checks if a string is null or empty.
   *
   * @param str the string to check
   * @return true if the string is null or empty
   */
  private static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * Checks if a collection is empty.
   *
   * @param collection the collection to check
   * @return true if the collection is empty
   */
  private static boolean isCollectionEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * Validates that occurrences is within acceptable bounds.
   *
   * @param occurrences the number of occurrences to validate
   * @throws InvalidEventException if occurrences is not within acceptable bounds
   */
  private void validateOccurrences(int occurrences) throws InvalidEventException {
    validateWithPredicate(occurrences, value -> value <= 0, "Occurrences must be positive");
    validateWithPredicate(occurrences, value -> value > MAX_OCCURRENCES,
        "Maximum occurrences exceeded");
  }

  /**
   * Validates a value using a predicate.
   *
   * @param value            the value to validate
   * @param invalidCondition predicate that returns true if the condition is invalid
   * @param errorMessage     the error message to throw if the condition is invalid
   * @throws InvalidEventException if the invalidCondition returns true
   */
  private <T> void validateWithPredicate(T value, Predicate<T> invalidCondition,
      String errorMessage) throws InvalidEventException {
    if (invalidCondition.test(value)) {
      throw new InvalidEventException(errorMessage);
    }
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

    validateWithPredicate(startDateTime, dateTime -> dateTime == null,
        "Start date/time cannot be null");

    validateWithPredicate(endDateTime, dateTime -> dateTime == null,
        "End date/time cannot be null");

    validateWithPredicate(new DateTimePair(startDateTime, endDateTime),
        pair -> pair.second.isBefore(pair.first), "End date/time cannot be before start date/time");

    validateWithPredicate(repeatDays, collection -> isCollectionEmpty(collection),
        "Repeat days cannot be empty");

    validateOccurrences(occurrences);
  }

  /**
   * Simple pair class for date time validation.
   */
  private static class DateTimePair {

    final LocalDateTime first;
    final LocalDateTime second;

    DateTimePair(LocalDateTime first, LocalDateTime second) {
      this.first = first;
      this.second = second;
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
      return new RecurringEvent.Builder(eventName, startDateTime, endDateTime,
          repeatDays).description(description).location(location).isPublic(isPublic)
          .occurrences(occurrences).build();
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
        eventName, occurrences, DateTimeUtil.formatWeekdays(repeatDays));
  }
}