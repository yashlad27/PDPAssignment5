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

    Event regularEvent = new Event("Team Meeting", LocalDateTime.of(2023, 5, 15, 9, 0),
        LocalDateTime.of(2023, 5, 15, 10, 30), "Weekly team sync", "Conference Room A", true);

    Event allDayEvent = Event.createAllDayEvent("Company Holiday", LocalDate.of(2023, 5, 29),
        "Memorial Day", null, true);

    Event multiDayEvent = new Event("Conference", LocalDateTime.of(2023, 6, 1, 9, 0),
        LocalDateTime.of(2023, 6, 3, 17, 0), "Annual tech conference", "Convention Center", true);

    Event privateEvent = new Event("Meeting with \"Client, Inc.\"",
        LocalDateTime.of(2023, 5, 16, 14, 0), LocalDateTime.of(2023, 5, 16, 15, 0),
        "Discuss new project\nwith action items", "Client's office", false);

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
  public void testExportEmptyList() throws IOException {
    String filePath = CSVExporter.exportToCSV(TEST_FILE_PATH, new ArrayList<>());

    assertNotNull("File path should not be null", filePath);
    assertTrue("CSV file should exist for empty list", new File(filePath).exists());

    List<String> lines = Files.readAllLines(Paths.get(filePath));

    assertEquals("CSV file should only contain header", 1, lines.size());
    assertEquals("CSV header should be correct",
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,"
            + "Location,Private", lines.get(0));
  }

  @Test
  public void testFormatEventsForDisplay_SingleDay() {
    String formatted = CSVExporter.formatEventsForDisplay(events, true);

    assertTrue("Output should contain Team Meeting", formatted.contains("Team Meeting"));
    assertTrue("Output should contain Conference Room A", formatted.contains("Conference Room A"));
    assertTrue("Output should indicate all-day events", formatted.contains("(All day)"));
    assertTrue("Output should show time format", formatted.contains("09:00 to 10:30"));
    assertFalse("Single day view should not show dates in times",
        formatted.contains("2023-05-15 09:00"));
  }

  @Test
  public void testFormatEventsForDisplay_MultiDay() {
    String formatted = CSVExporter.formatEventsForDisplay(events, false);

    assertTrue("Output should contain dates for multi-day view", formatted.contains("2023-05-15"));
    assertTrue("Output should show all-day event date", formatted.contains("on 2023-05-29"));

    assertTrue("Should show start date for multi-day event", formatted.contains("2023-06-01"));
    assertTrue("Should show end date for multi-day event", formatted.contains("2023-06-03"));
  }

  @Test
  public void testFormatEventsForDisplay_EmptyList() {
    String formatted = CSVExporter.formatEventsForDisplay(new ArrayList<>(), true);

    assertEquals("Should show no events message", "No events found.", formatted);
  }

  @Test
  public void testEventWithNullFields() throws IOException {
    Event nullFieldsEvent = new Event("Null Fields Event", LocalDateTime.of(2023, 5, 20, 10, 0),
        LocalDateTime.of(2023, 5, 20, 11, 0), null, null, true);

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
  public void testEscapeCSV() throws IOException {
    Event commaInTitle = new Event("Meeting, with, commas", LocalDateTime.now(),
        LocalDateTime.now().plusHours(1), "Description", "Location", true);

    Event quoteInDescription = new Event("Regular Meeting", LocalDateTime.now(),
        LocalDateTime.now().plusHours(1), "With \"quoted\" text", "Location", true);

    Event newlineInLocation = new Event("Another Meeting", LocalDateTime.now(),
        LocalDateTime.now().plusHours(1), "Description", "First floor\nSecond building", true);

    List<Event> specialCharEvents = Arrays.asList(commaInTitle, quoteInDescription,
        newlineInLocation);

    String filePath = CSVExporter.exportToCSV(TEST_FILE_PATH, specialCharEvents);

    String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));

    assertTrue("Comma in title should be escaped",
        fileContent.contains("\"Meeting, with, commas\""));

    assertTrue("Quote in description should be escaped",
        fileContent.contains("\"With \"\"quoted\"\" text\""));

    assertTrue("Newline in location should be escaped",
        fileContent.contains("\"First floor\nSecond building\""));
  }

  @Test
  public void testExportToCSV() throws IOException {
    String filePath = CSVExporter.exportToCSV(TEST_FILE_PATH, events);

    assertNotNull("File path should not be null", filePath);
    assertTrue("CSV file should exist", new File(filePath).exists());

    String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));

    assertTrue("CSV should contain header", fileContent.contains(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,"
            + "Location,Private"));

    assertTrue("CSV should contain Team Meeting", fileContent.contains("Team Meeting,"));

    assertTrue("CSV should contain Company Holiday as all-day event",
        fileContent.contains("Company Holiday") && fileContent.contains(",True,"));

    assertTrue("CSV should contain the private event",
        fileContent.contains("\"Meeting with \"\"Client, Inc.\"\"\""));

    assertTrue("Private event should be marked as private",
        fileContent.contains("Client's office,True"));
  }
}