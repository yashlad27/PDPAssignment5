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
 * Represents a recurring event that repeats on specific days of the week. Extends the base Event
 * class to add repetition functionality.
 */
public class RecurringEvent extends Event {

  private final Set<DayOfWeek> repeatDays;
  private final int occurrences;
  private final LocalDate endDate;
  private final UUID recurringId;

  /**
   * Private constructor used by the builder
   */
  private RecurringEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                         String description, String location, boolean isPublic,
                         Set<DayOfWeek> repeatDays, int occurrences, LocalDate endDate, UUID recurringId) {
    super(subject, startDateTime, endDateTime, description, location, isPublic);

    this.repeatDays = EnumSet.copyOf(repeatDays);
    this.occurrences = occurrences;
    this.endDate = endDate;
    this.recurringId = recurringId != null ? recurringId : UUID.randomUUID();
  }

  /**
   * Builder class for RecurringEvent.
   */
  public static class Builder {

    /**
     * These are required params for event creation
     */
    private final String subject;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Set<DayOfWeek> repeatDays;

    /**
     * These are optional params for event creation
     */
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
    public Builder(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                   Set<DayOfWeek> repeatDays) {
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
     */
    public RecurringEvent build() {
      validate();
      RecurringEvent event = new RecurringEvent(subject, startDateTime, endDateTime,
              description, location, isPublic, repeatDays, occurrences, endDate, recurringId);

      if (isAllDay) {
        event.setAllDay(true);
      }

      return event;
    }

    /**
     * Validates the builder parameters.
     */
    private void validate() {
      if (repeatDays == null || repeatDays.isEmpty()) {
        throw new IllegalArgumentException(
                "Repeat days cannot be null or empty."
        );
      }

      if (occurrences > 0 && endDate != null) {
        throw new IllegalArgumentException("Cannot specify both occurrences and endDate");
      }

      if (occurrences <= 0 && endDate == null) {
        throw new IllegalArgumentException("Must specify either occurrences or endDate");
      }

      if (occurrences <= 0 && endDate == null) {
        throw new IllegalArgumentException("Occurrences must be positive");
      }

      if (endDate != null && !endDate.isAfter(startDateTime.toLocalDate())) {
        throw new IllegalArgumentException("End date must be after start date");
      }

//      if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
//        throw new IllegalArgumentException("Recurring events must start and end on the same day");
//      }
    }

  }

  /**
   * Creates an all-day recurring event with a specified number of occurrences.
   *
   * @param subject     the subject/title of the event
   * @param date        the date of the first occurrence
   * @param description the description of the event
   * @param location    the location of the event
   * @param isPublic    whether the event is public
   * @param repeatDays  set of days of the week on which the event repeats
   * @param occurrences the number of times the event repeats
   * @return a new all-day recurring event
   */
  public RecurringEvent createAllDayRecurringEvent(String subject, LocalDate date,
                                                   String description, String location,
                                                   boolean isPublic,
                                                   Set<DayOfWeek> repeatDays,
                                                   int occurrences) {
    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(0, 0));
    LocalDateTime end = LocalDateTime.of(date, LocalTime.of(23, 59, 59));

    RecurringEvent event = new Builder(subject, start, end, repeatDays)
            .description(description)
            .location(location)
            .isPublic(isPublic)
            .occurrences(occurrences)
            .build();

    event.setAllDay(true);
    return event;
  }

  /**
   * Creates an all-day recurring event that repeats until a specified end date.
   *
   * @param subject     the subject/title of the event
   * @param date        the date of the first occurrence
   * @param description the description of the event
   * @param location    the location of the event
   * @param isPublic    whether the event is public
   * @param repeatDays  set of days of the week on which the event repeats
   * @param endDate     the date after which the event stops repeating
   * @return a new all-day recurring event
   */
  public RecurringEvent createAllDayRecurringEvent(String subject, LocalDate date,
                                                   String description, String location,
                                                   boolean isPublic,
                                                   Set<DayOfWeek> repeatDays,
                                                   LocalDate endDate) {
    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(0, 0));
    LocalDateTime end = LocalDateTime.of(date, LocalTime.of(23, 59, 59));

    RecurringEvent event = new Builder(subject, start, end, repeatDays)
            .description(description)
            .location(location)
            .isPublic(isPublic)
            .endDate(endDate)
            .build();

    event.setAllDay(true);
    return event;
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

    while ((this.occurrences > 0 && count < this.occurrences) ||
            (this.endDate != null && !currentDate.isAfter(this.endDate))) {

      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime occurrenceStart = LocalDateTime.of(currentDate, startTime);
        LocalDateTime occurrenceEnd = LocalDateTime.of(currentDate, endTime);

        Event occurrence = new Event(
                getSubject(),
                occurrenceStart,
                occurrenceEnd,
                getDescription(),
                getLocation(),
                isPublic()
        );
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

  /**
   * Gets the next occurrence of this event after the given date.
   *
   * @param after the date after which to find the next occurrence
   * @return the next occurrence, or null if no more occurrences
   */
  private Event getNextOccurrence(LocalDate after) {
    LocalDate nextDate = after.plusDays(1);
    LocalTime startTime = getStartDateTime().toLocalTime();
    LocalTime endTime = getEndDateTime().toLocalTime();

    // If we're using an end date and have passed it, no more occurrences
    if (endDate != null && nextDate.isAfter(endDate)) {
      return null;
    }

    // Find the next date that falls on one of the repeat days
    while (!repeatDays.contains(nextDate.getDayOfWeek())) {
      nextDate = nextDate.plusDays(1);

      // If we've passed the end date, no more occurrences
      if (endDate != null && nextDate.isAfter(endDate)) {
        return null;
      }
    }

    // Create a new event for this occurrence
    LocalDateTime occurrenceStart = LocalDateTime.of(nextDate, startTime);
    LocalDateTime occurrenceEnd = LocalDateTime.of(nextDate, endTime);

    Event occurrence = new Event(
            getSubject(),
            occurrenceStart,
            occurrenceEnd,
            getDescription(),
            getLocation(),
            isPublic()
    );
    occurrence.setAllDay(isAllDay());

    return occurrence;
  }

}