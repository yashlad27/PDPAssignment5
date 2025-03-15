import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import controller.command.ExportCalendarCommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test suite verifies the functionality of the ExportCalendarCommand using a mock ICalendar.
 */
public class ExportCalendarCommandTest {

  /**
   * A mock implementation of the {@link ICalendar} interface used for testing.
   * This mock allows control over the behavior of the {@code exportToCSV} method
   * by setting predefined responses or exceptions. It also keeps track of the last
   * file path used for exporting.
   */
  private static class MockCalendar implements ICalendar {
    private String lastFilePath = null;
    private String exportResult = null;
    private IOException exportException = null;

    public void setExportResult(String result) {
      this.exportResult = result;
      this.exportException = null;
    }

    public void setExportException(IOException exception) {
      this.exportException = exception;
      this.exportResult = null;
    }

    public String getLastFilePath() {
      return lastFilePath;
    }

    @Override
    public String exportToCSV(String filePath) throws IOException {
      this.lastFilePath = filePath;
      if (exportException != null) {
        throw exportException;
      }
      return exportResult;
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                             String weekdays, LocalDate untilDate,
                                             boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline,
                                              String description, String location,
                                              boolean isPublic) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate,
                                                   boolean autoDecline,
                                                   String description,
                                                   String location,
                                                   boolean isPublic) {
      return false;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return null;
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      return null;
    }

    @Override
    public Event findEvent(String subject, LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public List<Event> getAllEvents() {
      return null;
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime,
                                   String property, String newValue) {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime,
                                  String property, String newValue) {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) {
      return 0;
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return null;
    }
  }

  private MockCalendar calendar;
  private ExportCalendarCommand command;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    command = new ExportCalendarCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("export", command.getName());
  }

  @Test
  public void testExecuteWithValidFilename() {
    String[] args = {"calendar.csv"};
    String expectedPath = "/absolute/path/to/calendar.csv";
    calendar.setExportResult(expectedPath);

    String result = command.execute(args);

    assertEquals("calendar.csv", calendar.getLastFilePath());
    assertEquals("Calendar exported successfully to: " + expectedPath, result);
  }

  @Test
  public void testExecuteWithMissingFilename() {
    String[] args = {};

    String result = command.execute(args);

    assertEquals(null, calendar.getLastFilePath());
    assertEquals("Error: Missing filename for export command", result);
  }

  @Test
  public void testExecuteWithIOException() {
    String[] args = {"invalid_path.csv"};
    IOException expectedException = new IOException("Permission denied");
    calendar.setExportException(expectedException);

    String result = command.execute(args);

    assertEquals("invalid_path.csv", calendar.getLastFilePath());
    assertEquals("Failed to export calendar: Permission denied", result);
  }

  @Test
  public void testExecuteWithNullFilename() {
    String[] args = {null};

    calendar.setExportResult("/path/to/null.csv");

    String result = command.execute(args);

    assertEquals(null, calendar.getLastFilePath());
    assertTrue(result.startsWith("Calendar exported successfully") ||
            result.startsWith("Error") ||
            result.startsWith("Failed"));
  }

  @Test
  public void testExecuteWithEmptyFilename() {
    String[] args = {""};

    calendar.setExportException(new IOException("Filename cannot be empty"));

    String result = command.execute(args);

    assertEquals("", calendar.getLastFilePath());
    assertTrue(result.startsWith("Failed to export calendar:"));
    assertTrue(result.contains("Filename cannot be empty"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new ExportCalendarCommand(null);
  }

  @Test
  public void testExecuteWithExtraArguments() {
    String[] args = {"calendar.csv", "extra", "arguments"};
    String expectedPath = "/absolute/path/to/calendar.csv";
    calendar.setExportResult(expectedPath);

    String result = command.execute(args);

    assertEquals("calendar.csv", calendar.getLastFilePath());
    assertEquals("Calendar exported successfully to: " + expectedPath, result);
  }
}