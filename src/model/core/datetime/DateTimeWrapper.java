package model.core.datetime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;

/**
 * Wrapper class for date/time operations that handles timezone conversions.
 * This abstraction makes the design more flexible for dealing with different date/time types.
 */
public class DateTimeWrapper {
  private final ZonedDateTime dateTime;

  public DateTimeWrapper(LocalDateTime dateTime, String timezone) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    if (timezone.isEmpty()) {
      throw new IllegalArgumentException("Timezone cannot be empty");
    }
    
    try {
      this.dateTime = dateTime.atZone(ZoneId.of(timezone));
    } catch (ZoneRulesException e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone, e);
    }
  }

  public DateTimeWrapper(ZonedDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    this.dateTime = dateTime;
  }

  public LocalDateTime toLocalDateTime() {
    return dateTime.toLocalDateTime();
  }

  public ZonedDateTime toZonedDateTime() {
    return dateTime;
  }

  public DateTimeWrapper convertToTimezone(String targetTimezone) {
    if (targetTimezone == null || targetTimezone.isEmpty()) {
      throw new IllegalArgumentException("Target timezone cannot be null or empty");
    }
    try {
      return new DateTimeWrapper(dateTime.withZoneSameInstant(ZoneId.of(targetTimezone)));
    } catch (ZoneRulesException e) {
      throw new IllegalArgumentException("Invalid target timezone: " + targetTimezone, e);
    }
  }

  public boolean isBefore(DateTimeWrapper other) {
    return dateTime.toInstant().isBefore(other.dateTime.toInstant());
  }

  public boolean isAfter(DateTimeWrapper other) {
    return dateTime.toInstant().isAfter(other.dateTime.toInstant());
  }

  public boolean overlaps(DateTimeWrapper start, DateTimeWrapper end) {
    return !this.isBefore(start) && !this.isAfter(end);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DateTimeWrapper that = (DateTimeWrapper) o;
    // Compare based on instant, not on the local date time
    return dateTime.toInstant().equals(that.dateTime.toInstant());
  }

  @Override
  public int hashCode() {
    return dateTime.toInstant().hashCode();
  }
  
  @Override
  public String toString() {
    return "DateTimeWrapper{dateTime=" + dateTime.toLocalDateTime() + 
           ", timezone=" + dateTime.getZone() + "}";
  }
} 