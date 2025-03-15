import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import model.event.Event;
import model.event.RecurringEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for RecurringEvent.
 */
public class RecurringEventTest {

  private String subject;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String description;
  private String location;
  private boolean isPublic;
  private Set<DayOfWeek> repeatDays;
  private int occurrences;
  private LocalDate endDate;

  @Before
  public void setUp() {
    subject = "Weekly Meeting";
    // Start with a Monday
    LocalDate startDate = LocalDate.of(2023, 5, 1);
    startDateTime = LocalDateTime.of(startDate, LocalTime.of(10, 0));
    endDateTime = LocalDateTime.of(startDate, LocalTime.of(11, 0));
    description = "Team sync-up";
    location = "Conference Room A";
    isPublic = true;
    repeatDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    occurrences = 4;
    endDate = LocalDate.of(2023, 5, 15);
  }

  @Test
  public void testConstructorWithOccurrences() {
    RecurringEvent event = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );

    assertEquals(subject, event.getSubject());
    assertEquals(startDateTime, event.getStartDateTime());
    assertEquals(endDateTime, event.getEndDateTime());
    assertEquals(description, event.getDescription());
    assertEquals(location, event.getLocation());
    assertEquals(isPublic, event.isPublic());
    assertEquals(repeatDays, event.getRepeatDays());
    assertEquals(occurrences, event.getOccurrences());
    assertNull(event.getEndDate());
    assertNotNull(event.getRecurringId());
  }

  @Test
  public void testConstructorWithEndDate() {
    RecurringEvent event = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, endDate
    );

    assertEquals(subject, event.getSubject());
    assertEquals(startDateTime, event.getStartDateTime());
    assertEquals(endDateTime, event.getEndDateTime());
    assertEquals(description, event.getDescription());
    assertEquals(location, event.getLocation());
    assertEquals(isPublic, event.isPublic());
    assertEquals(repeatDays, event.getRepeatDays());
    assertEquals(-1, event.getOccurrences());
    assertEquals(endDate, event.getEndDate());
    assertNotNull(event.getRecurringId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptyRepeatDays() {
    Set<DayOfWeek> emptySet = EnumSet.noneOf(DayOfWeek.class);
    new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            emptySet, occurrences
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNegativeOccurrences() {
    new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, -1
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithZeroOccurrences() {
    new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, 0
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEndDateBeforeStartDate() {
    LocalDate invalidEndDate = startDateTime.toLocalDate().minusDays(1);
    new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, invalidEndDate
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithMultiDayEvent() {
    LocalDateTime nextDayEndTime = startDateTime.plusDays(1);
    new RecurringEvent(
            subject, startDateTime, nextDayEndTime,
            description, location, isPublic,
            repeatDays, occurrences
    );
  }

  @Test
  public void testGetAllOccurrencesWithOccurrences() {
    RecurringEvent event = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );

    List<Event> allOccurrences = event.getAllOccurrences();

    assertEquals(occurrences, allOccurrences.size());

    assertEquals(LocalDate.of(2023, 5, 1),
            allOccurrences.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2023, 5, 3),
            allOccurrences.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2023, 5, 5),
            allOccurrences.get(2).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2023, 5, 8),
            allOccurrences.get(3).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGetAllOccurrencesWithEndDate() {
    RecurringEvent event = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, endDate
    );

    List<Event> allOccurrences = event.getAllOccurrences();

    assertEquals(7, allOccurrences.size());

    assertEquals(LocalDate.of(2023, 5, 1),
            allOccurrences.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2023, 5, 8),
            allOccurrences.get(3).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2023, 5, 15),
            allOccurrences.get(6).getStartDateTime().toLocalDate());
  }

  @Test
  public void testAllDayRecurringEvent() {
    LocalDate date = LocalDate.of(2023, 5, 1);
    RecurringEvent recurringEvent = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );
    recurringEvent.setAllDay(true);

    assertTrue(recurringEvent.isAllDay());

    List<Event> occurrences = recurringEvent.getAllOccurrences();
    for (Event occurrence : occurrences) {
      assertTrue(occurrence.isAllDay());
    }
  }

  @Test
  public void testRecurringIdUniqueness() {
    RecurringEvent event1 = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );

    RecurringEvent event2 = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );

    assertNotEquals(event1.getRecurringId(), event2.getRecurringId());
  }

  @Test
  public void testOccurrenceDetailsMatchTemplate() {
    RecurringEvent template = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );

    List<Event> occurrences = template.getAllOccurrences();
    for (Event occurrence : occurrences) {
      assertEquals(template.getSubject(), occurrence.getSubject());
      assertEquals(template.getDescription(), occurrence.getDescription());
      assertEquals(template.getLocation(), occurrence.getLocation());
      assertEquals(template.isPublic(), occurrence.isPublic());
      assertEquals(template.isAllDay(), occurrence.isAllDay());

      assertEquals(template.getStartDateTime().toLocalTime(),
              occurrence.getStartDateTime().toLocalTime());
      assertEquals(template.getEndDateTime().toLocalTime(),
              occurrence.getEndDateTime().toLocalTime());
    }
  }

  @Test
  public void testUpdateRecurringEventAffectsAllOccurrences() {
    RecurringEvent recurringEvent = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );

    List<Event> initialOccurrences = recurringEvent.getAllOccurrences();

    String newSubject = "Updated Meeting";
    recurringEvent.setSubject(newSubject);

    List<Event> updatedOccurrences = recurringEvent.getAllOccurrences();

    for (Event occurrence : updatedOccurrences) {
      assertEquals(newSubject, occurrence.getSubject());
    }
  }

  @Test
  public void testGetRepeatDaysReturnsCopy() {
    RecurringEvent event = new RecurringEvent(
            subject, startDateTime, endDateTime,
            description, location, isPublic,
            repeatDays, occurrences
    );

    Set<DayOfWeek> returnedDays = event.getRepeatDays();

    assertEquals(repeatDays, returnedDays);
    assertNotSame(repeatDays, returnedDays);

    returnedDays.add(DayOfWeek.SUNDAY);
    assertFalse(repeatDays.contains(DayOfWeek.SUNDAY));
  }
}