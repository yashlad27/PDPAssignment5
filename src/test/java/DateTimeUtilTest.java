import org.junit.Test;

import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import utilities.DateTimeUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for DateTimeUtil.
 */
public class DateTimeUtilTest {

  @Test
  public void testParseDateValid() {
    LocalDate date = DateTimeUtil.parseDate("2023-05-15");
    assertEquals(2023, date.getYear());
    assertEquals(5, date.getMonthValue());
    assertEquals(15, date.getDayOfMonth());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateInvalidFormat() {
    DateTimeUtil.parseDate("05/15/2023");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateInvalidDate() {
    DateTimeUtil.parseDate("2023-13-45");
  }

  @Test
  public void testParseTimeValid() {
    LocalTime time = DateTimeUtil.parseTime("14:30");
    assertEquals(14, time.getHour());
    assertEquals(30, time.getMinute());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseTimeInvalidFormat() {
    DateTimeUtil.parseTime("2:30 PM");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseTimeInvalidTime() {
    DateTimeUtil.parseTime("25:70");
  }

  @Test
  public void testParseDateTimeValid() {
    LocalDateTime dateTime = DateTimeUtil.parseDateTime("2023-05-15T14:30");
    assertEquals(2023, dateTime.getYear());
    assertEquals(5, dateTime.getMonthValue());
    assertEquals(15, dateTime.getDayOfMonth());
    assertEquals(14, dateTime.getHour());
    assertEquals(30, dateTime.getMinute());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeInvalidFormat() {
    DateTimeUtil.parseDateTime("2023-05-15 14:30");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeInvalidDateTime() {
    DateTimeUtil.parseDateTime("2023-13-45T25:70");
  }

  @Test
  public void testCombineDateAndTimeValid() {
    LocalDateTime dateTime = DateTimeUtil.combineDateAndTime("2023-05-15", "14:30");
    assertEquals(2023, dateTime.getYear());
    assertEquals(5, dateTime.getMonthValue());
    assertEquals(15, dateTime.getDayOfMonth());
    assertEquals(14, dateTime.getHour());
    assertEquals(30, dateTime.getMinute());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCombineDateAndTimeInvalidDate() {
    DateTimeUtil.combineDateAndTime("05/15/2023", "14:30");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCombineDateAndTimeInvalidTime() {
    DateTimeUtil.combineDateAndTime("2023-05-15", "2:30 PM");
  }

  @Test
  public void testFormatDateValid() {
    LocalDate date = LocalDate.of(2023, 5, 15);
    String formattedDate = DateTimeUtil.formatDate(date);
    assertEquals("2023-05-15", formattedDate);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatDateNull() {
    DateTimeUtil.formatDate(null);
  }

  @Test
  public void testFormatTimeValid() {
    LocalTime time = LocalTime.of(14, 30);
    String formattedTime = DateTimeUtil.formatTime(time);
    assertEquals("14:30", formattedTime);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatTimeNull() {
    DateTimeUtil.formatTime(null);
  }

  @Test
  public void testFormatDateTimeValid() {
    LocalDateTime dateTime = LocalDateTime.of(2023, 5, 15,
            14, 30);
    String formattedDateTime = DateTimeUtil.formatDateTime(dateTime);
    assertEquals("2023-05-15T14:30", formattedDateTime);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatDateTimeNull() {
    DateTimeUtil.formatDateTime(null);
  }

  @Test
  public void testParseWeekdaysValid() {
    Set<DayOfWeek> weekdays = DateTimeUtil.parseWeekdays("MWF");
    assertEquals(3, weekdays.size());
    assertTrue(weekdays.contains(DayOfWeek.MONDAY));
    assertTrue(weekdays.contains(DayOfWeek.WEDNESDAY));
    assertTrue(weekdays.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdaysAllDays() {
    Set<DayOfWeek> weekdays = DateTimeUtil.parseWeekdays("MTWRFSU");
    assertEquals(7, weekdays.size());
    assertTrue(weekdays.contains(DayOfWeek.MONDAY));
    assertTrue(weekdays.contains(DayOfWeek.TUESDAY));
    assertTrue(weekdays.contains(DayOfWeek.WEDNESDAY));
    assertTrue(weekdays.contains(DayOfWeek.THURSDAY));
    assertTrue(weekdays.contains(DayOfWeek.FRIDAY));
    assertTrue(weekdays.contains(DayOfWeek.SATURDAY));
    assertTrue(weekdays.contains(DayOfWeek.SUNDAY));
  }

  @Test
  public void testParseWeekdaysLowercase() {
    Set<DayOfWeek> weekdays = DateTimeUtil.parseWeekdays("mtwrfsu");
    assertEquals(7, weekdays.size());
    assertTrue(weekdays.contains(DayOfWeek.MONDAY));
    assertTrue(weekdays.contains(DayOfWeek.TUESDAY));
    assertTrue(weekdays.contains(DayOfWeek.WEDNESDAY));
    assertTrue(weekdays.contains(DayOfWeek.THURSDAY));
    assertTrue(weekdays.contains(DayOfWeek.FRIDAY));
    assertTrue(weekdays.contains(DayOfWeek.SATURDAY));
    assertTrue(weekdays.contains(DayOfWeek.SUNDAY));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysEmptyString() {
    DateTimeUtil.parseWeekdays("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysNull() {
    DateTimeUtil.parseWeekdays(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysInvalidCharacter() {
    DateTimeUtil.parseWeekdays("MWFZ");
  }

  @Test
  public void testFormatWeekdaysValid() {
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    String formattedWeekdays = DateTimeUtil.formatWeekdays(weekdays);
    assertTrue(formattedWeekdays.contains("M"));
    assertTrue(formattedWeekdays.contains("W"));
    assertTrue(formattedWeekdays.contains("F"));
    assertEquals(3, formattedWeekdays.length());
  }

  @Test
  public void testFormatWeekdaysAllDays() {
    Set<DayOfWeek> weekdays = EnumSet.allOf(DayOfWeek.class);
    String formattedWeekdays = DateTimeUtil.formatWeekdays(weekdays);
    assertEquals(7, formattedWeekdays.length());
    assertTrue(formattedWeekdays.contains("M"));
    assertTrue(formattedWeekdays.contains("T"));
    assertTrue(formattedWeekdays.contains("W"));
    assertTrue(formattedWeekdays.contains("R"));
    assertTrue(formattedWeekdays.contains("F"));
    assertTrue(formattedWeekdays.contains("S"));
    assertTrue(formattedWeekdays.contains("U"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatWeekdaysEmptySet() {
    DateTimeUtil.formatWeekdays(EnumSet.noneOf(DayOfWeek.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatWeekdaysNull() {
    DateTimeUtil.formatWeekdays(null);
  }

  @Test(expected = AssertionError.class)
  public void testConstructorShouldNotBeInstantiated() {
    try {
      Constructor<DateTimeUtil> constructor = DateTimeUtil.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    } catch (ReflectiveOperationException e) {
      if (e.getCause() instanceof AssertionError) {
        throw (AssertionError) e.getCause();
      }
      fail("Expected AssertionError but got: " + e);
    }
  }

  @Test
  public void testFormatWeekdaysOrder() {
    // test that the formatted weekdays are in the correct order: M, T, W, R, F, S, U
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.SUNDAY,   // U
            DayOfWeek.FRIDAY,   // F
            DayOfWeek.WEDNESDAY, // W
            DayOfWeek.MONDAY    // M
    );

    String formattedWeekdays = DateTimeUtil.formatWeekdays(weekdays);

    int posM = formattedWeekdays.indexOf('M');
    int posF = formattedWeekdays.indexOf('F');
    int posW = formattedWeekdays.indexOf('W');
    int posU = formattedWeekdays.indexOf('U');

    assertTrue(posM >= 0);
    assertTrue(posF >= 0);
    assertTrue(posW >= 0);
    assertTrue(posU >= 0);

    String resultOrder = "";
    for (char c : formattedWeekdays.toCharArray()) {
      resultOrder += c;
    }

    assertTrue("M should come before W", posM < posW);
    assertTrue("W should come before F", posW < posF);
    assertTrue("F should come before U", posF < posU);
  }

  /**
   * Test parsing weekdays with duplicated characters.
   */
  @Test
  public void testParseWeekdaysDuplicateCharacters() {
    Set<DayOfWeek> weekdays = DateTimeUtil.parseWeekdays("MMWFF");

    // Should still have 3 unique days
    assertEquals(3, weekdays.size());
    assertTrue(weekdays.contains(DayOfWeek.MONDAY));
    assertTrue(weekdays.contains(DayOfWeek.WEDNESDAY));
    assertTrue(weekdays.contains(DayOfWeek.FRIDAY));
  }

  /**
   * Test parsing weekdays with mixed case characters.
   */
  @Test
  public void testParseWeekdaysMixedCase() {
    Set<DayOfWeek> weekdays = DateTimeUtil.parseWeekdays("MwF");

    assertEquals(3, weekdays.size());
    assertTrue(weekdays.contains(DayOfWeek.MONDAY));
    assertTrue(weekdays.contains(DayOfWeek.WEDNESDAY));
    assertTrue(weekdays.contains(DayOfWeek.FRIDAY));
  }

  /**
   * Test that parseWeekdays handles whitespace correctly.
   */
  @Test
  public void testParseWeekdaysWithWhitespace() {
    // This should throw an exception because it contains a space
    try {
      DateTimeUtil.parseWeekdays("M W F");
      fail("Expected IllegalArgumentException but no exception was thrown");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }

    // Whitespace at the start/end should also throw an exception
    try {
      DateTimeUtil.parseWeekdays(" MWF");
      fail("Expected IllegalArgumentException but no exception was thrown");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
  }

  /**
   * Test boundary cases for date parsing.
   */
  @Test
  public void testParseDateBoundaryValues() {
    // Test minimum date
    LocalDate minDate = DateTimeUtil.parseDate("0001-01-01");
    assertEquals(1, minDate.getYear());
    assertEquals(1, minDate.getMonthValue());
    assertEquals(1, minDate.getDayOfMonth());

    // Test maximum date that's reasonably supported
    LocalDate maxDate = DateTimeUtil.parseDate("9999-12-31");
    assertEquals(9999, maxDate.getYear());
    assertEquals(12, maxDate.getMonthValue());
    assertEquals(31, maxDate.getDayOfMonth());
  }

  /**
   * Test boundary cases for time parsing.
   */
  @Test
  public void testParseTimeBoundaryValues() {
    // Test minimum time
    LocalTime minTime = DateTimeUtil.parseTime("00:00");
    assertEquals(0, minTime.getHour());
    assertEquals(0, minTime.getMinute());

    // Test maximum time
    LocalTime maxTime = DateTimeUtil.parseTime("23:59");
    assertEquals(23, maxTime.getHour());
    assertEquals(59, maxTime.getMinute());
  }

  /**
   * Test that the formatWeekdays method consistently handles the ordering.
   */
  @Test
  public void testFormatWeekdaysConsistentOrder() {
    // Test with different subsets and ordering 
    Set<DayOfWeek> weekdays1 = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    String result1 = DateTimeUtil.formatWeekdays(weekdays1);
    assertEquals("MWF", result1);

    Set<DayOfWeek> weekdays2 = EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.MONDAY);
    String result2 = DateTimeUtil.formatWeekdays(weekdays2);
    assertEquals("MWF", result2);

    // Verify results are consistent
    assertEquals(result1, result2);
  }

  /**
   * Test formatting dates with single-digit month and day values.
   */
  @Test
  public void testFormatDateSingleDigits() {
    LocalDate date = LocalDate.of(2023, 1, 2); // January 2
    String formatted = DateTimeUtil.formatDate(date);
    assertEquals("2023-01-02", formatted); // Should have leading zeros
  }

  /**
   * Test that parseDateTime and combineDateAndTime produce the same result.
   */
  @Test
  public void testParseDateTimeEquivalence() {
    LocalDateTime dateTime1 = DateTimeUtil.parseDateTime("2023-05-15T14:30");
    LocalDateTime dateTime2 = DateTimeUtil.combineDateAndTime("2023-05-15", "14:30");

    assertEquals(dateTime1, dateTime2);
  }

  /**
   * Test special case where a single weekday is provided.
   */
  @Test
  public void testParseWeekdaysSingleDay() {
    // Test each day of the week individually
    Set<DayOfWeek> monday = DateTimeUtil.parseWeekdays("M");
    assertEquals(1, monday.size());
    assertTrue(monday.contains(DayOfWeek.MONDAY));

    Set<DayOfWeek> tuesday = DateTimeUtil.parseWeekdays("T");
    assertEquals(1, tuesday.size());
    assertTrue(tuesday.contains(DayOfWeek.TUESDAY));

    Set<DayOfWeek> wednesday = DateTimeUtil.parseWeekdays("W");
    assertEquals(1, wednesday.size());
    assertTrue(wednesday.contains(DayOfWeek.WEDNESDAY));

    Set<DayOfWeek> thursday = DateTimeUtil.parseWeekdays("R");
    assertEquals(1, thursday.size());
    assertTrue(thursday.contains(DayOfWeek.THURSDAY));

    Set<DayOfWeek> friday = DateTimeUtil.parseWeekdays("F");
    assertEquals(1, friday.size());
    assertTrue(friday.contains(DayOfWeek.FRIDAY));

    Set<DayOfWeek> saturday = DateTimeUtil.parseWeekdays("S");
    assertEquals(1, saturday.size());
    assertTrue(saturday.contains(DayOfWeek.SATURDAY));

    Set<DayOfWeek> sunday = DateTimeUtil.parseWeekdays("U");
    assertEquals(1, sunday.size());
    assertTrue(sunday.contains(DayOfWeek.SUNDAY));
  }
}