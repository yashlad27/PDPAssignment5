package utilities;

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
}
