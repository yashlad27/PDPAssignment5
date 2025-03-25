import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import io.CSVCalendarExporter;
import model.calendar.Calendar;
import model.event.Event;
import model.event.RecurringEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CSVCalendarExporterTest {
  private Calendar calendar;
  private CSVCalendarExporter exporter;

  @Before
  public void setUp() {
    calendar = new Calendar();
    calendar.setName("Test Calendar");
    calendar.setTimezone("UTC");
    exporter = new CSVCalendarExporter();
  }

  @Test
  public void testExportToAppendable() throws Exception {
    Event event1 = new Event("Meeting", LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Room 1", "Team meeting", true);
    Event event2 = new Event("Lunch", LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), "Cafeteria", "Team lunch", false);

    RecurringEvent recurringEvent = new RecurringEvent.Builder(
            "Weekly Standup",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            java.util.EnumSet.of(java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.FRIDAY))
            .description("Weekly team standup")
            .location("Conference Room")
            .isPublic(true)
            .occurrences(4)
            .build();

    calendar.addEvent(event1, false);
    calendar.addEvent(event2, false);
    calendar.addRecurringEvent(recurringEvent, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Subject,Start Date,Start Time,End Date,End Time,Location,Description,Is Public"));
    assertTrue(csvContent.contains("Meeting"));
    assertTrue(csvContent.contains("Lunch"));
    assertTrue(csvContent.contains("Weekly Standup"));
    assertTrue(csvContent.contains("Room 1"));
    assertTrue(csvContent.contains("Cafeteria"));
    assertTrue(csvContent.contains("Conference Room"));
  }

  @Test
  public void testExportWithNullValues() throws Exception {
    Event event = new Event("Test", LocalDateTime.now(), LocalDateTime.now().plusHours(1), null, null, true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Test"));
    assertTrue(csvContent.contains(",,true"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportWithNullCalendar() throws Exception {
    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(null, sb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportWithNullAppendable() throws Exception {
    exporter.exportToAppendable(calendar, null);
  }

  @Test
  public void testExportWithEmptyCalendar() throws Exception {
    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Subject,Start Date,Start Time,End Date,End Time,Location,Description,Is Public"));
    assertEquals(1, csvContent.split("\n").length); // Only header line
  }

  @Test
  public void testExportWithAllDayEvent() throws Exception {
    Event allDayEvent = Event.createAllDayEvent("Holiday", LocalDateTime.now().toLocalDate(), "Company Holiday", "Office", true);
    calendar.addEvent(allDayEvent, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Holiday"));
    assertTrue(csvContent.contains("Company Holiday"));
    assertTrue(csvContent.contains("Office"));
    assertTrue(csvContent.contains("true"));
  }

  @Test
  public void testExportWithRecurringEventWithEndDate() throws Exception {
    RecurringEvent recurringEvent = new RecurringEvent.Builder(
            "Monthly Meeting",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            java.util.EnumSet.of(java.time.DayOfWeek.MONDAY))
            .description("Monthly team meeting")
            .location("Board Room")
            .isPublic(true)
            .endDate(LocalDateTime.now().plusMonths(3).toLocalDate())
            .build();

    calendar.addRecurringEvent(recurringEvent, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Monthly Meeting"));
    assertTrue(csvContent.contains("Monthly team meeting"));
    assertTrue(csvContent.contains("Board Room"));
    assertTrue(csvContent.contains("true"));
  }

  @Test
  public void testExportWithSpecialCharacters() throws Exception {
    Event event = new Event("Meeting with \"quotes\" and, commas",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "Room 1,2,3",
            "Description with \"quotes\" and, commas",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("\"Meeting with \"\"quotes\"\" and, commas\""));
    assertTrue(csvContent.contains("\"Room 1,2,3\""));
    assertTrue(csvContent.contains("\"Description with \"\"quotes\"\" and, commas\""));
  }

  @Test
  public void testExportWithMultipleRecurringEvents() throws Exception {
    RecurringEvent weeklyEvent = new RecurringEvent.Builder(
            "Weekly Meeting",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            java.util.EnumSet.of(java.time.DayOfWeek.MONDAY))
            .description("Weekly sync")
            .location("Room 1")
            .isPublic(true)
            .occurrences(4)
            .build();

    RecurringEvent monthlyEvent = new RecurringEvent.Builder(
            "Monthly Review",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2),
            java.util.EnumSet.of(java.time.DayOfWeek.FRIDAY))
            .description("Monthly review")
            .location("Room 2")
            .isPublic(false)
            .occurrences(3)
            .build();

    calendar.addRecurringEvent(weeklyEvent, false);
    calendar.addRecurringEvent(monthlyEvent, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Weekly Meeting"));
    assertTrue(csvContent.contains("Monthly Review"));
    assertTrue(csvContent.contains("Weekly sync"));
    assertTrue(csvContent.contains("Monthly review"));
    assertTrue(csvContent.contains("Room 1"));
    assertTrue(csvContent.contains("Room 2"));
    assertTrue(csvContent.contains("true"));
    assertTrue(csvContent.contains("false"));
  }

  @Test
  public void testExportWithDifferentTimezones() throws Exception {
    // Create a fixed date for consistent testing
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);

    // Create a new calendar for each timezone to avoid timezone conflicts
    Calendar nyCalendar = new Calendar();
    nyCalendar.setName("NY Calendar");
    nyCalendar.setTimezone("America/New_York");

    Calendar londonCalendar = new Calendar();
    londonCalendar.setName("London Calendar");
    londonCalendar.setTimezone("Europe/London");

    // Create events for each timezone
    Event nyEvent = new Event("Meeting 1",
            baseTime,
            baseTime.plusHours(1),
            "Room 1",
            "Meeting in NY timezone",
            true);

    Event londonEvent = new Event("Meeting 2",
            baseTime,
            baseTime.plusHours(1),
            "Room 2",
            "Meeting in London timezone",
            true);

    nyCalendar.addEvent(nyEvent, false);
    londonCalendar.addEvent(londonEvent, false);

    // Export NY calendar
    StringBuilder nySb = new StringBuilder();
    exporter.exportToAppendable(nyCalendar, nySb);
    String nyContent = nySb.toString();

    // Export London calendar
    StringBuilder londonSb = new StringBuilder();
    exporter.exportToAppendable(londonCalendar, londonSb);
    String londonContent = londonSb.toString();

    // Verify NY calendar content
    assertTrue("NY event not found in NY calendar export", nyContent.contains("Meeting 1"));
    assertTrue("NY description not found in NY calendar export", nyContent.contains("Meeting in NY timezone"));
    assertTrue("NY location not found in NY calendar export", nyContent.contains("Room 1"));

    // Verify London calendar content
    assertTrue("London event not found in London calendar export", londonContent.contains("Meeting 2"));
    assertTrue("London description not found in London calendar export", londonContent.contains("Meeting in London timezone"));
    assertTrue("London location not found in London calendar export", londonContent.contains("Room 2"));
  }
}