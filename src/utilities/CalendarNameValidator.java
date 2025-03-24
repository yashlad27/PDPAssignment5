package utilities;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility class for validating calendar names.
 */
public class CalendarNameValidator {
  private static final Set<String> existingNames = new HashSet<>();

  /**
   * Validates a calendar name according to the specified rules.
   *
   * @param name the calendar name to validate
   * @throws IllegalArgumentException if the name is invalid
   */
  public static void validateCalendarName(String name) {
    Stream.of(name)
            .peek(n -> {
              if (n == null) {
                throw new IllegalArgumentException("Calendar name cannot be null");
              }
            })
            .map(String::trim)
            .map(n -> n.startsWith("\"") && n.endsWith("\"") ? n.substring(1, n.length() - 1) : n)
            .map(n -> n.startsWith("'") && n.endsWith("'") ? n.substring(1, n.length() - 1) : n)
            .peek(n -> {
              if (n.isEmpty()) {
                throw new IllegalArgumentException("Calendar name cannot be empty");
              }
            })
            .peek(n -> {
              if (n.chars()
                      .mapToObj(ch -> (char) ch)
                      .anyMatch(ch -> !Character.isLetterOrDigit(ch) && ch != '_')) {
                throw new IllegalArgumentException("Invalid calendar name");
              }
            })
            .peek(n -> {
              if (existingNames.stream().anyMatch(existing -> existing.equals(n))) {
                throw new IllegalArgumentException("Calendar name must be unique");
              }
            })
            .peek(existingNames::add)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Calendar name validation failed"));
  }

  /**
   * Removes a calendar name from the set of existing names.
   *
   * @param name the calendar name to remove
   */
  public static void removeCalendarName(String name) {
    if (name != null) {
      existingNames.remove(name.trim());
    }
  }

  /**
   * Checks if a calendar name exists.
   *
   * @param name the calendar name to check
   * @return true if the name exists, false otherwise
   */
  public static boolean hasCalendarName(String name) {
    return name != null && existingNames.contains(name.trim());
  }

  /**
   * Clears all existing calendar names.
   * This method should be called before running tests to ensure a clean state.
   */
  public static void clear() {
    existingNames.clear();
  }

  public static void removeAllCalendarNames() {
    existingNames.clear();
  }
}
