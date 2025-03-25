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
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidTimezoneException;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
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
      if (name == null || name.trim().isEmpty()) {
        return null;
      }

      if (name.equalsIgnoreCase("exit")) {
        return exitCommand;
      } else if ("error".equals(name)) {
        return errorCommand;
      } else if (name.equals("create")) {
        return new ICommand() {
          @Override
          public String execute(String[] args) {
            if (args == null || args.length < 2 || !args[0].equals("calendar")) {
              return "Error: Invalid create calendar command format";
            }
            return "Calendar 'My Calendar' created with timezone 'America/New_York'";
          }

          @Override
          public String getName() {
            return "create";
          }
        };
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
      return name != null && !name.trim().isEmpty() && (
              name.equalsIgnoreCase("exit") ||
                      "error".equals(name) ||
                      "mock".equals(name) ||
                      "create".equals(name) ||
                      "use".equals(name) ||
                      "show".equals(name)
      );
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

        // Process all commands except the last one
        for (int i = 0; i < commands.size() - 1; i++) {
          String command = commands.get(i);
          String result = processCommand(command);
          view.displayMessage(result);

          if (result.startsWith("Error:")) {
            view.displayError("Command failed, stopping execution: " + result);
            return false;
          }
        }

        // Process the last command (should be exit)
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

  /**
   * Mock implementation of CalendarManager for testing.
   */
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
    String result = controller.processCommand("create calendar --name Work --timezone America/New_York");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'", result);
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

  @Test
  public void testControllerWithNullCommandFactory() {
    assertThrows(IllegalArgumentException.class, () -> {
      new CalendarController(null, null, null, view);
    });
  }

  @Test
  public void testControllerWithNullCalendarManager() {
    assertThrows(IllegalArgumentException.class, () ->
            new CalendarController(commandFactory, commandFactory, null, view));
  }

  @Test
  public void testControllerWithNullView() {
    assertThrows(IllegalArgumentException.class, () ->
            new CalendarController(commandFactory, commandFactory, mockCalendarManager, null));
  }

  @Test
  public void testControllerWithNullParser() {
    assertThrows(IllegalArgumentException.class, () -> {
      new CalendarController(null, null, null, view);
    });
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
    assertTrue("Error messages should indicate that file needs an exit command to prevent infinite loops",
            errors.contains("Error: Command file must contain an 'exit' command to prevent infinite loops"));
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
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'", result);
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
    assertEquals("Error: Invalid command: unknown. Valid commands are: create, use, show, edit, copy, exit, print, export", result);
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
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'", result);
  }

  @Test
  public void testProcessCommandWithSingleQuotedStrings() {
    String result = controller.processCommand("create calendar 'My Calendar'");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'", result);
  }

  @Test
  public void testProcessCommandWithMultipleSpaces() {
    String result = controller.processCommand("create    calendar    My Calendar");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'", result);
  }

  @Test
  public void testProcessCommandWithLeadingSpaces() {
    String result = controller.processCommand("   create calendar My Calendar");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'", result);
  }

  @Test
  public void testProcessCommandWithTrailingSpaces() {
    String result = controller.processCommand("create calendar My Calendar   ");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'", result);
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
  public void testHeadlessModeWithValidCommands() {
    // Setup mock file reader with valid commands
    String commands = "create calendar --name Work --timezone America/New_York\n" +
            "create calendar --name Personal --timezone Europe/Paris\n" +
            "use calendar --name Work\n" +
            "exit";

    BufferedReader reader = new BufferedReader(new StringReader(commands));
    TestableCalendarController testController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view, reader);

    boolean result = testController.startHeadlessMode("dummy-path.txt");

    assertTrue("Headless mode should succeed with valid commands", result);
    assertEquals("Should have 4 display messages (one for each command including exit)",
            4, view.getDisplayedMessages().size());
    assertEquals(0, view.getErrorMessages().size());
  }

  @Test
  public void testHeadlessModeWithErrorInCommands() {
    // Setup a command factory that returns an error for the second command
    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if (name.equals("use")) {
          return new MockCommand("Error: Calendar not found", "use");
        }
        return super.getCommand(name);
      }
    };

    // Setup mock file reader with commands where the second one will fail
    String commands = "create calendar --name Work --timezone America/New_York\n" +
            "use calendar --name NonExistentCalendar\n" +
            "create calendar --name Personal --timezone Europe/Paris\n" +
            "exit";

    BufferedReader reader = new BufferedReader(new StringReader(commands));
    TestableCalendarController testController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view, reader);

    boolean result = testController.startHeadlessMode("dummy-path.txt");

    assertFalse("Headless mode should fail when a command returns an error", result);
    assertEquals("Should have 2 display messages (including successful commands and error message)",
            2, view.getDisplayedMessages().size());
    assertEquals("Should have 1 error message", 1, view.getErrorMessages().size());
    assertTrue(view.getErrorMessages().get(0).contains("Error: Calendar not found"));
  }

  @Test
  public void testHeadlessModeWithExceptionThrown() {
    // Setup a calendar manager that throws an exception
    MockCalendarManager exceptionManager = new MockCalendarManager(mockCalendar) {
      @Override
      public void setActiveCalendar(String name) throws CalendarNotFoundException {
        throw new CalendarNotFoundException("Test exception");
      }
    };

    // Setup command factory to use the exception-throwing manager
    ICommandFactory exceptionFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if (name.equals("use")) {
          return new MockCommand("Error: Test exception", "use") {
            @Override
            public String execute(String[] args) {
              try {
                exceptionManager.setActiveCalendar(args[2]);
                return "This should not be returned";
              } catch (CalendarNotFoundException e) {
                return "Error: " + e.getMessage();
              }
            }
          };
        }
        return super.getCommand(name);
      }
    };

    String commands = "create calendar --name Work --timezone America/New_York\n" +
            "use calendar --name Work\n" +
            "exit";

    BufferedReader reader = new BufferedReader(new StringReader(commands));
    TestableCalendarController testController = new TestableCalendarController(
            exceptionFactory, exceptionFactory, exceptionManager, view, reader);

    boolean result = testController.startHeadlessMode("dummy-path.txt");

    assertFalse("Headless mode should fail when an exception is thrown", result);
    assertEquals(2, view.getDisplayedMessages().size());
    assertEquals(1, view.getErrorMessages().size());
  }

  @Test
  public void testHeadlessModeWithMalformedCommands() {
    String commands = "create calendar --name Work --timezone America/New_York\n" +
            "invalid command format\n" +
            "exit";

    // Setup a parser that returns an error for invalid commands
    parser = new MockCommandParser(commandFactory) {
      @Override
      public CommandWithArgs parseCommand(String commandString) {
        if (commandString.equals("invalid command format")) {
          return new CommandWithArgs(
                  new MockCommand("Error: Invalid command format", "error"),
                  new String[]{}
          );
        }
        return super.parseCommand(commandString);
      }
    };

    try {
      // Inject our mock parser into the controller
      Field parserField = CalendarController.class.getDeclaredField("parser");
      parserField.setAccessible(true);
      parserField.set(controller, parser);
    } catch (Exception e) {
      fail("Failed to inject mock parser: " + e.getMessage());
    }

    BufferedReader reader = new BufferedReader(new StringReader(commands));
    TestableCalendarController testController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view, reader);

    boolean result = testController.startHeadlessMode("dummy-path.txt");

    assertFalse("Headless mode should fail with malformed commands", result);
    assertTrue(view.getErrorMessages().size() > 0);
  }

  @Test
  public void testHeadlessModeWithComplexCommands() {
    String commands = "create calendar --name \"Work Calendar\" --timezone America/New_York\n" +
            "create calendar --name 'Personal Calendar' --timezone Europe/Paris\n" +
            "use calendar --name \"Work Calendar\"\n" +
            "exit";

    BufferedReader reader = new BufferedReader(new StringReader(commands));
    TestableCalendarController testController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view, reader);

    boolean result = testController.startHeadlessMode("dummy-path.txt");

    assertTrue("Headless mode should handle complex commands with quotes", result);
    assertEquals(4, view.getDisplayedMessages().size());
  }

  @Test
  public void testHeadlessModeWithMixedCommandTypes() {
    view = new MockCalendarView();

    BufferedReader reader = new BufferedReader(new StringReader(
            "create calendar --name Work --timezone America/New_York\n" +
                    "use calendar --name Work\n" +
                    "exit"));

    TestableCalendarController testController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view, reader);

    boolean result = testController.startHeadlessMode("dummy-path.txt");

    assertTrue("Headless mode should execute successfully", result);
  }

  @Test
  public void testHeadlessModeWithCommentsAndEmptyLines() {
    String commands = "# This is a comment\n" +
            "\n" +
            "create calendar --name Work --timezone America/New_York\n" +
            "# Another comment\n" +
            "\n" +
            "use calendar --name Work\n" +
            "exit";

    final BufferedReader finalReader = new BufferedReader(new StringReader(commands));
    TestableCalendarController testController = new TestableCalendarController(
            commandFactory, commandFactory, mockCalendarManager, view, finalReader) {

      @Override
      public boolean startHeadlessMode(String commandsFilePath) {
        try {
          List<String> filteredCommands = new ArrayList<>();
          String line;
          while ((line = finalReader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
              filteredCommands.add(line);
            }
          }

          if (filteredCommands.isEmpty()) {
            view.displayError("Error: Command file is empty after filtering comments");
            return false;
          }

          String lastCommand = filteredCommands.get(filteredCommands.size() - 1);
          if (!lastCommand.equalsIgnoreCase("exit")) {
            view.displayError("Headless mode requires the last command to be 'exit'");
            return false;
          }

          for (String command : filteredCommands) {
            String result = processCommand(command);
            if (result.startsWith("Error")) {
              view.displayError(result);
              return false;
            }
            if (!command.equalsIgnoreCase("exit")) {
              view.displayMessage(result);
            }
          }

          return true;

        } catch (IOException e) {
          view.displayError("Error reading command file: " + e.getMessage());
          return false;
        }
      }
    };

    boolean result = testController.startHeadlessMode("dummy-path.txt");

    assertTrue("Headless mode should handle comments and empty lines", result);
    assertEquals(2, view.getDisplayedMessages().size());
  }

  @Test
  public void testProcessCommand_WithCalendarCommands() {
    // Test create calendar command
    String result = controller.processCommand("create calendar --name TestCal --timezone America/New_York");
    assertTrue("Should confirm calendar creation was successful",
            result.contains("Calendar 'My Calendar' created with timezone 'America/New_York'"));

    // Test use calendar command
    result = controller.processCommand("use calendar --name TestCal");
    assertTrue("Should confirm calendar is now in use",
            result.contains("Now using calendar: 'Work'"));

    // Test incomplete create calendar command
    result = controller.processCommand("create calendar");
    assertTrue("Should return error for incomplete create calendar command",
            result.contains("Error: Invalid create calendar command format"));
  }

  @Test
  public void testProcessCommandWithMultipleWhitespaces() {
    String result = controller.processCommand("   create    calendar    --name   "
            + "My Calendar   --timezone   America/New_York   ");
    assertEquals("Calendar 'My Calendar' created with timezone 'America/New_York'",
            result);
  }

  @Test
  public void testUpdateCommandFactory() {
    controller.processCommand("create calendar --name Calendar1 --timezone "
            + "America/New_York");
    controller.processCommand("create calendar --name Calendar2 --timezone "
            + "Europe/London");

    controller.processCommand("use calendar --name Calendar1");

    controller.processCommand("use calendar --name Calendar2");
  }

//  @Test
//  public void testIOWithInvalidFilePath() {
//    assertThrows(IOException.class, () ->
//            FileIO.readFile("/invalid/path/file.txt"));
//  }
//
//  @Test
//  public void testIOWithNullContent() {
//    assertThrows(IllegalArgumentException.class, () ->
//            FileIO.writeFile("test.txt", null));
//  }
//
//  @Test
//  public void testIOWithEmptyContent() {
//    assertThrows(IllegalArgumentException.class, () ->
//            FileIO.writeFile("test.txt", ""));
//  }

  @Test
  public void testCalendarWithInvalidTimezone() {
    CalendarManager manager = new CalendarManager.Builder().build();
    assertThrows(InvalidTimezoneException.class, () ->
            manager.createCalendar("Test", "Invalid/Timezone"));
  }

  @Test
  public void testCalendarWithNullName() {
    CalendarManager manager = new CalendarManager.Builder().build();
    assertThrows(IllegalArgumentException.class, () ->
            manager.createCalendar(null, "America/New_York"));
  }

  @Test
  public void testCalendarWithEmptyName() {
    CalendarManager manager = new CalendarManager.Builder().build();
    assertThrows(IllegalArgumentException.class, () ->
            manager.createCalendar("", "America/New_York"));
  }

  @Test
  public void testCalendarEventOverlap() throws ConflictingEventException {
    Calendar calendar = new Calendar();
    calendar.setName("Test");
    calendar.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.parse("2024-03-20T10:00:00");
    LocalDateTime end = LocalDateTime.parse("2024-03-20T11:00:00");

    Event event1 = new Event("Event1", start, end, "Description", "Location", true);
    Event event2 = new Event("Event2", start, end, "Description", "Location", true);

    calendar.addEvent(event1, true);
    assertThrows(ConflictingEventException.class, () ->
            calendar.addEvent(event2, true));
  }

  @Test
  public void testCommandWithInvalidArguments() {
    MockCommand mockCommand = new MockCommand("Error", "test") {
      @Override
      public String execute(String[] args) {
        throw new IllegalArgumentException("Invalid arguments");
      }
    };
    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if ("test".equals(name)) {
          return mockCommand;
        }
        return super.getCommand(name);
      }

      @Override
      public boolean hasCommand(String name) {
        return "test".equals(name) || super.hasCommand(name);
      }
    };
    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    String result = controller.processCommand("test");
    assertEquals("Error: Invalid command: test. Valid commands are: create, use, show, edit, copy, exit, print, export", result);
  }

  @Test
  public void testCommandWithEmptyArguments() {
    MockCommand mockCommand = new MockCommand("Error", "test") {
      @Override
      public String execute(String[] args) {
        throw new IllegalArgumentException("Empty arguments");
      }
    };
    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if ("test".equals(name)) {
          return mockCommand;
        }
        return super.getCommand(name);
      }

      @Override
      public boolean hasCommand(String name) {
        return "test".equals(name) || super.hasCommand(name);
      }
    };
    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    String result = controller.processCommand("test");
    assertEquals("Error: Invalid command: test. Valid commands are: create, use, show, edit, copy, exit, print, export", result);
  }

  @Test
  public void testControllerWithNullCommandResult() {
    MockCommand mockCommand = new MockCommand(null, "test");
    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if ("test".equals(name)) {
          return mockCommand;
        }
        return super.getCommand(name);
      }

      @Override
      public boolean hasCommand(String name) {
        return "test".equals(name) || super.hasCommand(name);
      }
    };
    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    String result = controller.processCommand("test");
    assertEquals("Error: Invalid command: test. Valid commands are: create, use, show, edit, copy, exit, print, export", result);
  }

  @Test
  public void testControllerWithInvalidCommandFactory() {
    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        throw new RuntimeException("Invalid command factory");
      }

      @Override
      public boolean hasCommand(String name) {
        return true;
      }
    };

    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);
    String result = controller.processCommand("test");
    assertEquals("Error: Invalid command: test. Valid commands are: create, use, show, edit, copy, exit, print, export", result);
  }

  @Test
  public void testControllerWithExceptionInCommandExecution() {
    MockCommand mockCommand = new MockCommand("Error", "test") {
      @Override
      public String execute(String[] args) {
        throw new RuntimeException("Command execution failed");
      }
    };
    commandFactory = new MockCommandFactory(mockCalendar, view) {
      @Override
      public ICommand getCommand(String name) {
        if ("test".equals(name)) {
          return mockCommand;
        }
        return super.getCommand(name);
      }

      @Override
      public boolean hasCommand(String name) {
        return "test".equals(name) || super.hasCommand(name);
      }
    };

    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);
    String result = controller.processCommand("test");
    assertEquals("Error: Invalid command: test. Valid commands are: create, use, show, edit, copy, exit, print, export", result);
  }

  @Test
  public void testIOWithFilePermissionError() {
    String readOnlyFile = "readonly.txt";
    try {
      // Create a read-only file
      java.io.File file = new java.io.File(readOnlyFile);
      file.createNewFile();
      file.setReadOnly();

      assertThrows(IOException.class, () ->
              java.nio.file.Files.write(java.nio.file.Paths.get(readOnlyFile), "test content".getBytes()));
    } catch (IOException e) {
      fail("Failed to setup test: " + e.getMessage());
    } finally {
      // Clean up
      new java.io.File(readOnlyFile).delete();
    }
  }

  @Test
  public void testIOWithDirectoryAsFile() {
    String dirPath = "testdir";
    try {
      // Create a directory
      new java.io.File(dirPath).mkdir();

      assertThrows(IOException.class, () ->
              java.nio.file.Files.write(java.nio.file.Paths.get(dirPath), "test content".getBytes()));
    } finally {
      // Clean up
      new java.io.File(dirPath).delete();
    }
  }

  @Test
  public void testCalendarWithDuplicateEventName() throws ConflictingEventException {
    Calendar calendar = new Calendar();
    calendar.setName("Test");
    calendar.setTimezone("America/New_York");

    LocalDateTime start1 = LocalDateTime.parse("2024-03-20T10:00:00");
    LocalDateTime end1 = LocalDateTime.parse("2024-03-20T11:00:00");
    LocalDateTime start2 = LocalDateTime.parse("2024-03-20T10:30:00");
    LocalDateTime end2 = LocalDateTime.parse("2024-03-20T11:30:00");

    Event event1 = new Event("Meeting", start1, end1, "Description", "Location", true);
    Event event2 = new Event("Meeting", start2, end2, "Description", "Location", true);

    calendar.addEvent(event1, true);
    assertThrows(ConflictingEventException.class, () ->
            calendar.addEvent(event2, true));
  }

  @Test
  public void testCalendarWithInvalidExportPath() {
    Calendar calendar = new Calendar();
    calendar.setName("Test");
    calendar.setTimezone("America/New_York");

    assertThrows(IOException.class, () ->
            calendar.exportToCSV("/invalid/path/events.csv"));
  }

  @Test
  public void testCalendarWithInvalidEventSearch() {
    Calendar calendar = new Calendar();
    calendar.setName("Test");
    calendar.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.parse("2024-03-20T10:00:00");

    assertNull(calendar.findEvent("NonExistent", start));
  }

  @Test
  public void testCalendarWithInvalidEventEdit() {
    Calendar calendar = new Calendar();
    calendar.setName("Test");
    calendar.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.parse("2024-03-20T10:00:00");

    assertFalse(calendar.editSingleEvent("NonExistent", start, "subject", "New Subject"));
  }

  @Test
  public void testEventWithNullEndTime() {
    LocalDateTime start = LocalDateTime.parse("2024-03-20T10:00:00");
    Event event = new Event("Test", start, null, "Description", "Location", true);
    assertNotNull(event.getEndDateTime());
    assertEquals(LocalDateTime.parse("2024-03-20T23:59:59"), event.getEndDateTime());
  }

  @After
  public void tearDown() {
    // Reset all mocks
    mockCalendar = new MockCalendar();
    mockCalendarManager = new MockCalendarManager(mockCalendar);
    view = new MockCalendarView("command1", "command2", "exit");
    commandFactory = new MockCommandFactory(mockCalendar, view);

    // Reset command factory flags
    commandFactory.setShouldThrowError(false);
    commandFactory.setShouldThrowInvalidNameError(false);
    commandFactory.setShouldThrowEmptyNameError(false);

    // Create new controller with reset mocks
    controller = new CalendarController(commandFactory, commandFactory, mockCalendarManager, view);

    // Reset parser
    try {
      Field parserField = CalendarController.class.getDeclaredField("parser");
      parserField.setAccessible(true);
      parser = new MockCommandParser(commandFactory);
      parser.setThrowException(false);
      parserField.set(controller, parser);
    } catch (Exception e) {
      throw new RuntimeException("Failed to setup test", e);
    }
  }
}