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
   * @param dateTime the LocalDateTime to convert
   * @param fromTimezone the source timezone
   * @param toTimezone the target timezone
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
   * Get all available timezone IDs.
   *
   * @return a set of all available timezone IDs
   */
  public Set<String> getAvailableTimezones() {
    return ZoneId.getAvailableZoneIds();
  }

  /**
   * Get common timezone IDs for user interface display.
   *
   * @return a set of common timezone IDs
   */
  public Set<String> getCommonTimezones() {
    Set<String> commonTimezones = new HashSet<>();

    // North America
    commonTimezones.add("America/New_York");     // Eastern Time
    commonTimezones.add("America/Chicago");      // Central Time
    commonTimezones.add("America/Denver");       // Mountain Time
    commonTimezones.add("America/Los_Angeles");  // Pacific Time
    commonTimezones.add("America/Anchorage");    // Alaska Time
    commonTimezones.add("Pacific/Honolulu");     // Hawaii Time

    // Europe
    commonTimezones.add("Europe/London");        // GMT/BST
    commonTimezones.add("Europe/Paris");         // Central European Time
    commonTimezones.add("Europe/Helsinki");      // Eastern European Time

    // Asia
    commonTimezones.add("Asia/Tokyo");           // Japan Standard Time
    commonTimezones.add("Asia/Shanghai");        // China Standard Time
    commonTimezones.add("Asia/Kolkata");         // India Standard Time
    commonTimezones.add("Asia/Dubai");           // Gulf Standard Time

    // Australia/Oceania
    commonTimezones.add("Australia/Sydney");     // Australian Eastern Time
    commonTimezones.add("Pacific/Auckland");     // New Zealand Time

    // South America
    commonTimezones.add("America/Sao_Paulo");    // Brasilia Time

    // Africa
    commonTimezones.add("Africa/Cairo");         // Eastern European Time
    commonTimezones.add("Africa/Johannesburg");  // South African Standard Time

    return commonTimezones;
  }

  /**
   * Get a timezone converter for converting between two timezones.
   *
   * @param fromTimezone the source timezone
   * @param toTimezone the target timezone
   * @return a TimezoneConverter for the specified conversion
   */
  public TimezoneConverter getConverter(String fromTimezone, String toTimezone) {
    return TimezoneConverter.between(fromTimezone, toTimezone, this);
  }
}