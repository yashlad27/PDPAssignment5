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
        
        // Trim any quotes, whitespace, and normalize spaces
        name = name.trim()
                  .replaceAll("^['\"]|['\"]$", "")  // Remove leading/trailing quotes
                  .replaceAll("\\s+", " ")          // Replace multiple spaces with single space
                  .trim();                          // Trim again after space normalization
        
        // Check if name is empty after trimming
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Calendar name cannot be empty");
        }
        
        // Check if name already exists (after normalization)
        if (existingNames.contains(name)) {
            throw new IllegalArgumentException("Calendar name must be unique");
        }
        
        // Check if name contains numbers
        if (name.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Calendar name cannot contain numbers");
        }
        
        // Check if name contains special characters (except spaces)
        if (name.matches(".*[^a-zA-Z\\s].*")) {
            throw new IllegalArgumentException("Calendar name cannot contain special characters");
        }
        
        // If all validations pass, add to existing names
        existingNames.add(name);
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
        if (name == null) {
            return false;
        }
        return existingNames.contains(name.trim());
    }
}
