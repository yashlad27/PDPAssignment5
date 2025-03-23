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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
      assertEquals("Insufficient arguments for creating a recurring event",
              e.getMessage());
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
  public void testCreateEventWithInvalidOccurrences() throws InvalidEventException {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF", "0",
            "false"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for invalid occurrences");
    } catch (InvalidEventException e) {
      assertEquals("Occurrences must be positive", e.getMessage());
    }
  }

  @Test
  public void testCreateEventSuccess() throws InvalidEventException {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF", "3",
            "false",
            "Team meeting", "Conference Room", "true"};
    RecurringEventCreator creator = new RecurringEventCreator(args);
    Event event = creator.createEvent();

    assertTrue("Event should be a RecurringEvent", event instanceof RecurringEvent);
    RecurringEvent recurringEvent = (RecurringEvent) event;

    assertEquals("Meeting", recurringEvent.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0),
            recurringEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0),
            recurringEvent.getEndDateTime());

    Set<DayOfWeek> expectedDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    assertEquals(expectedDays, recurringEvent.getRepeatDays());

    assertEquals(3, recurringEvent.getOccurrences());
    assertEquals("Team meeting", recurringEvent.getDescription());
    assertEquals("Conference Room", recurringEvent.getLocation());
    assertTrue(recurringEvent.isPublic());
    assertFalse(recurringEvent.isAllDay());
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
                                              int occurrences, boolean autoDecline,
                                              String description, String location,
                                              boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return true;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate, boolean autoDecline,
                                                   String description, String location,
                                                   boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
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
            throws EventNotFoundException, InvalidEventException,
            ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
                                  String newValue)
            throws InvalidEventException, ConflictingEventException {
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