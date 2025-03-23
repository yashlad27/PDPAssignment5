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
import controller.command.event.CommandFactory;
import controller.command.ICommand;
import controller.parser.CommandParser;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import view.ICalendarView;
import model.calendar.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    // Minimal implementation with no functionality
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
        return "exit";  // Return exit if we've run out of commands
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

    public MockCommandFactory(MockCalendar calendar, MockCalendarView view) {
      super(calendar, view);
      this.calendar = calendar;
      this.view = view;
      this.mockCommand = new MockCommand("Command executed successfully", "mock");
      this.errorCommand = new MockCommand("Error: Test error", "error");
      this.exitCommand = new MockCommand("Exiting application.", "exit");
    }

    @Override
    public ICommand getCommand(String name) {
      if (name.equalsIgnoreCase("exit")) {
        return exitCommand;
      } else if ("error".equals(name)) {
        return errorCommand;
      } else if (name.equals("create")) {
        return new MockCommand("Calendar 'Work' created with timezone 'America/New_York'", "create");
      } else if (name.equals("use")) {
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

      if (commandString.startsWith("error")) {
        return new CommandWithArgs(factory.getCommand("error"), new String[0]);
      } else if (commandString.toLowerCase().startsWith("exit")) {
        return new CommandWithArgs(factory.getCommand("exit"), new String[0]);
      } else if (commandString.equals("runtime-error")) {
        throw new RuntimeException("Mock runtime error");
      } else if (commandString.startsWith("create calendar")) {
        return new CommandWithArgs(factory.getCommand("create"), new String[0]);
      } else if (commandString.startsWith("use calendar")) {
        return new CommandWithArgs(factory.getCommand("use"), new String[0]);
      } else if (commandString.startsWith("show")) {
        return new CommandWithArgs(factory.getCommand("show"), new String[0]);
      } else {
        return new CommandWithArgs(factory.getCommand("mock"), new String[0]);
      }
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
          view.displayError("Error: Command file is empty. At least one command (exit) is required.");
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
    String result = controller.processCommand("valid command");
    assertEquals("Command executed successfully", result);
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
  public void testProcessCommandWithRuntimeError() {
    String result = controller.processCommand("runtime-error");
    assertTrue(result.startsWith("Unexpected error: Mock runtime error"));
  }

  @Test
  public void testStartInteractiveMode() {
    controller.startInteractiveMode();
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Calendar Application Started"));
    assertTrue(messages.contains("Enter commands (type 'exit' to quit):"));
    assertTrue(messages.contains("Command executed successfully"));
    assertTrue(messages.contains("Command executed successfully"));
    assertTrue(messages.contains("Calendar Application Terminated"));
  }

//  @Test
//  public void testStartHeadlessModeWithValidCommands() {
//    String mockFileContent = "create calendar --name Work --timezone America/New_York\n" +
//        "use calendar --name Work\n" +
//        "create single Meeting 2023-05-15T10:00 2023-05-15T11:00\n" +
//        "show 2023-05-15T10:30\n" +
//        "exit\n";
//    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
//    TestableCalendarController testableController =
//        new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager, view, reader);
//    boolean result = testableController.startHeadlessMode("valid_file.txt");
//    assertTrue("Headless mode should complete successfully", result);
//    List<String> messages = view.getDisplayedMessages();
//    assertTrue("Should contain calendar creation success message", messages.contains("Calendar 'Work' created with timezone 'America/New_York'"));
//    assertTrue("Should contain calendar selection message", messages.contains("Now using calendar: 'Work'"));
//    assertTrue("Should contain event creation success message", messages.contains("Event 'Meeting' created successfully"));
//    assertTrue("Should contain status message", messages.contains("Status on 2023-05-15T10:30: Busy"));
//  }

  @Test
  public void testStartHeadlessModeWithEmptyFilePath() {
    TestableCalendarController testableController = new TestableCalendarController(
        commandFactory, commandFactory, mockCalendarManager, view, new BufferedReader(new StringReader("")));
    boolean result = testableController.startHeadlessMode("");
    assertFalse(result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error: File path cannot be empty"));
  }

  @Test
  public void testStartHeadlessModeWithNullFilePath() {
    TestableCalendarController testableController = new TestableCalendarController(
        commandFactory, commandFactory, mockCalendarManager, view, new BufferedReader(new StringReader("")));
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
    new CalendarController(null, null, null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullView() {
    new CalendarController(commandFactory, null, null, null);
  }

  @Test
  public void testHeadlessModeWithEmptyFile() {
    BufferedReader reader = new BufferedReader(new StringReader(""));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager, view, reader);
    boolean result = testableController.startHeadlessMode("empty_file.txt");
    assertFalse("Should return false for empty file", result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error: Command file is empty. At least one command (exit) is required."));
  }

//  @Test
//  public void testHeadlessModeWithNoExitCommand() {
//    String mockFileContent = "command1\ncommand2\n";
//    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
//    TestableCalendarController testableController =
//        new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager, view, reader);
//    boolean result = testableController.startHeadlessMode("no_exit.txt");
//    assertFalse("Should return false when exit command is missing", result);
//    List<String> errors = view.getErrorMessages();
//    assertTrue(errors.contains("Headless mode requires the last command to be 'exit'"));
//  }

  @Test
  public void testHeadlessModeWithOnlyExitCommand() {
    String mockFileContent = "exit\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager, view, reader);
    boolean result = testableController.startHeadlessMode("only_exit.txt");
    assertTrue("Should return true with only exit command", result);
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Exiting application."));
  }

//   @Test
//   public void testHeadlessModeWithEmptyLines() {
//     String mockFileContent = "\ncommand1\n\ncommand2\n\nexit\n";
//     BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
//     TestableCalendarController testableController =
//         new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager, view, reader);
//     boolean result = testableController.startHeadlessMode("valid_file.txt");
//     assertTrue(result);
//     List<String> messages = view.getDisplayedMessages();
//     assertTrue(messages.contains("Command executed successfully"));
//   }

  // @Test
  // public void testStartHeadlessModeWithValidCommands() {
  //   String mockFileContent = "command1\ncommand2\nexit\n";
  //   BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
  //   TestableCalendarController testableController =
  //       new TestableCalendarController(commandFactory, commandFactory, mockCalendarManager, view, reader);
  //   boolean result = testableController.startHeadlessMode("valid_file.txt");
  //   assertTrue(result);
  //   List<String> messages = view.getDisplayedMessages();
  //   assertTrue(messages.contains("Command executed successfully"));
  // }
}