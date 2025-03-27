import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import controller.command.create.strategy.RecurringEventCreator;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for RecurringEventCreator. Tests the creation of recurring events with various
 * parameters and validation rules.
 */
public class RecurringEventCreatorTest {

  @Test
  public void testConstructorWithNullArgs() {
    try {
      new RecurringEventCreator(null);
      fail("Should throw IllegalArgumentException for null args");
    } catch (IllegalArgumentException | InvalidEventException e) {
      assertEquals("Arguments array cannot be null", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInsufficientArgs() {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF"};
    try {
      new RecurringEventCreator(args);
      fail("Should throw IllegalArgumentException for insufficient args");
    } catch (IllegalArgumentException | InvalidEventException e) {
      assertEquals("Insufficient arguments for creating a recurring event", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInvalidDateTime() {
    String[] args = {"recurring", "Meeting", "invalid-datetime", "2023-05-15T11:00", "MWF", "3",
        "false"};
    try {
      new RecurringEventCreator(args);
      fail("Should throw IllegalArgumentException for invalid datetime");
    } catch (IllegalArgumentException | InvalidEventException e) {
      assertTrue(e.getMessage().contains("Error parsing arguments"));
    }
  }

  @Test
  public void testCreateEventWithNullEventName() throws InvalidEventException {
    String[] args = {"recurring", null, "2023-05-15T10:00", "2023-05-15T11:00", "MWF", "3",
        "false"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for null event name");
    } catch (InvalidEventException e) {
      assertEquals("Event name cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithEmptyWeekdays() {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "", "3",
        "false"};
    try {
      new RecurringEventCreator(args);
      fail("Should throw InvalidEventException for empty weekdays");
    } catch (InvalidEventException e) {
      assertEquals("Repeat days cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithInvalidOccurrences() {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF", "0",
        "false"};
    assertThrows(InvalidEventException.class, () -> new RecurringEventCreator(args));
  }

  @Test
  public void testCreateEventSuccess() throws InvalidEventException {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF", "3",
        "false", "Team meeting", "Conference Room", "true"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();

    assertTrue("Event should be a RecurringEvent", event instanceof RecurringEvent);
    RecurringEvent recurringEvent = (RecurringEvent) event;

    assertEquals("Meeting", recurringEvent.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), recurringEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), recurringEvent.getEndDateTime());

    Set<DayOfWeek> expectedDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    assertEquals(expectedDays, recurringEvent.getRepeatDays());

    assertEquals(3, recurringEvent.getOccurrences());
    assertEquals("Team meeting", recurringEvent.getDescription());
    assertEquals("Conference Room", recurringEvent.getLocation());
    assertTrue(recurringEvent.isPublic());
    assertFalse(recurringEvent.isAllDay());
  }

  @Test
  public void testCreateRecurringEventWithInvalidInputs() throws InvalidEventException {
    String[] args1 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MXF", "5",
        "false"};
    assertThrows(InvalidEventException.class, () -> new RecurringEventCreator(args1));

    String[] args2 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "-1",
        "false"};
    assertThrows(InvalidEventException.class, () -> new RecurringEventCreator(args2));

    String[] args3 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T09:00", "MWF", "5",
        "false"};
    RecurringEventCreator creator3 = new RecurringEventCreator(args3);
    assertThrows(InvalidEventException.class, () -> creator3.createEvent());

    String[] args4 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "1000",
        "false"};
    InvalidEventException exception = assertThrows(InvalidEventException.class,
        () -> new RecurringEventCreator(args4));
    assertEquals("Maximum occurrences exceeded", exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithInvalidTimeRange() throws InvalidEventException {
    String[] args = {"recurring", "Meeting", "2024-03-26T11:00", "2024-03-26T10:00", "MWF", "5",
        "false"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    assertThrows(InvalidEventException.class, () -> creator.createEvent());
  }

  @Test
  public void testCreateRecurringEventWithEmptyDescription() throws InvalidEventException {
    String[] args = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "5",
        "false", "", "Location", "true"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();
    assertEquals("", event.getDescription());
  }

  @Test
  public void testCreateRecurringEventWithEmptyLocation() throws InvalidEventException {
    String[] args = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "5",
        "false", "Description", "", "true"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();
    assertEquals("", event.getLocation());
  }

  @Test
  public void testCreateRecurringEventWithSpecialCharacters() throws InvalidEventException {
    String[] args = {"recurring", "Meeting@#$%", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "5",
        "false", "Description with @#$%", "Location with @#$%", "true"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();
    assertEquals("Meeting@#$%", event.getSubject());
    assertEquals("Description with @#$%", event.getDescription());
    assertEquals("Location with @#$%", event.getLocation());
  }

  @Test
  public void testCreateRecurringEventWithMaxLengthValues() throws InvalidEventException {
    String longName = "a".repeat(1000);
    String longDesc = "b".repeat(1000);
    String longLoc = "c".repeat(1000);

    String[] args = {"recurring", longName, "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "5",
        "false", longDesc, longLoc, "true"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();
    assertEquals(longName, event.getSubject());
    assertEquals(longDesc, event.getDescription());
    assertEquals(longLoc, event.getLocation());
  }

  @Test
  public void testCreateRecurringEventWithBoundaryDates() throws InvalidEventException {
    String[] args = {"recurring", "Boundary Test", "2023-12-31T23:59", "2024-01-01T00:01", "MWF",
        "5", "false"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();
    assertEquals("Boundary Test", event.getSubject());
  }

  @Test
  public void testCreateRecurringEventWithLeapYear() throws InvalidEventException {
    String[] args = {"recurring", "Leap Year Test", "2024-02-29T10:00", "2024-02-29T11:00", "MWF",
        "5", "false"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();
    assertEquals("Leap Year Test", event.getSubject());
  }

  /**
   * Test that the constructor correctly catches InvalidEventException from DateTimeUtil.
   */
  @Test
  public void testConstructorWithInvalidWeekdays() {
    String[] args = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "XYZ", "5",
        "false"};
    try {
      new RecurringEventCreator(args);
      fail("Should throw InvalidEventException for invalid weekdays");
    } catch (InvalidEventException e) {
      assertEquals("Invalid weekday combination", e.getMessage());
    }
  }

  /**
   * Test that getAutoDecline returns the expected value.
   */
  @Test
  public void testGetAutoDecline() throws InvalidEventException {
    String[] argsTrue = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "5",
        "true"};
    RecurringEventCreator creatorTrue = new RecurringEventCreator(argsTrue);

    boolean result = false;
    try {
      java.lang.reflect.Method method = RecurringEventCreator.class.getDeclaredMethod(
          "getAutoDecline");
      method.setAccessible(true);
      result = (boolean) method.invoke(creatorTrue);
    } catch (Exception e) {
      fail("Failed to invoke getAutoDecline via reflection: " + e.getMessage());
    }

    assertTrue("getAutoDecline should return true", result);

    String[] argsFalse = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF",
        "5", "false"};
    RecurringEventCreator creatorFalse = new RecurringEventCreator(argsFalse);

    try {
      java.lang.reflect.Method method = RecurringEventCreator.class.getDeclaredMethod(
          "getAutoDecline");
      method.setAccessible(true);
      result = (boolean) method.invoke(creatorFalse);
    } catch (Exception e) {
      fail("Failed to invoke getAutoDecline via reflection: " + e.getMessage());
    }

    assertFalse("getAutoDecline should return false", result);
  }

  /**
   * Test boundary conditions for occurrences.
   */
  @Test
  public void testOccurrencesBoundaryConditions() throws InvalidEventException {
    String[] args1 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "1",
        "false"};
    RecurringEventCreator creator1 = new RecurringEventCreator(args1);
    Event event1 = creator1.createEvent();
    assertTrue(event1 instanceof RecurringEvent);
    assertEquals(1, ((RecurringEvent) event1).getOccurrences());

    String[] args999 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF",
        "999", "false"};
    RecurringEventCreator creator999 = new RecurringEventCreator(args999);
    Event event999 = creator999.createEvent();
    assertTrue(event999 instanceof RecurringEvent);
    assertEquals(999, ((RecurringEvent) event999).getOccurrences());
  }

  /**
   * Test invalid boundary conditions for occurrences.
   */
  @Test
  public void testOccurrencesInvalidBoundaryConditions() {
    String[] args0 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "0",
        "false"};
    assertThrows(InvalidEventException.class, () -> {
      new RecurringEventCreator(args0);
    });

    String[] argsNeg = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "-1",
        "false"};
    assertThrows(InvalidEventException.class, () -> {
      new RecurringEventCreator(argsNeg);
    });

    String[] args1000 = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF",
        "1000", "false"};
    assertThrows(InvalidEventException.class, () -> {
      new RecurringEventCreator(args1000);
    });
  }

  /**
   * Test minimum required number of arguments.
   */
  @Test
  public void testMinimumRequiredArguments() {
    String[] argsMin = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00", "MWF", "5",
        "false"};
    try {
      new RecurringEventCreator(argsMin);
    } catch (Exception e) {
      fail("Should not throw exception for minimum required arguments: " + e.getMessage());
    }

    String[] argsInsufficient = {"recurring", "Meeting", "2024-03-26T10:00", "2024-03-26T11:00",
        "MWF", "5"};
    try {
      new RecurringEventCreator(argsInsufficient);
      fail("Should throw IllegalArgumentException for insufficient arguments");
    } catch (IllegalArgumentException e) {
      assertEquals("Insufficient arguments for creating a recurring event", e.getMessage());
    } catch (Exception e) {
      fail("Unexpected exception type: " + e.getClass().getName());
    }
  }

  private static class MockCalendar implements ICalendar {

    RecurringEvent lastRecurringEvent;
    boolean lastAutoDecline;

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
        throws ConflictingEventException {
      this.lastRecurringEvent = recurringEvent;
      this.lastAutoDecline = autoDecline;
      return true;
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      return true;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
        String weekdays, LocalDate untilDate, boolean autoDecline)
        throws InvalidEventException, ConflictingEventException {
      return true;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
        int occurrences, boolean autoDecline, String description, String location, boolean isPublic)
        throws InvalidEventException, ConflictingEventException {
      return true;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
        LocalDate untilDate, boolean autoDecline, String description, String location,
        boolean isPublic) throws InvalidEventException, ConflictingEventException {
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
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public Event findEvent(String subject, LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public List<Event> getAllEvents() {
      return new ArrayList<>();
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
        String newValue)
        throws EventNotFoundException, InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
        String newValue) throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue)
        throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return new ArrayList<>();
    }

    @Override
    public String exportToCSV(String filePath) throws IOException {
      return "";
    }
  }
}