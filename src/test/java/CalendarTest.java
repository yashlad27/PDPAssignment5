import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.calendar.Calendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for Calendar.
 */
public class CalendarTest {

  private Map<String, String> mockFileSystem;

  private Calendar calendar;
  private Event singleEvent;
  private RecurringEvent recurringEvent;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Set<DayOfWeek> repeatDays;

  @Before
  public void setUp() {
    mockFileSystem = new HashMap<>();

    calendar = new Calendar();
    calendar.setName("Test Calendar");
    calendar.setTimezone("America/New_York");

    startDateTime = LocalDateTime.of(2023, 5, 10, 10, 0);
    endDateTime = LocalDateTime.of(2023, 5, 10, 11, 0);
    singleEvent = new Event("Team Meeting", startDateTime, endDateTime,
            "Weekly sync-up", "Conference Room A", true);
    repeatDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    recurringEvent = new RecurringEvent.Builder("Recurring Meeting",
            LocalDateTime.of(2023, 5, 8, 14, 0),
            LocalDateTime.of(2023, 5, 8, 15, 0),
            repeatDays).description("Recurring sync-up").location("Conference Room B")
            .isPublic(true).occurrences(4).build();
  }

  @Test
  public void testAddEvent() throws ConflictingEventException {
    assertTrue(calendar.addEvent(singleEvent, false));

    List<Event> events = calendar.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(singleEvent, events.get(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullEvent() throws ConflictingEventException {
    calendar.addEvent(null, false);
  }

  @Test
  public void testAddEventWithAutoDeclineNoConflict() throws ConflictingEventException {
    assertTrue(calendar.addEvent(singleEvent, true));

    Event noConflictEvent = new Event("Another Meeting",
            startDateTime.plusHours(2), endDateTime.plusHours(2),
            "Description", "Location", true);

    assertTrue(calendar.addEvent(noConflictEvent, true));
    assertEquals(2, calendar.getAllEvents().size());
  }

  @Test(expected = ConflictingEventException.class)
  public void testAddEventWithAutoDeclineWithConflict() throws ConflictingEventException {
    assertTrue(calendar.addEvent(singleEvent, true));

    Event conflictingEvent = new Event("Conflicting Meeting",
            startDateTime.plusMinutes(30), endDateTime.plusHours(1), "Description",
            "Location", true);

    calendar.addEvent(conflictingEvent, true);
  }

  @Test
  public void testAddEventWithoutAutoDeclineWithConflict() throws ConflictingEventException {
    assertTrue(calendar.addEvent(singleEvent, false));

    Event conflictingEvent = new Event("Conflicting Meeting",
            startDateTime.plusMinutes(30), endDateTime.plusHours(1), "Description",
            "Location", true);

    assertFalse(calendar.addEvent(conflictingEvent, false));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testAddRecurringEvent() throws ConflictingEventException {
    assertTrue(calendar.addRecurringEvent(recurringEvent, false));
    List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
    assertEquals(1, recurringEvents.size());

    // Verify the recurring event properties using the Builder pattern getters
    RecurringEvent savedEvent = recurringEvents.get(0);
    assertEquals("Recurring Meeting", savedEvent.getSubject());
    assertEquals(4, savedEvent.getOccurrences());
    assertEquals(repeatDays, savedEvent.getRepeatDays());

    List<Event> allEvents = calendar.getAllEvents();
    assertEquals(4, allEvents.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullRecurringEvent() throws ConflictingEventException {
    calendar.addRecurringEvent(null, false);
  }

  @Test
  public void testAddRecurringEventWithAutoDeclineNoConflict() throws ConflictingEventException {
    assertTrue(calendar.addRecurringEvent(recurringEvent, true));

    RecurringEvent noConflictRecurringEvent = new RecurringEvent
            .Builder("Another Recurring Meeting",
            LocalDateTime.of(2023, 5, 8, 16, 0),
            LocalDateTime.of(2023, 5, 8, 17, 0), repeatDays)
            .description("Description").location("Location").isPublic(true).occurrences(4).build();

    assertTrue(calendar.addRecurringEvent(noConflictRecurringEvent, true));
    assertEquals(2, calendar.getAllRecurringEvents().size());
    assertEquals(8, calendar.getAllEvents().size());
  }

  @Test(expected = ConflictingEventException.class)
  public void testAddRecurringEventWithAutoDeclineWithConflict() throws ConflictingEventException {
    assertTrue(calendar.addRecurringEvent(recurringEvent, true));

    LocalDateTime conflictStart = recurringEvent.getAllOccurrences().get(0).getStartDateTime();

    RecurringEvent conflictingRecurringEvent = new RecurringEvent
            .Builder("Conflicting Recurring Meeting",
            conflictStart, conflictStart.plusHours(1),
            EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
            .description("Description").location("Location").isPublic(true).occurrences(4).build();

    calendar.addRecurringEvent(conflictingRecurringEvent, true);
  }

  @Test
  public void testCreateAllDayRecurringEvent() throws ConflictingEventException,
          InvalidEventException {
    LocalDate start = LocalDate.of(2023, 5, 8);

    assertTrue(calendar.createAllDayRecurringEvent("All Day Recurring Event",
            start, "MWF", 3, true, "Description",
            "Location", true));

    assertEquals(1, calendar.getAllRecurringEvents().size());
    assertEquals(3, calendar.getAllEvents().size());

    List<Event> events = calendar.getAllEvents();
    for (Event event : events) {
      assertTrue(event.isAllDay());
    }
  }

  @Test
  public void testCreateAllDayRecurringEventUntil() throws ConflictingEventException,
          InvalidEventException {
    LocalDate start = LocalDate.of(2023, 5, 8);
    LocalDate until = LocalDate.of(2023, 5, 19);

    assertTrue(calendar.createAllDayRecurringEventUntil("All Day Recurring Until Event",
            start, "MWF", until, true, "Description",
            "Location", true));

    assertEquals(1, calendar.getAllRecurringEvents().size());
    assertEquals(6, calendar.getAllEvents().size());

    List<Event> events = calendar.getAllEvents();
    for (Event event : events) {
      assertTrue(event.isAllDay());
    }
  }

  @Test
  public void testFindEvent() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);

    Event found = calendar.findEvent("Team Meeting", startDateTime);
    assertNotNull(found);
    assertEquals(singleEvent, found);
  }

  @Test
  public void testFindNonExistentEvent() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);

    Event notFound = calendar.findEvent("Non-existent Meeting", startDateTime);
    assertNull(notFound);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEventWithNullSubject() {
    calendar.findEvent(null, startDateTime);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEventWithNullDateTime() {
    calendar.findEvent("Team Meeting", null);
  }

  @Test
  public void testGetAllEvents() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);
    calendar.addRecurringEvent(recurringEvent, false);

    List<Event> allEvents = calendar.getAllEvents();

    assertEquals(5, allEvents.size());
  }

  @Test
  public void testEditSingleEvent() throws ConflictingEventException, InvalidEventException {
    calendar.addEvent(singleEvent, false);

    assertTrue(calendar.editSingleEvent("Team Meeting", startDateTime, "subject",
            "Updated Meeting"));

    Event updated = calendar.findEvent("Updated Meeting", startDateTime);
    assertNotNull(updated);
    assertEquals("Updated Meeting", updated.getSubject());
  }

  @Test
  public void testEditNonExistentEvent() throws ConflictingEventException, InvalidEventException {
    assertFalse(calendar.editSingleEvent("Non-existent Meeting", startDateTime,
            "subject", "Updated"));
  }

  @Test
  public void testEditAllEvents() throws ConflictingEventException, InvalidEventException {
    calendar.addEvent(singleEvent, false);

    Event anotherEvent = new Event("Team Meeting", startDateTime.plusDays(1),
            endDateTime.plusDays(1), "Another meeting", "Conference Room C",
            true);
    calendar.addEvent(anotherEvent, false);

    int count = calendar.editAllEvents("Team Meeting", "location",
            "New Location");

    assertEquals(2, count);
    List<Event> allEvents = calendar.getAllEvents();
    for (Event event : allEvents) {
      assertEquals("New Location", event.getLocation());
    }
  }

  @Test
  public void testGetAllRecurringEvents() throws ConflictingEventException {
    calendar.addRecurringEvent(recurringEvent, false);

    List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
    assertEquals(1, recurringEvents.size());
    assertEquals(recurringEvent, recurringEvents.get(0));
  }

  @Test
  public void testExportToCSV() throws IOException, ConflictingEventException {
    calendar.addEvent(singleEvent, false);
    calendar.addRecurringEvent(recurringEvent, false);

    Calendar mockCalendar = new Calendar() {
      @Override
      public String exportToCSV(String filePath) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
                + "Description,Location," + "Private\n");
        csv.append("Team Meeting,05/10/2023,10:00 AM,05/10/2023,11:00 AM,False,"
                + "Weekly sync-up,Conference " + "Room A,False\n");
        mockFileSystem.put(filePath, csv.toString());

        return filePath;
      }
    };
    mockCalendar.setName("Test Mock Calendar");
    mockCalendar.setTimezone("America/New_York");

    mockCalendar.addEvent(singleEvent, false);
    mockCalendar.addRecurringEvent(recurringEvent, false);

    String filePath = "calendar_export.csv";
    String exportedPath = mockCalendar.exportToCSV(filePath);

    assertEquals(filePath, exportedPath);
    assertTrue(mockFileSystem.containsKey(filePath));

    String csvContent = mockFileSystem.get(filePath);
    assertNotNull(csvContent);
    assertFalse(csvContent.isEmpty());

    assertTrue(csvContent.contains("Team Meeting"));
    assertTrue(csvContent.contains("Subject,Start Date,Start Time"));
  }

  @Test
  public void testGetEventsOnDate() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);

    LocalDate date = LocalDate.of(2023, 5, 10);
    List<Event> eventsOnDate = calendar.getEventsOnDate(date);

    assertEquals(1, eventsOnDate.size());
    assertEquals(singleEvent, eventsOnDate.get(0));
  }

  @Test
  public void testGetEventsOnDateWithNoEvents() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);

    LocalDate date = LocalDate.of(2023, 5, 11);
    List<Event> eventsOnDate = calendar.getEventsOnDate(date);

    assertTrue(eventsOnDate.isEmpty());
  }

  @Test
  public void testGetEventsOnDateWithMultiDayEvent() throws ConflictingEventException {
    Event multiDayEvent = new Event("Multi-day Conference",
            LocalDateTime.of(2023, 5, 10, 9, 0),
            LocalDateTime.of(2023, 5, 12, 17, 0),
            "Annual conference", "Convention Center", true);
    calendar.addEvent(multiDayEvent, false);

    List<Event> day1Events = calendar.getEventsOnDate(LocalDate.of(2023, 5,
            10));
    assertEquals(1, day1Events.size());

    List<Event> day2Events = calendar.getEventsOnDate(LocalDate.of(2023, 5,
            11));
    assertEquals(1, day2Events.size());

    List<Event> day3Events = calendar.getEventsOnDate(LocalDate.of(2023, 5,
            12));
    assertEquals(1, day3Events.size());

    List<Event> day4Events = calendar.getEventsOnDate(LocalDate.of(2023, 5,
            13));
    assertTrue(day4Events.isEmpty());
  }

  @Test
  public void testGetEventsInRange() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);
    calendar.addRecurringEvent(recurringEvent, false);

    LocalDate startDate = LocalDate.of(2023, 5, 8);
    LocalDate endDate = LocalDate.of(2023, 5, 12);

    List<Event> eventsInRange = calendar.getEventsInRange(startDate, endDate);

    assertEquals(4, eventsInRange.size());
  }

  @Test
  public void testGetEventsInRangeWithNoEvents() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);

    LocalDate startDate = LocalDate.of(2023, 5, 20);
    LocalDate endDate = LocalDate.of(2023, 5, 25);

    List<Event> eventsInRange = calendar.getEventsInRange(startDate, endDate);

    assertTrue(eventsInRange.isEmpty());
  }

  @Test
  public void testIsBusy() throws ConflictingEventException {
    calendar.addEvent(singleEvent, false);

    assertTrue(calendar.isBusy(startDateTime.plusMinutes(30)));

    assertFalse(calendar.isBusy(startDateTime.minusMinutes(1)));

    assertFalse(calendar.isBusy(endDateTime.plusMinutes(1)));
  }

  @Test
  public void testIsBusyWithAllDayEvent() throws ConflictingEventException {
    Event allDayEvent = Event.createAllDayEvent("All-day Event", LocalDate.of(2023,
            5, 15), "Description", "Location", true);
    calendar.addEvent(allDayEvent, false);

    assertTrue(calendar.isBusy(LocalDateTime.of(2023, 5, 15, 9,
            0)));

    assertTrue(calendar.isBusy(LocalDateTime.of(2023, 5, 15, 15
            , 0)));

    assertFalse(calendar.isBusy(LocalDateTime.of(2023, 5, 16, 9,
            0)));
  }

  @Test
  public void testUpdateEventWithInvalidProperty() throws ConflictingEventException,
          InvalidEventException {
    calendar.addEvent(singleEvent, false);

    assertFalse(calendar.editSingleEvent("Team Meeting", startDateTime,
            "invalid_property", "value"));
  }

  @Test
  public void testGetAndSetName() {
    String newName = "Personal Calendar";
    calendar.setName(newName);
    assertEquals(newName, calendar.getName());
  }

  @Test
  public void testGetAndSetTimezone() {
    String newTimezone = "Europe/London";
    calendar.setTimezone(newTimezone);
    assertEquals(newTimezone, calendar.getTimezone());
  }

  @Test
  public void testBoundaryConditionsForBusyTime() throws ConflictingEventException {
    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15,
            10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 5, 15,
            11, 0);

    Event event = new Event("Test Event", eventStart, eventEnd,
            null, null, true);
    calendar.addEvent(event, true);

    assertTrue(calendar.isBusy(eventStart));
    assertTrue(calendar.isBusy(eventEnd));
    assertFalse(calendar.isBusy(eventStart.minusMinutes(1)));
    assertFalse(calendar.isBusy(eventEnd.plusMinutes(1)));
  }

  @Test
  public void testBoundaryConditionsForEventOverlap() throws ConflictingEventException {
    LocalDateTime baseTime = LocalDateTime.of(2023, 5, 15, 10,
            0);

    // Test events with exact overlap at start
    Event event1 = new Event("Event 1", baseTime, baseTime.plusHours(1),
            null, null, true);
    Event event2 = new Event("Event 2", baseTime, baseTime.plusMinutes(30),
            null, null, true);

    assertTrue(calendar.addEvent(event1, true));
    assertFalse(calendar.addEvent(event2, false));

    // Test events with exact overlap at end
    Event event3 = new Event("Event 3", baseTime.plusHours(2),
            baseTime.plusHours(3), null, null, true);
    Event event4 = new Event("Event 4", baseTime.plusHours(2).plusMinutes(30),
            baseTime.plusHours(3), null, null, true);

    assertTrue(calendar.addEvent(event3, true));
    assertFalse(calendar.addEvent(event4, false));

    // Test events with exact overlap in middle
    Event event5 = new Event("Event 5", baseTime.plusHours(4), baseTime.plusHours(6),
            null, null, true);
    Event event6 = new Event("Event 6", baseTime.plusHours(4).plusMinutes(30),
            baseTime.plusHours(5).plusMinutes(30), null, null, true);

    assertTrue(calendar.addEvent(event5, true));
    assertFalse(calendar.addEvent(event6, false));
  }

  @Test
  public void testBooleanReturnValues() throws ConflictingEventException {
    LocalDateTime baseTime = LocalDateTime.of(2023, 5, 15,
            10, 0);
    Event event = new Event("Test Event", baseTime, baseTime.plusHours(1),
            null, null, true);

    // Test addEvent with various conditions
    assertTrue("Should return true for first event add", calendar.addEvent(event,
            true));
    assertFalse("Should return false for duplicate event", calendar.addEvent(event,
            false));

    // Test isBusy with exact boundaries
    assertTrue("Should be busy at start time",
            calendar.isBusy(baseTime));
    assertTrue("Should be busy at end time",
            calendar.isBusy(baseTime.plusHours(1)));
    assertFalse("Should not be busy before start",
            calendar.isBusy(baseTime.minusSeconds(1)));
    assertFalse("Should not be busy after end",
            calendar.isBusy(baseTime.plusHours(1).plusSeconds(1)));

    // Test editSingleEvent with various fields
    assertTrue("Should return true for valid edit",
            calendar.editSingleEvent(event.getSubject(), event.getStartDateTime(),
                    "description", "New description"));
    assertTrue("Should return true for empty string edit",
            calendar.editSingleEvent(event.getSubject(), event.getStartDateTime(),
                    "description", ""));
    assertFalse("Should return false for non-existent event",
            calendar.editSingleEvent("Non-existent", baseTime,
                    "description", "New description"));
  }

  @Test
  public void testRecurringEventBoundaries() throws ConflictingEventException {
    LocalDateTime baseTime = LocalDateTime.of(2023, 5, 15, 10,
            0);
    Set<DayOfWeek> weekdays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    // Test recurring event with exactly matching times
    RecurringEvent event1 = new RecurringEvent.Builder("Event 1", baseTime,
            baseTime.plusHours(1), weekdays).occurrences(4).build();
    assertTrue(calendar.addRecurringEvent(event1, true));

    // Try to add single event at exact recurring event time
    Event conflictEvent = new Event("Conflict Event", baseTime.plusWeeks(1),
            baseTime.plusWeeks(1).plusHours(1), null, null, true);
    assertFalse(calendar.addEvent(conflictEvent, false));

    // Test recurring event with adjacent times
    RecurringEvent event2 = new RecurringEvent.Builder("Event 2", baseTime.plusHours(2),
            baseTime.plusHours(3), weekdays).occurrences(4).build();
    assertTrue(calendar.addRecurringEvent(event2, true));

    // Try to add event between recurring events
    Event betweenEvent = new Event("Between Event", baseTime.plusHours(1).plusMinutes(1),
            baseTime.plusHours(1).plusMinutes(59),
            null, null, true);
    assertTrue(calendar.addEvent(betweenEvent, true));

    // Test recurring event with exact conflict
    RecurringEvent conflictEvent2 = new RecurringEvent.Builder("Conflict",
            baseTime.plusHours(4), baseTime.plusHours(5), weekdays)
            .occurrences(2).build();
    assertTrue(calendar.addRecurringEvent(conflictEvent2, false));

    // Test recurring event with partial overlap
    RecurringEvent overlapEvent = new RecurringEvent.Builder("Overlap",
            baseTime.plusHours(6), baseTime.plusHours(7), weekdays)
            .occurrences(2).build();
    assertTrue(calendar.addRecurringEvent(overlapEvent, false));

    // Test recurring event with adjacent times (no overlap)
    RecurringEvent adjacentEvent = new RecurringEvent.Builder("Adjacent",
            baseTime.plusHours(8), baseTime.plusHours(9), weekdays).occurrences(2).build();
    assertTrue(calendar.addRecurringEvent(adjacentEvent, true));
  }

  @Test
  public void testDateTimeBoundaries() throws ConflictingEventException {
    LocalDateTime baseTime = LocalDateTime.of(2023, 5, 15, 0, 0);

    // Test day boundary events
    Event dayStartEvent = new Event("Day Start", baseTime, baseTime.plusHours(1),
            null, null, true);
    Event dayEndEvent = new Event("Day End", baseTime.plusHours(23), baseTime.plusDays(1),
            null, null, true);

    assertTrue(calendar.addEvent(dayStartEvent, true));
    assertTrue(calendar.addEvent(dayEndEvent, true));

    // Test month boundary events
    LocalDateTime monthEnd = LocalDateTime.of(2023, 5, 31, 23,
            0);
    LocalDateTime nextMonthStart = LocalDateTime.of(2023, 6, 1, 0,
            0);

    Event monthEndEvent = new Event("Month End", monthEnd, monthEnd.plusMinutes(15),
            null, null, true);
    Event nextMonthEvent = new Event("Next Month Start", nextMonthStart.plusHours(1),
            nextMonthStart.plusHours(2), null, null, true);

    assertTrue(calendar.addEvent(monthEndEvent, true));
    assertTrue(calendar.addEvent(nextMonthEvent, true));

    // Test year boundary events
    LocalDateTime yearEnd = LocalDateTime.of(2023, 12, 31, 23,
            0);
    LocalDateTime nextYearStart = LocalDateTime.of(2024, 1, 1, 0,
            0);

    Event yearEndEvent = new Event("Year End", yearEnd, yearEnd.plusMinutes(15),
            null, null, true);
    Event nextYearEvent = new Event("Next Year Start", nextYearStart.plusHours(1),
            nextYearStart.plusHours(2), null, null, true);

    assertTrue(calendar.addEvent(yearEndEvent, true));
    assertTrue(calendar.addEvent(nextYearEvent, true));

    // Verify events are found in correct ranges
    assertTrue(calendar.getEventsInRange(baseTime.toLocalDate(),
            baseTime.toLocalDate()).contains(dayStartEvent));
    assertTrue(calendar.getEventsInRange(monthEnd.toLocalDate(),
            nextMonthStart.toLocalDate()).containsAll(Arrays.asList(monthEndEvent,
            nextMonthEvent)));
    assertTrue(calendar.getEventsInRange(yearEnd.toLocalDate(),
            nextYearStart.toLocalDate()).containsAll(Arrays.asList(yearEndEvent, nextYearEvent)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventWithInvalidDateTimeRange() throws ConflictingEventException {
    // Test adding event with end time before start time
    Event invalidEvent = new Event("Invalid Meeting", endDateTime, startDateTime,
            "Description", "Location", true);
    calendar.addEvent(invalidEvent, false);
  }

  @Test
  public void testAddEventWithFutureDate() throws ConflictingEventException {
    // Test adding event with future date
    LocalDateTime futureDate = LocalDateTime.now().plusYears(1);
    Event futureEvent = new Event("Future Meeting", futureDate, futureDate.plusHours(1),
            "Description", "Location", true);
    assertTrue(calendar.addEvent(futureEvent, false));
  }

  @Test
  public void testAddEventWithPastDate() throws ConflictingEventException {
    // Test adding event with past date
    LocalDateTime pastDate = LocalDateTime.now().minusYears(1);
    Event pastEvent = new Event("Past Meeting", pastDate, pastDate.plusHours(1),
            "Description", "Location", true);
    assertTrue(calendar.addEvent(pastEvent, false));
  }

  @Test
  public void testAddEventWithMaxDuration() throws ConflictingEventException {
    // Test adding event with maximum allowed duration
    Event maxDurationEvent = new Event("Long Meeting", startDateTime,
            startDateTime.plusDays(30), "Description", "Location", true);
    assertTrue(calendar.addEvent(maxDurationEvent, false));
  }

  @Test
  public void testAddEventWithSpecialCharacters() throws ConflictingEventException {
    // Test adding event with special characters in name
    Event specialCharEvent = new Event("Meeting@#$%", startDateTime,
            endDateTime, "Description", "Location", true);
    assertTrue(calendar.addEvent(specialCharEvent, false));
  }

  /**
   * Test that createRecurringEventUntil catches IllegalArgumentException.
   */
  @Test
  public void testCreateRecurringEventUntilWithInvalidWeekdays() throws ConflictingEventException {
    String invalidWeekdays = "XYZ"; // Invalid weekday characters
    boolean result = calendar.createRecurringEventUntil("Invalid Recurring Event",
            LocalDateTime.of(2023, 5, 8, 14, 0),
            LocalDateTime.of(2023, 5, 8, 15, 0),
            invalidWeekdays, LocalDate.of(2023, 5, 31), false);
    assertFalse(result);

    assertEquals(0, calendar.getAllEvents().size());
    assertEquals(0, calendar.getAllRecurringEvents().size());
  }

  /**
   * Test recurring event with end date before start date.
   */
  @Test
  public void testCreateRecurringEventUntilWithEndDateBeforeStartDate() throws
          ConflictingEventException {
    LocalDateTime start = LocalDateTime.of(2023, 5, 15, 14, 0);
    LocalDateTime end = LocalDateTime.of(2023, 5, 15, 15, 0);
    LocalDate untilDate = LocalDate.of(2023, 5, 1);

    boolean result = calendar.createRecurringEventUntil("Invalid Recurring Event",
            start, end, "MWF", untilDate, false);

    assertFalse(result);
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateRecurringEventUntilWithFarFutureDate() throws ConflictingEventException {
    LocalDateTime start = LocalDateTime.of(2023, 5, 15, 14, 0);
    LocalDateTime end = LocalDateTime.of(2023, 5, 15, 15, 0);
    LocalDate untilDate = LocalDate.of(2033, 5, 15);

    boolean result = calendar.createRecurringEventUntil("Far Future Recurring Event",
            start, end, "MWF", untilDate, false);

    assertTrue(result);

    List<Event> events = calendar.getAllEvents();
    assertTrue("Should have many occurrences", events.size() > 100);
  }

  @Test
  public void testCreateAllDayRecurringEventWithNullParams() throws ConflictingEventException {
    LocalDate start = LocalDate.of(2023, 5, 8);

    assertFalse(calendar.createAllDayRecurringEvent(null, start, "MWF",
            3, false, "Description", "Location",
            true));

    assertFalse(calendar.createAllDayRecurringEvent("Event", start, null,
            3, false, "Description", "Location",
            true));

    assertTrue(calendar.createAllDayRecurringEvent("Event", start, "MWF",
            3, false, null, null, true));

    try {
      RecurringEvent recurringEvent = new RecurringEvent.Builder("Test Event",
              null, null,
              EnumSet.of(DayOfWeek.MONDAY)).occurrences(3).build();
      fail("Expected IllegalArgumentException but none was thrown");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
  }

  @Test
  public void testCreateAllDayRecurringEventUntilWithNullParams() throws ConflictingEventException {
    LocalDate start = LocalDate.of(2023, 5, 8);
    LocalDate until = LocalDate.of(2023, 5, 22);

    assertFalse(calendar.createAllDayRecurringEventUntil(null, start,
            "MWF", until, false, "Description",
            "Location", true));

    assertFalse(calendar.createAllDayRecurringEventUntil("Event", start,
            null, until, false, "Description",
            "Location", true));

    try {
      RecurringEvent recurringEvent = new RecurringEvent.Builder("Test Event",
              LocalDateTime.of(2023, 5, 8, 0, 0),
              LocalDateTime.of(2023, 5, 8, 23, 59),
              EnumSet.of(DayOfWeek.MONDAY)).endDate(null)
              .build();
      fail("Expected IllegalArgumentException but none was thrown for null end date");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
  }


  @Test
  public void testRecurringEventComplexConflictScenarios() throws ConflictingEventException {
    LocalDateTime start1 = LocalDateTime.of(2023, 5, 8, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2023, 5, 8, 11, 0);
    Set<DayOfWeek> days1 = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

    RecurringEvent event1 = new RecurringEvent
            .Builder("Recurring Event 1", start1, end1, days1).occurrences(4).build();

    assertTrue(calendar.addRecurringEvent(event1, false));

    LocalDateTime start2 = LocalDateTime.of(2023, 5, 10, 10,
            30);
    LocalDateTime end2 = LocalDateTime.of(2023, 5, 10, 11, 30);
    Set<DayOfWeek> days2 = EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    RecurringEvent event2 = new RecurringEvent.Builder("Recurring Event 2",
            start2, end2, days2).occurrences(4).build();

    assertFalse(calendar.addRecurringEvent(event2, false));

    Exception exception = assertThrows(ConflictingEventException.class, () -> {
      calendar.addRecurringEvent(event2, true);
    });

    assertTrue(exception.getMessage().contains("conflict"));

    assertEquals(1, calendar.getAllRecurringEvents().size());
    assertEquals(4, calendar.getAllEvents().size());
  }


  @Test
  public void testEditRecurringEventProperties() throws ConflictingEventException {
    calendar.addRecurringEvent(recurringEvent, false);

    int count = calendar.editAllEvents("Recurring Meeting", "subject",
            "Updated Meeting");
    assertEquals(4, count);

    List<Event> events = calendar.getAllEvents();
    for (Event event : events) {
      assertEquals("Updated Meeting", event.getSubject());
    }

    count = calendar.editAllEvents("Updated Meeting", "location",
            "New Location");
    assertEquals(4, count);

    events = calendar.getAllEvents();
    for (Event event : events) {
      assertEquals("New Location", event.getLocation());
    }
  }

  @Test
  public void testEditEventsFromDate() throws ConflictingEventException {
    calendar.addRecurringEvent(recurringEvent, false);

    List<Event> allEvents = calendar.getAllEvents();
    assertEquals(4, allEvents.size());

    Event secondOccurrence = allEvents.get(1);

    int count = calendar.editEventsFromDate("Recurring Meeting",
            secondOccurrence.getStartDateTime(), "subject",
            "Updated from Second");

    assertEquals(3, count);

    Event firstOccurrence = calendar.findEvent("Recurring Meeting",
            allEvents.get(0).getStartDateTime());
    assertNotNull(firstOccurrence);
    assertEquals("Recurring Meeting", firstOccurrence.getSubject());

    for (int i = 1; i < allEvents.size(); i++) {
      Event occurrence = calendar.findEvent("Updated from Second",
              allEvents.get(i).getStartDateTime());
      assertNotNull(occurrence);
      assertEquals("Updated from Second", occurrence.getSubject());
    }
  }

  @Test
  public void testCreateAllDayRecurringEventWithInvalidOccurrences()
          throws ConflictingEventException {
    LocalDate start = LocalDate.of(2023, 5, 8);

    assertFalse(calendar.createAllDayRecurringEvent("Zero Occurrences",
            start, "MWF", 0, false,
            "Description", "Location", true));

    assertFalse(calendar.createAllDayRecurringEvent("Negative Occurrences", start,
            "MWF", -5, false, "Description",
            "Location", true));

    assertEquals(0, calendar.getAllEvents().size());
    assertEquals(0, calendar.getAllRecurringEvents().size());
  }

  @Test
  public void testCopyWithTimezoneConversion() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime, "Test Description",
        "Test Location", true);
    calendar.addEvent(testEvent, false);

    Calendar mockCalendar = new Calendar() {
      @Override
      public String exportToCSV(String filePath) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
                + "Description,Location," + "Private\n");
        csv.append("Test Meeting,03/15/2024,10:00 AM,03/15/2024,11:00 AM,False,"
                + "Test Description,Test Location,False\n");
        mockFileSystem.put(filePath, csv.toString());

        return filePath;
      }
    };
    mockCalendar.setName("Test Mock Calendar");
    mockCalendar.setTimezone("America/New_York");

    String result = mockCalendar.exportToCSV("calendar_export.csv");

    assertTrue(result.contains("copied successfully"));
  }

}