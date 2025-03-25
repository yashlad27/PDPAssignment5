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

  @Test
  public void testExportWithMultiDayEvent() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event multiDayEvent = new Event("Conference",
            baseTime,
            baseTime.plusDays(3).plusHours(8), // 3 days and 8 hours
            "Annual tech conference",
            "Convention Center",
            true);
    calendar.addEvent(multiDayEvent, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Conference"));
    assertTrue(csvContent.contains("Annual tech conference"));
    assertTrue(csvContent.contains("Convention Center"));
    assertTrue(csvContent.contains("true"));
  }

  @Test
  public void testExportWithEventsAtDifferentTimes() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    
    // Morning event
    Event morningEvent = new Event("Morning Meeting",
            baseTime,
            baseTime.plusHours(1),
            "Morning sync",
            "Room 1",
            true);

    // Afternoon event
    Event afternoonEvent = new Event("Afternoon Meeting",
            baseTime.plusHours(4),
            baseTime.plusHours(5),
            "Project review",
            "Room 2",
            true);

    // Evening event
    Event eveningEvent = new Event("Evening Meeting",
            baseTime.plusHours(8),
            baseTime.plusHours(9),
            "Team dinner",
            "Restaurant",
            true);

    calendar.addEvent(morningEvent, false);
    calendar.addEvent(afternoonEvent, false);
    calendar.addEvent(eveningEvent, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Morning Meeting"));
    assertTrue(csvContent.contains("Afternoon Meeting"));
    assertTrue(csvContent.contains("Evening Meeting"));
    assertTrue(csvContent.contains("Morning sync"));
    assertTrue(csvContent.contains("Project review"));
    assertTrue(csvContent.contains("Team dinner"));
  }

  @Test
  public void testExportWithEventsWithLongDescriptions() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    String longDescription = "This is a very long description that contains multiple lines\n" +
            "and special characters like: !@#$%^&*()\n" +
            "and some more text to make it even longer...";
    
    Event event = new Event("Long Description Event",
            baseTime,
            baseTime.plusHours(1),
            longDescription,
            "Room 1",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Long Description Event"));
    assertTrue(csvContent.contains(longDescription));
  }

  @Test
  public void testExportWithEventsWithUnicodeCharacters() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event event = new Event("Meeting with Unicode: 你好世界",
            baseTime,
            baseTime.plusHours(1),
            "Description with Unicode: 🌟✨",
            "Location with Unicode: 会议室",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Meeting with Unicode: 你好世界"));
    assertTrue(csvContent.contains("Description with Unicode: 🌟✨"));
    assertTrue(csvContent.contains("Location with Unicode: 会议室"));
  }

  @Test
  public void testExportWithEventsWithVeryLongSubjects() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    String longSubject = "This is a very long subject that should be properly handled in the CSV export " +
            "and should not cause any issues with the formatting or escaping of the CSV content";
    
    Event event = new Event(longSubject,
            baseTime,
            baseTime.plusHours(1),
            "Description",
            "Location",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains(longSubject));
  }

  @Test
  public void testExportWithEventsWithWhitespace() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event event = new Event("  Event with Spaces  ",
            baseTime,
            baseTime.plusHours(1),
            "  Description with Spaces  ",
            "  Location with Spaces  ",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("  Event with Spaces  "));
    assertTrue(csvContent.contains("  Description with Spaces  "));
    assertTrue(csvContent.contains("  Location with Spaces  "));
  }

  @Test
  public void testExportWithEventsWithTabsAndNewlines() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event event = new Event("Event\twith\tTabs",
            baseTime,
            baseTime.plusHours(1),
            "Description\nwith\nNewlines",
            "Location\twith\tTabs",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Event\twith\tTabs"));
    assertTrue(csvContent.contains("Description\nwith\nNewlines"));
    assertTrue(csvContent.contains("Location\twith\tTabs"));
  }

  @Test
  public void testExportWithEventsWithVeryShortDuration() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event event = new Event("Short Meeting",
            baseTime,
            baseTime.plusMinutes(15), // 15-minute meeting
            "Quick sync",
            "Room 1",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Short Meeting"));
    assertTrue(csvContent.contains("Quick sync"));
  }

  @Test
  public void testExportWithEventsWithVeryLongDuration() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2024, 3, 15, 10, 0);
    Event event = new Event("Long Meeting",
            baseTime,
            baseTime.plusDays(7).plusHours(12), // 7 days and 12 hours
            "Extended conference",
            "Conference Center",
            true);
    calendar.addEvent(event, false);

    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    assertTrue(csvContent.contains("Long Meeting"));
    assertTrue(csvContent.contains("Extended conference"));
  }
}