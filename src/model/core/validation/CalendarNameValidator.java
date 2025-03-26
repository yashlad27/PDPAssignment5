package model.core.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility class for validating calendar names.
 * Maintains a set of existing calendar names to prevent duplicates.
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
    if (name == null) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }

    String trimmedName = name.trim();
    if (trimmedName.isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    // Remove quotes and validate characters in a single pass
    String processedName = trimmedName;
    if (trimmedName.startsWith("\"") && trimmedName.endsWith("\"")) {
      processedName = trimmedName.substring(1, trimmedName.length() - 1);
    } else if (trimmedName.startsWith("'") && trimmedName.endsWith("'")) {
      processedName = trimmedName.substring(1, trimmedName.length() - 1);
    }

    // Check for valid characters
    for (char ch : processedName.toCharArray()) {
      if (!Character.isLetterOrDigit(ch) && ch != '_') {
        throw new IllegalArgumentException("Invalid calendar name");
      }
    }

    // Check for uniqueness
    if (existingNames.contains(processedName)) {
      throw new IllegalArgumentException("Calendar name must be unique");
    }

    existingNames.add(processedName);
  }

  /**
   * Removes a calendar name from the set of existing names.
   *
   * @param name the calendar name to remove
   */
  public static void removeCalendarName(String name) {
    existingNames.remove(name != null ? name.trim() : null);
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

  public static void removeAllCalendarNames() {
    existingNames.clear();
  }
}
