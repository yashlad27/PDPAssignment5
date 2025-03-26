package model.event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
      if (repeatDays == null || repeatDays.isEmpty()) {
        throw new IllegalArgumentException("Repeat days cannot be null or empty.");
      }

      if (occurrences > 0 && endDate != null) {
        throw new IllegalArgumentException("Cannot specify both occurrences and endDate");
      }

      if (occurrences <= 0 && endDate == null) {
        throw new IllegalArgumentException("Must specify either occurrences or endDate");
      }

      if (endDate != null && !endDate.isAfter(startDateTime.toLocalDate())) {
        throw new IllegalArgumentException("End date must be after start date");
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

    while ((this.occurrences > 0 && count < this.occurrences) || (this.endDate != null && !currentDate.isAfter(this.endDate))) {

      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
        LocalDateTime occurrenceEnd = LocalDateTime.of(currentDate, endTime);

        Event occurrence = new Event(getSubject(), occurrenceStart, occurrenceEnd, getDescription(), getLocation(), isPublic());
        occurrence.setAllDay(isAllDay());

        occurrences.add(occurrence);
        count++;
      }

      currentDate = currentDate.plusDays(1);

      if (this.occurrences > 0 && count >= this.occurrences) {
        break;
      }

      if (this.endDate != null && currentDate.isAfter(this.endDate)) {
        break;
      }
    }

    return occurrences;
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