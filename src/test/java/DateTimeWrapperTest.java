import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import model.core.datetime.DateTimeWrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for DateTimeWrapper.
 * This class tests the functionality of the DateTimeWrapper class,
 * which wraps a LocalDateTime with timezone information for comparison
 * and conversion purposes.
 */
public class DateTimeWrapperTest {

  private static final String UTC = "UTC";
  private static final String NEW_YORK = "America/New_York";
  private static final String TOKYO = "Asia/Tokyo";
  private static final String PARIS = "Europe/Paris";
  private static final String INVALID_TIMEZONE = "Invalid/Timezone";

  private LocalDateTime referenceDateTime;
  private DateTimeWrapper utcWrapper;

  @Before
  public void setUp() {
    referenceDateTime = LocalDateTime.of(2023, 10, 15, 12, 0); // 2023-10-15 12:00
    utcWrapper = new DateTimeWrapper(referenceDateTime, UTC);
  }

  /**
   * Test constructor with valid parameters.
   */
  @Test
  public void testConstructorWithValidParameters() {
    DateTimeWrapper wrapper = new DateTimeWrapper(referenceDateTime, NEW_YORK);
    assertNotNull(wrapper);
  }

  /**
   * Test constructor with null LocalDateTime.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullDateTime() {
    new DateTimeWrapper(null, UTC);
  }

  /**
   * Test constructor with null timezone.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullTimezone() {
    new DateTimeWrapper(referenceDateTime, null);
  }

  /**
   * Test constructor with empty timezone.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptyTimezone() {
    new DateTimeWrapper(referenceDateTime, "");
  }

  /**
   * Test constructor with invalid timezone.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithInvalidTimezone() {
    new DateTimeWrapper(referenceDateTime, INVALID_TIMEZONE);
  }

  /**
   * Test equals method for same date and timezone.
   */
  @Test
  public void testEqualsWithSameDateTimeAndTimezone() {
    DateTimeWrapper wrapper1 = new DateTimeWrapper(referenceDateTime, UTC);
    DateTimeWrapper wrapper2 = new DateTimeWrapper(referenceDateTime, UTC);
    assertEquals(wrapper1, wrapper2);
  }

  /**
   * Test equals method for equivalent date-times in different timezones.
   */
  @Test
  public void testEqualsWithEquivalentDateTimeInDifferentTimezones() {
    // Create a time in New York that is equivalent to the reference time in UTC
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime nyZoned = utcZoned.withZoneSameInstant(ZoneId.of(NEW_YORK));
    LocalDateTime nyLocalDateTime = nyZoned.toLocalDateTime();

    DateTimeWrapper wrapper1 = new DateTimeWrapper(referenceDateTime, UTC);
    DateTimeWrapper wrapper2 = new DateTimeWrapper(nyLocalDateTime, NEW_YORK);

    assertEquals(wrapper1, wrapper2);
  }

  /**
   * Test equals method for different date-times.
   */
  @Test
  public void testEqualsWithDifferentDateTime() {
    LocalDateTime differentDateTime = referenceDateTime.plusHours(1);
    DateTimeWrapper wrapper1 = new DateTimeWrapper(referenceDateTime, UTC);
    DateTimeWrapper wrapper2 = new DateTimeWrapper(differentDateTime, UTC);

    assertNotEquals(wrapper1, wrapper2);
  }

  /**
   * Test equals method with non-DateTimeWrapper object.
   */
  @Test
  public void testEqualsWithNonDateTimeWrapper() {
    DateTimeWrapper wrapper = new DateTimeWrapper(referenceDateTime, UTC);
    assertNotEquals(wrapper, "not a wrapper");
  }

  /**
   * Test equals method with null.
   */
  @Test
  public void testEqualsWithNull() {
    DateTimeWrapper wrapper = new DateTimeWrapper(referenceDateTime, UTC);
    assertNotEquals(wrapper, null);
  }

  /**
   * Test hashCode consistency with equals.
   */
  @Test
  public void testHashCodeConsistency() {
    DateTimeWrapper wrapper1 = new DateTimeWrapper(referenceDateTime, UTC);
    DateTimeWrapper wrapper2 = new DateTimeWrapper(referenceDateTime, UTC);

    assertEquals(wrapper1, wrapper2);
    assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
  }

  /**
   * Test hashCode with equivalent date-times in different timezones.
   */
  @Test
  public void testHashCodeWithEquivalentDateTimes() {
    // Create a time in New York that is equivalent to the reference time in UTC
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime nyZoned = utcZoned.withZoneSameInstant(ZoneId.of(NEW_YORK));
    LocalDateTime nyLocalDateTime = nyZoned.toLocalDateTime();

    DateTimeWrapper wrapper1 = new DateTimeWrapper(referenceDateTime, UTC);
    DateTimeWrapper wrapper2 = new DateTimeWrapper(nyLocalDateTime, NEW_YORK);

    assertEquals(wrapper1, wrapper2);
    assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
  }

  /**
   * Test equality of an earlier date-time.
   */
  @Test
  public void testEqualityWithEarlierDateTime() {
    LocalDateTime earlierDateTime = referenceDateTime.minusHours(1);
    DateTimeWrapper wrapper1 = new DateTimeWrapper(earlierDateTime, UTC);
    DateTimeWrapper wrapper2 = new DateTimeWrapper(referenceDateTime, UTC);

    assertNotEquals(wrapper1, wrapper2);
  }

  /**
   * Test equality of a later date-time.
   */
  @Test
  public void testEqualityWithLaterDateTime() {
    LocalDateTime laterDateTime = referenceDateTime.plusHours(1);
    DateTimeWrapper wrapper1 = new DateTimeWrapper(laterDateTime, UTC);
    DateTimeWrapper wrapper2 = new DateTimeWrapper(referenceDateTime, UTC);

    assertNotEquals(wrapper1, wrapper2);
  }

  /**
   * Test equality of times across Daylight Saving Time change.
   */
  @Test
  public void testEqualityAcrossDSTChange() {
    // March 13, 2022 at 1am in America/New_York (before DST change)
    LocalDateTime beforeDST = LocalDateTime.of(2022, 3, 13, 1, 0);
    // March 13, 2022 at 3am in America/New_York (after DST change)
    LocalDateTime afterDST = LocalDateTime.of(2022, 3, 13, 3, 0);

    DateTimeWrapper wrapperBefore = new DateTimeWrapper(beforeDST, NEW_YORK);
    DateTimeWrapper wrapperAfter = new DateTimeWrapper(afterDST, NEW_YORK);

    assertNotEquals(wrapperBefore, wrapperAfter);
  }

  /**
   * Test toString method.
   */
  @Test
  public void testToString() {
    DateTimeWrapper wrapper = new DateTimeWrapper(referenceDateTime, NEW_YORK);
    String result = wrapper.toString();

    assertTrue(result.contains(referenceDateTime.toString()));
    assertTrue(result.contains(NEW_YORK));
  }

  /**
   * Test creating a wrapper with a specific formatted string.
   */
  @Test
  public void testCreateWithFormattedString() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    LocalDateTime dateTime = LocalDateTime.parse("2023-10-15 12:00", formatter);
    DateTimeWrapper wrapper = new DateTimeWrapper(dateTime, UTC);
    
    assertNotNull(wrapper);
  }
  
  // Additional test cases
  
  /**
   * Test constructor with ZonedDateTime.
   */
  @Test
  public void testConstructorWithZonedDateTime() {
    ZonedDateTime zonedDateTime = referenceDateTime.atZone(ZoneId.of(UTC));
    DateTimeWrapper wrapper = new DateTimeWrapper(zonedDateTime);
    
    assertNotNull(wrapper);
    assertEquals(referenceDateTime, wrapper.toLocalDateTime());
  }
  
  /**
   * Test constructor with null ZonedDateTime.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullZonedDateTime() {
    ZonedDateTime nullZonedDateTime = null;
    new DateTimeWrapper(nullZonedDateTime);
  }
  
  /**
   * Test toLocalDateTime method.
   */
  @Test
  public void testToLocalDateTime() {
    DateTimeWrapper wrapper = new DateTimeWrapper(referenceDateTime, UTC);
    assertEquals(referenceDateTime, wrapper.toLocalDateTime());
  }
  
  /**
   * Test toZonedDateTime method.
   */
  @Test
  public void testToZonedDateTime() {
    DateTimeWrapper wrapper = new DateTimeWrapper(referenceDateTime, UTC);
    ZonedDateTime expected = referenceDateTime.atZone(ZoneId.of(UTC));
    assertEquals(expected, wrapper.toZonedDateTime());
  }
  
  /**
   * Test convertToTimezone method.
   */
  @Test
  public void testConvertToTimezone() {
    DateTimeWrapper utcWrapper = new DateTimeWrapper(referenceDateTime, UTC);
    DateTimeWrapper tokyoWrapper = utcWrapper.convertToTimezone(TOKYO);
    
    assertEquals(utcWrapper, tokyoWrapper); // Same instant, different zone
    assertEquals(ZoneId.of(TOKYO), tokyoWrapper.toZonedDateTime().getZone());
  }
  
  /**
   * Test convertToTimezone with null timezone.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConvertToTimezoneWithNullTimezone() {
    utcWrapper.convertToTimezone(null);
  }
  
  /**
   * Test convertToTimezone with empty timezone.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConvertToTimezoneWithEmptyTimezone() {
    utcWrapper.convertToTimezone("");
  }
  
  /**
   * Test convertToTimezone with invalid timezone.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConvertToTimezoneWithInvalidTimezone() {
    utcWrapper.convertToTimezone(INVALID_TIMEZONE);
  }
  
  /**
   * Test isBefore method with earlier time.
   */
  @Test
  public void testIsBeforeWithEarlierTime() {
    LocalDateTime earlierTime = referenceDateTime.minusHours(1);
    DateTimeWrapper earlierWrapper = new DateTimeWrapper(earlierTime, UTC);
    
    assertTrue(earlierWrapper.isBefore(utcWrapper));
    assertFalse(utcWrapper.isBefore(earlierWrapper));
  }
  
  /**
   * Test isBefore method with same time.
   */
  @Test
  public void testIsBeforeWithSameTime() {
    DateTimeWrapper sameWrapper = new DateTimeWrapper(referenceDateTime, UTC);
    
    assertFalse(sameWrapper.isBefore(utcWrapper));
    assertFalse(utcWrapper.isBefore(sameWrapper));
  }
  
  /**
   * Test isBefore method with different timezones.
   */
  @Test
  public void testIsBeforeWithDifferentTimezones() {
    // Same instant but different timezone
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime tokyoZoned = utcZoned.withZoneSameInstant(ZoneId.of(TOKYO));
    LocalDateTime tokyoLocalDateTime = tokyoZoned.toLocalDateTime();
    
    DateTimeWrapper tokyoWrapper = new DateTimeWrapper(tokyoLocalDateTime, TOKYO);
    
    assertFalse(utcWrapper.isBefore(tokyoWrapper));
    assertFalse(tokyoWrapper.isBefore(utcWrapper));
  }
  
  /**
   * Test isAfter method with later time.
   */
  @Test
  public void testIsAfterWithLaterTime() {
    LocalDateTime laterTime = referenceDateTime.plusHours(1);
    DateTimeWrapper laterWrapper = new DateTimeWrapper(laterTime, UTC);
    
    assertTrue(laterWrapper.isAfter(utcWrapper));
    assertFalse(utcWrapper.isAfter(laterWrapper));
  }
  
  /**
   * Test isAfter method with same time.
   */
  @Test
  public void testIsAfterWithSameTime() {
    DateTimeWrapper sameWrapper = new DateTimeWrapper(referenceDateTime, UTC);
    
    assertFalse(sameWrapper.isAfter(utcWrapper));
    assertFalse(utcWrapper.isAfter(sameWrapper));
  }
  
  /**
   * Test isAfter method with different timezones.
   */
  @Test
  public void testIsAfterWithDifferentTimezones() {
    // Same instant but different timezone
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime parisZoned = utcZoned.withZoneSameInstant(ZoneId.of(PARIS));
    LocalDateTime parisLocalDateTime = parisZoned.toLocalDateTime();
    
    DateTimeWrapper parisWrapper = new DateTimeWrapper(parisLocalDateTime, PARIS);
    
    assertFalse(utcWrapper.isAfter(parisWrapper));
    assertFalse(parisWrapper.isAfter(utcWrapper));
  }
  
  /**
   * Test overlaps method with date inside range.
   */
  @Test
  public void testOverlapsWithDateInsideRange() {
    LocalDateTime startTime = referenceDateTime.minusHours(1);
    LocalDateTime endTime = referenceDateTime.plusHours(1);
    
    DateTimeWrapper startWrapper = new DateTimeWrapper(startTime, UTC);
    DateTimeWrapper endWrapper = new DateTimeWrapper(endTime, UTC);
    
    assertTrue(utcWrapper.overlaps(startWrapper, endWrapper));
  }
  
  /**
   * Test overlaps method with date at start of range.
   */
  @Test
  public void testOverlapsWithDateAtStartOfRange() {
    LocalDateTime startTime = referenceDateTime;
    LocalDateTime endTime = referenceDateTime.plusHours(1);
    
    DateTimeWrapper startWrapper = new DateTimeWrapper(startTime, UTC);
    DateTimeWrapper endWrapper = new DateTimeWrapper(endTime, UTC);
    
    assertTrue(utcWrapper.overlaps(startWrapper, endWrapper));
  }
  
  /**
   * Test overlaps method with date at end of range.
   */
  @Test
  public void testOverlapsWithDateAtEndOfRange() {
    LocalDateTime startTime = referenceDateTime.minusHours(1);
    LocalDateTime endTime = referenceDateTime;
    
    DateTimeWrapper startWrapper = new DateTimeWrapper(startTime, UTC);
    DateTimeWrapper endWrapper = new DateTimeWrapper(endTime, UTC);
    
    assertTrue(utcWrapper.overlaps(startWrapper, endWrapper));
  }
  
  /**
   * Test overlaps method with date outside range.
   */
  @Test
  public void testOverlapsWithDateOutsideRange() {
    LocalDateTime startTime = referenceDateTime.plusHours(1);
    LocalDateTime endTime = referenceDateTime.plusHours(2);
    
    DateTimeWrapper startWrapper = new DateTimeWrapper(startTime, UTC);
    DateTimeWrapper endWrapper = new DateTimeWrapper(endTime, UTC);
    
    assertFalse(utcWrapper.overlaps(startWrapper, endWrapper));
    
    startTime = referenceDateTime.minusHours(2);
    endTime = referenceDateTime.minusHours(1);
    
    startWrapper = new DateTimeWrapper(startTime, UTC);
    endWrapper = new DateTimeWrapper(endTime, UTC);
    
    assertFalse(utcWrapper.overlaps(startWrapper, endWrapper));
  }
  
  /**
   * Test overlaps with different timezones.
   */
  @Test
  public void testOverlapsWithDifferentTimezones() {
    // Same instants but different timezones
    ZonedDateTime utcStartZoned = referenceDateTime.minusHours(1).atZone(ZoneId.of(UTC));
    ZonedDateTime tokyoStartZoned = utcStartZoned.withZoneSameInstant(ZoneId.of(TOKYO));
    LocalDateTime tokyoStartLocalDateTime = tokyoStartZoned.toLocalDateTime();
    
    ZonedDateTime utcEndZoned = referenceDateTime.plusHours(1).atZone(ZoneId.of(UTC));
    ZonedDateTime tokyoEndZoned = utcEndZoned.withZoneSameInstant(ZoneId.of(TOKYO));
    LocalDateTime tokyoEndLocalDateTime = tokyoEndZoned.toLocalDateTime();
    
    DateTimeWrapper tokyoStartWrapper = new DateTimeWrapper(tokyoStartLocalDateTime, TOKYO);
    DateTimeWrapper tokyoEndWrapper = new DateTimeWrapper(tokyoEndLocalDateTime, TOKYO);
    
    assertTrue(utcWrapper.overlaps(tokyoStartWrapper, tokyoEndWrapper));
  }
} 