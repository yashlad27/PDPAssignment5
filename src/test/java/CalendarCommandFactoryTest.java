import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import controller.command.calendar.CalendarCommandFactory;
import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.CalendarNameValidator;
import utilities.TimeZoneHandler;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CalendarCommandFactoryTest {
  private CalendarManager calendarManager;
  private MockCalendarView mockView;
  private CalendarCommandFactory factory;

  @Before
  public void setUp() {
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();
    calendarManager = new CalendarManager.Builder()
            .timezoneHandler(timezoneHandler)
            .build();
    mockView = new MockCalendarView();
    factory = new CalendarCommandFactory(calendarManager, mockView);
    CalendarNameValidator.clear(); // Clear the validator before each test
  }

  @After
  public void tearDown() {
    CalendarNameValidator.clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendarManager() {
    new CalendarCommandFactory(null, mockView);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullView() {
    new CalendarCommandFactory(calendarManager, null);
  }

  @Test
  public void testHasCommand() {
    assertTrue(factory.hasCommand("create"));
    assertTrue(factory.hasCommand("edit"));
    assertTrue(factory.hasCommand("use"));
    assertTrue(factory.hasCommand("copy"));
    assertFalse(factory.hasCommand("invalid"));
  }

  @Test
  public void testCreateCalendarCommand() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "OneTestCalendar", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.contains("Calendar 'OneTestCalendar' created with timezone 'America/New_York'"));
    assertTrue(calendarManager.getCalendarNames().contains("OneTestCalendar"));

    // Verify view interactions
    assertTrue(mockView.getDisplayedMessages().contains(result));
  }

  @Test
  public void testCreateCalendarCommandWithInvalidArgs() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "TestCalendar"}; // Missing timezone
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.contains("Error: Insufficient arguments"));

    // Verify error was displayed
    assertTrue(mockView.getDisplayedErrors().contains(result));
  }

  @Test
  public void testEditCalendarCommand() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "NewTestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then edit it
    String[] editArgs = {"calendar", "--name", "NewTestCalendar", "--property", "name", "NewName"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue(result.contains("Calendar name changed"));
    assertTrue(calendarManager.getCalendarNames().contains("NewName"));

    // Verify view interactions
    assertTrue(mockView.getDisplayedMessages().contains(result));
  }

  @Test
  public void testEditCalendarTimezone() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "NewnewTestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then edit timezone
    String[] editArgs = {"calendar", "--name", "NewnewTestCalendar", "--property",
            "timezone", "America/Los_Angeles"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue(result.contains("Timezone for calendar"));

    // Verify view interactions
    assertTrue(mockView.getDisplayedMessages().contains(result));
  }

  @Test
  public void testUseCalendarCommand() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException, CalendarNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "thirdNewTestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then use it
    String[] useArgs = {"calendar", "--name", "thirdNewTestCalendar"};
    String result = factory.getCommand("use").execute(useArgs);
    assertTrue(result.contains("Now using calendar: 'thirdNewTestCalendar'"));
    assertEquals("thirdNewTestCalendar", calendarManager.getActiveCalendar().getName());

    // Verify view interactions
    assertTrue(mockView.getDisplayedMessages().contains(result));
  }

  @Test
  public void testUseNonExistentCalendar() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "NonExistentCalendar"};
    String result = factory.getCommand("use").execute(args);
    assertTrue(result.contains("Error"));

    // Verify error was displayed
    assertTrue(mockView.getDisplayedErrors().contains(result));
  }

  @Test
  public void testEditNonExistentCalendar() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "NonExistentCalendar", "--property", "name", "NewName"};
    String result = factory.getCommand("edit").execute(args);
    assertTrue(result.contains("Error"));

    // Verify error was displayed
    assertTrue(mockView.getDisplayedErrors().contains(result));
  }

  @Test
  public void testCreateDuplicateCalendar() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Try to create it again
    String result = factory.getCommand("create").execute(createArgs);
    assertTrue(result.contains("Error"));

    // Verify error was displayed
    assertTrue(mockView.getDisplayedErrors().contains(result));
  }

  @Test
  public void testEditCalendarWithInvalidProperty() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Try to edit with invalid property
    String[] editArgs = {"calendar", "--name", "TestCalendar", "--property", "invalid", "value"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue(result.contains("Error: Unsupported property"));

    // Verify error was displayed
    assertTrue(mockView.getDisplayedErrors().contains(result));
  }

  @Test
  public void testGetCommandWithInvalidCommand() {
    assertNull(factory.getCommand("invalid"));
  }

  @Test
  public void testGetCommandWithNullCommand() {
    assertNull(factory.getCommand(null));
  }

  @Test
  public void testCreateCalendarWithInvalidTimezone() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "TestCalendar", "--timezone", "Invalid/Timezone"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.contains("Error"));

    // Verify error was displayed
    assertTrue(mockView.getDisplayedErrors().contains(result));
  }

  @Test
  public void testEditCalendarWithInvalidTimezone() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Try to edit with invalid timezone
    String[] editArgs = {"calendar", "--name", "TestCalendar", "--property", "timezone", "Invalid/Timezone"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue(result.contains("Error"));

    // Verify error was displayed
    assertTrue(mockView.getDisplayedErrors().contains(result));
  }

  // Test calendar creation with invalid parameters
  @Test
  public void testCreateCalendarWithNullName() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", null, "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithEmptyName() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithMissingTimezone() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "TestCalendar", "--timezone"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }


  // Test calendar operations with empty calendars
  @Test
  public void testUseEmptyCalendar() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "EmptyCalendar"};
    String result = factory.getCommand("use").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testEditEmptyCalendar() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "EmptyCalendar", "--property", "name", "NewName"};
    String result = factory.getCommand("edit").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  // Test calendar operations with maximum number of events
  @Test
  public void testCreateCalendarWithMaximumEvents() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "MaxEventsCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);

    // Add maximum number of events (assuming a reasonable limit)
    for (int i = 0; i < 1000; i++) {
      String[] eventArgs = {"event", "create", "--name", "Event" + i, "--start", "2024-01-01T10:00", "--end", "2024-01-01T11:00"};
      factory.getCommand("create").execute(eventArgs);
    }

    // Try to add one more event
    String[] extraEventArgs = {"event", "create", "--name", "ExtraEvent", "--start", "2024-01-01T10:00", "--end", "2024-01-01T11:00"};
    String result = factory.getCommand("create").execute(extraEventArgs);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithDuplicateName() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "DuplicateCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);

    // Try to create another calendar with the same name
    String[] duplicateArgs = {"calendar", "--name", "DuplicateCalendar", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(duplicateArgs);
    assertTrue(result.startsWith("Error:"));
  }

  // Test calendar operations with special characters in names
  @Test
  public void testCreateCalendarWithSpecialCharacters() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "Test@#$Calendar", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  // Test calendar operations with very long names
  @Test
  public void testCreateCalendarWithLongName() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String longName = "A".repeat(1000); // Create a very long name
    String[] args = {"calendar", "--name", longName, "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  /**
   * Mock implementation of ICalendarView for testing purposes.
   * Tracks displayed messages and errors for verification.
   */
  private static class MockCalendarView implements ICalendarView {
    private final List<String> displayedMessages;
    private final List<String> displayedErrors;

    public MockCalendarView() {
      this.displayedMessages = new ArrayList<>();
      this.displayedErrors = new ArrayList<>();
    }

    @Override
    public String readCommand() {
      return "";
    }

    @Override
    public void displayMessage(String message) {
      displayedMessages.add(message);
    }

    @Override
    public void displayError(String error) {
      displayedErrors.add(error);
    }

    public List<String> getDisplayedMessages() {
      return new ArrayList<>(displayedMessages);
    }

    public List<String> getDisplayedErrors() {
      return new ArrayList<>(displayedErrors);
    }

    public void clear() {
      displayedMessages.clear();
      displayedErrors.clear();
    }

    public int getSuccessMessageCount() {
      return (int) displayedMessages.stream().filter(message -> message.contains("Success")).count();
    }
  }
} 