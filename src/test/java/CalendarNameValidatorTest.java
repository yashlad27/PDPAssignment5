import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.core.validation.CalendarNameValidator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CalendarNameValidatorTest {

  private CalendarNameValidator validator;

  @Before
  public void setUp() {
    validator = new CalendarNameValidator();
  }

  @After
  public void tearDown() {
    validator = null;
    // Clear the static usedNames set to prevent test interference
    CalendarNameValidator.removeAllCalendarNames();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNullName() {
    CalendarNameValidator.validateCalendarName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEmptyName() {
    CalendarNameValidator.validateCalendarName("");
  }

  @Test
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
    CalendarNameValidator.validateCalendarName("\"MyCalendar\"");
    assertTrue(CalendarNameValidator.hasCalendarName("MyCalendar"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateDuplicateName() {
    CalendarNameValidator.validateCalendarName("Work");
    CalendarNameValidator.validateCalendarName("Work");
  }

  @Test
  public void testValidateValidName() {
    CalendarNameValidator.validateCalendarName("newWork");
    assertTrue(CalendarNameValidator.hasCalendarName("newWork"));
  }

  @Test
  public void testValidateNameWithExtraSpaces() {
    CalendarNameValidator.validateCalendarName("   newCalendar  ");
    assertTrue(CalendarNameValidator.hasCalendarName("newCalendar"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNameWithMixedQuotes() {
    CalendarNameValidator.validateCalendarName("\"WorkCalendar'");
  }

  @Test
  public void testRemoveCalendarName() {
    // First add a name
    CalendarNameValidator.validateCalendarName("TestCalendar");
    assertTrue(CalendarNameValidator.hasCalendarName("TestCalendar"));

    // Remove the name
    CalendarNameValidator.removeCalendarName("TestCalendar");
    assertFalse(CalendarNameValidator.hasCalendarName("TestCalendar"));
  }

  @Test
  public void testHasCalendarName() {
    // Initially should be false
    assertFalse(CalendarNameValidator.hasCalendarName("New Calendar"));

    // Add a name
    CalendarNameValidator.validateCalendarName("NewCalendar");
    assertTrue(CalendarNameValidator.hasCalendarName("NewCalendar"));
  }

  @Test
  public void testValidateNameWithMixedCase() {
    CalendarNameValidator.validateCalendarName("MyCalendar123");
    assertTrue(CalendarNameValidator.hasCalendarName("MyCalendar123"));
  }

  @Test
  public void testValidateNameWithUnderscores() {
    CalendarNameValidator.validateCalendarName("My_Calendar_123");
    assertTrue(CalendarNameValidator.hasCalendarName("My_Calendar_123"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNameWithSpaces() {
    CalendarNameValidator.validateCalendarName("My Calendar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNameWithHyphens() {
    CalendarNameValidator.validateCalendarName("My-Calendar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNameWithPeriods() {
    CalendarNameValidator.validateCalendarName("My.Calendar");
  }

  @Test
  public void testValidateNameWithMaximumLength() {
    String longName = "A".repeat(100);
    CalendarNameValidator.validateCalendarName(longName);
    assertTrue(CalendarNameValidator.hasCalendarName(longName));
  }

  @Test
  public void testValidateNameWithLeadingUnderscore() {
    CalendarNameValidator.validateCalendarName("_MyCalendar");
    assertTrue(CalendarNameValidator.hasCalendarName("_MyCalendar"));
  }

  @Test
  public void testValidateNameWithTrailingUnderscore() {
    CalendarNameValidator.validateCalendarName("MyCalendar_");
    assertTrue(CalendarNameValidator.hasCalendarName("MyCalendar_"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNameWithMultipleQuotes() {
    CalendarNameValidator.validateCalendarName("\"My\"Calendar\"");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNameWithMismatchedQuotes() {
    CalendarNameValidator.validateCalendarName("\"MyCalendar'");
  }

  @Test
  public void testValidateNameWithSingleQuote() {
    CalendarNameValidator.validateCalendarName("'MyCalendar'");
    assertTrue(CalendarNameValidator.hasCalendarName("MyCalendar"));
  }

  @Test
  public void testValidateNameWithDoubleQuote() {
    CalendarNameValidator.validateCalendarName("\"MyCalendar\"");
    assertTrue(CalendarNameValidator.hasCalendarName("MyCalendar"));
  }

  @Test
  public void testValidateNameWithMixedQuotesAndSpaces() {
    CalendarNameValidator.validateCalendarName("  \"MyCalendar\"  ");
    assertTrue(CalendarNameValidator.hasCalendarName("MyCalendar"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNameWithWhitespaceCharacters() {
    CalendarNameValidator.validateCalendarName("My Calendar\t");
  }
}