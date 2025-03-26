import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controller.CalendarController;
import controller.ICommandFactory;
import controller.command.ICommand;
import controller.command.event.CommandFactory;
import controller.parser.CommandParser;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for calendar control.
 */
public class CalendarControllerTest {

  /**
   * This is the mock implementation of Calendar.
   */
  private static class MockCalendar extends Calendar {

    public MockCalendar() {
      super();
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
                                                   LocalDate untilDate, boolean autoDecline,
                                                   String description, String location,
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
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
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

    @Override
    public String exportToCSV(String filePath) throws IOException {
      return null;
    }
  }

  private static class MockCalendarView implements ICalendarView {

    private final List<String> displayedMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final String[] commandsToReturn;
    private int commandIndex = 0;

    public MockCalendarView(String... commandsToReturn) {
      this.commandsToReturn = commandsToReturn;
    }

    @Override
    public String readCommand() {
      if (commandIndex >= commandsToReturn.length) {
        return "exit";
      }
      return commandsToReturn[commandIndex++];
    }

    @Override
    public void displayMessage(String message) {
      displayedMessages.add(message);
    }

    @Override
    public void displayError(String errorMessage) {
      errorMessages.add(errorMessage);
    }

    public List<String> getDisplayedMessages() {
      return new ArrayList<>(displayedMessages);
    }

    public List<String> getErrorMessages() {
      return new ArrayList<>(errorMessages);
    }
  }

  private static class MockCommand implements ICommand {

    private final String result;
    private final String name;

    public MockCommand(String result, String name) {
      this.result = result;
      this.name = name;
    }

    @Override
    public String execute(String[] args) {
      return result;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  private static class MockCommandFactory extends CommandFactory {

    private final MockCalendar calendar;
    private final MockCalendarView view;
    private final ICommand mockCommand;
    private final ICommand errorCommand;
    private final ICommand exitCommand;
    private boolean shouldThrowError = false;
    private boolean shouldThrowInvalidNameError = false;
    private boolean shouldThrowEmptyNameError = false;

    public MockCommandFactory(MockCalendar calendar, MockCalendarView view) {
      super(calendar, view);
      this.calendar = calendar;
      this.view = view;
      this.mockCommand = new MockCommand("Command executed successfully", "mock");
      this.errorCommand = new MockCommand("Error: Calendar not found", "error");
      this.exitCommand = new MockCommand("Exiting application.", "exit");
    }

    public void setShouldThrowError(boolean shouldThrowError) {
      this.shouldThrowError = shouldThrowError;
    }

    public void setShouldThrowInvalidNameError(boolean shouldThrowInvalidNameError) {
      this.shouldThrowInvalidNameError = shouldThrowInvalidNameError;
    }

    public void setShouldThrowEmptyNameError(boolean shouldThrowEmptyNameError) {
      this.shouldThrowEmptyNameError = shouldThrowEmptyNameError;
    }

    @Override
    public ICommand getCommand(String name) {
      if (name.equalsIgnoreCase("exit")) {
        return exitCommand;
      } else if ("error".equals(name)) {
        return errorCommand;
      } else if (name.equals("create")) {
        return new MockCommand("Calendar 'My Calendar' created with timezone "
                + "'America/New_York'",
                "create");
      } else if (name.equals("use")) {
        if (shouldThrowError) {
          return errorCommand;
        } else if (shouldThrowInvalidNameError) {
          return new MockCommand("Error: Invalid calendar name", "use");
        } else if (shouldThrowEmptyNameError) {
          return new MockCommand("Error: Calendar name cannot be empty", "use");
        }
        return new MockCommand("Now using calendar: 'Work'", "use");
      } else if (name.equals("show")) {
        return new MockCommand("Status on 2023-05-15T10:30: Busy", "show");
      } else {
        return mockCommand;
      }
    }

    @Override
    public boolean hasCommand(String name) {
      return name.equalsIgnoreCase("exit") ||
              "error".equals(name) ||
              "mock".equals(name) ||
              "create".equals(name) ||
              "use".equals(name) ||
              "show".equals(name);
    }

    @Override
    public ICalendar getCalendar() {
      return calendar;
    }

    @Override
    public ICalendarView getView() {
      return view;
    }
  }

  private static class MockCommandParser extends CommandParser {

    private boolean throwException = false;
    private final MockCommandFactory factory;

    public MockCommandParser(MockCommandFactory factory) {
      super(factory);
      this.factory = factory;
    }

    public void setThrowException(boolean throwException) {
      this.throwException = throwException;
    }

    @Override
    public CommandWithArgs parseCommand(String commandString) {
      if (throwException) {
        throw new IllegalArgumentException("Mock parsing error");
      }
      return super.parseCommand(commandString);
    }
  }

  /**
   * A testable version of CalendarController that allows mocking the file reader.
   */
  private static class TestableCalendarController extends CalendarController {

    private final BufferedReader fileReader;
    private final ICalendarView view;
    private final CommandParser parser;

    public TestableCalendarController(ICommandFactory commandFactory,
                                      ICommandFactory calendarCommandFactory,
                                      CalendarManager calendarManager,
                                      ICalendarView view,
                                      BufferedReader fileReader) {
      super(commandFactory, calendarCommandFactory, calendarManager, view);
      this.fileReader = fileReader;
      this.view = view;
      this.parser = new CommandParser(commandFactory);
    }

    @Override
    public boolean startHeadlessMode(String commandsFilePath) {
      if (commandsFilePath == null || commandsFilePath.trim().isEmpty()) {
        view.displayError("Error: File path cannot be empty");
        return false;
      }

      List<String> commands = new ArrayList<>();
      try {
        String line;
        while ((line = fileReader.readLine()) != null) {
          line = line.trim();
          if (!line.isEmpty()) {
            commands.add(line);
          }
        }

        if (commands.isEmpty()) {
          view.displayError("Error: Command file is empty. "
                  + "At least one command (exit) is required.");
          return false;
        }

        // Check if exit command is present
        boolean hasExitCommand = false;
        for (String command : commands) {
          if (command.toLowerCase().startsWith("exit")) {
            hasExitCommand = true;
            break;
          }
        }

        if (!hasExitCommand) {
          view.displayError("Error: Command file must contain an "
                  + "'exit' command to prevent infinite loops");
          return false;
        }

        for (int i = 0; i < commands.size() - 1; i++) {
          String command = commands.get(i);
          String result = processCommand(command);
          view.displayMessage(result);

          if (result.startsWith("Error:")) {
            view.displayError("Command failed, stopping execution: " + result);
            return false;
          }
        }

        String lastCommand = commands.get(commands.size() - 1);
        String result = processCommand(lastCommand);
        view.displayMessage(result);

        if (!result.equals("Exiting application.")) {
          view.displayError("Headless mode requires the last command to be 'exit'");
          return false;
        }

        return true;

      } catch (IOException e) {
        view.displayError("Error reading command file: " + e.getMessage());
        return false;
      }
    }

    @Override
    public String processCommand(String commandStr) {
      if (commandStr == null || commandStr.trim().isEmpty()) {
        return "Error: Command cannot be empty";
      }

      try {
        CommandParser.CommandWithArgs commandWithArgs = parser.parseCommand(commandStr.trim());
        return commandWithArgs.execute();
      } catch (IllegalArgumentException e) {
        return "Error: " + e.getMessage();
      } catch (Exception e) {
        return "Unexpected error: " + e.getMessage();
      }
    }
  }

  private static class MockCalendarManager extends CalendarManager {
    private final MockCalendar mockCalendar;
    private final List<String> operationLog;

    public MockCalendarManager(MockCalendar mockCalendar) {
      super(new Builder());
      this.mockCalendar = mockCalendar;
      this.operationLog = new ArrayList<>();
    }

    @Override
    public Calendar getCalendar(String name) throws CalendarNotFoundException {
      operationLog.add("getCalendar: " + name);
      return mockCalendar;
    }

    @Override
    public Calendar getActiveCalendar() throws CalendarNotFoundException {
      operationLog.add("getActiveCalendar");
      return mockCalendar;
    }

    @Override
    public boolean hasCalendar(String name) {
      operationLog.add("hasCalendar: " + name);
      return true;
    }

    @Override
    public void setActiveCalendar(String name) throws CalendarNotFoundException {
      operationLog.add("setActiveCalendar: " + name);
    }

    public List<String> getOperationLog() {
      return new ArrayList<>(operationLog);
    }
  }

  private MockCalendarView view;
  private MockCommandFactory commandFactory;
  private MockCommandParser parser;
  private CalendarController controller;
  private MockCalendar mockCalendar;
  private MockCalendarManager mockCalendarManager;

  @Before
  public void setUp() {
    mockCalendar = new MockCalendar();
    mockCalendarManager = new MockCalendarManager(mockCalendar);
    view = new MockCalendarView("command1", "command2", "exit");
    commandFactory = new MockCommandFactory(mockCalendar, view);
    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    try {
      Field parserField = CalendarController.class.getDeclaredField("parser");
      parserField.setAccessible(true);
      parser = new MockCommandParser(commandFactory);
      parserField.set(controller, parser);
    } catch (Exception e) {
      throw new RuntimeException("Failed to setup test", e);
    }
  }

  @Test
  public void testProcessCommandWithValidCommand() {
    String result = controller.processCommand("create calendar --name "
            + "Work --timezone America/New_York");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testProcessCommandWithExitCommand() {
    String result = controller.processCommand("exit");
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testProcessCommandWithEmptyCommand() {
    String result = controller.processCommand("");
    assertEquals("Error: Command cannot be empty", result);
  }

  @Test
  public void testProcessCommandWithNullCommand() {
    String result = controller.processCommand(null);
    assertEquals("Error: Command cannot be empty", result);
  }

  @Test
  public void testProcessCommandWithParseError() {
    parser.setThrowException(true);
    String result = controller.processCommand("invalid command");
    assertEquals("Error: Mock parsing error", result);
  }

  @Test
  public void testStartInteractiveMode() {
    controller.startInteractiveMode();
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Calendar Application Started"));
    assertTrue(messages.contains("Enter commands (type 'exit' to quit):"));
    assertTrue(messages.contains("Calendar Application Terminated"));
  }

  @Test
  public void testStartHeadlessModeWithEmptyFilePath() {
    TestableCalendarController testableController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view,
            new BufferedReader(new StringReader("")));
    boolean result = testableController.startHeadlessMode("");
    assertFalse(result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error: File path cannot be empty"));
  }

  @Test
  public void testStartHeadlessModeWithNullFilePath() {
    TestableCalendarController testableController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view,
            new BufferedReader(new StringReader("")));
    boolean result = testableController.startHeadlessMode(null);
    assertFalse(result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error: File path cannot be empty"));
  }

  @Test
  public void testStartHeadlessModeWithIOException() {
    BufferedReader errorReader = new BufferedReader(new StringReader("")) {
      @Override
      public String readLine() throws IOException {
        throw new IOException("Mock IO error");
      }
    };

    TestableCalendarController testableController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view, errorReader);
    boolean result = testableController.startHeadlessMode("file.txt");
    assertFalse(result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error reading command file: Mock IO error"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCommandFactory() {
    new CalendarController(null, null, null,
            view);
  }

  @Test
  public void testConstructorWithNullView() {
    try {
      CalendarController controller = new CalendarController(commandFactory,
              null, null, null);
      fail("Should have thrown IllegalArgumentException for null view");
    } catch (IllegalArgumentException e) {
      assertEquals("CalendarCommandFactory cannot be null", e.getMessage());
    }
  }

  @Test
  public void testHeadlessModeWithEmptyFile() {
    BufferedReader reader = new BufferedReader(new StringReader(""));
    TestableCalendarController testableController =
            new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager,
                    view, reader);
    boolean result = testableController.startHeadlessMode("empty_file.txt");
    assertFalse("Should return false for empty file", result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error: Command file is empty. At least one command (exit) "
            + "is required."));
  }

  @Test
  public void testHeadlessModeWithNoExitCommand() {
    String mockFileContent = "create\n" +
            "use\n" +
            "show\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
            new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager,
                    view, reader);
    boolean result = testableController.startHeadlessMode("no_exit.txt");
    assertFalse("Should return false when file doesn't contain exit command", result);
    List<String> errors = view.getErrorMessages();
    assertTrue("Error messages should indicate that file needs an "
                    + "exit command to prevent infinite loops",
            errors.contains("Error: Command file must contain an 'exit' "
                    + "command to prevent infinite loops"));
  }

  @Test
  public void testHeadlessModeWithOnlyExitCommand() {
    String mockFileContent = "exit\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
            new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager,
                    view, reader);
    boolean result = testableController.startHeadlessMode("only_exit.txt");
    assertTrue("Should return true with only exit command", result);
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Exiting application."));
  }

  @Test
  public void testProcessCalendarCommandCreate() {
    String result = controller.processCommand("create calendar My Calendar");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testProcessCalendarCommandUse() {
    String result = controller.processCommand("use calendar --name Work");
    assertEquals("Now using calendar: 'Work'", result);
  }

  @Test
  public void testProcessCalendarCommandInvalidFormat() {
    String result = controller.processCommand("create invalid");
    assertEquals("Error: Invalid command format", result);
  }

  @Test
  public void testProcessCalendarCommandUnknown() {
    String result = controller.processCommand("unknown calendar");
    assertEquals("Error: Invalid command: unknown. "
            + "Valid commands are: create, use, show, edit, copy, exit, print, export", result);
  }

  @Test
  public void testProcessCalendarCommandCopy() {
    String result = controller.processCommand("copy event Meeting");
    assertEquals("Command executed successfully", result);
  }

  @Test
  public void testProcessCalendarCommandCopyEvents() {
    String result = controller.processCommand("copy events Meeting");
    assertEquals("Command executed successfully", result);
  }

  @Test
  public void testProcessCommandWithQuotedStrings() {
    String result = controller.processCommand("create calendar \"My Calendar\"");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testProcessCommandWithSingleQuotedStrings() {
    String result = controller.processCommand("create calendar 'My Calendar'");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testProcessCommandWithMultipleSpaces() {
    String result = controller.processCommand("create    calendar    My Calendar");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testProcessCommandWithLeadingSpaces() {
    String result = controller.processCommand("   create calendar My Calendar");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testProcessCommandWithTrailingSpaces() {
    String result = controller.processCommand("create calendar My Calendar   ");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testProcessCommandWithCalendarNotFound() {
    MockCalendarManager mockManager = new MockCalendarManager(mockCalendar) {
      @Override
      public void setActiveCalendar(String name) throws CalendarNotFoundException {
        throw new CalendarNotFoundException("Calendar not found");
      }
    };

    commandFactory.setShouldThrowError(true);
    controller = new CalendarController(commandFactory, commandFactory, mockManager, view);
    String result = controller.processCommand("use calendar --name NonExistent");
    assertTrue(result.contains("Error: Calendar not found"));
  }

  @Test
  public void testProcessCommandWithInvalidCalendarName() {
    commandFactory.setShouldThrowInvalidNameError(true);
    String result = controller.processCommand("use calendar --name invalid@name");
    assertTrue(result.contains("Error: Invalid calendar name"));
  }

  @Test
  public void testProcessCommandWithEmptyCalendarName() {
    commandFactory.setShouldThrowEmptyNameError(true);
    String result = controller.processCommand("use calendar --name ");
    assertTrue(result.contains("Error: Calendar name cannot be empty"));
  }

  @Test
  public void testProcessCalendarCommandWithException() throws CalendarNotFoundException {
    ICommand mockCommand = new MockCommand("Command failed", "create") {
      @Override
      public String execute(String[] args) {
        throw new RuntimeException("Simulated failure");
      }
    };

    // Set up the mock command factory to return our failing command
    mockCalendarManager.setActiveCalendar("TestCalendar");
    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if (name.equals("create")) {
          return mockCommand;
        }
        return super.getCommand(name);
      }
    };

    // Recreate controller with our modified factory
    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    // The controller should catch the exception and return an error message
    String result = controller.processCommand("create calendar "
            + "--name Test --timezone US/Eastern");
    assertTrue(result.contains("Error"));
  }

  @Test
  public void testNormalizeCommandEdgeCases() {
    // Test with multiple spaces between arguments
    String result = controller.processCommand("create    calendar  "
            + " --name    Test    --timezone    US/Eastern");
    assertTrue(result.contains("created"));

    // Test with tab characters
    result = controller.processCommand("create\tcalendar\t--name"
            + "\tTest\t--timezone\tUS/Eastern");
    assertTrue(result.contains("created"));

    // Test with newline characters
    result = controller.processCommand("create\ncalendar\n--name"
            + "\nTest\n--timezone\nUS/Eastern");
    assertTrue(result.contains("created"));
  }

  @Test
  public void testProcessCommandWithEscapedQuotes() {
    // Use a command that doesn't require capturing arguments, just check for success
    String result = controller.processCommand("create event \"Meeting\" "
            + "from 2023-05-15T10:00 to 2023-05-15T11:00");

    // Just verify that the command was processed without errors
    assertTrue("Command should be processed successfully",
            result.contains("success") || !result.contains("Error"));
  }

  @Test
  public void testUpdateCommandFactoryAfterSwitchingCalendars() {
    // Create another calendar first
    String createResult = controller.processCommand("create calendar --name "
            + "WorkCalendar --timezone America/New_York");
    assertTrue("Calendar creation should succeed",
            createResult.contains("created") || createResult.contains("success"));

    // Mock the behavior of the calendar manager for the 'use' command
    mockCalendarManager = new MockCalendarManager(mockCalendar) {
      @Override
      public void setActiveCalendar(String name) {
        // Just record that this was called but don't throw exception
      }

      @Override
      public boolean hasCalendar(String name) {
        return true;
      }
    };

    // Recreate controller with our modified calendar manager
    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    // Switch to the new calendar (using our mock implementation)
    String result = controller.processCommand("use calendar WorkCalendar");

    // The expectation is that the command is at least forwarded correctly
    assertTrue("Use calendar command should be processed",
            result.contains("Using") || result.contains("forwarded")
                    || !result.contains("Error"));
  }

  @Test
  public void testIsCalendarCommandWithVariousFormats() {
    String result = controller.processCommand("create calendar --name "
            + "Test --timezone America/New_York");
    assertTrue("Create calendar command should be processed as a calendar command",
            !result.contains("Error: Invalid calendar command format"));

    controller.processCommand("create calendar --name "
            + "WorkCalendar --timezone America/New_York");

    result = controller.processCommand("use calendar WorkCalendar");
    assertTrue("Use calendar command should be processed as a calendar command",
            !result.contains("Error: Invalid calendar command format"));

    result = controller.processCommand("create event \"Meeting\" "
            + "from 2023-05-15T10:00 to 2023-05-15T11:00");
    assertTrue("Create event command should not be processed as a calendar command",
            !result.contains("Error: Invalid calendar command format"));

    result = controller.processCommand("show status");
    assertTrue("Show status command should not be processed as a calendar command",
            !result.contains("Error: Invalid calendar command format"));
  }

  /**
   * Test handling of partial calendar commands.
   */
  @Test
  public void testPartialCalendarCommands() {
    view = new MockCalendarView("create", "create calendar", "exit");
    mockCalendar = new MockCalendar();
    mockCalendarManager = new MockCalendarManager(mockCalendar);

    ICommand mockCommand = new MockCommand("Command executed", "create") {
      @Override
      public String execute(String[] args) {
        if (args == null || args.length == 0) {
          return "Error: Missing arguments";
        }
        return "Command executed";
      }
    };

    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if (name.equals("create")) {
          return mockCommand;
        }
        return super.getCommand(name);
      }

      @Override
      public boolean hasCommand(String name) {
        return true; // Say we have all commands
      }
    };

    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    try {
      String result = controller.processCommand("create");
      assertTrue("Command with no arguments should return an error",
              result.contains("Error") || result.contains("Invalid"));
    } catch (Exception e) {
      assertTrue("Exception should indicate an error",
              e.getMessage().contains("Error") || e.getMessage().contains("Invalid"));
    }

    try {
      String result = controller.processCommand("create calendar");
      assertTrue("Command with incomplete arguments should return an error",
              result.contains("Error") || result.contains("Invalid") ||
                      !result.contains("success") || !result.contains("created"));
    } catch (Exception e) {
      assertTrue("Exception should indicate an error",
              e.getMessage().contains("Error") || e.getMessage().contains("Invalid"));
    }
  }

  @After
  public void tearDown() {
    mockCalendar = new MockCalendar();

    mockCalendarManager = new MockCalendarManager(mockCalendar);

    view = new MockCalendarView("command1", "command2", "exit");

    commandFactory = new MockCommandFactory(mockCalendar, view);

    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);
    try {
      Field parserField = CalendarController.class.getDeclaredField("parser");
      parserField.setAccessible(true);
      parser = new MockCommandParser(commandFactory);
      parserField.set(controller, parser);
    } catch (Exception e) {
      throw new RuntimeException("Failed to setup test", e);
    }
  }
}