import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.calendar.EventFilter;
import model.event.Event;
import model.event.RecurringEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

  @After
  public void tearDown() {
    events.clear();
    regularEvent = null;
    allDayEvent = null;
    multiDayEvent = null;
    midnightEvent = null;
    endOfDayEvent = null;
  }

  @Test
  public void testDateRangeAndTimeFilter() {
    LocalDate targetDate = LocalDate.of(2023, 5, 15);
    LocalDateTime filterStart = LocalDateTime.of(2023, 5, 15, 8, 0);
    LocalDateTime filterEnd = LocalDateTime.of(2023, 5, 15, 17, 0);

    EventFilter dateTimeFilter = event -> {
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
      return !targetDate.isBefore(startDate) &&
              !targetDate.isAfter(endDate);
    };

    List<Event> filteredEvents = dateTimeFilter.filterEvents(events);
    assertEquals("Should find 3 events on the target date", 3, filteredEvents.size());
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertTrue("Should include midnight event", filteredEvents.contains(midnightEvent));
    assertTrue("Should include end of day event", filteredEvents.contains(endOfDayEvent));
  }

  @Test
  public void testTextBasedFilter() {
    EventFilter textFilter = event ->
            event != null &&
                    event.getDescription() != null &&
                    event.getLocation() != null &&
                    (event.getDescription().toLowerCase().contains("meeting") ||
                            event.getDescription().toLowerCase().contains("sync") ||
                            event.getLocation().toLowerCase().contains("room") ||
                            event.getLocation().toLowerCase().contains("center") ||
                            event.getLocation().toLowerCase().contains("virtual"));

    List<Event> filteredEvents = textFilter.filterEvents(events);
    assertEquals("Should find 4 events with matching text", 4, filteredEvents.size());
    assertTrue("Should include regular event", filteredEvents.contains(regularEvent));
    assertTrue("Should include multi-day event", filteredEvents.contains(multiDayEvent));
    assertTrue("Should include midnight event", filteredEvents.contains(midnightEvent));
    assertTrue("Should include end of day event", filteredEvents.contains(endOfDayEvent));
  }

  @Test
  public void testRecurringEventFilter() {
    List<Event> testEvents = new ArrayList<>();
    LocalDateTime baseTime = LocalDateTime.of(2023, 5, 15, 9, 0);
    RecurringEvent recurringEvent = new RecurringEvent.Builder(
            "Weekly Meeting",
            baseTime,
            baseTime.plusHours(1),
            java.util.EnumSet.of(java.time.DayOfWeek.MONDAY))
            .description("Weekly sync")
            .location("Room 1")
            .isPublic(true)
            .occurrences(4)
            .build();

    testEvents.add(recurringEvent);

    LocalDate targetDate = baseTime.toLocalDate();
    EventFilter recurringFilter = event1 ->
            event1 != null &&
                    event1.getStartDateTime() != null &&
                    event1.getStartDateTime().toLocalDate().equals(targetDate);

    List<Event> filteredEvents = recurringFilter.filterEvents(testEvents);
    assertEquals("Should find 1 recurring event", 1, filteredEvents.size());
    assertTrue("Should include the recurring event", filteredEvents.contains(recurringEvent));
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
}
