package utilities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Functional interface for converting time between different time zones.
 * This interface provides a way to encapsulate time zone conversion logic.
 */
@FunctionalInterface
public interface TimezoneConverter {

  /**
   * Converts a date-time from one time zone to another.
   *
   * @param dateTime the date-time to convert
   * @return the converted date time
   */
  LocalDateTime convert(LocalDateTime dateTime);

  /**
   * Creates a converter that converts from one timezone to another.
   *
   * @param fromTimezone the source timezone
   * @param toTimezone   the target timezone
   * @param handler      the timezone handler to use
   * @return a converter that performs the specified conversion
   */
  static TimezoneConverter between(String fromTimezone, String toTimezone, TimeZoneHandler handler) {
    return dateTime -> handler.convertTime(dateTime, fromTimezone, toTimezone);
  }

  /**
   * Creates a converter that preserves local time but changes the timezone context.
   * This converter returns a time with the same local time components but considered
   * to be in a different timezone.
   *
   * @param fromTimezone the source timezone
   * @param toTimezone   the target timezone
   * @return a converter that preserves local time
   */
  static TimezoneConverter preservingLocalTime(String fromTimezone, String toTimezone) {
    return dateTime -> dateTime; // Same local time, just in a different timezone context
  }

  /**
   * Creates a copy converter that preserves the duration of an event but shifts
   * it to a specific target date and time.
   *
   * @param targetDateTime the target date and time for the start of the event
   * @return a converter that shifts an event to a specific time while preserving duration
   */
  static TimezoneConverter shiftToSpecificTime(LocalDateTime targetDateTime) {
    return dateTime -> targetDateTime;
  }

  /**
   * Creates a converter that shifts a date-time by a specific number of days.
   *
   * @param days the number of days to shift (can be negative)
   * @return a converter that shifts by days
   */
  static TimezoneConverter shiftByDays(long days) {
    return dateTime -> dateTime.plusDays(days);
  }

  /**
   * Calculates the duration between two date-times.
   *
   * @param start the start date-time
   * @param end   the end date-time
   * @return the duration in seconds
   */
  static long durationInSeconds(LocalDateTime start, LocalDateTime end) {
    return ChronoUnit.SECONDS.between(start, end);
  }

  /**
   * Creates a converter that applies this conversion followed by another.
   *
   * @param after the conversion to apply after this one
   * @return a composed converter
   */
  default TimezoneConverter andThen(TimezoneConverter after) {
    return dateTime -> after.convert(convert(dateTime));
  }

  /**
   * Creates a converter that applies no conversion (identity).
   *
   * @return a converter that returns the input unchanged
   */
  static TimezoneConverter identity() {
    return dateTime -> dateTime;
  }

  /**
   * Creates a converter from a source timezone to UTC.
   *
   * @param fromTimezone the source timezone
   * @param handler      the timezone handler to use
   * @return a converter that converts from the source timezone to UTC
   */
  static TimezoneConverter toUTC(String fromTimezone, TimeZoneHandler handler) {
    return between(fromTimezone, "UTC", handler);
  }

  /**
   * Creates a converter from UTC to a target timezone.
   *
   * @param toTimezone the target timezone
   * @param handler    the timezone handler to use
   * @return a converter that converts from UTC to the target timezone
   */
  static TimezoneConverter fromUTC(String toTimezone, TimeZoneHandler handler) {
    return between("UTC", toTimezone, handler);
  }

  /**
   * Creates a converter that first converts to UTC, then to the target timezone.
   * This is useful for converting between arbitrary timezones.
   *
   * @param fromTimezone the source timezone
   * @param toTimezone   the target timezone
   * @param handler      the timezone handler to use
   * @return a converter that converts via UTC
   */
  static TimezoneConverter viaUTC(String fromTimezone, String toTimezone, TimeZoneHandler handler) {
    return toUTC(fromTimezone, handler).andThen(fromUTC(toTimezone, handler));
  }

  /**
   * Converts a date to the same date in another timezone.
   * This does not adjust for timezone differences, just provides the same calendar date.
   *
   * @param date the date to convert
   * @return the same calendar date
   */
  default LocalDate convertDate(LocalDate date) {
    return date; // Same calendar date, regardless of timezone
  }
}