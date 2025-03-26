import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

public class AllDayRecurringUntilEventCreatorTest {

  @Test
  public void testConstructorWithNullArgs() {
    try {
      new AllDayRecurringUntilEventCreator(null);
      fail("Should throw IllegalArgumentException for null args");
    } catch (IllegalArgumentException e) {
      assertEquals("Arguments array cannot be null", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInsufficientArgs() {
    String[] args = {"allday-recurring-until", "Meeting", "2023-05-15", "MWF"};
    try {
      new AllDayRecurringUntilEventCreator(args);
      fail("Should throw IllegalArgumentException for insufficient args");
    } catch (IllegalArgumentException e) {
      assertEquals("Insufficient arguments for all-day recurring event until date",
              e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInvalidDate() {
    String[] args = {"allday-recurring-until", "Meeting", "invalid-date", "MWF", "2023-06-15",
            "false"};
    try {
      new AllDayRecurringUntilEventCreator(args);
      fail("Should throw IllegalArgumentException for invalid date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Error parsing arguments"));
    }
  }

  @Test
  public void testConstructorWithInvalidUntilDate() {
    String[] args = {"allday-recurring-until", "Meeting", "2023-05-15", "MWF", "invalid-date",
            "false"};
    try {
      new AllDayRecurringUntilEventCreator(args);
      fail("Should throw IllegalArgumentException for invalid until date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Error parsing arguments"));
    }
  }

  @Test
  public void testCreateEventWithNullEventName() {
    String[] args = {"allday-recurring-until", null, "2023-05-15", "MWF", "2023-06-15", "false"};
    AllDayRecurringUntilEventCreator creator = new AllDayRecurringUntilEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for null event name");
    } catch (InvalidEventException e) {
      assertEquals("Event name cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithEmptyWeekdays() {
    String[] args = {"allday-recurring-until", "Meeting", "2023-05-15", "", "2023-06-15", "false"};
    AllDayRecurringUntilEventCreator creator = new AllDayRecurringUntilEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for empty weekdays");
    } catch (InvalidEventException e) {
      assertEquals("Weekdays cannot be empty", e.getMessage());
    }
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

  @Test
  public void testCreateEventSuccess() throws InvalidEventException {
    String[] args = {"allday-recurring-until", "Meeting", "2023-05-15", "MWF", "2023-06-15", "false",
            "Team meeting", "Conference Room", "true"};
    AllDayRecurringUntilEventCreator creator = new AllDayRecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    assertNull("createEvent should return null for recurring events", event);
  }

  @Test
  public void testExecuteCreationSuccess() throws ConflictingEventException, InvalidEventException {
    String[] args = {"allday-recurring-until", "Meeting", "2023-05-15", "MWF", "2023-06-15", "false",
            "Team meeting", "Conference Room", "true"};
    AllDayRecurringUntilEventCreator creator = new AllDayRecurringUntilEventCreator(args);

    MockCalendar calendar = new MockCalendar();

    String result = creator.executeCreation(calendar);

    assertTrue(result.contains("created successfully"));
    assertTrue(result.contains("until 2023-06-15"));

    assertEquals("Meeting", calendar.lastEventName);
    assertEquals(LocalDate.of(2023, 5, 15), calendar.lastDate);
    assertEquals("MWF", calendar.lastWeekdays);
    assertEquals(LocalDate.of(2023, 6, 15), calendar.lastUntilDate);
    assertFalse(calendar.lastAutoDecline);
    assertEquals("Team meeting", calendar.lastDescription);
    assertEquals("Conference Room", calendar.lastLocation);
    assertTrue(calendar.lastIsPublic);
  }

  private static class MockCalendar implements ICalendar {
    String lastEventName;
    LocalDate lastDate;
    String lastWeekdays;
    LocalDate lastUntilDate;
    boolean lastAutoDecline;
    String lastDescription;
    String lastLocation;
    boolean lastIsPublic;

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate, boolean autoDecline,
                                                   String description, String location,
                                                   boolean isPublic) throws InvalidEventException,
            ConflictingEventException {
      // Store the parameters
      this.lastEventName = name;
      this.lastDate = date;
      this.lastWeekdays = weekdays;
      this.lastUntilDate = untilDate;
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
                                             String weekdays, LocalDate untilDate,
                                             boolean autoDecline)
            throws InvalidEventException, ConflictingEventException {
      return true;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline,
                                              String description, String location,
                                              boolean isPublic) throws InvalidEventException,
            ConflictingEventException {
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
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
                                   String newValue) throws EventNotFoundException,
            InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
                                  String newValue) throws InvalidEventException,
            ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) throws
            InvalidEventException, ConflictingEventException {
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