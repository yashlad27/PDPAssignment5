import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import model.core.timezone.TimeZoneHandler;
import model.core.timezone.TimezoneConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for TimezoneConverter.
 * This class tests the functionality of the TimezoneConverter interface,
 * including its factory methods and default methods.
 */
public class TimezoneConverterTest {

  private static final String UTC = "UTC";
  private static final String NEW_YORK = "America/New_York";
  private static final String TOKYO = "Asia/Tokyo";
  private static final String PARIS = "Europe/Paris";
  private static final String SYDNEY = "Australia/Sydney";
  private static final String INVALID_TIMEZONE = "Invalid/Timezone";

  private TimeZoneHandler timeZoneHandler;
  private LocalDateTime referenceDateTime;

  @Before
  public void setUp() {
    timeZoneHandler = new TimeZoneHandler();
    referenceDateTime = LocalDateTime.of(2023, 10, 15, 12, 0); // 2023-10-15 12:00 UTC
  }

  /**
   * Test the between factory method.
   */
  @Test
  public void testBetweenMethod() {
    TimezoneConverter converter = TimezoneConverter.between(UTC, NEW_YORK, timeZoneHandler);
    assertNotNull(converter);

    // Convert from UTC to New York (expect time to change but instant remains the same)
    LocalDateTime convertedTime = converter.convert(referenceDateTime);
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime nyZoned = utcZoned.withZoneSameInstant(ZoneId.of(NEW_YORK));
    
    assertEquals(nyZoned.toLocalDateTime(), convertedTime);
  }

  /**
   * Test the identity factory method.
   */
  @Test
  public void testIdentityMethod() {
    TimezoneConverter converter = TimezoneConverter.identity();
    assertNotNull(converter);
    
    // Identity converter should not change the time
    LocalDateTime convertedTime = converter.convert(referenceDateTime);
    assertEquals(referenceDateTime, convertedTime);
  }

  /**
   * Test the toUTC factory method.
   */
  @Test
  public void testToUTCMethod() {
    // Create time in Tokyo timezone
    ZonedDateTime tokyoZoned = referenceDateTime.atZone(ZoneId.of(TOKYO));
    LocalDateTime tokyoTime = tokyoZoned.toLocalDateTime();
    
    TimezoneConverter converter = TimezoneConverter.toUTC(TOKYO, timeZoneHandler);
    assertNotNull(converter);
    
    // Convert from Tokyo to UTC
    LocalDateTime convertedTime = converter.convert(tokyoTime);
    ZonedDateTime expectedUTC = tokyoZoned.withZoneSameInstant(ZoneId.of(UTC)).toLocalDateTime().atZone(ZoneId.of(UTC));
    
    assertEquals(expectedUTC.toLocalDateTime(), convertedTime);
  }

  /**
   * Test the fromUTC factory method.
   */
  @Test
  public void testFromUTCMethod() {
    TimezoneConverter converter = TimezoneConverter.fromUTC(PARIS, timeZoneHandler);
    assertNotNull(converter);
    
    // Convert from UTC to Paris
    LocalDateTime convertedTime = converter.convert(referenceDateTime);
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime parisZoned = utcZoned.withZoneSameInstant(ZoneId.of(PARIS));
    
    assertEquals(parisZoned.toLocalDateTime(), convertedTime);
  }

  /**
   * Test the viaUTC factory method.
   */
  @Test
  public void testViaUTCMethod() {
    TimezoneConverter converter = TimezoneConverter.viaUTC(TOKYO, PARIS, timeZoneHandler);
    assertNotNull(converter);
    
    // Create time in Tokyo timezone
    ZonedDateTime tokyoZoned = referenceDateTime.atZone(ZoneId.of(TOKYO));
    LocalDateTime tokyoTime = tokyoZoned.toLocalDateTime();
    
    // Convert from Tokyo to Paris via UTC
    LocalDateTime convertedTime = converter.convert(tokyoTime);
    
    // Manually convert from Tokyo -> UTC -> Paris
    ZonedDateTime utcZoned = tokyoZoned.withZoneSameInstant(ZoneId.of(UTC));
    ZonedDateTime parisZoned = utcZoned.withZoneSameInstant(ZoneId.of(PARIS));
    
    assertEquals(parisZoned.toLocalDateTime(), convertedTime);
  }

  /**
   * Test the andThen default method.
   */
  @Test
  public void testAndThenMethod() {
    // Create converters for UTC -> Tokyo and Tokyo -> Paris
    TimezoneConverter utcToTokyo = TimezoneConverter.between(UTC, TOKYO, timeZoneHandler);
    TimezoneConverter tokyoToParis = TimezoneConverter.between(TOKYO, PARIS, timeZoneHandler);
    
    // Chain the converters
    TimezoneConverter utcToParis = utcToTokyo.andThen(tokyoToParis);
    assertNotNull(utcToParis);
    
    // Convert using the chained converter
    LocalDateTime convertedTime = utcToParis.convert(referenceDateTime);
    
    // Manually convert from UTC -> Paris directly for comparison
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime parisZoned = utcZoned.withZoneSameInstant(ZoneId.of(PARIS));
    
    assertEquals(parisZoned.toLocalDateTime(), convertedTime);
  }
  
  /**
   * Test multiple andThen chains.
   */
  @Test
  public void testMultipleAndThenChains() {
    // Create converters for each leg of the journey
    TimezoneConverter utcToTokyo = TimezoneConverter.between(UTC, TOKYO, timeZoneHandler);
    TimezoneConverter tokyoToParis = TimezoneConverter.between(TOKYO, PARIS, timeZoneHandler);
    TimezoneConverter parisToSydney = TimezoneConverter.between(PARIS, SYDNEY, timeZoneHandler);
    
    // Chain multiple converters
    TimezoneConverter worldTour = utcToTokyo.andThen(tokyoToParis).andThen(parisToSydney);
    assertNotNull(worldTour);
    
    // Convert using the multi-chained converter
    LocalDateTime convertedTime = worldTour.convert(referenceDateTime);
    
    // Manually convert directly for comparison
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime sydneyZoned = utcZoned.withZoneSameInstant(ZoneId.of(SYDNEY));
    
    assertEquals(sydneyZoned.toLocalDateTime(), convertedTime);
  }
  
  /**
   * Test converting through several timezones and back to the original.
   */
  @Test
  public void testRoundTripConversion() {
    // Create converters for a round trip: UTC -> Tokyo -> Paris -> New York -> UTC
    TimezoneConverter utcToTokyo = TimezoneConverter.between(UTC, TOKYO, timeZoneHandler);
    TimezoneConverter tokyoToParis = TimezoneConverter.between(TOKYO, PARIS, timeZoneHandler);
    TimezoneConverter parisToNY = TimezoneConverter.between(PARIS, NEW_YORK, timeZoneHandler);
    TimezoneConverter nyToUTC = TimezoneConverter.between(NEW_YORK, UTC, timeZoneHandler);
    
    // Chain them into a round trip
    TimezoneConverter roundTrip = utcToTokyo.andThen(tokyoToParis).andThen(parisToNY).andThen(nyToUTC);
    assertNotNull(roundTrip);
    
    // After a round trip, we should be back to the original time
    LocalDateTime convertedTime = roundTrip.convert(referenceDateTime);
    assertEquals(referenceDateTime, convertedTime);
  }
  
  /**
   * Test conversion with a specific timestamp across DST changes.
   */
  @Test
  public void testConversionAcrossDST() {
    // March 13, 2022 at 1am in UTC (before DST change in NY)
    LocalDateTime beforeDSTChange = LocalDateTime.of(2022, 3, 13, 1, 0);
    
    TimezoneConverter converter = TimezoneConverter.between(UTC, NEW_YORK, timeZoneHandler);
    LocalDateTime convertedTime = converter.convert(beforeDSTChange);
    
    // Manually convert for verification
    ZonedDateTime utcZoned = beforeDSTChange.atZone(ZoneId.of(UTC));
    ZonedDateTime nyZoned = utcZoned.withZoneSameInstant(ZoneId.of(NEW_YORK));
    
    assertEquals(nyZoned.toLocalDateTime(), convertedTime);
  }
  
  /**
   * Test conversion with midnight crossing.
   */
  @Test
  public void testConversionWithMidnightCrossing() {
    // 11:00 PM in UTC
    LocalDateTime lateEvening = LocalDateTime.of(2023, 10, 15, 23, 0);
    
    // Converting to Tokyo (UTC+9) should result in the next day
    TimezoneConverter converter = TimezoneConverter.between(UTC, TOKYO, timeZoneHandler);
    LocalDateTime convertedTime = converter.convert(lateEvening);
    
    // Manually convert for verification
    ZonedDateTime utcZoned = lateEvening.atZone(ZoneId.of(UTC));
    ZonedDateTime tokyoZoned = utcZoned.withZoneSameInstant(ZoneId.of(TOKYO));
    
    assertEquals(tokyoZoned.toLocalDateTime(), convertedTime);
    // In Tokyo, it should already be the next day
    assertEquals(16, convertedTime.getDayOfMonth());
  }
  
  /**
   * Test conversion with date boundary crossing.
   */
  @Test
  public void testConversionWithDateBoundaryCrossing() {
    // December 31, 2023 at 11:00 PM in UTC
    LocalDateTime newYearsEve = LocalDateTime.of(2023, 12, 31, 23, 0);
    
    // Converting to Sydney (UTC+11) should result in the next year
    TimezoneConverter converter = TimezoneConverter.between(UTC, SYDNEY, timeZoneHandler);
    LocalDateTime convertedTime = converter.convert(newYearsEve);
    
    // Manually convert for verification
    ZonedDateTime utcZoned = newYearsEve.atZone(ZoneId.of(UTC));
    ZonedDateTime sydneyZoned = utcZoned.withZoneSameInstant(ZoneId.of(SYDNEY));
    
    assertEquals(sydneyZoned.toLocalDateTime(), convertedTime);
    // In Sydney, it should already be the next year
    assertEquals(2024, convertedTime.getYear());
    assertEquals(1, convertedTime.getMonthValue());
    assertEquals(1, convertedTime.getDayOfMonth());
  }
  
  /**
   * Test identity converter with andThen.
   */
  @Test
  public void testIdentityWithAndThen() {
    TimezoneConverter identity = TimezoneConverter.identity();
    TimezoneConverter utcToTokyo = TimezoneConverter.between(UTC, TOKYO, timeZoneHandler);
    
    // Identity followed by conversion should be equivalent to just the conversion
    TimezoneConverter combined = identity.andThen(utcToTokyo);
    LocalDateTime convertedTime = combined.convert(referenceDateTime);
    
    // Direct conversion for comparison
    LocalDateTime directConverted = utcToTokyo.convert(referenceDateTime);
    
    assertEquals(directConverted, convertedTime);
  }
  
  /**
   * Test conversion with fractions of hours in timezone offset.
   */
  @Test
  public void testConversionWithFractionalOffset() {
    // Some timezones have offsets that aren't whole hours
    String indiaTimezone = "Asia/Kolkata"; // UTC+5:30
    
    TimezoneConverter converter = TimezoneConverter.between(UTC, indiaTimezone, timeZoneHandler);
    LocalDateTime convertedTime = converter.convert(referenceDateTime);
    
    // Manually convert for verification
    ZonedDateTime utcZoned = referenceDateTime.atZone(ZoneId.of(UTC));
    ZonedDateTime indiaZoned = utcZoned.withZoneSameInstant(ZoneId.of(indiaTimezone));
    
    assertEquals(indiaZoned.toLocalDateTime(), convertedTime);
    // Check that the minutes reflect the half-hour offset
    assertEquals(30, convertedTime.getMinute());
  }
} 