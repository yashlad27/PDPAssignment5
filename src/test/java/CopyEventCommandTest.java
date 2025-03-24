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
import utilities.TimeZoneHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CopyEventCommandTest {

  private CopyEventCommand copyCommand;
  private CalendarManager calendarManager;
  private TimeZoneHandler timezoneHandler;
  private ICalendar sourceCalendar;
  private ICalendar targetCalendar;

  @Before
  public void setUp() throws CalendarNotFoundException, InvalidTimezoneException,
          DuplicateCalendarException {
    timezoneHandler = new TimeZoneHandler();

    calendarManager = new CalendarManager.Builder()
            .timezoneHandler(timezoneHandler)
            .build();
    copyCommand = new CopyEventCommand(calendarManager, timezoneHandler);

    calendarManager.createCalendar("source", "UTC");
    sourceCalendar = calendarManager.getActiveCalendar();

    calendarManager.createCalendar("target", "UTC");
    targetCalendar = calendarManager.getCalendar("target");
  }

  @Test
  public void testCopySingleEvent() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15,
            10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15,
            11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime,
            "Test Description", "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(new String[]{
            "copy", "event", "Test Meeting", "on", "2024-03-15T10:00",
            "--target", "target", "to", "2024-03-16T10:00"
    });

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
    LocalDateTime startTime1 = LocalDateTime.of(2024, 3, 15,
            10, 0);
    LocalDateTime endTime1 = LocalDateTime.of(2024, 3, 15,
            11, 0);
    Event event1 = new Event("Morning Meeting", startTime1, endTime1,
            "Description 1", "Location 1", true);
    sourceCalendar.addEvent(event1, false);

    LocalDateTime startTime2 = LocalDateTime.of(2024, 3, 15,
            14, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2024, 3, 15,
            15, 0);
    Event event2 = new Event("Afternoon Meeting", startTime2, endTime2,
            "Description 2", "Location 2", true);
    sourceCalendar.addEvent(event2, false);

    String result = copyCommand.execute(new String[]{
            "copy", "events", "on", "2024-03-15",
            "--target", "target", "to", "2024-03-16"
    });

    assertTrue(result.contains("Successfully copied 2 events"));

    assertEquals(2, targetCalendar.getEventsOnDate(LocalDateTime.of(2024, 3,
            16, 0, 0).toLocalDate()).size());
  }

  @Test
  public void testCopyEventsBetweenDates() throws Exception {
    LocalDateTime startTime1 = LocalDateTime.of(2024, 3, 15,
            10, 0);
    LocalDateTime endTime1 = LocalDateTime.of(2024, 3, 15,
            11, 0);
    Event event1 = new Event("Day 1 Meeting", startTime1, endTime1,
            "Description 1", "Location 1", true);
    sourceCalendar.addEvent(event1, false);

    LocalDateTime startTime2 = LocalDateTime.of(2024, 3, 16,
            14, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2024, 3, 16,
            15, 0);
    Event event2 = new Event("Day 2 Meeting", startTime2, endTime2,
            "Description 2", "Location 2", true);
    sourceCalendar.addEvent(event2, false);

    String result = copyCommand.execute(new String[]{
            "copy", "events", "between", "2024-03-15", "and", "2024-03-16",
            "--target", "target", "to", "2024-03-17"
    });

    assertTrue(result.contains("Successfully copied 2 events"));

    assertEquals(2, targetCalendar.getEventsInRange(
            LocalDateTime.of(2024, 3, 17, 0, 0).toLocalDate(),
            LocalDateTime.of(2024, 3, 18, 0, 0).toLocalDate()
    ).size());
  }

  @Test
  public void testCopyToNonExistentCalendar() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15,
            10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15,
            11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime,
            "Test Description", "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    String result = copyCommand.execute(new String[]{
            "copy", "event", "Test Meeting", "on", "2024-03-15T10:00",
            "--target", "nonexistent", "to", "2024-03-16T10:00"
    });

    assertTrue("Error message should contain 'Target calendar'",
            result.contains("Target calendar"));
    assertTrue("Error message should contain 'does not exist'",
            result.contains("does not exist"));
  }

  @Test
  public void testCopyNonExistentEvent() throws Exception {
    String result = copyCommand.execute(new String[]{
            "copy", "event", "Nonexistent Meeting", "on", "2024-03-15T10:00",
            "--target", "target", "to", "2024-03-16T10:00"
    });

    assertTrue("Error message should contain 'Event not found'",
            result.contains("Event not found"));
    assertTrue("Error message should contain the event name",
            result.contains("Nonexistent Meeting"));
  }

  @Test
  public void testInvalidCommandFormat() throws Exception {
    String result = copyCommand.execute(new String[]{
            "copy", "invalid", "format"
    });

    assertTrue("Error message should contain 'Error: Unknown copy command format'",
            result.contains("Error: Unknown copy command format"));
  }

  @Test
  public void testCopyWithTimezoneConversion() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15,
            10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15,
            11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime,
            "Test Description", "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    calendarManager.createCalendar("targetEST", "America/New_York");
    ICalendar targetCalendarEST = calendarManager.getCalendar("targetEST");

    String result = copyCommand.execute(new String[]{
            "copy", "event", "Test Meeting", "on", "2024-03-15T10:00",
            "--target", "targetEST", "to", "2024-03-16T10:00"
    });

    assertTrue(result.contains("copied successfully"));
  }

  @Test
  public void testCopyWithConflicts() throws Exception {
    LocalDateTime startTime = LocalDateTime.of(2024, 3, 15,
            10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 3, 15,
            11, 0);
    Event testEvent = new Event("Test Meeting", startTime, endTime,
            "Test Description", "Test Location", true);
    sourceCalendar.addEvent(testEvent, false);

    Event conflictingEvent = new Event("Conflicting Meeting", startTime, endTime,
            "Conflicting Description", "Conflicting Location", true);
    targetCalendar.addEvent(conflictingEvent, false);

    String result = copyCommand.execute(new String[]{
            "copy", "event", "Test Meeting", "on", "2024-03-15T10:00",
            "--target", "target", "to", "2024-03-15T10:00"
    });

    assertTrue("Error message should contain conflict information",
            result.contains("Cannot add event 'Test Meeting' due to conflict "
                    + "with an existing event"));
  }
}
