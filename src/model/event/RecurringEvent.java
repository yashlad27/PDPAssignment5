package model.event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Represents a recurring event that repeats on specific days of the week.
 * Extends the base Event class to add repetition functionality.
 */
public class RecurringEvent extends Event {

  private final Set<DayOfWeek> repeatDays;
  private final int occurrences;
  private final LocalDate endDate;
  private final UUID recurringId;

  /**
   * Private constructor used by the builder
   */
  private RecurringEvent(Builder builder) {
    super(builder.subject, builder.startDateTime, builder.endDateTime, builder.description, builder.location, builder.isPublic);

    this.repeatDays = EnumSet.copyOf(builder.repeatDays);
    this.occurrences = builder.occurrences;
    this.endDate = builder.endDate;
    this.recurringId = builder.recurringId != null ? builder.recurringId : UUID.randomUUID();

    if (builder.isAllDay) {
      this.setAllDay(true);
    }
  }

  /**
   * Builder class for RecurringEvent.
   */
  public static class Builder {
    // Required parameters
    private final String subject;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Set<DayOfWeek> repeatDays;

    // Optional parameters with default values
    private String description = null;
    private String location = null;
    private boolean isPublic = true;
    private int occurrences = -1;
    private LocalDate endDate = null;
    private UUID recurringId = null;
    private boolean isAllDay = false;

    /**
     * Constructor for the builder with required params.
     */
    public Builder(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime, Set<DayOfWeek> repeatDays) {
      this.subject = subject;
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
      this.repeatDays = repeatDays;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    public Builder isAllDay(boolean isAllDay) {
      this.isAllDay = isAllDay;
      return this;
    }

    public Builder occurrences(int occurrences) {
      this.occurrences = occurrences;
      this.endDate = null;   // reset endDate as we are using occurrences
      return this;
    }

    public Builder endDate(LocalDate endDate) {
      this.endDate = endDate;
      this.occurrences = -1;  // reset occurrences as we are using endDate
      return this;
    }

    public Builder recurringId(UUID recurringId) {
      this.recurringId = recurringId;
      return this;
    }

    /**
     * Builds the RecurringEvent with the specified parameters.
     *
     * @return a new Recurring event.
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public RecurringEvent build() {
      validate();
      return new RecurringEvent(this);
    }

    /**
     * Validates the builder parameters.
     *
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validate() {
      validateCollection(repeatDays, "Repeat days cannot be null or empty.");
      
      // Check that exactly one of occurrences or endDate is specified
      if (isPositive(occurrences) && endDate != null) {
        throw new IllegalArgumentException("Cannot specify both occurrences and endDate");
      }

      if (!isPositive(occurrences) && endDate == null) {
        throw new IllegalArgumentException("Must specify either occurrences or endDate");
      }

      if (endDate != null && !endDate.isAfter(startDateTime.toLocalDate())) {
        throw new IllegalArgumentException("End date must be after start date");
      }
    }
    
    /**
     * Checks if a value is positive (greater than zero).
     *
     * @param value the value to check
     * @return true if the value is positive
     */
    private boolean isPositive(int value) {
      return value > 0;
    }
    
    /**
     * Validates that a collection is not null or empty.
     *
     * @param collection the collection to validate
     * @param errorMessage the error message to throw if invalid
     * @throws IllegalArgumentException if the collection is null or empty
     */
    private <T> void validateCollection(Collection<T> collection, String errorMessage) {
      if (collection == null || collection.isEmpty()) {
        throw new IllegalArgumentException(errorMessage);
      }
    }
  }

  /**
   * Gets all occurrences of this recurring event.
   *
   * @return a list of all occurrences of this recurring event
   */
  public List<Event> getAllOccurrences() {
    List<Event> occurrences = new ArrayList<>();

    LocalDate currentDate = getStartDateTime().toLocalDate();
    LocalTime startTime = getStartDateTime().toLocalTime();
    LocalTime endTime = getEndDateTime().toLocalTime();
    int count = 0;

    // Continue while we're within the constraints (occurrences limit or end date)
    while (shouldContinueGeneratingOccurrences(count, currentDate)) {
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        occurrences.add(createOccurrence(currentDate, startTime, endTime));
        count++;
      }

      currentDate = currentDate.plusDays(1);

      // Early termination checks
      if (hasReachedOccurrenceLimit(count) || hasPassedEndDate(currentDate)) {
        break;
      }
    }

    return occurrences;
  }
  
  /**
   * Determines if we should continue generating occurrences.
   *
   * @param count the current count of occurrences
   * @param currentDate the current date being processed
   * @return true if we should continue generating occurrences
   */
  private boolean shouldContinueGeneratingOccurrences(int count, LocalDate currentDate) {
    return (this.occurrences > 0 && count < this.occurrences) || 
           (this.endDate != null && !currentDate.isAfter(this.endDate));
  }
  
  /**
   * Checks if we've reached the occurrence limit.
   *
   * @param count the current count of occurrences
   * @return true if we've reached the limit
   */
  private boolean hasReachedOccurrenceLimit(int count) {
    return this.occurrences > 0 && count >= this.occurrences;
  }
  
  /**
   * Checks if the current date has passed the end date.
   *
   * @param currentDate the current date being processed
   * @return true if we've passed the end date
   */
  private boolean hasPassedEndDate(LocalDate currentDate) {
    return this.endDate != null && currentDate.isAfter(this.endDate);
  }
  
  /**
   * Creates a new occurrence of this event on the specified date.
   *
   * @param date the date for the occurrence
   * @param startTime the start time for the occurrence
   * @param endTime the end time for the occurrence
   * @return a new Event representing the occurrence
   */
  private Event createOccurrence(LocalDate date, LocalTime startTime, LocalTime endTime) {
    LocalDateTime occurrenceStart = LocalDateTime.of(date, startTime);
    LocalDateTime occurrenceEnd = LocalDateTime.of(date, endTime);

    Event occurrence = new Event(getSubject(), occurrenceStart, occurrenceEnd, 
                                getDescription(), getLocation(), isPublic());
    occurrence.setAllDay(isAllDay());
    
    return occurrence;
  }

  /**
   * Gets the ID specific to this recurring event series.
   *
   * @return the recurring event series ID
   */
  public UUID getRecurringId() {
    return recurringId;
  }

  /**
   * Gets the set of days on which this event repeats.
   *
   * @return a set of days of the week
   */
  public Set<DayOfWeek> getRepeatDays() {
    return EnumSet.copyOf(repeatDays);
  }

  /**
   * Gets the number of occurrences of this recurring event.
   *
   * @return the number of occurrences, or -1 if based on end date
   */
  public int getOccurrences() {
    return occurrences;
  }

  /**
   * Gets the end date of this recurring event.
   *
   * @return the end date, or null if based on occurrences
   */
  public LocalDate getEndDate() {
    return endDate;
  }
}