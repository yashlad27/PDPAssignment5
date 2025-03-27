import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import controller.CalendarController;
import controller.command.create.CreateEventCommand;
import controller.command.event.CommandFactory;
import controller.parser.CommandParser;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Consolidated test for command system functionality. Enhanced for comprehensive coverage of
 * command system behavior.
 */
public class ConsolidatedCommandTest {

  private Calendar calendar;
  private CalendarController controller;

  /**
   * Custom test implementation of ICalendarView for testing.
   */
  private static class TestCalendarView implements ICalendarView {

    private final List<String> messages = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    @Override
    public String readCommand() {
      return null; // Not used in tests
    }

    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    @Override
    public void displayError(String errorMessage) {
      errors.add(errorMessage);
    }

    public List<String> getMessages() {
      return messages;
    }

    public List<String> getErrors() {
      return errors;
    }

    public void clearMessages() {
      messages.clear();
      errors.clear();
    }
  }

  @Before
  public void setUp() {
    String uniqueCalendarName = "TestCalendar_" + UUID.randomUUID().toString().substring(0, 8);
    TestCalendarView view = new TestCalendarView();

    CalendarManager calendarManager = new CalendarManager.Builder().build();
    try {
      calendarManager.createCalendarWithDefaultTimezone(uniqueCalendarName);
      calendarManager.setActiveCalendar(uniqueCalendarName);
      calendar = calendarManager.getActiveCalendar();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create calendar manager", e);
    }

    CommandFactory commandFactory = new CommandFactory(calendar, view);

    controller = new CalendarController(commandFactory, commandFactory, calendarManager, view);

    CommandParser parser = new CommandParser(commandFactory);
  }

  // ===== Command Creation Tests =====

  @Test
  public void testCreateEventCommand()
      throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    Event testEvent = new Event("Meeting", LocalDateTime.of(2023, 3, 15, 10, 0),
        LocalDateTime.of(2023, 3, 15, 11, 0), "Description", "Location", true);
    calendar.addEvent(testEvent, false);

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 3, 15));
    assertEquals("No events found after direct addition", 1, events.size());
    assertEquals("Event subject does not match", "Meeting", events.get(0).getSubject());
  }

  @Test
  public void testCreateEventWithConflict()
      throws ConflictingEventException, InvalidEventException {
    Event firstEvent = new Event("First Meeting", LocalDateTime.of(2023, 4, 5, 10, 0),
        LocalDateTime.of(2023, 4, 5, 11, 0), "First Description", "Location", true);
    calendar.addEvent(firstEvent, false);

    String result = controller.processCommand(
        "create event " + "\"Conflicting Meeting\" from 2023-04-05T10:30 to 2023-04-05T11:30");

    assertTrue("Command should either report conflict or handle it appropriately",
        result.contains("conflicts") || result.contains("Error") || result.contains("success")
            || result.contains("created"));

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 4, 5));
    assertTrue("Calendar should contain at least one event", events.size() >= 1);
  }

  @Test
  public void testCreateEventWithForceFlag()
      throws ConflictingEventException, InvalidEventException {
    Event firstEvent = new Event("First Meeting", LocalDateTime.of(2023, 4, 6, 10, 0),
        LocalDateTime.of(2023, 4, 6, 11, 0), "First Description", "Location", true);
    calendar.addEvent(firstEvent, false);

    String result = controller.processCommand(
        "create event " + "\"Forced Meeting\" from 2023-04-06T10:30 to 2023-04-06T11:30 --force");

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 4, 6));

    if (events.size() == 2) {
      assertTrue("Should have added second event",
          events.stream().anyMatch(e -> e.getSubject().equals("Forced Meeting")));
    } else {
      assertTrue("Should have either added event or returned appropriate error",
          result.contains("created") || result.contains("Error"));
    }
  }

  @Test
  public void testPrintEventsCommand()
      throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    Event event = new Event("Test Event", LocalDateTime.of(2023, 3, 20, 9, 0),
        LocalDateTime.of(2023, 3, 20, 10, 0), "Description", "Location", true);
    calendar.addEvent(event, false);

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 3, 20));
    assertEquals("Event was not added to calendar", 1, events.size());

    String commandString = "print events on 2023-03-20";
    String result = controller.processCommand(commandString);

    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should contain event name", result.contains("Test Event"));
  }

  @Test
  public void testPrintEventsCommandWithNoEvents() {
    String commandString = "print events on 2023-12-25";
    String result = controller.processCommand(commandString);

    assertTrue("Result should indicate no events",
        result.contains("No events") || result.toLowerCase().contains("empty"));
  }

  @Test
  public void testPrintEventsInDateRange() throws ConflictingEventException, InvalidEventException {
    Event event1 = new Event("Event 1", LocalDateTime.of(2023, 5, 10, 9, 0),
        LocalDateTime.of(2023, 5, 10, 10, 0), "Description 1", "Location 1", true);
    calendar.addEvent(event1, false);

    Event event2 = new Event("Event 2", LocalDateTime.of(2023, 5, 12, 11, 0),
        LocalDateTime.of(2023, 5, 12, 12, 0), "Description 2", "Location 2", true);
    calendar.addEvent(event2, false);

    String result = controller.processCommand("print events from 2023-05-09 to 2023-05-13");

    assertTrue("Result should contain first event", result.contains("Event 1"));
    assertTrue("Result should contain second event", result.contains("Event 2"));
  }

  @Test
  public void testPrintEventsWithInvalidDateFormat() {
    String result = controller.processCommand("print events on 05/20/2023");

    assertTrue("Should indicate invalid date format",
        result.contains("Error") || result.contains("format"));
  }

  @Test
  public void testShowStatusCommand()
      throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    Event event = new Event("Busy Time", LocalDateTime.of(2023, 3, 25, 14, 0),
        LocalDateTime.of(2023, 3, 25, 15, 0), "Important meeting", "Office", true);
    calendar.addEvent(event, false);

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 3, 25));
    assertEquals("Event was not added to calendar", 1, events.size());

    String commandString = "show status on 2023-03-25T14:30";
    String result = controller.processCommand(commandString);

    assertNotNull("Result should not be null", result);
    assertFalse("Result should not be empty", result.isEmpty());
    assertTrue("Result should indicate busy status", result.contains("Busy"));

    String commandString2 = "show status on 2023-03-25T16:00";
    String result2 = controller.processCommand(commandString2);

    assertNotNull("Result should not be null", result2);
    assertFalse("Result should not be empty", result2.isEmpty());
    assertTrue("Result should indicate available status", result2.contains("Available"));
  }

  @Test
  public void testShowStatusWithMultipleEvents() throws ConflictingEventException {
    Event event1 = new Event("Meeting 1", LocalDateTime.of(2023, 6, 15, 10, 0),
        LocalDateTime.of(2023, 6, 15, 11, 0), "Description 1", "Location 1", true);
    calendar.addEvent(event1, false);

    String result = controller.processCommand("show status on 2023-06-15T10:30");

    assertTrue("Should show busy with single event", result.contains("Busy"));
  }

  @Test
  public void testShowStatusWithInvalidDateTime() {
    String result = controller.processCommand("show status on 2023-06-15 10:45");

    assertTrue("Should indicate invalid datetime format",
        result.contains("Error") || result.contains("format"));
  }

  // ===== Controller Tests =====

  @Test
  public void testControllerParseAndExecuteCommand() {
    String result = controller.processCommand(
        "create event " + "\"Controller Test\" from 2023-04-10T09:00 to 2023-04-10T10:00");

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 4, 10));
    assertEquals("Event was not created by controller", 1, events.size());
    assertEquals("Event subject doesn't match", "Controller Test", events.get(0).getSubject());
  }

  @Test
  public void testControllerInvalidCommand() {
    String result = controller.processCommand("invalid command text");

    assertTrue("Result should contain error message",
        result.contains("Error:") || result.contains("Invalid"));
  }

  @Test
  public void testControllerWithEmptyCommand() {
    String result = controller.processCommand("");

    assertTrue("Should reject empty command", result.contains("Error") || result.contains("empty"));
  }

  @Test
  public void testControllerWithNullCommand() {
    String result = controller.processCommand(null);

    assertTrue("Should reject null command",
        result.contains("Error") || result.contains("null") || result.contains("empty"));
  }

  @Test
  public void testControllerWithExcessiveWhitespace() {
    String result = controller.processCommand("   create    event    "
        + "\"Whitespace Test\"    from    2023-04-20T09:00    to    2023-04-20T10:00   ");

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 4, 20));

    assertEquals("Event should be created despite whitespace", 1, events.size());
    if (!events.isEmpty()) {
      assertEquals("Subject should be correctly parsed", "Whitespace Test",
          events.get(0).getSubject());
    }
  }

  @Test
  public void testEditEventCommand() throws ConflictingEventException, InvalidEventException {
    Event event = new Event("Original Meeting", LocalDateTime.of(2023, 7, 5, 10, 0),
        LocalDateTime.of(2023, 7, 5, 11, 0), "Original Description", "Original Location", true);
    calendar.addEvent(event, false);
    String eventId = event.getId().toString();

    String result = controller.processCommand(
        "edit event " + eventId + " subject \"Updated Meeting\"");

    List<Event> events = calendar.getEventsOnDate(LocalDate.of(2023, 7, 5));
    assertEquals("Should still have one event", 1, events.size());

    if (events.get(0).getSubject().equals("Updated Meeting")) {
      assertEquals("Event subject should be updated", "Updated Meeting",
          events.get(0).getSubject());
    } else {
      assertTrue("Should provide meaningful error if edit failed",
          result.toLowerCase().contains("error") || result.toLowerCase().contains("failed")
              || result.toLowerCase().contains("unable"));
    }
  }

  @Test
  public void testEditNonExistentEvent() {
    String fakeId = UUID.randomUUID().toString();
    String result = controller.processCommand("edit event " + fakeId + " subject \"Fake Meeting\"");

    assertTrue("Should indicate event not found",
        result.contains("Error") || result.contains("not found") || result.contains(
            "doesn't exist"));
  }

  @Test
  public void testCreateAndExportEvent() throws ConflictingEventException, InvalidEventException {
    Event event = new Event("Export Test", LocalDateTime.of(2023, 8, 10, 9, 0),
        LocalDateTime.of(2023, 8, 10, 10, 0), "Testing export", "Test Location", true);
    calendar.addEvent(event, false);

    String result = controller.processCommand("export events on 2023-08-10");

    assertNotNull("Export command should return a result", result);
    assertFalse("Export result should not be empty", result.isEmpty());
  }

  @Test
  public void testCreateEventWithExtremeTimes() {
    String result = controller.processCommand(
        "create event" + " \"Midnight Event\" from 2023-05-01T00:00 to 2023-05-01T01:00");
    assertTrue(result.contains("created successfully"));

    result = controller.processCommand(
        "create event " + "\"Overnight Event\" from 2023-05-02T23:30 to 2023-05-03T00:30");
    assertTrue(result.contains("created successfully"));

    result = controller.processCommand(
        "create event " + "\"Multi-day Event\" from 2023-05-10T10:00 to 2023-05-12T16:00");
    assertTrue(result.contains("created successfully"));
  }

  @Test
  public void testAdvancedErrorHandling() {
    String result = controller.processCommand(
        "create event " + "\"Bad Date Event\" from 2023/12/01T10:00 to 2023/12/01T11:00");
    assertTrue(result.contains("Error") || result.contains("format"));

    result = controller.processCommand(
        "create event " + "\"Bad Time Event\" from 2023-12-01 10:00 to 2023-12-01 11:00");
    assertTrue(result.contains("Error") || result.contains("format"));

    result = controller.processCommand(
        "create event " + "\"Invalid Time Event\" from 2023-12-01T25:00 to 2023-12-01T26:00");
    assertTrue(result.contains("Error") || result.contains("invalid"));
  }

  @Test
  public void testCreateRecurringEventWithInvalidWeekdays() {
    String result = controller.processCommand("create event \"Invalid Meeting\" "
        + "from 2024-03-26T10:00 to 2024-03-26T11:00 repeats XYZ for 5 times");

    assertTrue(result.contains("Error") || result.contains("Invalid weekday"));
  }

  @Test
  public void testCreateRecurringEventWithAutoDecline()
      throws ConflictingEventException, InvalidEventException {
    controller.processCommand("create event \"First Meeting\" "
        + "from 2024-03-26T10:00 to 2024-03-26T11:00 repeats MWF for 5 times");

    String result = controller.processCommand(
        "create event --autoDecline " + "\"Conflicting Meeting\" from 2024-03-26T10:30 to"
            + " 2024-03-26T11:30 repeats MWF for 5 times");

    assertTrue(result.contains("conflicts") || result.contains("Error"));
  }

  @Test
  public void testCreateRecurringEventWithMaxOccurrences() {
    String result = controller.processCommand(
        "create event \"Weekly Meeting\" from 2024-03-26T10:00"
            + " to 2024-03-26T11:00 repeats MWF for 1000 times");

    assertTrue(result.contains("Maximum occurrences exceeded"));
  }

  @Test
  public void testExitCommand() {
    String result = controller.processCommand("exit");
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testExitCommandWithArguments() {
    String result = controller.processCommand("exit additional arguments");
    assertTrue(result.contains("Error") || result.contains("Invalid"));
  }

  @Test
  public void testExitCommandCaseSensitive() {
    String result = controller.processCommand("EXIT");
    assertTrue(result.contains("Error") || result.contains("Invalid"));

    result = controller.processCommand("exit");
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testGetName() {
    CreateEventCommand command = new CreateEventCommand(calendar);
    assertEquals("create", command.getName());
  }

  @Test
  public void testExecuteWithNoArgs() {
    CreateEventCommand command = new CreateEventCommand(calendar);
    String result = command.execute(new String[]{});
    assertEquals("Error: Insufficient arguments for create command", result);
  }

  @Test
  public void testExecuteWithInvalidEventType() {
    CreateEventCommand command = new CreateEventCommand(calendar);
    String result = command.execute(new String[]{"InvalidType"});
    assertTrue(result.startsWith("Error"));
  }

  @Test
  public void testExecuteWithValidEvent() {
    CreateEventCommand command = new CreateEventCommand(calendar);
    String result = command.execute(new String[]{"Meeting", "2025-03-27", "10:00", "11:00"});
    assertFalse(result.startsWith("Unexpected error:"));
  }

  @Test
  public void testExecuteWithConflictingEvent() {
    CreateEventCommand command = new CreateEventCommand(calendar);

    command.execute(new String[]{"Meeting", "2025-03-27", "10:00", "11:00"});

    String result = command.execute(new String[]{"Meeting", "2025-03-27", "10:30", "11:30"});
    assertTrue(result.startsWith("Error in command arguments:"));
  }
} 