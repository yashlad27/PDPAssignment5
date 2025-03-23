import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for Event.
 */
public class EventTest {

  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private boolean isPublic;

  @Before
  public void setUp() {
    subject = "Team Meeting";
    startDateTime = LocalDateTime.of(2023, 4, 10, 10, 0);
    endDateTime = LocalDateTime.of(2023, 4, 10, 11, 0);
    description = "Weekly team sync-up";
    location = "Conference Room A";
    isPublic = true;
  }

  @Test
  public void testConstructorWithValidParameters() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);

    assertEquals(subject, event.getSubject());
    assertEquals(startDateTime, event.getStartDateTime());
    assertEquals(endDateTime, event.getEndDateTime());
    assertEquals(description, event.getDescription());
    assertEquals(location, event.getLocation());
    assertEquals(isPublic, event.isPublic());
    assertFalse(event.isAllDay());
    assertNotNull(event.getId());
  }

  @Test
  public void testConstructorWithNullEndDateTime() {
    Event event = new Event(subject, startDateTime, null, description, location, isPublic);

    assertEquals(subject, event.getSubject());
    assertEquals(startDateTime, event.getStartDateTime());
    assertEquals(
            LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.of(23, 59, 59)),
            event.getEndDateTime()
    );
    assertTrue(event.isAllDay());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullSubject() {
    new Event(null, startDateTime, endDateTime, description, location, isPublic);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptySubject() {
    new Event("", startDateTime, endDateTime, description, location, isPublic);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithBlankSubject() {
    new Event("   ", startDateTime, endDateTime, description, location, isPublic);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullStartDateTime() {
    new Event(subject, null, endDateTime, description, location, isPublic);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEndDateTimeBeforeStartDateTime() {
    LocalDateTime earlierEndTime = startDateTime.minusHours(1);
    new Event(subject, startDateTime, earlierEndTime, description, location, isPublic);
  }

  @Test
  public void testCreateAllDayEvent() {
    LocalDate date = LocalDate.of(2023, 4, 10);
    Event event = Event.createAllDayEvent(subject, date, description, location, isPublic);

    assertEquals(subject, event.getSubject());
    assertEquals(
            LocalDateTime.of(date, LocalTime.of(0, 0)),
            event.getStartDateTime()
    );
    assertEquals(
            LocalDateTime.of(date, LocalTime.of(23, 59, 59)),
            event.getEndDateTime()
    );
    assertEquals(description, event.getDescription());
    assertEquals(location, event.getLocation());
    assertEquals(isPublic, event.isPublic());
    assertTrue(event.isAllDay());
  }

  @Test
  public void testConflictsWithOverlappingEvents() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);

    LocalDateTime overlapStartTime = startDateTime.plusMinutes(30);
    LocalDateTime overlapEndTime = endDateTime.plusHours(1);
    Event overlappingEvent = new Event(
            "Overlapping Meeting", overlapStartTime, overlapEndTime, "Description", "Location", true
    );

    assertTrue(event.conflictsWith(overlappingEvent));
    assertTrue(overlappingEvent.conflictsWith(event));
  }

  @Test
  public void testConflictsWithSameTimeEvents() {
    Event event1 = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    Event event2 = new Event("Another Meeting", startDateTime, endDateTime,
            "Description", "Location", true);

    assertTrue(event1.conflictsWith(event2));
    assertTrue(event2.conflictsWith(event1));
  }

  @Test
  public void testNoConflictWithNonOverlappingEvents() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);

    LocalDateTime laterStartTime = endDateTime.plusMinutes(1);
    LocalDateTime laterEndTime = laterStartTime.plusHours(1);
    Event nonOverlappingEvent = new Event(
            "Later Meeting", laterStartTime, laterEndTime, "Description",
            "Location", true
    );

    assertFalse(event.conflictsWith(nonOverlappingEvent));
    assertFalse(nonOverlappingEvent.conflictsWith(event));
  }

  @Test
  public void testConflictsWithNullEvent() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    assertFalse(event.conflictsWith(null));
  }

  @Test
  public void testSpansMultipleDays() {
    LocalDateTime multiDayEndTime = startDateTime.plusDays(1);
    Event multiDayEvent = new Event(
            subject, startDateTime, multiDayEndTime, description, location, isPublic
    );

    assertTrue(multiDayEvent.spansMultipleDays());
  }

  @Test
  public void testDoesNotSpanMultipleDays() {
    Event singleDayEvent = new Event(
            subject, startDateTime, endDateTime, description, location, isPublic
    );

    assertFalse(singleDayEvent.spansMultipleDays());
  }

  @Test
  public void testSetSubject() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    String newSubject = "Updated Meeting";

    event.setSubject(newSubject);
    assertEquals(newSubject, event.getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNullSubject() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    event.setSubject(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetEmptySubject() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    event.setSubject("");
  }

  @Test
  public void testSetEndDateTime() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    LocalDateTime newEndTime = endDateTime.plusHours(1);

    event.setEndDateTime(newEndTime);
    assertEquals(newEndTime, event.getEndDateTime());
    assertFalse(event.isAllDay());
  }

  @Test
  public void testSetEndDateTimeToNull() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);

    event.setEndDateTime(null);
    assertEquals(
            LocalDateTime.of(startDateTime.toLocalDate(),
                    LocalTime.of(23, 59, 59)),
            event.getEndDateTime()
    );
    assertTrue(event.isAllDay());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetEndDateTimeBeforeStartDateTime() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    LocalDateTime invalidEndTime = startDateTime.minusMinutes(1);

    event.setEndDateTime(invalidEndTime);
  }

  @Test
  public void testSetDescription() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    String newDescription = "Updated description";

    event.setDescription(newDescription);
    assertEquals(newDescription, event.getDescription());
  }

  @Test
  public void testSetNullDescription() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);

    event.setDescription(null);
    assertNull(event.getDescription());
  }

  @Test
  public void testSetLocation() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    String newLocation = "Room B";

    event.setLocation(newLocation);
    assertEquals(newLocation, event.getLocation());
  }

  @Test
  public void testSetNullLocation() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);

    event.setLocation(null);
    assertNull(event.getLocation());
  }

  @Test
  public void testSetStartDateTime() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    LocalDateTime newStartTime = startDateTime.minusHours(1);

    event.setStartDateTime(newStartTime);
    assertEquals(newStartTime, event.getStartDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNullStartDateTime() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    event.setStartDateTime(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetStartDateTimeAfterEndDateTime() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    LocalDateTime invalidStartTime = endDateTime.plusMinutes(1);

    event.setStartDateTime(invalidStartTime);
  }

  @Test
  public void testSetPublic() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location,
            true);

    event.setPublic(false);
    assertFalse(event.isPublic());

    event.setPublic(true);
    assertTrue(event.isPublic());
  }

  @Test
  public void testSetAllDay() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);

    event.setAllDay(true);
    assertTrue(event.isAllDay());
    assertEquals(
            LocalDateTime.of(startDateTime.toLocalDate(),
                    LocalTime.of(23, 59, 59)),
            event.getEndDateTime()
    );

    event.setAllDay(false);
    assertFalse(event.isAllDay());
  }

  @Test
  public void testSetDate() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    LocalDate newDate = LocalDate.of(2023, 4, 15);

    event.setDate(newDate);
    assertEquals(newDate, event.getDate());
  }

  @Test
  public void testEquals() {
    Event event1 = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    Event event2 = new Event(subject, startDateTime, endDateTime,
            "Different description", "Different location", false);

    assertEquals(event1, event2);

    Event event3 = new Event(subject, startDateTime.plusHours(1), endDateTime,
            description, location, isPublic);
    assertNotEquals(event1, event3);

    Event event4 = new Event(subject, startDateTime, endDateTime.plusHours(1),
            description, location, isPublic);
    assertNotEquals(event1, event4);

    Event event5 = new Event("Different subject", startDateTime, endDateTime,
            description, location, isPublic);
    assertNotEquals(event1, event5);
  }

  @Test
  public void testHashCode() {
    Event event1 = new Event(subject, startDateTime, endDateTime,
            description, location, isPublic);
    Event event2 = new Event(subject, startDateTime, endDateTime,
            "Different description", "Different location", false);

    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testToString() {
    Event event = new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    String toString = event.toString();

    assertTrue(toString.contains(subject));
    assertTrue(toString.contains(startDateTime.toString()));
    assertTrue(toString.contains(endDateTime.toString()));
    assertTrue(toString.contains(location));
  }
}