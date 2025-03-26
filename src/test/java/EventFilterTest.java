import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.calendar.EventFilter;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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

    // Regular event
    regularEvent = new Event("Team Meeting",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 30),
            "Weekly team sync", "Conference Room A", true);

    // All-day event
    allDayEvent = Event.createAllDayEvent("Company Holiday",
            LocalDate.of(2023, 5, 29),
            "Memorial Day", null, true);

    // Multi-day event
    multiDayEvent = new Event("Conference",
            LocalDateTime.of(2023, 6, 1, 9, 0),
            LocalDateTime.of(2023, 6, 3, 17, 0),
            "Annual tech conference", "Convention Center", true);

    // Event starting at midnight
    midnightEvent = new Event("Midnight Meeting",
            LocalDateTime.of(2023, 5, 15, 0, 0),
            LocalDateTime.of(2023, 5, 15, 1, 0),
            "Early morning meeting", "Virtual", true);

    // Event ending at end of day
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
    LocalDateTime startTime = LocalDateTime.of(2023, 5, 15, 8, 0);
    LocalDateTime endTime = LocalDateTime.of(2023, 5, 15, 17, 0);
    
    EventFilter timeRangeFilter = event -> {
      if (event == null || event.getStartDateTime() == null || event.getEndDateTime() == null) {
        return false;
      }
      return !event.getStartDateTime().isBefore(startTime) && !event.getEndDateTime().isAfter(endTime);
    };

    List<Event> filteredEvents = timeRangeFilter.filterEvents(events);
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertFalse("Should not include midnight event", filteredEvents.contains(midnightEvent));
    assertFalse("Should not include end of day event", filteredEvents.contains(endOfDayEvent));
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
    assertTrue("Should include null location event", filteredEvents.contains(nullLocationEvent));
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
    assertTrue("Should include empty description event", filteredEvents.contains(emptyDescriptionEvent));
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
}
