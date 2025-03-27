package utilities;

import java.util.HashSet;
import java.util.Set;

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
    if (name == null) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }

    String trimmedName = name.trim();
    String unquotedName = removeQuotes(trimmedName);

    if (unquotedName.isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    if (containsInvalidCharacters(unquotedName)) {
      throw new IllegalArgumentException("Invalid calendar name");
    }

    if (isDuplicateName(unquotedName)) {
      throw new IllegalArgumentException("Calendar name must be unique");
    }

    existingNames.add(unquotedName);
  }

  private static String removeQuotes(String name) {
    if (name.startsWith("\"") && name.endsWith("\"")) {
      return name.substring(1, name.length() - 1);
    }
    if (name.startsWith("'") && name.endsWith("'")) {
      return name.substring(1, name.length() - 1);
    }
    return name;
  }

  private static boolean containsInvalidCharacters(String name) {
    return name.chars()
            .mapToObj(ch -> (char) ch)
            .anyMatch(ch -> !Character.isLetterOrDigit(ch) && ch != '_');
  }

  private static boolean isDuplicateName(String name) {
    return existingNames.stream()
            .anyMatch(existing -> existing.equals(name));
  }

  /**
   * Removes a calendar name from the set of existing names.
   *
   * @param name the calendar name to remove
   */
  public static void removeCalendarName(String name) {
    existingNames.remove(name);
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
