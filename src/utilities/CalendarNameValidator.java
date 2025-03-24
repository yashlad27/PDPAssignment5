package utilities;

import java.util.Set;
import java.util.HashSet;

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
        if (trimmedName.startsWith("\"") && trimmedName.endsWith("\"")) {
            trimmedName = trimmedName.substring(1, trimmedName.length() - 1);
        } else if (trimmedName.startsWith("'") && trimmedName.endsWith("'")) {
            trimmedName = trimmedName.substring(1, trimmedName.length() - 1);
        }
        
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Calendar name cannot be empty");
        }
        
        if (trimmedName.matches(".*[0-9@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?\\s].*")) {
            throw new IllegalArgumentException("Invalid calendar name");
        }
        
        if (existingNames.contains(trimmedName)) {
            throw new IllegalArgumentException("Calendar name must be unique");
        }
        
        existingNames.add(trimmedName);
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
