import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import controller.command.create.CreateEventCommand;
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


  @Test
  public void testCreateSingleEventSuccess() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
        "true", "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message with event name",
        result.contains("Event 'Meeting' created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Event subject should match input", "Meeting", addedEvent.getSubject());
    assertEquals("Event start time should match input", LocalDateTime.of(2023, 5, 15, 10, 0),
        addedEvent.getStartDateTime());
    assertEquals("Event end time should match input", LocalDateTime.of(2023, 5, 15, 11, 0),
        addedEvent.getEndDateTime());
  }

  @Test
  public void testCreateSingleEventWithDescriptionAndLocation() {
    String[] args = {"single", "Birthday Party", "2023-05-15T18:00", "2023-05-15T22:00",
        "Celebrating Dad's 50th birthday", "Copacabana Restaurant", "true", "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message with event name",
        result.contains("Event 'Birthday Party' created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Event subject should match input", "Birthday Party", addedEvent.getSubject());
    assertEquals("Event description should match input", "Celebrating Dad's 50th birthday",
        addedEvent.getDescription());
    assertEquals("Event location should match input", "Copacabana Restaurant",
        addedEvent.getLocation());
    assertTrue("Event should be public", addedEvent.isPublic());
  }

  @Test
  public void testCreatePrivateSingleEvent() {
    String[] args = {"single", "Therapy Session", "2023-05-15T15:00", "2023-05-15T16:00",
        "Weekly therapy appointment", "Dr. Smith's Office", "false", "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message with event name",
        result.contains("Event 'Therapy Session' created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Event subject should match input", "Therapy Session", addedEvent.getSubject());
    assertFalse("Event should be private", addedEvent.isPublic());
  }

  @Test
  public void testCreateSingleEventWithAutoDeclineSuccess() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
        "true", "true"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message with event name",
        result.contains("Event 'Meeting' created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithConflict() {
    String[] firstArgs = {"single", "Meeting 1", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
        "true", "false"};
    createCommand.execute(firstArgs);

    String[] conflictingArgs = {"single", "Meeting 2", "2023-05-15T10:30", "2023-05-15T11:30", null,
        null, "true", "true"};

    String result = createCommand.execute(conflictingArgs);

    assertTrue("Should return error message for conflicting events",
        result.contains("Error: Event conflicts with an existing event"));
    assertEquals("Should only have one event after conflict", 1, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithInvalidName() {
    String[] args = {"single", "", "2023-05-15T10:00", "2023-05-15T11:00", null, null, "true",
        "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return error message for empty event name",
        result.contains("Error: Event name cannot be empty"));
    assertEquals("Should have no events in calendar", 0, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithInvalidDateTime() {
    String[] args = {"single", "Meeting", "invalid-date", "2023-05-15T11:00", null, null, "true",
        "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return error message for invalid date format",
        result.contains("Error in command arguments"));
    assertEquals("Should have no events in calendar", 0, calendar.getAllEvents().size());
  }


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
    String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:30", "2023-05-15T09:45",
        "MTWRF", "invalid-date", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size());
  }


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


  @Test
  public void testExecuteWithInsufficientArgs() {
    String[] args = {};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteWithUnknownEventType() {
    String[] args = {"unknown", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Unknown event type"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForSingleEvent() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringEvent() {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringUntilEvent() {
    String[] args = {"recurring-until", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF",
        "2023-06-15"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Insufficient arguments"));
  }
}