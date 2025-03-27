import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import controller.command.copy.CopyEventCommand;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.CalendarNameValidator;
import utilities.TimeZoneHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for CopyEventCommand. Tests the copying of events between calendars with various
 * scenarios and edge cases.
 */
public class CopyEventCommandTest {

  private CopyEventCommand copyCommand;
  private CalendarManager calendarManager;
  private ICalendar sourceCalendar;
  private ICalendar targetCalendar;

  @Before
  public void setUp()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarNameValidator.clear();
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();

    calendarManager = new CalendarManager.Builder().timezoneHandler(timezoneHandler).build();
    copyCommand = new CopyEventCommand(calendarManager, timezoneHandler);

    calendarManager.createCalendar("source", "UTC");
    sourceCalendar = calendarManager.getActiveCalendar();

    calendarManager.createCalendar("target", "UTC");
    targetCalendar = calendarManager.getCalendar("target");
  }

  @After
  public void tearDown() {
    CalendarNameValidator.clear();
  }

  @Test
  public void testCopySingleEvent() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime, "Test Description",
        "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Meeting", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully"));

    Event copiedEvent = targetCalendar.findEvent("Test Meeting",
        LocalDateTime.of(2024, 3, 16, 10, 0));
    assertNotNull(copiedEvent);
    assertEquals("Test Meeting", copiedEvent.getSubject());
    assertEquals("Test Description", copiedEvent.getDescription());
    assertEquals("Test Location", copiedEvent.getLocation());
    assertTrue(copiedEvent.isPublic());
  }

  @Test
  public void testCopyEventsOnDate() throws Exception {
    LocalDateTime startTime1 = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime1 = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event event1 = new Event("Morning Meeting", startTime1, endTime1, "Description 1", "Location 1",
        true);
    sourceCalendar.addEvent(event1, false);

    LocalDateTime startTime2 = LocalDateTime.of(2024, 3, 15, 14, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2024, 3, 15, 15, 0);
    Event event2 = new Event("Afternoon Meeting", startTime2, endTime2, "Description 2",
        "Location 2", true);
    sourceCalendar.addEvent(event2, false);

    String result = copyCommand.execute(
        new String[]{"copy", "events", "on", "2024-03-15", "--target", "target", "to",
            "2024-03-16"});

    assertTrue(result.contains("Successfully copied 2 events"));

    assertEquals(2,
        targetCalendar.getEventsOnDate(LocalDateTime.of(2024, 3, 16, 0, 0).toLocalDate()).size());
  }

  @Test
  public void testCopyEventsBetweenDates() throws Exception {
    LocalDateTime startTime1 = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime1 = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event event1 = new Event("Day 1 Meeting", startTime1, endTime1, "Description 1", "Location 1",
        true);
    sourceCalendar.addEvent(event1, false);

    LocalDateTime startTime2 = LocalDateTime.of(2024, 3, 16, 14, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2024, 3, 16, 15, 0);
    Event event2 = new Event("Day 2 Meeting", startTime2, endTime2, "Description 2", "Location 2",
        true);
    sourceCalendar.addEvent(event2, false);

    String result = copyCommand.execute(
        new String[]{"copy", "events", "between", "2024-03-15", "and", "2024-03-16", "--target",
            "target", "to", "2024-03-17"});

    assertTrue(result.contains("Successfully copied 2 events"));

    assertEquals(2,
        targetCalendar.getEventsInRange(LocalDateTime.of(2024, 3, 17, 0, 0).toLocalDate(),
            LocalDateTime.of(2024, 3, 18, 0, 0).toLocalDate()).size());
  }

  @Test
  public void testCopyEventToSameCalendar() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event event = new Event("Internal Copy", startTime, endTime, null, null, true);
    sourceCalendar.addEvent(event, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Internal Copy", "on", "2024-03-15T10:00", "--target",
            "source", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully") || result.contains("Cannot add event"));
  }

  @Test
  public void testCopyToNonExistentCalendar() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime, "Test Description",
        "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Meeting", "on", "2024-03-15T10:00", "--target",
            "nonexistent", "to", "2024-03-16T10:00"});

    assertTrue("Error message should contain 'Target calendar'",
        result.contains("Target calendar"));
    assertTrue("Error message should contain 'does not exist'", result.contains("does not exist"));
  }

  @Test
  public void testCopyEventWithSameTimeDifferentSubject() throws Exception {
    LocalDateTime time = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event original = new Event("Original Event", time, time.plusHours(1), "Desc", "Loc", true);
    sourceCalendar.addEvent(original, false);

    Event existing = new Event("Different Event", time, time.plusHours(1), "Desc", "Loc", true);
    targetCalendar.addEvent(existing, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Original Event", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-15T10:00"});

    assertTrue(result.contains("copied successfully") || result.contains("conflict"));
  }

  @Test
  public void testCopyNonExistentEvent() throws Exception {
    String result = copyCommand.execute(
        new String[]{"copy", "event", "Nonexistent Meeting", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-16T10:00"});

    assertTrue("Error message should contain 'Event not found'",
        result.contains("Event not found"));
    assertTrue("Error message should contain the event name",
        result.contains("Nonexistent Meeting"));
  }

  @Test
  public void testInvalidCommandFormat() throws Exception {
    String result = copyCommand.execute(new String[]{"copy", "invalid", "format"});

    assertTrue("Error message should contain 'Error: Unknown copy command format'",
        result.contains("Error: Unknown copy command format"));
  }

  @Test
  public void testCopyZeroDurationEvent() throws Exception {
    LocalDateTime time = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event event = new Event("Zero Duration", time, time, null, null, true);
    sourceCalendar.addEvent(event, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Zero Duration", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully"));
  }

  @Test
  public void testCopyWithTimezoneConversion() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime, "Test Description",
        "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    calendarManager.createCalendar("targetEST", "America/New_York");
    ICalendar targetCalendarEST = calendarManager.getCalendar("targetEST");

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Meeting", "on", "2024-03-15T10:00", "--target",
            "targetEST", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully"));
  }

  @Test
  public void testCopyWithConflicts() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime, "Test Description",
        "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    Event conflictingEvent = new Event("Conflicting Meeting", startTime, endTime,
        "Conflicting Description", "Conflicting Location", true);
    targetCalendar.addEvent(conflictingEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Meeting", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-15T10:00"});

    assertTrue("Error message should contain conflict information", result.contains(
        "Cannot add event 'Test Meeting' due to conflict " + "with an existing event"));
  }

  @Test
  public void testCopyEventWithQuotedName() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Team Meeting with \"Quotes\"", startTime, endTime,
        "Test Description", "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Team Meeting with \"Quotes\"", "on", "2024-03-15T10:00",
            "--target", "target", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully"));

    Event copiedEvent = targetCalendar.findEvent("Team Meeting with \"Quotes\"",
        LocalDateTime.of(2024, 3, 16, 10, 0));
    assertNotNull(copiedEvent);
    assertEquals("Team Meeting with \"Quotes\"", copiedEvent.getSubject());
  }

  @Test
  public void testCopyEventWithSpecialCharacters() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Meeting with, commas", startTime, endTime,
        "Description with, commas", "Location with, commas", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Meeting with, commas", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully"));

    Event copiedEvent = targetCalendar.findEvent("Meeting with, commas",
        LocalDateTime.of(2024, 3, 16, 10, 0));
    assertNotNull(copiedEvent);
    assertEquals("Meeting with, commas", copiedEvent.getSubject());
    assertEquals("Description with, commas", copiedEvent.getDescription());
    assertEquals("Location with, commas", copiedEvent.getLocation());
  }

  @Test
  public void testCopyAllDayEvent() throws Exception {
    LocalDateTime start = LocalDateTime.of(2024, 3, 15, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 3, 15, 23, 59);
    Event event = new Event("All Day Event", start, end, null, null, true);
    sourceCalendar.addEvent(event, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "All Day Event", "on", "2024-03-15T00:00", "--target",
            "target", "to", "2024-03-16T00:00"});

    assertTrue(result.contains("copied successfully"));
  }

  @Test
  public void testCopyEventWithEmptyFields() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Empty Fields Event", startTime, endTime, "", "", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Empty Fields Event", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully"));

    Event copiedEvent = targetCalendar.findEvent("Empty Fields Event",
        LocalDateTime.of(2024, 3, 16, 10, 0));
    assertNotNull(copiedEvent);
    assertEquals("Empty Fields Event", copiedEvent.getSubject());
    assertEquals("", copiedEvent.getDescription());
    assertEquals("", copiedEvent.getLocation());
  }

  @Test
  public void testCopyEventToLeapDay() throws Exception {
    LocalDateTime start = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event event = new Event("Leap Test", start, end, "Leap Desc", "Leap Loc", true);
    sourceCalendar.addEvent(event, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Leap Test", "on", "2024-03-15T10:00", "--target", "target",
            "to", "2024-02-29T10:00"});

    assertTrue(result.contains("copied successfully"));
  }

  @Test
  public void testCopyEventWithNullFields() throws Exception {
    Event testEvent = new Event("Null Fields Event", LocalDateTime.of(2024, 3, 15, 10, 0),
        LocalDateTime.of(2024, 3, 15, 11, 0), null, null, true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Null Fields Event", "on", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-16T10:00"});

    assertTrue(result.contains("copied successfully"));

    Event copiedEvent = targetCalendar.findEvent("Null Fields Event",
        LocalDateTime.of(2024, 3, 16, 10, 0));
    assertNotNull(copiedEvent);
    assertEquals("Null Fields Event", copiedEvent.getSubject());
    assertEquals("", copiedEvent.getDescription());
    assertEquals("", copiedEvent.getLocation());
  }

  @Test
  public void testCopyEventWithInvalidDateTimeFormat() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Event", startTime, endTime, "Description", "Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Event", "on", "invalid-date", "--target", "target",
            "to", "2024-03-16T10:00"});

    assertTrue("Error message should contain 'Invalid date time format'",
        result.contains("Invalid date time format"));
  }

  @Test
  public void testCopyEventWithInvalidTargetDateTimeFormat() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Event", startTime, endTime, "Description", "Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Event", "on", "2024-03-15T10:00", "--target", "target",
            "to", "invalid-date"});

    assertTrue("Error message should contain 'Invalid date time format'",
        result.contains("Invalid date time format"));
  }

  @Test
  public void testCopyEventWithInvalidCommandFormat() throws Exception {
    String result = copyCommand.execute(new String[]{"copy", "invalid", "format"});

    assertTrue("Error message should contain 'Unknown copy command format'",
        result.contains("Unknown copy command format"));
  }

  @Test
  public void testCopyEventWithMissingArguments() throws Exception {
    String result = copyCommand.execute(new String[]{"copy", "event", "Test Event"});

    assertTrue("Error message should contain 'Insufficient arguments'",
        result.contains("Insufficient arguments"));
  }

  @Test
  public void testCopyEventWithInvalidTargetFlag() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Event", startTime, endTime, "Description", "Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Event", "on", "2024-03-15T10:00", "--invalid", "target",
            "to", "2024-03-16T10:00"});

    assertTrue("Error message should contain 'Expected '--target' flag'",
        result.contains("Expected '--target' flag"));
  }

  @Test
  public void testCopyEventWithInvalidToKeyword() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Event", startTime, endTime, "Description", "Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Event", "on", "2024-03-15T10:00", "--target", "target",
            "invalid", "2024-03-16T10:00"});

    assertTrue("Error message should contain 'Expected 'to' keyword'",
        result.contains("Expected 'to' keyword"));
  }

  @Test
  public void testCopyEventWithInvalidOnKeyword() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 11, 0);
    Event testEvent = new Event("Test Event", startTime, endTime, "Description", "Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(
        new String[]{"copy", "event", "Test Event", "invalid", "2024-03-15T10:00", "--target",
            "target", "to", "2024-03-16T10:00"});

    assertTrue("Error message should contain 'Expected 'on' keyword'",
        result.contains("Expected 'on' keyword"));
  }
}
