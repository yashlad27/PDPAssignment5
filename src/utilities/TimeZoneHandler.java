package utilities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Handles timezone operations for the calendar application.
 * Provides methods for converting times between different timezones
 * and validating timezone formats.
 */
public class TimeZoneHandler {

  private static final String DEFAULT_TIMEZONE = "America/New_York";

  /**
   * Validates if the provided timezone string is valid.
   * Checks if the timezone is a valid IANA timezone identifier.
   *
   * @param timezone the timezone to validate
   * @return true if valid, false otherwise
   */
  public boolean isValidTimezone(String timezone) {
    if (timezone == null || timezone.trim().isEmpty()) {
      return false;
    }

    try {
      ZoneId.of(timezone);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets the default timezone.
   *
   * @return the default timezone string
   */
  public String getDefaultTimezone() {
    return DEFAULT_TIMEZONE;
  }

  /**
   * Converts a LocalDateTime from one timezone to another.
   *
   * @param dateTime     the LocalDateTime to convert
   * @param fromTimezone the source timezone
   * @param toTimezone   the target timezone
   * @return the converted LocalDateTime
   * @throws IllegalArgumentException if parameters are invalid
   */
  public LocalDateTime convertTime(LocalDateTime dateTime, String fromTimezone, String toTimezone) {
    if (dateTime == null || !isValidTimezone(fromTimezone) || !isValidTimezone(toTimezone)) {
      throw new IllegalArgumentException("Invalid parameters for time conversion");
    }

    ZonedDateTime sourceZoned = dateTime.atZone(ZoneId.of(fromTimezone));
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(ZoneId.of(toTimezone));
    return targetZoned.toLocalDateTime();
  }

  /**
   * Get a timezone converter for converting between two timezones.
   *
   * @param fromTimezone the source timezone
   * @param toTimezone   the target timezone
   * @return a TimezoneConverter for the specified conversion
   */
  public TimezoneConverter getConverter(String fromTimezone, String toTimezone) {
    return TimezoneConverter.between(fromTimezone, toTimezone, this);
  }
}