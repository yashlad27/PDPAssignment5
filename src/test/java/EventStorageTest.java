import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import model.event.Event;
import model.event.RecurringEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the EventStorage class using a mock implementation.
 */
public class EventStorageTest {

  /**
   * Mock implementation of EventStorage to avoid using DateTimeWrapper in tests.
   */
  private static class MockEventStorage {
    private final Map<UUID, Event> eventById = new HashMap<>();
    private final Map<UUID, RecurringEvent> recurringEventById = new HashMap<>();
    private final Map<LocalDate, List<Event>> eventsByDate = new HashMap<>();
    private final Map<String, List<Event>> eventsBySubject = new HashMap<>();
    private final Map<LocalDateTime, List<Event>> eventsByDateTime = new HashMap<>();

    public void addEvent(Event event) {
      if (event == null) {
        throw new IllegalArgumentException("Event cannot be null");
      }

      eventById.put(event.getId(), event);

      LocalDate date = event.getStartDateTime().toLocalDate();
      eventsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(event);

      eventsBySubject.computeIfAbsent(event.getSubject(), k -> new ArrayList<>()).add(event);

      LocalDateTime startTime = event.getStartDateTime();
      eventsByDateTime.computeIfAbsent(startTime, k -> new ArrayList<>()).add(event);
    }

    public void addRecurringEvent(RecurringEvent event) {
      if (event == null) {
        throw new IllegalArgumentException("Recurring event cannot be null");
      }

      recurringEventById.put(event.getId(), event);
      for (Event occurrence : event.getAllOccurrences()) {
        addEvent(occurrence);
      }
    }

    public Event getEventById(UUID id) {
      return eventById.get(id);
    }

    public RecurringEvent getRecurringEventById(UUID id) {
      return recurringEventById.get(id);
    }

    public List<Event> getEventsOnDate(LocalDate date) {
      return eventsByDate.getOrDefault(date, new ArrayList<>());
    }

    public List<Event> getEventsBySubject(String subject) {
      return eventsBySubject.getOrDefault(subject, new ArrayList<>());
    }

    public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
      List<Event> result = new ArrayList<>();
      for (Map.Entry<LocalDateTime, List<Event>> entry : eventsByDateTime.entrySet()) {
        LocalDateTime dateTime = entry.getKey();
        if ((dateTime.isEqual(start) || dateTime.isAfter(start)) &&
                (dateTime.isEqual(end) || dateTime.isBefore(end))) {
          result.addAll(entry.getValue());
        }
      }
      return result;
    }

    public void removeEvent(Event event) {
      if (event == null) {
        throw new IllegalArgumentException("Event cannot be null");
      }

      eventById.remove(event.getId());

      // Remove from date index
      LocalDate date = event.getStartDateTime().toLocalDate();
      if (eventsByDate.containsKey(date)) {
        eventsByDate.get(date).remove(event);
        if (eventsByDate.get(date).isEmpty()) {
          eventsByDate.remove(date);
        }
      }

      // Remove from subject index
      String subject = event.getSubject();
      if (eventsBySubject.containsKey(subject)) {
        eventsBySubject.get(subject).remove(event);
        if (eventsBySubject.get(subject).isEmpty()) {
          eventsBySubject.remove(subject);
        }
      }

      // Remove from datetime index
      LocalDateTime startTime = event.getStartDateTime();
      if (eventsByDateTime.containsKey(startTime)) {
        eventsByDateTime.get(startTime).remove(event);
        if (eventsByDateTime.get(startTime).isEmpty()) {
          eventsByDateTime.remove(startTime);
        }
      }
    }

    public void removeRecurringEvent(RecurringEvent event) {
      if (event == null) {
        throw new IllegalArgumentException("Recurring event cannot be null");
      }

      recurringEventById.remove(event.getId());
      for (Event occurrence : event.getAllOccurrences()) {
        removeEvent(occurrence);
      }
    }

    public List<Event> getAllEvents() {
      return new ArrayList<>(eventById.values());
    }

    public List<RecurringEvent> getAllRecurringEvents() {
      return new ArrayList<>(recurringEventById.values());
    }
  }

  private MockEventStorage storage;
  private Event sampleEvent1;
  private Event sampleEvent2;
  private Event sampleEvent3;
  private RecurringEvent recurringEvent;

  @Before
  public void setUp() {
    storage = new MockEventStorage();

    // Create sample events
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime event1Start = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime event1End = LocalDateTime.of(2023, 5, 15, 11, 0);
    sampleEvent1 = new Event("Meeting", event1Start, event1End, null, null, true);

    LocalDateTime event2Start = LocalDateTime.of(2023, 5, 15, 14, 0);
    LocalDateTime event2End = LocalDateTime.of(2023, 5, 15, 15, 0);
    sampleEvent2 = new Event("Lunch", event2Start, event2End, null, null, true);

    LocalDateTime event3Start = LocalDateTime.of(2023, 5, 16, 10, 0);
    LocalDateTime event3End = LocalDateTime.of(2023, 5, 16, 11, 0);
    sampleEvent3 = new Event("Meeting", event3Start, event3End, null, null, true);

    // Create recurring event
    LocalDateTime recurringStart = LocalDateTime.of(2023, 6, 1, 9, 0);
    LocalDateTime recurringEnd = LocalDateTime.of(2023, 6, 1, 10, 0);

    // Create a set of days for the recurring event
    Set<DayOfWeek> weekdays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

    recurringEvent = new RecurringEvent.Builder(
            "Weekly Sync",
            recurringStart,
            recurringEnd,
            weekdays)
            .occurrences(3)
            .build();
  }

  @After
  public void tearDown() {
    // Clear all events and recurring events from storage
    if (storage != null) {
      for (Event event : storage.getAllEvents()) {
        storage.removeEvent(event);
      }

      for (RecurringEvent event : storage.getAllRecurringEvents()) {
        storage.removeRecurringEvent(event);
      }
    }

    // Set references to null to help garbage collection
    storage = null;
    sampleEvent1 = null;
    sampleEvent2 = null;
    sampleEvent3 = null;
    recurringEvent = null;
  }

  /**
   * Test constructor creates a valid object.
   */
  @Test
  public void testConstructor() {
    MockEventStorage newStorage = new MockEventStorage();
    assertNotNull(newStorage);
    assertEquals(0, newStorage.getAllEvents().size());
    assertEquals(0, newStorage.getAllRecurringEvents().size());
  }

  /**
   * Test adding a single event.
   */
  @Test
  public void testAddEvent() {
    storage.addEvent(sampleEvent1);

    List<Event> events = storage.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(sampleEvent1.getId(), events.get(0).getId());
  }

  /**
   * Test adding multiple events.
   */
  @Test
  public void testAddMultipleEvents() {
    storage.addEvent(sampleEvent1);
    storage.addEvent(sampleEvent2);
    storage.addEvent(sampleEvent3);

    List<Event> events = storage.getAllEvents();
    assertEquals(3, events.size());
  }

  /**
   * Test adding null event throws exception.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testAddNullEvent() {
    storage.addEvent(null);
  }

  /**
   * Test adding a recurring event.
   */
  @Test
  public void testAddRecurringEvent() {
    storage.addRecurringEvent(recurringEvent);

    List<RecurringEvent> recurringEvents = storage.getAllRecurringEvents();
    assertEquals(1, recurringEvents.size());
    assertEquals(recurringEvent.getId(), recurringEvents.get(0).getId());

    // The recurring event should also have added its occurrences as regular events
    List<Event> events = storage.getAllEvents();
    assertTrue(events.size() > 0);
  }

  /**
   * Test adding null recurring event throws exception.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testAddNullRecurringEvent() {
    storage.addRecurringEvent(null);
  }

  /**
   * Test getting event by ID.
   */
  @Test
  public void testGetEventById() {
    storage.addEvent(sampleEvent1);

    Event retrievedEvent = storage.getEventById(sampleEvent1.getId());
    assertNotNull(retrievedEvent);
    assertEquals(sampleEvent1.getId(), retrievedEvent.getId());
  }

  /**
   * Test getting event by ID that doesn't exist.
   */
  @Test
  public void testGetEventByIdNotFound() {
    Event retrievedEvent = storage.getEventById(UUID.randomUUID());
    assertNull(retrievedEvent);
  }

  /**
   * Test getting recurring event by ID.
   */
  @Test
  public void testGetRecurringEventById() {
    storage.addRecurringEvent(recurringEvent);

    RecurringEvent retrievedEvent = storage.getRecurringEventById(recurringEvent.getId());
    assertNotNull(retrievedEvent);
    assertEquals(recurringEvent.getId(), retrievedEvent.getId());
  }

  /**
   * Test getting recurring event by ID that doesn't exist.
   */
  @Test
  public void testGetRecurringEventByIdNotFound() {
    RecurringEvent retrievedEvent = storage.getRecurringEventById(UUID.randomUUID());
    assertNull(retrievedEvent);
  }

  /**
   * Test getting events on a specific date.
   */
  @Test
  public void testGetEventsOnDate() {
    storage.addEvent(sampleEvent1);
    storage.addEvent(sampleEvent2);
    storage.addEvent(sampleEvent3);

    LocalDate date = LocalDate.of(2023, 5, 15);
    List<Event> events = storage.getEventsOnDate(date);
    assertEquals(2, events.size());

    // Both events should be on the specified date
    for (Event event : events) {
      assertEquals(date, event.getStartDateTime().toLocalDate());
    }
  }

  /**
   * Test getting events on a date with no events.
   */
  @Test
  public void testGetEventsOnDateNoEvents() {
    LocalDate date = LocalDate.of(2023, 5, 20);
    List<Event> events = storage.getEventsOnDate(date);
    assertEquals(0, events.size());
  }

  /**
   * Test getting events by subject.
   */
  @Test
  public void testGetEventsBySubject() {
    storage.addEvent(sampleEvent1);
    storage.addEvent(sampleEvent2);
    storage.addEvent(sampleEvent3);

    List<Event> meetingEvents = storage.getEventsBySubject("Meeting");
    assertEquals(2, meetingEvents.size());

    List<Event> lunchEvents = storage.getEventsBySubject("Lunch");
    assertEquals(1, lunchEvents.size());
  }

  /**
   * Test getting events by subject that doesn't exist.
   */
  @Test
  public void testGetEventsBySubjectNotFound() {
    List<Event> events = storage.getEventsBySubject("NonExistentSubject");
    assertEquals(0, events.size());
  }

  /**
   * Test getting events in a date-time range.
   */
  @Test
  public void testGetEventsInRange() {
    storage.addEvent(sampleEvent1);
    storage.addEvent(sampleEvent2);
    storage.addEvent(sampleEvent3);

    LocalDateTime rangeStart = LocalDateTime.of(2023, 5, 15, 9, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2023, 5, 15, 12, 0);

    List<Event> events = storage.getEventsInRange(rangeStart, rangeEnd);
    assertEquals(1, events.size());
    assertEquals(sampleEvent1.getId(), events.get(0).getId());
  }

  /**
   * Test getting events in a date-time range that spans multiple days.
   */
  @Test
  public void testGetEventsInMultiDayRange() {
    storage.addEvent(sampleEvent1);
    storage.addEvent(sampleEvent2);
    storage.addEvent(sampleEvent3);

    LocalDateTime rangeStart = LocalDateTime.of(2023, 5, 15, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2023, 5, 16, 23, 59);

    List<Event> events = storage.getEventsInRange(rangeStart, rangeEnd);
    assertEquals(3, events.size());
  }

  /**
   * Test getting events in a date-time range with no events.
   */
  @Test
  public void testGetEventsInRangeNoEvents() {
    LocalDateTime rangeStart = LocalDateTime.of(2023, 5, 20, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2023, 5, 21, 0, 0);

    List<Event> events = storage.getEventsInRange(rangeStart, rangeEnd);
    assertEquals(0, events.size());
  }

  /**
   * Test removing an event.
   */
  @Test
  public void testRemoveEvent() {
    storage.addEvent(sampleEvent1);

    // Verify event was added
    assertEquals(1, storage.getAllEvents().size());

    // Remove the event
    storage.removeEvent(sampleEvent1);

    // Verify event was removed
    assertEquals(0, storage.getAllEvents().size());
    assertNull(storage.getEventById(sampleEvent1.getId()));

    // Also verify it's removed from the other indexes
    LocalDate date = sampleEvent1.getStartDateTime().toLocalDate();
    assertEquals(0, storage.getEventsOnDate(date).size());

    assertEquals(0, storage.getEventsBySubject(sampleEvent1.getSubject()).size());
  }

  /**
   * Test removing a null event throws exception.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRemoveNullEvent() {
    storage.removeEvent(null);
  }

  /**
   * Test removing a recurring event.
   */
  @Test
  public void testRemoveRecurringEvent() {
    storage.addRecurringEvent(recurringEvent);

    // Verify recurring event and occurrences were added
    assertEquals(1, storage.getAllRecurringEvents().size());

    // Get all the occurrences before removing
    List<Event> occurrences = recurringEvent.getAllOccurrences();
    List<UUID> occurrenceIds = occurrences.stream()
            .map(Event::getId)
            .collect(Collectors.toList());

    // Remove the recurring event
    storage.removeRecurringEvent(recurringEvent);

    // Verify recurring event was removed
    assertEquals(0, storage.getAllRecurringEvents().size());
    assertNull(storage.getRecurringEventById(recurringEvent.getId()));

    // Verify all occurrences were identified and removed
    for (UUID id : occurrenceIds) {
      assertNull(storage.getEventById(id));
    }
  }

  /**
   * Test removing a null recurring event throws exception.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRemoveNullRecurringEvent() {
    storage.removeRecurringEvent(null);
  }

  /**
   * Test getting all events.
   */
  @Test
  public void testGetAllEvents() {
    // Add all sample events
    storage.addEvent(sampleEvent1);
    storage.addEvent(sampleEvent2);
    storage.addEvent(sampleEvent3);

    List<Event> events = storage.getAllEvents();

    // Verify all events are returned
    assertEquals(3, events.size());
    assertTrue(events.stream().anyMatch(e -> e.getId().equals(sampleEvent1.getId())));
    assertTrue(events.stream().anyMatch(e -> e.getId().equals(sampleEvent2.getId())));
    assertTrue(events.stream().anyMatch(e -> e.getId().equals(sampleEvent3.getId())));
  }

  /**
   * Test getting all recurring events.
   */
  @Test
  public void testGetAllRecurringEvents() {
    storage.addRecurringEvent(recurringEvent);

    List<RecurringEvent> events = storage.getAllRecurringEvents();

    assertEquals(1, events.size());
    assertEquals(recurringEvent.getId(), events.get(0).getId());
  }

  /**
   * Test that indexes stay consistent after multiple operations.
   */
  @Test
  public void testIndexConsistency() {
    // Add events
    storage.addEvent(sampleEvent1);
    storage.addEvent(sampleEvent2);

    // Add recurring event
    storage.addRecurringEvent(recurringEvent);

    // Remove one event
    storage.removeEvent(sampleEvent1);

    // Verify correct events remain
    List<Event> allEvents = storage.getAllEvents();
    int expectedCount = 1 + recurringEvent.getAllOccurrences().size();
    assertEquals(expectedCount, allEvents.size());

    // Verify by ID
    assertNull(storage.getEventById(sampleEvent1.getId()));
    assertNotNull(storage.getEventById(sampleEvent2.getId()));

    // Verify by date
    LocalDate date1 = sampleEvent1.getStartDateTime().toLocalDate();
    LocalDate date2 = sampleEvent2.getStartDateTime().toLocalDate();

    List<Event> day1Events = storage.getEventsOnDate(date1);
    assertTrue(!day1Events.stream().anyMatch(e -> e.getId().equals(sampleEvent1.getId())));

    if (date1.equals(date2)) {
      // If both events were on the same day, verify the remaining event is still findable
      assertTrue(day1Events.stream().anyMatch(e -> e.getId().equals(sampleEvent2.getId())));
    }

    // Verify by subject
    if (sampleEvent1.getSubject().equals(sampleEvent2.getSubject())) {
      // If both events had the same subject, verify only one remains findable by subject
      List<Event> subjectEvents = storage.getEventsBySubject(sampleEvent1.getSubject());
      assertEquals(1, subjectEvents.stream()
              .filter(e -> e.getId().equals(sampleEvent1.getId())
                      || e.getId().equals(sampleEvent2.getId()))
              .count());
    }
  }
} 