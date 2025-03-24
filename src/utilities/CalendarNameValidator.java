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
    public static String validateCalendarName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Calendar name cannot be null");
        }
        
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Calendar name cannot be empty");
        }
        
        if (name.matches(".*[0-9@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Invalid calendar name");
        }
        
        String normalizedName = name.trim();
        if (existingNames.contains(normalizedName)) {
            throw new IllegalArgumentException("Calendar name must be unique");
        }
        
        existingNames.add(normalizedName);
        return normalizedName;
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
}
