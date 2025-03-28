import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import controller.command.calendar.CalendarCommandFactory;
import controller.command.event.CommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
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

/**
 * Test class for CalendarCommandFactory. Tests the creation and execution of calendar-related
 * commands.
 */
public class CalendarCommandFactoryTest {

  private CalendarManager calendarManager;
  private MockCalendarView mockView;
  private CalendarCommandFactory factory;

  @Before
  public void setUp() {
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();
    calendarManager = new CalendarManager.Builder().timezoneHandler(timezoneHandler).build();
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
  public void testEditCalendarCommand()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] createArgs = {"calendar", "--name", "NewTestCalendar", "--timezone",
            "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    String[] editArgs = {"calendar", "--name", "NewTestCalendar", "--property", "timezone",
            "America/Los_Angeles"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue("Success message should contain 'Timezone updated'", result.contains(
            "Timezone updated to America/Los_Angeles " + "for calendar 'NewTestCalendar'"));
    mockView.displaySuccess(result);
    assertTrue("Success message should be displayed in view", mockView.hasMessage(result));
  }

  @Test
  public void testEditCalendarTimezone()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] createArgs = {"calendar", "--name", "NewnewTestCalendar", "--timezone",
            "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    String[] editArgs = {"calendar", "--name", "NewnewTestCalendar", "--property", "timezone",
            "America/Los_Angeles"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue("Success message should contain 'Timezone updated'", result.contains(
            "Timezone updated to " + "America/Los_Angeles for calendar 'NewnewTestCalendar'"));
    mockView.displaySuccess(result);
    assertTrue("Success message should be displayed in view", mockView.hasMessage(result));
  }

  @Test
  public void testEditNonExistentCalendar()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "NonExistentCalendar", "--property", "timezone",
            "America/Los_Angeles"};
    String result = factory.getCommand("edit").execute(args);
    assertTrue("Error message should contain 'Calendar not found'",
            result.contains("Calendar not found"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testCreateDuplicateCalendar()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    String result = factory.getCommand("create").execute(createArgs);
    assertTrue("Error message should contain 'Error: Calendar name must be unique'",
            result.contains("Error: Calendar name must be unique"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testEditCalendarWithInvalidProperty()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

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
  public void testEditCalendarWithInvalidTimezone()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    String[] editArgs = {"calendar", "--name", "TestCalendar", "--property", "timezone",
            "Invalid/Timezone"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue("Error message should contain 'Invalid timezone'",
            result.contains("Invalid timezone"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testCreateCalendarWithNullName()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", null, "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithEmptyName()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithMissingTimezone()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "TestCalendar", "--timezone"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testEditEmptyCalendar()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "EmptyCalendar", "--property", "name", "NewName"};
    String result = factory.getCommand("edit").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithMaximumEvents()
          throws ConflictingEventException, InvalidEventException,
          EventNotFoundException, CalendarNotFoundException {
    String[] createArgs = {"calendar", "--name", "MaxEventsCalendar", "--timezone",
            "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    ICalendar calendar = calendarManager.getCalendar("MaxEventsCalendar");
    CommandFactory eventFactory = new CommandFactory(calendar, mockView);

    for (int i = 0; i < 1000; i++) {
      String[] eventArgs = {"single", "Event" + i, "2024-01-01T10:00", "2024-01-01T11:00", null,
              null, "true", "true"};
      eventFactory.getCommand("create").execute(eventArgs);
    }

    String[] extraEventArgs = {"single", "ExtraEvent", "2024-01-01T10:00", "2024-01-01T11:00", null,
            null, "true", "true"};
    String result = eventFactory.getCommand("create").execute(extraEventArgs);
    assertTrue("Error message should contain "
                    + "'Error: Event conflicts with an existing event'",
            result.contains("Error: Event conflicts with an existing event"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testCreateCalendarWithSpecialCharacters()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name", "Test@#$Calendar", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithLongName()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String longName = "A".repeat(1000);
    String[] args = {"calendar", "--name", longName, "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue("Error message should contain 'Calendar name cannot exceed 100 characters'",
            result.contains("Calendar name cannot exceed 100 characters"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testEditCalendarWithMissingProperty() throws Exception {
    String[] createArgs = {"calendar", "--name", "EditMissingProp", "--timezone",
            "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    String[] editArgs = {"calendar", "--name", "EditMissingProp", "--property"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testEditCalendarWithMissingValue() throws Exception {
    String[] createArgs = {"calendar", "--name", "EditMissingValue", "--timezone",
            "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    String[] editArgs = {"calendar", "--name", "EditMissingValue", "--property", "timezone"};
    String result = factory.getCommand("edit").execute(editArgs);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testExecuteWithEmptyArgs()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String result = factory.getCommand("create").execute(new String[]{});
    assertTrue("Should return error on insufficient arguments", result.startsWith("Error:"));
  }

  @Test
  public void testCommandCaseSensitivity() {
    assertFalse("Command lookup should be case-sensitive", factory.hasCommand("Create"));
  }

  @Test
  public void testUseCommandWithMissingName()
          throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    String[] args = {"calendar", "--name"};
    String result = factory.getCommand("use").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateCalendarWithNonStandardTimezone() throws Exception {
    String[] args = {"calendar", "--name", "KolkataCalendar", "--timezone", "Asia/Kolkata"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.contains("Calendar 'KolkataCalendar' created successfully"));
  }

  @Test
  public void testCreateCalendarWithSpecialCharactersInName() throws Exception {
    String[] args = {"calendar", "--name", "Test@#Calendar", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testCreateCalendarWithLongName1() throws Exception {
    String longName = "A".repeat(1000); // Create a string of 1000 A's
    String[] args = {"calendar", "--name", longName, "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue("Error message should contain 'Calendar name cannot exceed 100 characters'",
            result.contains("Calendar name cannot exceed 100 characters"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testCreateCalendarWithInvalidTimezoneFormat() throws Exception {
    String[] args = {"calendar", "--name", "InvalidTimezoneCalendar", "--timezone", "US/NewYork"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.contains("Invalid timezone"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testCreateEventWhenCalendarIsAtMaxLimit() throws Exception {
    String[] createArgs = {"calendar", "--name", "MaxEventCalendar", "--timezone",
            "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    ICalendar calendar = calendarManager.getCalendar("MaxEventCalendar");
    CommandFactory eventFactory = new CommandFactory(calendar, mockView);

    for (int i = 0; i < 1000; i++) {
      String[] eventArgs = {"single", "Event" + i, "2024-01-01T10:00", "2024-01-01T11:00", null,
              null, "true", "true"};
      eventFactory.getCommand("create").execute(eventArgs);
    }

    String[] eventArgs = {"single", "ExtraEvent", "2024-01-01T10:00", "2024-01-01T11:00", null,
            null, "true", "true"};
    String result = eventFactory.getCommand("create").execute(eventArgs);
    assertTrue(result.contains("Error: Event conflicts with an existing event"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testCreateCalendarWithNullOrEmptyValues() throws Exception {
    String[] args = {"calendar", "--name", null, "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));

    args = new String[]{"calendar", "--name", "", "--timezone", "America/New_York"};
    result = factory.getCommand("create").execute(args);
    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testUseCommandWithNonExistentCalendar() throws Exception {
    String[] args = {"calendar", "--name", "NonExistentCalendar"};
    String result = factory.getCommand("use").execute(args);
    assertTrue(result.startsWith("Calendar not found:"));
  }

  @Test
  public void testCreateCalendarWithDuplicateName() throws Exception {
    String[] createArgs1 = {"calendar", "--name", "DuplicateCalendar", "--timezone",
            "America/New_York"};
    String result1 = factory.getCommand("create").execute(createArgs1);

    String[] createArgs2 = {"calendar", "--name", "DuplicateCalendar", "--timezone",
            "America/New_York"};
    String result2 = factory.getCommand("create").execute(createArgs2);

    assertTrue(result2.contains("Error: Calendar name must be unique"));
    mockView.displayError(result2);
    assertTrue("Error should be displayed in view", mockView.hasError(result2));
  }

  @Test
  public void testCreateCalendarWithInvalidTimezone() throws Exception {
    String[] createArgs = {"calendar", "--name", "InvalidTimezoneCalendar", "--timezone",
            "Invalid/Timezone"};
    String result = factory.getCommand("create").execute(createArgs);

    assertTrue(result.contains("Invalid timezone:"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testEditCalendarWithDuplicateName() throws Exception {
    String[] createArgs1 = {"calendar", "--name", "Calendar1", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs1);

    String[] createArgs2 = {"calendar", "--name", "Calendar2", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs2);

    String[] editArgs = {"calendar", "--name", "Calendar2", "--new-name", "Calendar1"};
    String result = factory.getCommand("edit").execute(editArgs);

    assertTrue(result.contains("Error: Insufficient arguments for edit calendar command"));
    mockView.displayError(result);
    assertTrue("Error should be displayed in view", mockView.hasError(result));
  }

  @Test
  public void testSelectCalendarMessage() throws Exception {
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);

    String[] selectArgs = {"calendar", "--name", "TestCalendar"};
    String result = factory.getCommand("use").execute(selectArgs);

    String expectedMessage = "Now using calendar: 'TestCalendar'";
    assertEquals(expectedMessage, result);
  }

  /**
   * Mock implementation of ICalendarView for testing purposes. Tracks displayed messages and errors
   * for verification.
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