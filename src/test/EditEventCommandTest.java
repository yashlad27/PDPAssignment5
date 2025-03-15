import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import controller.command.EditEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the EditEventCommand class.
 */
public class EditEventCommandTest {

  private ICalendar calendar;
  private EditEventCommand editCommand;

  @Before
  public void setUp() {
    calendar = new Calendar();
    editCommand = new EditEventCommand(calendar);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startDateTime = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime endDateTime = LocalDateTime.of(2023, 5, 15, 11, 0);

    Event singleEvent = new Event("Meeting", startDateTime, endDateTime, null,
        null,
        true
    );
    calendar.addEvent(singleEvent, false);

    LocalDateTime recStartDateTime = LocalDateTime.of(2023, 6, 1, 14, 0);
    LocalDateTime recEndDateTime = LocalDateTime.of(2023, 6, 1, 15, 0);

    calendar.createRecurringEventUntil("Weekly Meeting", recStartDateTime, recEndDateTime, "MW",
        LocalDate.of(2023, 7, 1),
        false
    );
  }

  @Test
  public void testGetName() {
    assertEquals("edit", editCommand.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new EditEventCommand(null);
  }

  @Test
  public void testEditSingleEventSuccess() {
    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "Updated Meeting"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

    boolean foundUpdatedEvent = false;
    for (Event event : calendar.getAllEvents()) {
      if (event.getSubject().equals("Updated Meeting")) {
        foundUpdatedEvent = true;
        break;
      }
    }
    assertTrue(foundUpdatedEvent);
  }

  @Test
  public void testEditSingleEventDescription() {
    String[] args = {"single", "description", "Meeting", "2023-05-15T10:00",
        "Updated meeting description"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

  }

  @Test
  public void testEditSingleEventLocation() {
    String[] args = {"single", "location", "Meeting", "2023-05-15T10:00", "Conference Room B"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

  }

  @Test
  public void testEditSingleEventNotFound() {
    String[] args = {"single", "subject", "Non-existent Meeting", "2023-05-15T10:00",
        "Updated Meeting"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditEventsFromDateSuccess() {
    String[] args = {"series_from_date", "subject", "Weekly Meeting", "2023-06-01T14:00",
        "Updated Weekly Meeting"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited"));
    assertTrue(result.contains("events in the series"));

    boolean foundUpdatedEvents = false;
    for (Event event : calendar.getAllEvents()) {
      if (event.getSubject().equals("Updated Weekly Meeting")) {
        foundUpdatedEvents = true;
        break;
      }
    }
    assertTrue(foundUpdatedEvents);
  }

  @Test
  public void testEditEventsFromDateNotFound() {
    String[] args = {"series_from_date", "subject", "Non-existent Meeting", "2023-06-01T14:00",
        "Updated Meeting"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("No matching events found"));
  }

  @Test
  public void testEditAllEventsSuccess() {
    String[] args = {"all", "subject", "Weekly Meeting", "Updated All Meetings"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited"));

    boolean foundUpdatedEvents = false;
    for (Event event : calendar.getAllEvents()) {
      if (event.getSubject().equals("Updated All Meetings")) {
        foundUpdatedEvents = true;
        break;
      }
    }
    assertTrue(foundUpdatedEvents);
  }

  @Test
  public void testEditAllEventsNotFound() {
    String[] args = {"all", "subject", "Non-existent Meeting", "Updated Meeting"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("No events found"));
  }

  @Test
  public void testEditAllEventsLocation() {
    String[] args = {"all", "location", "Weekly Meeting", "New Conference Hall"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited"));
  }

  @Test
  public void testExecuteWithInsufficientArgs() {
    String[] args = {};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testExecuteWithUnknownEditType() {
    String[] args = {"unknown_type", "subject", "Meeting", "Updated Meeting"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Unknown edit command type"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForSingleEdit() {
    String[] args = {"single", "subject", "Meeting"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForSeriesFromDateEdit() {
    String[] args = {"series_from_date", "subject", "Weekly Meeting"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForAllEventsEdit() {
    String[] args = {"all", "subject"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInvalidDateFormat() {
    String[] args = {"single", "subject", "Meeting", "invalid-date", "Updated Meeting"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error parsing date/time"));
  }

  @Test
  public void testEditEventVisibility() {
    String[] args = {"single", "visibility", "Meeting", "2023-05-15T10:00", "false"};

    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));
  }
}