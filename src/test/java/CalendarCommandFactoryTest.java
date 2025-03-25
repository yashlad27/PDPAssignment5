import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import controller.CalendarController;
import controller.command.calendar.CalendarCommandFactory;
import controller.command.event.CommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.core.timezone.TimeZoneHandler;
import model.core.validation.CalendarNameValidator;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import view.ICalendarView;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CalendarCommandFactoryTest {
  private CalendarManager calendarManager;
  private MockCalendarView mockView;
  private CalendarCommandFactory factory;
  private CalendarController mockController;

  @Before
  public void setUp() {
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();
    calendarManager = new CalendarManager.Builder()
            .timezoneHandler(timezoneHandler)
            .build();
    mockView = new MockCalendarView();
    
    // Create a test calendar and add it to the manager
    try {
        calendarManager.createCalendar("TestCalendar", "America/New_York");
        calendarManager.setActiveCalendar("TestCalendar");
        ICalendar calendar = calendarManager.getActiveCalendar();
        
        // Use the valid calendar when creating command factory
        CommandFactory commandFactory = new CommandFactory(calendar, mockView);
        mockController = new CalendarController(commandFactory, commandFactory, calendarManager, mockView);
        factory = new CalendarCommandFactory(calendarManager, mockView, mockController);
    } catch (Exception e) {
        fail("Failed to create test calendar: " + e.getMessage());
    }
    
    CalendarNameValidator.clear(); // Clear the validator before each test
  }

  @After
  public void tearDown() {
    CalendarNameValidator.clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendarManager() {
    new CalendarCommandFactory(null, mockView, mockController);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullView() {
    new CalendarCommandFactory(calendarManager, null, mockController);
  }

  @Test
  public void testConstructorWithNullController() {
    CalendarCommandFactory factoryWithNullController = new CalendarCommandFactory(calendarManager, mockView, null);
    assertNotNull("Should create command factory with null controller", factoryWithNullController);
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
  public void testEditCalendarCommand() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "NewTestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then edit it
    String[] editArgs = {"calendar", "--name", "NewTestCalendar", "--property", "timezone", "America/Los_Angeles"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue("Success message should contain 'Timezone updated'",
            result.contains("Timezone updated to America/Los_Angeles for calendar 'NewTestCalendar'"));
    mockView.displaySuccess(result);
    assertTrue("Success message should be displayed in view", mockView.hasMessage(result));
  }

  @Test
  public void testEditCalendarTimezone() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "NewnewTestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then edit timezone
    String[] editArgs = {"calendar", "--name", "NewnewTestCalendar", "--property", "timezone", "America/Los_Angeles"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue("Success message should contain 'Timezone updated'",
            result.contains("Timezone updated to America/Los_Angeles for calendar 'NewnewTestCalendar'"));
    mockView.displaySuccess(result);
    assertTrue("Success message should be displayed in view", mockView.hasMessage(result));
  }

  @Test
  public void testEditNonExistentCalendar() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "NonExistentCalendar", "--property", "timezone", "America/Los_Angeles"};
    String result = factory.getCommand("edit").execute(args);
    assertTrue("Error message should contain 'Calendar not found'",
            result.contains("Calendar not found"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
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
    assertTrue("Error message should contain 'Error: Calendar name must be unique'",
            result.contains("Error: Calendar name must be unique"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
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
    assertTrue("Error message should contain 'Invalid property'",
            result.contains("Error: Invalid property 'invalid' for calendar edit"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
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
  public void testEditCalendarWithInvalidTimezone() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Try to edit with invalid timezone
    String[] editArgs = {"calendar", "--name", "TestCalendar", "--property", "timezone", "Invalid/Timezone"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue("Error message should contain 'Invalid timezone'",
            result.contains("Invalid timezone"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
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

  @Test
  public void testEditEmptyCalendar() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "EmptyCalendar", "--property", "name", "NewName"};
    String result = factory.getCommand("edit").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithMaximumEvents() throws ConflictingEventException, InvalidEventException, EventNotFoundException, CalendarNotFoundException {
    // First create a calendar
    String[] createArgs = {"calendar", "--name", "MaxEventsCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Get the created calendar
    ICalendar calendar = calendarManager.getCalendar("MaxEventsCalendar");
    // Create a new CommandFactory for event operations
    CommandFactory eventFactory = new CommandFactory(calendar, mockView);

    // Add maximum number of events (assuming a reasonable limit)
    for (int i = 0; i < 1000; i++) {
      String[] eventArgs = {"single", "Event" + i, "2024-01-01T10:00", "2024-01-01T11:00", null, null, "true", "true"};
      eventFactory.getCommand("create").execute(eventArgs);
    }

    // Try to add one more event
    String[] extraEventArgs = {"single", "ExtraEvent", "2024-01-01T10:00", "2024-01-01T11:00", null, null, "true", "true"};
    String result = eventFactory.getCommand("create").execute(extraEventArgs);
    assertTrue("Error message should contain 'Error: Event conflicts with an existing event'",
            result.contains("Error: Event conflicts with an existing event"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
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
    assertTrue("Error message should contain 'Calendar name cannot exceed 100 characters'",
            result.contains("Calendar name cannot exceed 100 characters"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
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
      if (message != null) {
        displayedMessages.add(message);
      }
    }

    @Override
    public void displayError(String error) {
      if (error != null) {
        displayedErrors.add(error);
      }
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

    public boolean hasMessage(String message) {
      return displayedMessages.stream().anyMatch(m -> m.contains(message));
    }

    public boolean hasError(String error) {
      return displayedErrors.stream().anyMatch(e -> e.contains(error));
    }

    public void displaySuccess(String message) {
      if (message != null) {
        displayedMessages.add(message);
      }
    }
  }
} 