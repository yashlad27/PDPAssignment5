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
  public void testFilterComposition() {
    EventFilter allDayFilter = event -> event != null && event.isAllDay();
    EventFilter dateFilter = event -> event != null && event.getDate().equals(LocalDate.of(2023, 5, 29));
    
    // Test AND composition
    EventFilter composedFilter = allDayFilter.and(dateFilter);
    List<Event> filteredEvents = composedFilter.filterEvents(events);
    assertEquals("Should find 1 matching event", 1, filteredEvents.size());
    assertTrue("Should include all-day event", filteredEvents.contains(allDayEvent));
  }
}
