import org.junit.Test;

import utilities.CalendarNameValidator;

import static org.junit.Assert.*;

public class CalendarNameValidatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testValidateNullName() {
        CalendarNameValidator.validateCalendarName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateEmptyName() {
        CalendarNameValidator.validateCalendarName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateNameWithNumbers() {
        CalendarNameValidator.validateCalendarName("Calendar123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateNameWithSpecialCharacters() {
        CalendarNameValidator.validateCalendarName("Calendar@#$");
    }

    @Test
    public void testValidateNameWithQuotes() {
        // Should pass as quotes are trimmed
        CalendarNameValidator.validateCalendarName("\"My Calendar\"");
        assertTrue(CalendarNameValidator.hasCalendarName("My Calendar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDuplicateName() {
        CalendarNameValidator.validateCalendarName("Work");
        CalendarNameValidator.validateCalendarName("Work");
    }

    @Test
    public void testValidateValidName() {
        CalendarNameValidator.validateCalendarName("Work Calendar");
        assertTrue(CalendarNameValidator.hasCalendarName("Work Calendar"));
    }

    @Test
    public void testValidateNameWithExtraSpaces() {
        CalendarNameValidator.validateCalendarName("   new Calendar  ");
        assertTrue(CalendarNameValidator.hasCalendarName("new Calendar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateNameWithMixedQuotes() {
        CalendarNameValidator.validateCalendarName("\"Work Calendar'");
    }

    @Test
    public void testRemoveCalendarName() {
        // First add a name
        CalendarNameValidator.validateCalendarName("Test Calendar");
        assertTrue(CalendarNameValidator.hasCalendarName("Test Calendar"));
        
        // Remove the name
        CalendarNameValidator.removeCalendarName("Test Calendar");
        assertFalse(CalendarNameValidator.hasCalendarName("Test Calendar"));
    }

    @Test
    public void testHasCalendarName() {
        // Initially should be false
        assertFalse(CalendarNameValidator.hasCalendarName("New Calendar"));
        
        // Add a name
        CalendarNameValidator.validateCalendarName("New Calendar");
        assertTrue(CalendarNameValidator.hasCalendarName("New Calendar"));
    }
}