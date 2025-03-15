package utilities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles timezone operations for the calendar application.
 * Provides methods for converting times between different timezones
 * and validating timezone formats.
 */
public class TimeZoneHandler {

  // Default timezone (EST)
  private static final String DEFAULT_TIMEZONE = "America/New_York";

  // Set of supported timezones
  private static final Set<String> SUPPORTED_TIMEZONES = new HashSet<>();

  // Initialize supported timezones
  static {
    SUPPORTED_TIMEZONES.add("America/New_York");
    SUPPORTED_TIMEZONES.add("America/Los_Angeles");
    SUPPORTED_TIMEZONES.add("Europe/London");
    SUPPORTED_TIMEZONES.add("Europe/Paris");
    SUPPORTED_TIMEZONES.add("Asia/Tokyo");
    SUPPORTED_TIMEZONES.add("Australia/Sydney");
    SUPPORTED_TIMEZONES.add("Africa/Cairo");
  }

  /**
   * Validates if the provided timezone string is in the correct format
   * and is supported by the application.
   *
   * @param timezone the timezone to validate
   * @return true if valid, false otherwise
   */
  public boolean isValidTimezone(String timezone) {
    if (timezone == null || timezone.trim().isEmpty()) {
      return false;
    }

    if (!SUPPORTED_TIMEZONES.contains(timezone)) {
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
   * @param dateTime the LocalDateTime to convert
   * @param fromTimezone the source timezone
   * @param toTimezone the target timezone
   * @return the converted LocalDateTime
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
   * Get all supported timezones.
   *
   * @return a set of all supported timezone strings
   */
  public Set<String> getSupportedTimezones() {
    return new HashSet<>(SUPPORTED_TIMEZONES);
  }
}