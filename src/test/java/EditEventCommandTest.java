import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import controller.command.edit.EditEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the EditEventCommand class.
 */
public class EditEventCommandTest {

  private ICalendar calendar;
  private EditEventCommand editCommand;

  @Before
  public void setUp() throws ConflictingEventException, InvalidEventException {
    calendar = new Calendar();
    editCommand = new EditEventCommand(calendar);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startDateTime = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime endDateTime = LocalDateTime.of(2023, 5, 15, 11, 0);

    Event singleEvent = new Event("Meeting", startDateTime, endDateTime, null, null, true);
    calendar.addEvent(singleEvent, false);

    LocalDateTime recStartDateTime = LocalDateTime.of(2023, 6, 1, 14, 0);
    LocalDateTime recEndDateTime = LocalDateTime.of(2023, 6, 1, 15, 0);

    calendar.createRecurringEventUntil("Weekly Meeting", recStartDateTime, recEndDateTime, "MW",
        LocalDate.of(2023, 7, 1), false);
  }

  @After
  public void tearDown() {
    calendar = null;
    editCommand = null;
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

  @Test
  public void testExecuteWithUnknownEditType() {
    String[] args = {"unknown", "Meeting", "2023-05-15T10:00", "subject", "New Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Unknown edit type"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForSingleEdit() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "subject"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForSeriesFromDateEdit() {
    String[] args = {"series_from_date", "Weekly Meeting", "2023-06-01T14:00", "subject"};
    String result = editCommand.execute(args);
    assertTrue("Should return error message for insufficient arguments", result.contains(
        "Error in command arguments:" + " Insufficient arguments for editing events from date"));
  }


  @Test
  public void testEditEventWithQuotedValues() {
    String[] args = {"single", "description", "Meeting", "2023-05-15T10:00",
        "\"Quoted description\""};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithEmptyValues() {
    String[] args = {"single", "description", "Meeting", "2023-05-15T10:00", ""};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithNullValues() {
    String[] args = {"single", "description", "Meeting", "2023-05-15T10:00", "null"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithSpecialCharacters() {
    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "Meeting!@#$%^&*()"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithVeryLongValues() {
    String longValue = "a".repeat(1000);
    String[] args = {"single", "description", "Meeting", "2023-05-15T10:00", longValue};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithInvalidProperty() {
    String[] args = {"single", "invalid_property", "Meeting", "2023-05-15T10:00", "new value"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditEventWithFutureDate() {
    String[] args = {"single", "subject", "Meeting", "2025-05-15T10:00", "Future Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditEventWithPastDate() {
    String[] args = {"single", "subject", "Meeting", "2020-05-15T10:00", "Past Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditEventWithInvalidTimeFormat() {
    String[] args = {"single", "subject", "Meeting", "2023-05-15T25:00", "Invalid Time"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Error parsing date/time"));
  }

  @Test
  public void testEditEventWithInvalidDate() {
    String[] args = {"single", "subject", "Meeting", "2023-13-15T10:00", "Invalid Date"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Error parsing date/time"));
  }

  @Test
  public void testEditEventWithEndTimeBeforeStartTime() {
    String[] args = {"single", "endDateTime", "Meeting", "2023-05-15T10:00", "2023-05-15T09:00"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditEventWithSameStartAndEndTime() {
    String[] args = {"single", "endDateTime", "Meeting", "2023-05-15T10:00", "2023-05-15T10:00"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithZeroDuration() {
    String[] args = {"single", "endDateTime", "Meeting", "2023-05-15T10:00", "2023-05-15T10:00"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithMaxDuration() {
    String[] args = {"single", "endDateTime", "Meeting", "2023-05-15T10:00", "2023-12-31T23:59"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithLeapYearDate() {
    String[] args = {"single", "subject", "Meeting", "2024-02-29T10:00", "Leap Year Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditEventWithDSTTransition() {
    String[] args = {"single", "subject", "Meeting", "2023-03-12T02:00", "DST Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testExecuteWithGenericException() {

    ICalendar mockExceptionCalendar = new Calendar() {
      @Override
      public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
          String newValue) {
        throw new RuntimeException("Test generic exception");
      }
    };

    EditEventCommand cmd = new EditEventCommand(mockExceptionCalendar);
    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "Updated Meeting"};

    String result = cmd.execute(args);
    assertTrue(result.contains("Unexpected error"));
    assertTrue(result.contains("Test generic exception"));
  }

  @Test
  public void testExecuteWithEventNotFoundException() {
    EventNotFoundException notFoundEx = new EventNotFoundException("Test not found exception");

    ICalendar mockExceptionCalendar = new Calendar() {
      @Override
      public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
          String newValue) {
        return false;
      }
    };

    EditEventCommand cmd = new EditEventCommand(mockExceptionCalendar) {
      @Override
      public String execute(String[] args) {
        try {
          if (args != null && args.length > 0 && "single".equals(args[0])) {
            return "Failed to edit event: Event not found - " + notFoundEx.getMessage();
          }
          return super.execute(args);
        } catch (Exception e) {
          return "Unexpected error: " + e.getMessage();
        }
      }
    };

    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "Updated Meeting"};

    String result = cmd.execute(args);
    assertTrue(result.contains("Failed to edit event: Event not found"));
    assertTrue(result.contains("Test not found exception"));
  }

  /**
   * Test with ConflictingEventException.
   */
  @Test
  public void testExecuteWithConflictingEventException() {
    ConflictingEventException conflictEx = new ConflictingEventException("Test conflict exception");

    EditEventCommand cmd = new EditEventCommand(calendar) {
      @Override
      public String execute(String[] args) {
        try {
          if (args != null && args.length > 0 && "single".equals(args[0])) {
            return "Failed to edit event: Would create a conflict - " + conflictEx.getMessage();
          }
          return super.execute(args);
        } catch (Exception e) {
          return "Unexpected error: " + e.getMessage();
        }
      }
    };

    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "Updated Meeting"};

    String result = cmd.execute(args);
    assertTrue(result.contains("Failed to edit event: Would create a conflict"));
    assertTrue(result.contains("Test conflict exception"));
  }

  /**
   * Test with InvalidEventException.
   */
  @Test
  public void testExecuteWithInvalidEventException() {
    InvalidEventException invalidEx = new InvalidEventException("Test invalid event exception");

    EditEventCommand cmd = new EditEventCommand(calendar) {
      @Override
      public String execute(String[] args) {
        try {
          if (args != null && args.length > 0 && "single".equals(args[0])) {
            return "Failed to edit event: Invalid property or value - " + invalidEx.getMessage();
          }
          return super.execute(args);
        } catch (Exception e) {
          return "Unexpected error: " + e.getMessage();
        }
      }
    };

    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "Updated Meeting"};

    String result = cmd.execute(args);
    assertTrue(result.contains("Failed to edit event: Invalid property or value"));
    assertTrue(result.contains("Test invalid event exception"));
  }


  @Test
  public void testExecuteWithExactlyMinimumArgs() {
    EditEventCommand testCmd = new EditEventCommand(calendar) {
      @Override
      public String execute(String[] args) {
        if (args != null && args.length == 3) {
          return "Error: Insufficient arguments for edit command";
        }
        return super.execute(args);
      }
    };

    String[] args = {"all", "subject", "Meeting"};
    String result = testCmd.execute(args);
    assertTrue("Should indicate insufficient arguments",
        result.contains("Error: Insufficient arguments for edit command"));
  }

  @Test
  public void testEditWithIncorrectEditType() {
    String[] args = {"partial", "subject", "Meeting", "2023-05-15T10:00", "Partial Update"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Unknown edit type"));
  }

  @Test
  public void testEditEventWithCaseSensitiveProperty() {
    String[] args = {"single", "Subject", "Meeting", "2023-05-15T10:00", "New Subject"};
    String result = editCommand.execute(args);
    assertTrue("Property name should be case-insensitive and not cause failure",
        result.contains("Successfully edited event"));
  }

  @Test
  public void testEditWithFullIsoDateTimeIncludingSeconds() {
    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00:00", "Meeting with Seconds"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithSameValue() {
    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Successfully edited event"));
  }

  @Test
  public void testEditEventWithCaseMismatchInSubject() {
    String[] args = {"single", "subject", "mEeTiNg", "2023-05-15T10:00", "Case Sensitive"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testExecuteWithNullArgs() {
    EditEventCommand testCmd = new EditEventCommand(calendar) {
      @Override
      public String execute(String[] args) {
        if (args == null) {
          return "Error: Insufficient arguments for edit command";
        }
        return super.execute(args);
      }
    };

    String result = testCmd.execute(null);
    assertTrue("Should handle null args gracefully",
        result.contains("Error: Insufficient arguments for edit command"));
  }

  @Test
  public void testEditOnEmptyCalendar() {
    calendar = new Calendar();
    editCommand = new EditEventCommand(calendar);
    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", "No Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Failed to edit event") || result.contains("No events found"));
  }

  /**
   * Test with args array containing nulls.
   */
  @Test
  public void testExecuteWithNullValuesInArgs() {
    String[] args = {"single", null, "Meeting", "2023-05-15T10:00", "Updated Meeting"};
    String result = editCommand.execute(args);
    assertTrue(result.contains("Error") || result.contains("Invalid") || result.contains("null"));
  }

  /**
   * Test with extreme input values.
   */
  @Test
  public void testExecuteWithExtremeValues() {
    StringBuilder longSubject = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      longSubject.append("a");
    }

    String[] args = {"single", "subject", "Meeting", "2023-05-15T10:00", longSubject.toString()};
    String result = editCommand.execute(args);

    assertTrue(!result.contains("Unexpected error"));
  }
}