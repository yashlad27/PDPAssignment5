package utilities;

import java.time.LocalDateTime;

/**
 * Functional interface for converting time between different time zones.
 * This interface provides a way to encapsulate time zone conversion logic using
 * a functional approach, allowing for composable and reusable time conversion operations.
 * Implementations can handle various time zone conversion scenarios while maintaining
 * a clean, consistent API.
 */
@FunctionalInterface
public interface TimezoneConverter {

  /**
   * Converts a date-time from one time zone to another.
   * This core method defines the conversion operation that transforms a LocalDateTime
   * from the source time zone to the target time zone.
   *
   * @param dateTime the date-time to convert, represented in the source time zone
   * @return the converted date time in the target time zone
   */
  LocalDateTime convert(LocalDateTime dateTime);

  /**
   * Creates a converter that converts from one timezone to another.
   * This factory method creates a converter that transforms dates and times
   * directly between specified source and target time zones.
   *
   * @param fromTimezone the source timezone identifier (e.g., "America/New_York")
   * @param toTimezone   the target timezone identifier (e.g., "Europe/Paris")
   * @param handler      the timezone handler to use for the actual conversion logic
   * @return a converter that performs the specified conversion
   */
  static TimezoneConverter between(String fromTimezone, String toTimezone,
                                   TimeZoneHandler handler) {
    return dateTime -> handler.convertTime(dateTime, fromTimezone, toTimezone);
  }

  /**
   * Creates a converter that applies this conversion followed by another.
   * This method enables function composition, allowing multiple conversions
   * to be chained together in a fluent, readable manner.
   *
   * @param after the conversion to apply after this one
   * @return a composed converter that applies this conversion first, then the after conversion
   */
  default TimezoneConverter andThen(TimezoneConverter after) {
    return dateTime -> after.convert(convert(dateTime));
  }

  /**
   * Creates a converter that applies no conversion (identity).
   * This is useful as a starting point for optional conversion chains
   * or when a no-op converter is needed to maintain API consistency.
   *
   * @return a converter that returns the input unchanged
   */
  static TimezoneConverter identity() {
    return dateTime -> dateTime;
  }

  /**
   * Creates a converter from a source timezone to UTC.
   * This convenience method simplifies creating converters that
   * convert from a specific timezone to Coordinated Universal Time.
   *
   * @param fromTimezone the source timezone identifier
   * @param handler      the timezone handler to use
   * @return a converter that converts from the source timezone to UTC
   */
  static TimezoneConverter toUTC(String fromTimezone, TimeZoneHandler handler) {
    return between(fromTimezone, "UTC", handler);
  }

  /**
   * Creates a converter from UTC to a target timezone.
   * This convenience method simplifies creating converters that
   * convert from Coordinated Universal Time to a specific timezone.
   *
   * @param toTimezone the target timezone identifier
   * @param handler    the timezone handler to use
   * @return a converter that converts from UTC to the target timezone
   */
  static TimezoneConverter fromUTC(String toTimezone, TimeZoneHandler handler) {
    return between("UTC", toTimezone, handler);
  }

  /**
   * Creates a converter that first converts to UTC, then to the target timezone.
   * This method provides a standardized way to convert between arbitrary timezones
   * by using UTC as an intermediate step, which can help avoid ambiguities and
   * ensure consistent handling of DST transitions and other timezone peculiarities.
   *
   * @param fromTimezone the source timezone identifier
   * @param toTimezone   the target timezone identifier
   * @param handler      the timezone handler to use
   * @return a converter that converts from source to target via UTC
   */
  static TimezoneConverter viaUTC(String fromTimezone, String toTimezone, TimeZoneHandler handler) {
    return toUTC(fromTimezone, handler).andThen(fromUTC(toTimezone, handler));
  }
}
