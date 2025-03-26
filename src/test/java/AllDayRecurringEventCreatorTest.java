import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import controller.command.create.strategy.AllDayRecurringEventCreator;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for AllDayRecurringEventCreator.
 * Tests the creation of all-day recurring events with various parameters and validation rules.
 */
public class AllDayRecurringEventCreatorTest {

  @Test
  public void testConstructorWithNullArgs() {
    try {
      new AllDayRecurringEventCreator(null);
      fail("Should throw IllegalArgumentException for null args");
    } catch (IllegalArgumentException e) {
      assertEquals("Arguments array cannot be null", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInsufficientArgs() {
    String[] args = {"allday-recurring", "Holiday", "2023-12-25"};
    try {
      new AllDayRecurringEventCreator(args);
      fail("Should throw IllegalArgumentException for insufficient args");
    } catch (IllegalArgumentException e) {
      assertEquals("Insufficient arguments for all-day recurring event", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInvalidDate() {
    String[] args = {"allday-recurring", "Holiday", "invalid-date", "MWF", "5", "false"};
    try {
      new AllDayRecurringEventCreator(args);
      fail("Should throw IllegalArgumentException for invalid date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Error parsing arguments"));
    }
  }

  @Test
  public void testConstructorWithInvalidOccurrences() {
    String[] args = {"allday-recurring", "Holiday", "2023-12-25", "MWF", "0", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for invalid occurrences");
    } catch (InvalidEventException e) {
      assertEquals("Occurrences must be positive", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithNullEventName() {
    String[] args = {"allday-recurring", null, "2023-12-25", "MWF", "5", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for null event name");
    } catch (InvalidEventException e) {
      assertEquals("Event name cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithEmptyWeekdays() {
    String[] args = {"allday-recurring", "Holiday", "2023-12-25", "", "5", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for empty weekdays");
    } catch (InvalidEventException e) {
      assertEquals("Weekdays cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithValidArgs() throws InvalidEventException {
    String[] args = {"allday-recurring", "Company Holiday", "2023-12-25", "MWF", "5", "false",
            "Holiday description", "Office", "true"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    Event event = creator.createEvent();

    assertNull("createEvent should return null for recurring events", event);
  }

  @Test
  public void testExecuteCreationSuccess() {
    String[] args = {"allday-recurring", "Company Holiday", "2023-12-25", "MWF", "5", "false",
            "Holiday description", "Office", "true"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    TestCalendar calendar = new TestCalendar();

    try {
      String result = creator.executeCreation(calendar);
      assertTrue(result.contains("All-day recurring event 'Company Holiday' created successfully"));
      assertTrue(result.contains("5 occurrences"));

      assertTrue(calendar.createAllDayRecurringEventCalled);
      assertEquals("Company Holiday", calendar.lastEventName);
      assertEquals(LocalDate.of(2023, 12, 25), calendar.lastDate);
      assertEquals("MWF", calendar.lastWeekdays);
      assertEquals(5, calendar.lastOccurrences);
      assertEquals(false, calendar.lastAutoDecline);
      assertEquals("Holiday description", calendar.lastDescription);
      assertEquals("Office", calendar.lastLocation);
      assertEquals(true, calendar.lastIsPublic);
    } catch (Exception e) {
      fail("Should not throw exception: " + e.getMessage());
    }
  }

  @Test
  public void testExecuteCreationWithConflict() {
    String[] args = {"allday-recurring", "Company Holiday", "2023-12-25", "MWF", "5", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    TestCalendar calendar = new TestCalendar();
    calendar.shouldThrowConflict = true;

    try {
      creator.executeCreation(calendar);
      fail("Should throw ConflictingEventException");
    } catch (ConflictingEventException e) {
      assertEquals("Test conflict exception", e.getMessage());
    } catch (Exception e) {
      fail("Should throw ConflictingEventException, not " + e.getClass().getName());
    }
  }

  @Test
  public void testExecuteCreationWithInvalidEvent() {
    String[] args = {"allday-recurring", "Company Holiday", "2023-12-25", "MWF", "5", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    TestCalendar calendar = new TestCalendar();
    calendar.shouldThrowInvalid = true;

    try {
      creator.executeCreation(calendar);
      fail("Should throw InvalidEventException");
    } catch (InvalidEventException e) {
      assertEquals("Error creating all-day recurring event: Test invalid event exception",
              e.getMessage());
    } catch (Exception e) {
      fail("Should throw InvalidEventException, not " + e.getClass().getName());
    }
  }

  @Test
  public void testExecuteCreationWithCreationFailure() {
    String[] args = {"allday-recurring", "Company Holiday", "2023-12-25", "MWF", "5", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    TestCalendar calendar = new TestCalendar();
    calendar.shouldReturnFalse = true;

    try {
      creator.executeCreation(calendar);
      fail("Should throw InvalidEventException");
    } catch (InvalidEventException e) {
      assertTrue(e.getMessage().contains("Failed to create"));
    } catch (Exception e) {
      fail("Should throw InvalidEventException, not " + e.getClass().getName());
    }
  }

  @Test
  public void testGetSuccessMessage() throws Exception {
    String[] args = {"allday-recurring", "Company Holiday", "2023-12-25", "MWF", "5", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);

    java.lang.reflect.Method method = AllDayRecurringEventCreator.class
            .getDeclaredMethod("getSuccessMessage", Event.class);
    method.setAccessible(true);
    String message = (String) method.invoke(creator, (Event) null);

    assertEquals("All-day recurring event 'Company Holiday' created successfully"
                    + " with 5 occurrences.",
            message);
  }

  private static class TestCalendar implements ICalendar {
    boolean createAllDayRecurringEventCalled = false;
    String lastEventName;
    LocalDate lastDate;
    String lastWeekdays;
    int lastOccurrences;
    boolean lastAutoDecline;
    String lastDescription;
    String lastLocation;
    boolean lastIsPublic;

    boolean shouldThrowConflict = false;
    boolean shouldThrowInvalid = false;
    boolean shouldReturnFalse = false;

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline, String description,
                                              String location, boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      if (shouldThrowConflict) {
        throw new ConflictingEventException("Test conflict exception");
      }
      if (shouldThrowInvalid) {
        throw new InvalidEventException("Test invalid event exception");
      }

      createAllDayRecurringEventCalled = true;
      lastEventName = name;
      lastDate = date;
      lastWeekdays = weekdays;
      lastOccurrences = occurrences;
      lastAutoDecline = autoDecline;
      lastDescription = description;
      lastLocation = location;
      lastIsPublic = isPublic;

      return !shouldReturnFalse;
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) {
      return true;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
      return true;
    }

    @Override
    public boolean createRecurringEventUntil(String name, java.time.LocalDateTime start,
                                             java.time.LocalDateTime end, String weekdays,
                                             LocalDate untilDate, boolean autoDecline) {
      return true;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate, boolean autoDecline,
                                                   String description, String location, boolean isPublic) {
      return true;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return new ArrayList<>();
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      return new ArrayList<>();
    }

    @Override
    public boolean isBusy(java.time.LocalDateTime dateTime) {
      return false;
    }

    @Override
    public Event findEvent(String subject, java.time.LocalDateTime startDateTime)
            throws EventNotFoundException {
      throw new EventNotFoundException("Event not found");
    }

    @Override
    public List<Event> getAllEvents() {
      return new ArrayList<>();
    }

    @Override
    public boolean editSingleEvent(String subject, java.time.LocalDateTime startDateTime,
                                   String property, String newValue) {
      return true;
    }

    @Override
    public int editEventsFromDate(String subject, java.time.LocalDateTime startDateTime,
                                  String property, String newValue) {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) {
      return 0;
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return new ArrayList<>();
    }

    @Override
    public String exportToCSV(String filePath) {
      return "";
    }
  }
}