import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import controller.command.ShowStatusCommand;
import model.calendar.ICalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test suite verifies the command’s availability status
 * using a mock {@link ICalendar} implementation.
 */
public class ShowStatusCommandTest {

  /**
   * A mock implementation of the {@link ICalendar} interface used for testing.
   * This class simulates a calendar system by providing controlled responses
   * for availability queries and tracking the last checked date-time.
   * It does not support actual event storage or modifications.
   */
  private static class MockCalendar implements ICalendar {
    private boolean isBusyResult = false;
    private LocalDateTime lastCheckedDateTime = null;

    public void setIsBusyResult(boolean result) {
      this.isBusyResult = result;
    }

    public LocalDateTime getLastCheckedDateTime() {
      return lastCheckedDateTime;
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      this.lastCheckedDateTime = dateTime;
      return isBusyResult;
    }

    @Override
    public boolean addEvent(model.event.Event event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(model.event.RecurringEvent recurringEvent,
                                     boolean autoDecline) {
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
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date,
                                                   String weekdays,
                                                   LocalDate untilDate,
                                                   boolean autoDecline, String description,
                                                   String location, boolean isPublic) {
      return false;
    }

    @Override
    public List<model.event.Event> getEventsOnDate(LocalDate date) {
      return null;
    }

    @Override
    public List<model.event.Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      return null;
    }

    @Override
    public model.event.Event findEvent(String subject, LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public List<model.event.Event> getAllEvents() {
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
    public List<model.event.RecurringEvent> getAllRecurringEvents() {
      return null;
    }

    @Override
    public String exportToCSV(String filePath) throws IOException {
      return null;
    }
  }

  private MockCalendar calendar;
  private ShowStatusCommand command;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    command = new ShowStatusCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("show", command.getName());
  }

  @Test
  public void testExecuteWithValidDateTimeBusy() {
    String[] args = {"2023-04-10T10:30"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T10:30");
    calendar.setIsBusyResult(true);

    String result = command.execute(args);

    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T10:30:00: Busy", result);
  }

  @Test
  public void testExecuteWithValidDateTimeAvailable() {
    String[] args = {"2023-04-10T10:30"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T10:30");
    calendar.setIsBusyResult(false);

    String result = command.execute(args);

    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T10:30:00: Available", result);
  }

  @Test
  public void testExecuteWithMissingArguments() {
    String[] args = {};

    String result = command.execute(args);

    assertEquals("Error: Missing date/time for status command", result);
    assertEquals(null, calendar.getLastCheckedDateTime());
  }

  @Test
  public void testExecuteWithInvalidDateTime() {
    String[] args = {"invalid-date-time"};

    String result = command.execute(args);

    assertTrue(result.startsWith("Error parsing date/time:"));
    assertEquals(null, calendar.getLastCheckedDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new ShowStatusCommand(null);
  }

  @Test
  public void testExecuteAtMidnight() {
    String[] args = {"2023-04-10T00:00"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T00:00");
    calendar.setIsBusyResult(true);

    String result = command.execute(args);

    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T00:00:00: Busy", result);
  }

  @Test
  public void testExecuteWithExtraArguments() {
    String[] args = {"2023-04-10T10:30", "extra", "arguments"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T10:30");
    calendar.setIsBusyResult(true);

    String result = command.execute(args);

    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T10:30:00: Busy", result);
  }
}