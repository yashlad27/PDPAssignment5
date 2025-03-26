import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.calendar.EventFilter;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the EventFilter interface.
 */
public class EventFilterTest {

  private List<Event> events;
  private Event regularEvent;
  private Event allDayEvent;
  private Event multiDayEvent;
  private Event midnightEvent;
  private Event endOfDayEvent;

  @Before
  public void setUp() {
    events = new ArrayList<>();

    regularEvent = new Event("Team Meeting",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 30),
            "Weekly team sync", "Conference Room A", true);

    allDayEvent = Event.createAllDayEvent("Company Holiday",
            LocalDate.of(2023, 5, 29),
            "Memorial Day", null, true);

    multiDayEvent = new Event("Conference",
            LocalDateTime.of(2023, 6, 1, 9, 0),
            LocalDateTime.of(2023, 6, 3, 17, 0),
            "Annual tech conference", "Convention Center", true);

    midnightEvent = new Event("Midnight Meeting",
            LocalDateTime.of(2023, 5, 15, 0, 0),
            LocalDateTime.of(2023, 5, 15, 1, 0),
            "Early morning meeting", "Virtual", true);

    endOfDayEvent = new Event("End of Day Meeting",
            LocalDateTime.of(2023, 5, 15, 23, 0),
            LocalDateTime.of(2023, 5, 15, 23, 59),
            "Late night meeting", "Virtual", true);

    events.add(regularEvent);
    events.add(allDayEvent);
    events.add(multiDayEvent);
    events.add(midnightEvent);
    events.add(endOfDayEvent);
  }

  @Test
  public void testDateRangeFilter() {
    LocalDate targetDate = LocalDate.of(2023, 5, 15);
    EventFilter dateRangeFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      if (event.isAllDay()) {
        return event.getDate().equals(targetDate);
      }
      LocalDateTime start = event.getStartDateTime();
      LocalDateTime end = event.getEndDateTime();
      LocalDate startDate = start.toLocalDate();
      LocalDate endDate = end.toLocalDate();
      return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
    };

    List<Event> filteredEvents = dateRangeFilter.filterEvents(events);
    assertEquals("Should find 3 events on May 15", 3, filteredEvents.size());
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertTrue("Should include midnight event", filteredEvents.contains(midnightEvent));
    assertTrue("Should include end of day event", filteredEvents.contains(endOfDayEvent));
  }

  @Test
  public void testAllDayEventFilter() {
    LocalDate targetDate = LocalDate.of(2023, 5, 29);
    EventFilter allDayFilter = event ->
            event != null && event.isAllDay() && event.getDate().equals(targetDate);

    List<Event> filteredEvents = allDayFilter.filterEvents(events);
    assertEquals("Should find 1 all-day event", 1, filteredEvents.size());
    assertTrue("Should include all-day event", filteredEvents.contains(allDayEvent));
  }

  @Test
  public void testMultiDayEventFilter() {
    LocalDate targetDate = LocalDate.of(2023, 6, 2);
    EventFilter multiDayFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      LocalDateTime start = event.getStartDateTime();
      LocalDateTime end = event.getEndDateTime();
      LocalDate startDate = start.toLocalDate();
      LocalDate endDate = end.toLocalDate();
      return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
    };

    List<Event> filteredEvents = multiDayFilter.filterEvents(events);
    assertEquals("Should find 1 multi-day event", 1, filteredEvents.size());
    assertTrue("Should include multi-day event", filteredEvents.contains(multiDayEvent));
  }

  @Test
  public void testEmptyListFilter() {
    EventFilter anyFilter = event -> true;
    List<Event> filteredEvents = anyFilter.filterEvents(new ArrayList<>());
    assertTrue("Should return empty list", filteredEvents.isEmpty());
  }

  @Test
  public void testNullEventFilter() {
    EventFilter nullFilter = event -> event != null;
    assertFalse("Should not match null event", nullFilter.matches(null));
  }

  @Test
  public void testTimeRangeFilter() {
    LocalDateTime startTime = LocalDateTime.of(2023, 5, 15,
            8, 0);
    LocalDateTime endTime = LocalDateTime.of(2023, 5, 15,
            17, 0);

    EventFilter timeRangeFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      return !event.getStartDateTime().isBefore(startTime)
              && !event.getEndDateTime().isAfter(endTime);
    };

    List<Event> filteredEvents = timeRangeFilter.filterEvents(events);
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertFalse("Should not include midnight event",
            filteredEvents.contains(midnightEvent));
    assertFalse("Should not include end of day event",
            filteredEvents.contains(endOfDayEvent));
  }

  @Test
  public void testLocationFilter() {
    String targetLocation = "Conference Room A";

    EventFilter locationFilter = event ->
            event != null && targetLocation.equals(event.getLocation());

    List<Event> filteredEvents = locationFilter.filterEvents(events);
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertFalse("Should not include all-day event", filteredEvents.contains(allDayEvent));
  }

  @Test
  public void testPublicEventFilter() {
    EventFilter publicFilter = event -> event != null && event.isPublic();

    List<Event> filteredEvents = publicFilter.filterEvents(events);
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertTrue("Should include all-day event", filteredEvents.contains(allDayEvent));
  }

  @Test
  public void testDescriptionFilter() {
    String targetDescription = "Weekly team sync";

    EventFilter descriptionFilter = event ->
            event != null && targetDescription.equals(event.getDescription());

    List<Event> filteredEvents = descriptionFilter.filterEvents(events);
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertFalse("Should not include all-day event", filteredEvents.contains(allDayEvent));
  }

  @Test
  public void testFilterWithNullValues() {
    Event nullLocationEvent = new Event("Null Location Event",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 0),
            "Description", null, true);

    events.add(nullLocationEvent);

    EventFilter nullLocationFilter = event ->
            event != null && event.getLocation().isEmpty();

    List<Event> filteredEvents = nullLocationFilter.filterEvents(events);
    assertTrue("Should include null location event",
            filteredEvents.contains(nullLocationEvent));
    assertFalse("Should not include regular event", filteredEvents.contains(regularEvent));
  }

  @Test
  public void testFilterWithEmptyValues() {
    Event emptyDescriptionEvent = new Event("Empty Description Event",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 0),
            "", "Location", true);

    events.add(emptyDescriptionEvent);

    EventFilter emptyDescriptionFilter = event ->
            event != null && event.getDescription() != null && event.getDescription().isEmpty();

    List<Event> filteredEvents = emptyDescriptionFilter.filterEvents(events);
    assertTrue("Should include empty description event",
            filteredEvents.contains(emptyDescriptionEvent));
    assertFalse("Should not include regular event", filteredEvents.contains(regularEvent));
  }

  @Test
  public void testFilterWithSpecialCharacters() {
    String specialDescription = "Meeting with \"quotes\" and, commas";
    Event specialEvent = new Event("Special Event",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 0),
            specialDescription, "Location", true);

    events.add(specialEvent);

    EventFilter specialDescriptionFilter = event ->
            event != null && specialDescription.equals(event.getDescription());

    List<Event> filteredEvents = specialDescriptionFilter.filterEvents(events);
    assertTrue("Should include special event", filteredEvents.contains(specialEvent));
    assertFalse("Should not include regular event", filteredEvents.contains(regularEvent));
  }

  @Test
  public void testFilterWithInvalidDateRange() {
    LocalDate startDate = LocalDate.of(2024, 3, 26);
    LocalDate endDate = LocalDate.of(2024, 3, 25);
    EventFilter dateRangeFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      LocalDateTime start = event.getStartDateTime();
      LocalDateTime end = event.getEndDateTime();
      LocalDate eventStartDate = start.toLocalDate();
      LocalDate eventEndDate = end.toLocalDate();
      return !startDate.isBefore(eventStartDate) && !endDate.isAfter(eventEndDate);
    };
    List<Event> filteredEvents = dateRangeFilter.filterEvents(events);
    assertTrue("Should return empty list for invalid date range", filteredEvents.isEmpty());
  }

  @Test
  public void testFilterWithFutureDateRange() {
    LocalDate futureStart = LocalDate.now().plusYears(1);
    LocalDate futureEnd = futureStart.plusMonths(1);
    EventFilter futureDateFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      LocalDateTime start = event.getStartDateTime();
      LocalDateTime end = event.getEndDateTime();
      LocalDate eventStartDate = start.toLocalDate();
      LocalDate eventEndDate = end.toLocalDate();
      return !futureStart.isBefore(eventStartDate) && !futureEnd.isAfter(eventEndDate);
    };
    List<Event> filteredEvents = futureDateFilter.filterEvents(events);
    assertTrue("Should return empty list for future dates", filteredEvents.isEmpty());
  }

  @Test
  public void testFilterWithPastDateRange() {
    LocalDate pastStart = LocalDate.now().minusYears(1);
    LocalDate pastEnd = pastStart.plusMonths(1);
    EventFilter pastDateFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      LocalDateTime start = event.getStartDateTime();
      LocalDateTime end = event.getEndDateTime();
      LocalDate eventStartDate = start.toLocalDate();
      LocalDate eventEndDate = end.toLocalDate();
      return !pastStart.isBefore(eventStartDate) && !pastEnd.isAfter(eventEndDate);
    };
    List<Event> filteredEvents = pastDateFilter.filterEvents(events);
    assertTrue("Should return empty list for past dates", filteredEvents.isEmpty());
  }

  @Test
  public void testFilterWithSameStartEndDate() {
    LocalDate date = LocalDate.now();
    EventFilter sameDateFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      LocalDateTime start = event.getStartDateTime();
      LocalDateTime end = event.getEndDateTime();
      LocalDate eventStartDate = start.toLocalDate();
      LocalDate eventEndDate = end.toLocalDate();
      return date.equals(eventStartDate) && date.equals(eventEndDate);
    };
    List<Event> filteredEvents = sameDateFilter.filterEvents(events);
    assertNotNull("Filtered events list should not be null", filteredEvents);
  }

  @Test
  public void testMatches() {
    Event event1 = new Event("Meeting", LocalDateTime.of(2025,
            3, 26, 10, 0),
            LocalDateTime.of(2025, 3, 26, 11, 0),
            "Project discussion", "Room 101", true);
    Event event2 = new Event("Workshop", LocalDateTime.of(2025,
            3, 27, 14, 0),
            LocalDateTime.of(2025, 3, 27, 16, 0),
            "Tech workshop", "Hall A", false);

    EventFilter meetingFilter = event -> "Meeting".equals(event.getSubject());
    assertTrue(meetingFilter.matches(event1));
    assertFalse(meetingFilter.matches(event2));
  }

  @Test
  public void testAnd() {
    Event event1 = new Event("Meeting", LocalDateTime.of(2025, 3,
            26, 10, 0), LocalDateTime.of(2025, 3,
            26, 11, 0), "Project discussion",
            "Room 101", true);
    Event event2 = new Event("Workshop", LocalDateTime.of(2025, 3,
            27, 14, 0), LocalDateTime.of(2025, 3,
            27, 16, 0), "Tech workshop", "Hall A",
            false);

    EventFilter meetingFilter = event -> "Meeting".equals(event.getSubject());
    EventFilter dateFilter = event -> event
            .getStartDateTime().toLocalDate().toString().equals("2025-03-26");

    EventFilter combinedFilter = meetingFilter.and(dateFilter);
    assertTrue(combinedFilter.matches(event1));
    assertFalse(combinedFilter.matches(event2));
  }

  @Test
  public void testNegate() {
    Event event1 = new Event("Meeting", LocalDateTime.of(2025, 3,
            26, 10, 0), LocalDateTime.of(2025, 3,
            26, 11, 0), "Project discussion",
            "Room 101", true);
    Event event2 = new Event("Workshop", LocalDateTime.of(2025, 3,
            27, 14, 0), LocalDateTime.of(2025, 3,
            27, 16, 0), "Tech workshop",
            "Hall A", false);

    EventFilter meetingFilter = event -> "Meeting".equals(event.getSubject());
    EventFilter notMeetingFilter = meetingFilter.negate();

    assertFalse(notMeetingFilter.matches(event1));
    assertTrue(notMeetingFilter.matches(event2));
  }

  @Test
  public void testFilterEvents() {
    Event event1 = new Event("Meeting", LocalDateTime.of(2025, 3,
            26, 10, 0), LocalDateTime.of(2025, 3,
            26, 11, 0), "Project discussion",
            "Room 101", true);
    Event event2 = new Event("Workshop", LocalDateTime.of(2025, 3,
            27, 14, 0), LocalDateTime.of(2025, 3,
            27, 16, 0), "Tech workshop",
            "Hall A", false);
    Event event3 = new Event("Conference", LocalDateTime.of(2025, 3,
            28, 9, 0), LocalDateTime.of(2025,
            3, 28, 17, 0),
            "Annual conference", "Conference Hall", true);
    List<Event> events = Arrays.asList(event1, event2, event3);

    EventFilter workshopFilter = event -> "Workshop".equals(event.getSubject());
    List<Event> filteredEvents = workshopFilter.filterEvents(events);

    assertEquals(1, filteredEvents.size());
    assertEquals("Workshop", filteredEvents.get(0).getSubject());
  }
}
