import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import controller.command.CreateEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the CreateEventCommand class without using Mockito.
 */
public class CreateEventCommandTest {

  private ICalendar calendar;
  private CreateEventCommand createCommand;

  @Before
  public void setUp() {
    // Create a real calendar for testing
    calendar = new Calendar();
    createCommand = new CreateEventCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("create", createCommand.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new CreateEventCommand(null);
  }

  // SINGLE EVENT TESTS

  @Test
  public void testCreateSingleEventSuccess() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
        "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));

    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Meeting", addedEvent.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), addedEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), addedEvent.getEndDateTime());
  }

  @Test
  public void testCreateSingleEventWithDescriptionAndLocation() {
    String[] args = {"single", "Birthday Party", "2023-05-15T18:00", "2023-05-15T22:00",
        "Celebrating Dad's 50th birthday", "Copacabana Restaurant", "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Birthday Party", addedEvent.getSubject());
    assertEquals("Celebrating Dad's 50th birthday", addedEvent.getDescription());
    assertEquals("Copacabana Restaurant", addedEvent.getLocation());
    assertTrue(addedEvent.isPublic());
  }

  @Test
  public void testCreatePrivateSingleEvent() {
    String[] args = {"single", "Therapy Session", "2023-05-15T15:00", "2023-05-15T16:00",
        "Weekly therapy appointment", "Dr. Smith's Office", "false", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Therapy Session", addedEvent.getSubject());
    assertFalse(addedEvent.isPublic());
  }

  @Test
  public void testCreateSingleEventWithAutoDeclineSuccess() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
        "true", "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithConflict() {
    String[] firstArgs = {"single", "Meeting 1", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
        "true", "false"};
    createCommand.execute(firstArgs);
    String[] secondArgs = {"single", "Meeting 2", "2023-05-15T10:30", "2023-05-15T11:30", null,
        null, "true", "true"};

    String result = createCommand.execute(secondArgs);

    assertTrue(result.contains("Failed to create event due to conflicts"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithInvalidName() {
    String[] args = {"single", "", "2023-05-15T10:00", "2023-05-15T11:00", null, null, "true",
        "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error") || result.contains("name cannot be empty"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithInvalidDateTime() {
    String[] args = {"single", "Meeting", "invalid-date", "2023-05-15T11:00", null, null, "true",
        "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error parsing arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  // ALL DAY EVENT TESTS

  @Test
  public void testCreateAllDayEventSuccess() {
    String[] args = {"allday", "Holiday", "2023-05-15", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Holiday", addedEvent.getSubject());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreateAllDayEventWithDescriptionAndLocation() {
    String[] args = {"allday", "Conference Day", "2023-05-15", "false", "Annual Tech Conference",
        "Convention Center", "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Conference Day", addedEvent.getSubject());
    assertEquals("Annual Tech Conference", addedEvent.getDescription());
    assertEquals("Convention Center", addedEvent.getLocation());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreatePrivateAllDayEvent() {
    String[] args = {"allday", "Mental Health Day", "2023-05-15", "false", "Personal day off",
        "Home", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Mental Health Day", addedEvent.getSubject());
    assertFalse(addedEvent.isPublic());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreateAllDayEventWithInvalidDate() {
    String[] args = {"allday", "Holiday", "invalid-date", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error parsing arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  // RECURRING EVENT TESTS

  @Test
  public void testCreateRecurringEventSuccess() {
    String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MW",
        "8", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventWithDescriptionAndLocation() {
    String[] args = {"recurring", "Yoga Class", "2023-05-15T18:00", "2023-05-15T19:00", "TR",

        "12", "false", "Beginner's yoga with Instructor Sarah", "Downtown Fitness Center", "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertEquals("Yoga Class", firstOccurrence.getSubject());
    assertEquals("Beginner's yoga with Instructor Sarah", firstOccurrence.getDescription());
    assertEquals("Downtown Fitness Center", firstOccurrence.getLocation());
  }

  @Test
  public void testCreatePrivateRecurringEvent() {
    String[] args = {"recurring", "Therapy Session", "2023-05-15T15:00", "2023-05-15T16:00", "M",

        "10", "false", "Weekly therapy appointment", "Dr. Smith's Office", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertFalse(firstOccurrence.isPublic());
  }

  @Test
  public void testCreateRecurringEventWithInvalidWeekdays() {
    String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "XYZ",

        "8", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testCreateRecurringEventWithInvalidOccurrences() {
    String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MW",
        "-1", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  // RECURRING UNTIL EVENT TESTS

  @Test
  public void testCreateRecurringEventUntilSuccess() {
    String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:30", "2023-05-15T09:45",
        "MTWRF", "2023-05-31", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventUntilWithDetailsSuccess() {
    String[] args = {"recurring-until", "Weekly Review", "2023-05-15T16:00", "2023-05-15T17:00",
        "F", "2023-06-30", "false", "Project progress review", "Conference Room A", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertEquals("Weekly Review", firstOccurrence.getSubject());
    assertEquals("Project progress review", firstOccurrence.getDescription());
    assertEquals("Conference Room A", firstOccurrence.getLocation());
    assertFalse(firstOccurrence.isPublic());
  }

  @Test
  public void testCreateRecurringEventUntilWithInvalidDate() {
    // Execute - Create a recurring event with invalid until date
    String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:30", "2023-05-15T09:45",
        "MTWRF", "invalid-date", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  // ALL-DAY RECURRING EVENT TESTS

  @Test
  public void testCreateAllDayRecurringEventSuccess() {
    String[] args = {"allday-recurring", "Team Building Day", "2023-05-15", "F", "8", "false",
        "Monthly team building activity", "Various Locations", "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateAllDayRecurringEventUntilSuccess() {
    String[] args = {"allday-recurring-until", "Holiday", "2023-05-15", "MF", "2023-12-31", "false",
        "Company holiday", null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  // ERROR HANDLING TESTS

  @Test
  public void testExecuteWithInsufficientArgs() {
    String[] args = {};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteWithUnknownEventType() {
    String[] args = {"unknown", "Meeting"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Unknown create event type"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteWithInsufficientArgsForSingleEvent() {
    String[] args = {"single", "Meeting"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringEvent() {
    String[] args = {"recurring", "Weekly Meeting"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringUntilEvent() {
    String[] args = {"recurring-until", "Daily Standup"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }
}