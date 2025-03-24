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
    calendarManager = new CalendarManager.Builder().build();
    mockView = new MockCalendarView();
    factory = new CalendarCommandFactory(calendarManager, mockView);
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
    String[] args = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    String result = factory.getCommand("create").execute(args);
    assertTrue(result.contains("Calendar 'TestCalendar' created"));
    assertTrue(calendarManager.getCalendarNames().contains("TestCalendar"));

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
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then edit it
    String[] editArgs = {"calendar", "--name", "TestCalendar", "--property", "name", "NewName"};
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
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then edit timezone
    String[] editArgs = {"calendar", "--name", "TestCalendar", "--property",
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
    String[] createArgs = {"calendar", "--name", "TestCalendar", "--timezone", "America/New_York"};
    factory.getCommand("create").execute(createArgs);
    mockView.clear();

    // Then use it
    String[] useArgs = {"calendar", "--name", "TestCalendar"};
    String result = factory.getCommand("use").execute(useArgs);
    assertTrue(result.contains("Now using calendar"));
    assertEquals("TestCalendar", calendarManager.getActiveCalendar().getName());

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
  }
} 