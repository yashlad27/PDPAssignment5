import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controller.command.create.strategy.AllDayRecurringEventCreator;
import controller.command.create.strategy.AllDayRecurringUntilEventCreator;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    String[] args = {"allday-recurring", "Meeting", "2023-05-15", "MWF"}; // Missing occurrences and autoDecline
    try {
      new AllDayRecurringEventCreator(args);
      fail("Should throw IllegalArgumentException for insufficient args");
    } catch (IllegalArgumentException e) {
      assertEquals("Insufficient arguments for all-day recurring event", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInvalidDate() {
    String[] args = {"allday-recurring", "Meeting", "invalid-date", "MWF", "3", "false"};
    try {
      new AllDayRecurringEventCreator(args);
      fail("Should throw IllegalArgumentException for invalid date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Error parsing arguments"));
    }
  }

  @Test
  public void testCreateEventWithNullEventName() {
    String[] args = {"allday-recurring", null, "2023-05-15", "MWF", "3", "false"};
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
    String[] args = {"allday-recurring", "Meeting", "2023-05-15", "", "3", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for empty weekdays");
    } catch (InvalidEventException e) {
      assertEquals("Weekdays cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithInvalidOccurrences() {
    String[] args = {"allday-recurring", "Meeting", "2023-05-15", "MWF", "0", "false"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for invalid occurrences");
    } catch (InvalidEventException e) {
      assertEquals("Occurrences must be positive", e.getMessage());
    }
  }

  @Test
  public void testCreateEventSuccess() throws InvalidEventException {
    String[] args = {"allday-recurring", "Meeting", "2023-05-15", "MWF", "3", "false",
            "Team meeting", "Conference Room", "true"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);
    Event event = creator.createEvent();

    assertNull("createEvent should return null for recurring events", event);
  }

  @Test
  public void testExecuteCreationSuccess() throws ConflictingEventException, InvalidEventException {
    String[] args = {"allday-recurring", "Meeting", "2023-05-15", "MWF", "3", "false",
            "Team meeting", "Conference Room", "true"};
    AllDayRecurringEventCreator creator = new AllDayRecurringEventCreator(args);

    MockCalendar calendar = new MockCalendar();

    String result = creator.executeCreation(calendar);

    assertTrue(result.contains("created successfully"));
    assertTrue(result.contains("3 occurrences"));

    assertEquals("Meeting", calendar.lastEventName);
    assertEquals(LocalDate.of(2023, 5, 15), calendar.lastDate);
    assertEquals("MWF", calendar.lastWeekdays);
    assertEquals(3, calendar.lastOccurrences);
    assertFalse(calendar.lastAutoDecline);
    assertEquals("Team meeting", calendar.lastDescription);
    assertEquals("Conference Room", calendar.lastLocation);
    assertTrue(calendar.lastIsPublic);
  }

  @Test
  public void testCreateEventWithNullUntilDate() {
    String[] args = {"allday-recurring-until", "Meeting", "2023-05-15", "MWF", null, "false"};
    try {
      new AllDayRecurringUntilEventCreator(args);
      fail("Should throw IllegalArgumentException for null until date");
    } catch (IllegalArgumentException e) {
      assertEquals("Error parsing arguments: text", e.getMessage());
    }
  }

  //MOCK CLASS
  private static class MockCalendar implements ICalendar {
    String lastEventName;
    LocalDate lastDate;
    String lastWeekdays;
    int lastOccurrences;
    boolean lastAutoDecline;
    String lastDescription;
    String lastLocation;
    boolean lastIsPublic;

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline, String description, String location,
                                              boolean isPublic) throws InvalidEventException, ConflictingEventException {
      // Store the parameters
      this.lastEventName = name;
      this.lastDate = date;
      this.lastWeekdays = weekdays;
      this.lastOccurrences = occurrences;
      this.lastAutoDecline = autoDecline;
      this.lastDescription = description;
      this.lastLocation = location;
      this.lastIsPublic = isPublic;
      return true;
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      return true;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
            throws ConflictingEventException {
      return true;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                             String weekdays, LocalDate untilDate, boolean autoDecline)
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
      return List.of();
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property, String newValue) throws EventNotFoundException, InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property, String newValue) throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return List.of();
    }

    @Override
    public String exportToCSV(String filePath) throws IOException {
      return "";
    }
  }
}