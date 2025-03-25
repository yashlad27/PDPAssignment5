package model.core.timezone;

import java.time.LocalDateTime;

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


}
