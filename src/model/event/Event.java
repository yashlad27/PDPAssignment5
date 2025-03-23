package model.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a calendar event with properties like subject, start and end times, description,
 * location, and privacy setting.
 */
public class Event {

  private final UUID id;
  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private boolean isPublic;
  private boolean isAllDay;

  /**
   * Constructs a new Event with the given parameters.
   *
   * @param subject       the subject/title of the event
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time, null if all-day event
   * @param description   a description of the event, can be null
   * @param location      the location of the event, can be null
   * @param isPublic      whether the event is public
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
      String description, String location, boolean isPublic) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    this.id = UUID.randomUUID();
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;

    if (endDateTime == null) {
      this.isAllDay = true;
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.of(23, 59, 59));
    } else {
      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("End date/time cannot be before start date/time");
      }
      this.isAllDay = false;
      this.endDateTime = endDateTime;
    }
  }

  /**
   * Creates an all-day event for a specific date.
   *
   * @param subject     the subject/title of the event
   * @param date        the date of the all-day event
   * @param description a description of the event, can be null
   * @param location    the location of the event, can be null
   * @param isPublic    whether the event is public
   * @return a new all-day Event
   */
  public static Event createAllDayEvent(String subject, LocalDate date, String description,
      String location, boolean isPublic) {
    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(0, 0));
    LocalDateTime end = LocalDateTime.of(date, LocalTime.of(23, 59, 59));

    Event event = new Event(subject, start, end, description, location, isPublic);
    event.isAllDay = true;
    event.date = date;
    return event;
  }

  /**
   * Checks if this event conflicts with another event. Two events conflict if their time intervals
   * overlap.
   *
   * @param other the event to check for conflicts
   * @return true if there is a conflict, false otherwise
   */
  public boolean conflictsWith(Event other) {
    if (other == null) {
      return false;
    }

    return !this.endDateTime.isBefore(other.startDateTime) && !other.endDateTime.isBefore(
        this.startDateTime);
  }

  /**
   * Checks if this event spans multiple days.
   *
   * @return true if the event spans multiple days, false otherwise
   */
  public boolean spansMultipleDays() {
    return !startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Gets the unique identifier for this event.
   *
   * @return the UUID of this event
   */
  public UUID getId() {
    return id;
  }

  /**
   * Gets the subject of this event.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of this event.
   *
   * @param subject the new subject
   * @throws IllegalArgumentException if subject is null or empty
   */
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    this.subject = subject;
  }

  /**
   * Gets the start date and time of this event.
   *
   * @return the start date and time
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the end date and time of this event.
   *
   * @return the end date and time
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Sets the end date and time of this event.
   *
   * @param endDateTime the new end date and time
   * @throws IllegalArgumentException if endDateTime is before startDateTime
   */
  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime == null) {
      // Converting to all-day event
      this.isAllDay = true;
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.of(23, 59, 59));
    } else {
      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("End date/time cannot be before start date/time");
      }
      this.endDateTime = endDateTime;
      this.isAllDay = false;
    }
  }

  /**
   * Gets the description of this event.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of this event.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Checks if this event is public.
   *
   * @return true if the event is public, false otherwise
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Checks if this is an all-day event.
   *
   * @return true if this is an all-day event, false otherwise
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Sets whether this is an all-day event.
   *
   * @param isAllDay true if this should be an all-day event, false otherwise
   */
  public void setAllDay(boolean isAllDay) {
    this.isAllDay = isAllDay;

    if (isAllDay) {
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.of(23, 59, 59));
    }
  }

  /**
   * Sets the description of this event.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the location of this event.
   *
   * @param location the new location
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Sets the start date and time of this event.
   *
   * @param startDateTime the new start date and time
   */
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (this.endDateTime != null && startDateTime.isAfter(this.endDateTime)) {
      throw new IllegalArgumentException("Start date/time cannot be after end date/time");
    }
    this.startDateTime = startDateTime;
  }

  /**
   * Sets whether this event is public.
   *
   * @param isPublic true if the event is public, false otherwise
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * The date of an all-day event.
   */
  private LocalDate date;

  /**
   * Gets the date of this all-day event.
   *
   * @return the date of this event, or null if this is not an all-day event
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Sets the date of this all-day event.
   *
   * @param date the date of this all-day event
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  /**
   * Checks if this event is an all-day event.
   *
   * @return true if this is an all-day event, false otherwise
   */
  public boolean isAllDayEvent() {
    return date != null && startDateTime == null && endDateTime == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(subject, event.subject) && Objects.equals(startDateTime,
        event.startDateTime) && Objects.equals(endDateTime, event.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return "Event{" + "subject='" + subject + '\'' + ", startDateTime=" + startDateTime
        + ", endDateTime=" + endDateTime + ", isAllDay=" + isAllDay + ", location='" + (
        location != null ? location : "N/A") + '\'' + '}';
  }
}