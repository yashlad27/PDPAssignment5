import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import io.CSVCalendarExporter;
import model.calendar.Calendar;
import model.event.Event;
import model.event.RecurringEvent;

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
    // Create test events
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

    // Add events to calendar
    calendar.addEvent(event1, false);
    calendar.addEvent(event2, false);
    calendar.addRecurringEvent(recurringEvent, false);

    // Test export to StringBuilder
    StringBuilder sb = new StringBuilder();
    exporter.exportToAppendable(calendar, sb);
    String csvContent = sb.toString();

    // Verify CSV content
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
    assertTrue(csvContent.contains(",,true")); // Empty location and description
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
}