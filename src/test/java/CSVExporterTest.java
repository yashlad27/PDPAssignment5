import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.event.Event;
import utilities.CSVExporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the CSVExporter utility.
 */
public class CSVExporterTest {

  private static final String TEST_FILE_PATH = "test_export.csv";
  private List<Event> events;

  @Before
  public void setUp() {
    events = new ArrayList<>();

    Event regularEvent = new Event("Team Meeting",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 30),
            "Weekly team sync", "Conference Room A", true);

    Event allDayEvent = Event.createAllDayEvent("Company Holiday",
            LocalDate.of(2023, 5, 29),
            "Memorial Day", null, true);

    Event multiDayEvent = new Event("Conference",
            LocalDateTime.of(2023, 6, 1, 9, 0),
            LocalDateTime.of(2023, 6, 3, 17, 0),
            "Annual tech conference", "Convention Center", true);

    Event privateEvent = new Event("Meeting with \"Client, Inc.\"",
            LocalDateTime.of(2023, 5, 16, 14, 0),
            LocalDateTime.of(2023, 5, 16, 15, 0),
            "Discuss new project\nwith action items", "Client's office",
            false);

    events.add(regularEvent);
    events.add(allDayEvent);
    events.add(multiDayEvent);
    events.add(privateEvent);
  }

  @After
  public void tearDown() {
    File testFile = new File(TEST_FILE_PATH);
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  @Test
  public void testExportToCSV() throws IOException {
    String filePath = CSVExporter.exportToCSV(TEST_FILE_PATH, events);
    assertNotNull("File path should not be null", filePath);
    assertTrue("File should exist", new File(filePath).exists());
  }

  @Test
  public void testFormatEventsForDisplay_WithDetails() {
    String formatted = CSVExporter.formatEventsForDisplay(events, true);

    // Check basic event information
    assertTrue("Output should contain Team Meeting",
            formatted.contains("Team Meeting"));
    assertTrue("Output should contain Conference Room A",
            formatted.contains("Conference Room A"));
    assertTrue("Output should contain Company Holiday",
            formatted.contains("Company Holiday"));

    // Check time format
    assertTrue("Output should show time format",
            formatted.contains("09:00 to 10:30"));

    // Check all-day event format
    assertTrue("Output should indicate all-day events",
            formatted.contains("(All Day)"));

    // Check details
    assertTrue("Output should contain description",
            formatted.contains("Weekly team sync"));
    assertTrue("Output should contain location",
            formatted.contains("Conference Room A"));
    assertTrue("Output should indicate private events",
            formatted.contains("Private"));
  }

  @Test
  public void testFormatEventsForDisplay_WithoutDetails() {
    String formatted = CSVExporter.formatEventsForDisplay(events, false);

    // Check basic event information
    assertTrue("Output should contain Team Meeting",
            formatted.contains("Team Meeting"));
    assertTrue("Output should contain Company Holiday",
            formatted.contains("Company Holiday"));

    // Check time format
    assertTrue("Output should show time format",
            formatted.contains("09:00 to 10:30"));

    // Check all-day event format
    assertTrue("Output should indicate all-day events",
            formatted.contains("(All Day)"));

    // Verify details are not shown
    assertFalse("Output should not contain description",
            formatted.contains("Weekly team sync"));
    assertFalse("Output should not contain location",
            formatted.contains("Conference Room A"));
    assertFalse("Output should not indicate private events",
            formatted.contains("Private"));
  }

  @Test
  public void testFormatEventsForDisplay_EmptyList() {
    String formatted = CSVExporter.formatEventsForDisplay(new ArrayList<>(), true);
    assertEquals("Should show no events message", "No events found.", formatted);
  }

  @Test
  public void testFormatEventsForDisplay_NullList() {
    String formatted = CSVExporter.formatEventsForDisplay(null, true);
    assertEquals("Should show no events message", "No events found.", formatted);
  }

  @Test
  public void testEventWithNullFields() throws IOException {
    Event nullFieldsEvent = new Event("Null Fields Event",
            LocalDateTime.of(2023, 5, 20, 10, 0),
            LocalDateTime.of(2023, 5, 20, 11, 0),
            null, null, true);

    List<Event> singleEventList = Arrays.asList(nullFieldsEvent);
    String filePath = CSVExporter.exportToCSV(TEST_FILE_PATH, singleEventList);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String eventLine = lines.get(1);

    assertTrue("Event line should have the correct name",
            eventLine.startsWith("Null Fields Event,"));

    String[] parts = eventLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    assertEquals("Description should be empty for null", "", parts[6]);
    assertEquals("Location should be empty for null", "", parts[7]);

    String formatted = CSVExporter.formatEventsForDisplay(singleEventList, true);
    assertTrue("Should contain event name", formatted.contains("Null Fields Event"));
    assertFalse("Should not mention null location", formatted.contains("at null"));
  }

  @Test
  public void testEventWithSpecialCharacters() throws IOException {
    Event specialCharsEvent = new Event("Meeting with \"Client, Inc.\"",
            LocalDateTime.of(2023, 5, 20, 10, 0),
            LocalDateTime.of(2023, 5, 20, 11, 0),
            "Description with, comma", "Location with, comma", true);

    List<Event> singleEventList = Arrays.asList(specialCharsEvent);
    String filePath = CSVExporter.exportToCSV(TEST_FILE_PATH, singleEventList);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String eventLine = lines.get(1);

    assertTrue("Event line should properly escape quotes",
            eventLine.contains("\"Meeting with \"\"Client, Inc.\"\"\""));
    assertTrue("Event line should properly escape commas in description",
            eventLine.contains("\"Description with, comma\""));
    assertTrue("Event line should properly escape commas in location",
            eventLine.contains("\"Location with, comma\""));
  }
}