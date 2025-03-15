import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import controller.command.CommandFactory;
import controller.command.ICommand;
import controller.parser.CommandParser;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test file for Command Parser.
 */
public class CommandParserTest {

  /**
   * Mock implementations of calendar.
   */
  private static class MockCalendar implements ICalendar {

    /**
     * Minimal implementation with no functionality.
     */

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
        String weekdays, LocalDate untilDate, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
        int occurrences, boolean autoDecline, String description, String location,
        boolean isPublic) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
        LocalDate untilDate, boolean autoDecline, String description, String location,
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
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
        String newValue) {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
        String newValue) {
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

    @Override
    public String readCommand() {
      return null;
    }

    @Override
    public void displayMessage(String message) {
      int i = 0;
    }

    @Override
    public void displayError(String errorMessage) {
      int i = 0;
    }
  }

  private static class MockCreateCommand implements ICommand {

    @Override
    public String execute(String[] args) {
      return "MockCreateCommand.execute called";
    }

    @Override
    public String getName() {
      return "create";
    }
  }

  private static class MockPrintCommand implements ICommand {

    @Override
    public String execute(String[] args) {
      return "MockPrintCommand.execute called";
    }

    @Override
    public String getName() {
      return "print";
    }
  }

  private static class MockShowCommand implements ICommand {

    @Override
    public String execute(String[] args) {
      return "MockShowCommand.execute called";
    }

    @Override
    public String getName() {
      return "show";
    }
  }

  private static class MockExportCommand implements ICommand {

    @Override
    public String execute(String[] args) {
      return "MockExportCommand.execute called";
    }

    @Override
    public String getName() {
      return "export";
    }
  }

  private static class MockEditCommand implements ICommand {

    @Override
    public String execute(String[] args) {
      return "MockEditCommand.execute called";
    }

    @Override
    public String getName() {
      return "edit";
    }
  }

  private static class MockExitCommand implements ICommand {

    @Override
    public String execute(String[] args) {
      return "MockExitCommand.execute called";
    }

    @Override
    public String getName() {
      return "exit";
    }
  }

  private static class MockCommandFactory extends CommandFactory {

    private final ICommand createCommand;
    private final ICommand printCommand;
    private final ICommand showCommand;
    private final ICommand exportCommand;
    private final ICommand editCommand;
    private final ICommand exitCommand;

    public MockCommandFactory(ICalendar calendar, ICalendarView view) {
      super(calendar, view);
      createCommand = new MockCreateCommand();
      printCommand = new MockPrintCommand();
      showCommand = new MockShowCommand();
      exportCommand = new MockExportCommand();
      editCommand = new MockEditCommand();
      exitCommand = new MockExitCommand();
    }

    @Override
    public ICommand getCommand(String name) {
      if (name == null) {
        return null;
      }

      switch (name) {
        case "create":
          return createCommand;
        case "print":
          return printCommand;
        case "show":
          return showCommand;
        case "export":
          return exportCommand;
        case "edit":
          return editCommand;
        case "exit":
          return exitCommand;
        default:
          return null;
      }
    }
  }

  private CommandParser parser;

  @Before
  public void setUp() {
    ICalendar calendar = new MockCalendar();
    ICalendarView view = new MockCalendarView();
    MockCommandFactory commandFactory = new MockCommandFactory(calendar, view);
    parser = new CommandParser(commandFactory);
  }

  @Test
  public void testParseExitCommand() {
    String commandString = "exit";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockExitCommand);
    assertEquals(0, result.getArgs().length);
    assertEquals("MockExitCommand.execute called", result.execute());
  }

  @Test
  public void testParseCreateEventCommand() {
    String commandString = "create event \"Team Meeting\" from 2023-04-10T10:00 "
        + "to 2023-04-10T11:00";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockCreateCommand);
    String[] args = result.getArgs();
    assertEquals(8, args.length);
    assertEquals("single", args[0]);
    assertEquals("Team Meeting", args[1]);
    assertEquals("2023-04-10T10:00", args[2]);
    assertEquals("2023-04-10T11:00", args[3]);
    assertNull(args[4]);
    assertNull(args[5]);
    assertEquals("true", args[6]);
    assertEquals("false", args[7]);
  }

  @Test
  public void testParseCreateEventWithAutoDecline() {
    String commandString = "create event --autoDecline \"Project Review\" from 2023-04-10T11:30 "
        + "to 2023-04-10T12:30";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockCreateCommand);
    String[] args = result.getArgs();
    assertEquals("single", args[0]);
    assertEquals("Project Review", args[1]);
    assertEquals("true", args[7]);
  }

  @Test
  public void testParseCreateAllDayEvent() {
    String commandString = "create event \"All Day Conference\" on 2023-04-15";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockCreateCommand);
    String[] args = result.getArgs();
    assertEquals("allday", args[0]);
    assertEquals("All Day Conference", args[1]);
    assertEquals("2023-04-15", args[2]);
  }

  @Test
  public void testParseCreateRecurringEvent() {
    String commandString = "create event \"Weekly Status Meeting\" "
        + "from 2023-04-12T09:00 to 2023-04-12T10:00 repeats MW for 4 times";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockCreateCommand);
    String[] args = result.getArgs();
    assertEquals("recurring", args[0]);
    assertEquals("Weekly Status Meeting", args[1]);
    assertEquals("MW", args[4]);
    assertEquals("4", args[5]);
  }

  @Test
  public void testParseCreateRecurringUntilEvent() {
    String commandString = "create event \"Department Sync\" from 2023-04-14T14:00 to"
        + " 2023-04-14T15:00 repeats F until 2023-05-05";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockCreateCommand);
    String[] args = result.getArgs();
    assertEquals("recurring-until", args[0]);
    assertEquals("Department Sync", args[1]);
    assertEquals("F", args[4]);
    assertEquals("2023-05-05", args[5]);
  }

  @Test
  public void testParseCreateAllDayRecurringEvent() {
    String commandString =
        "create event \"Morning Standup\" on " + "2023-04-17 repeats MTWRF for 10 times";
    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockCreateCommand);
    String[] args = result.getArgs();
    assertEquals("allday-recurring", args[0]);
    assertEquals("Morning Standup", args[1]);
    assertEquals("MTWRF", args[3]);
    assertEquals("10", args[4]);
  }

  @Test
  public void testParseCreateAllDayRecurringUntilEvent() {
    String commandString =
        "create event \"Monthly Planning\" " + "on 2023-04-20 repeats F until 2023-07-20";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockCreateCommand);
    String[] args = result.getArgs();
    assertEquals("allday-recurring-until", args[0]);
    assertEquals("Monthly Planning", args[1]);
    assertEquals("F", args[3]);
    assertEquals("2023-07-20", args[4]);
  }

  @Test
  public void testParsePrintEventsOnDate() {
    String commandString = "print events on 2023-04-15";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockPrintCommand);
    String[] args = result.getArgs();
    assertEquals("on_date", args[0]);
    assertEquals("2023-04-15", args[1]);
  }

  @Test
  public void testParsePrintEventsInRange() {
    String commandString = "print events from 2023-04-10 to 2023-04-20";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockPrintCommand);
    String[] args = result.getArgs();
    assertEquals("date_range", args[0]);
    assertEquals("2023-04-10", args[1]);
    assertEquals("2023-04-20", args[2]);
  }

  @Test
  public void testParseShowStatus() {
    String commandString = "show status on 2023-04-10T10:30";
    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockShowCommand);
    String[] args = result.getArgs();
    assertEquals(1, args.length);
    assertEquals("2023-04-10T10:30", args[0]);
  }

  @Test
  public void testParseExportCalendar() {
    String commandString = "export cal calendar.csv";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockExportCommand);
    String[] args = result.getArgs();
    assertEquals(1, args.length);
    assertEquals("calendar.csv", args[0]);
  }

  @Test
  public void testParseEditSingleEvent() {
    String commandString = "edit event subject \"Team Meeting\" from "
        + "2023-04-10T10:00 to 2023-04-10T11:00 with \"Team Sync\"";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockEditCommand);
    String[] args = result.getArgs();
    assertEquals("single", args[0]);
    assertEquals("subject", args[1]);
    assertEquals("Team Meeting", args[2]);
    assertEquals("2023-04-10T10:00", args[3]);
    assertEquals("Team Sync", args[4]);
  }

  @Test
  public void testParseEditEventsFromDate() {
    String commandString =
        "edit events visibility \"Weekly Meeting\" " + "from 2023-04-12T09:00 with private";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    assertNotNull(result);
    assertTrue(result.getCommand() instanceof MockEditCommand);
    String[] args = result.getArgs();
    assertEquals("series_from_date", args[0]);
    assertEquals("visibility", args[1]);
    assertEquals("Weekly Meeting", args[2]);
    assertEquals("2023-04-12T09:00", args[3]);
    assertEquals("private", args[4]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEmptyCommand() {
    parser.parseCommand("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseNullCommand() {
    parser.parseCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidCommand() {
    parser.parseCommand("invalid command");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidCreateCommand() {
    parser.parseCommand("create event Invalid Format");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidPrintCommand() {
    parser.parseCommand("print events invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidShowCommand() {
    parser.parseCommand("show status invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidExportCommand() {
    parser.parseCommand("export invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidEditCommand() {
    parser.parseCommand("edit invalid");
  }

  @Test
  public void testEventWithDescription() {
    String commandString = "create event \"Meeting\" from 2023-04-10T10:00 to 2023-04-10T11:00 "
        + "desc \"Team discussion\"";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    String[] args = result.getArgs();
    assertEquals("Team discussion", args[4]);
  }

  @Test
  public void testEventWithLocation() {
    String commandString = "create event \"Meeting\" from 2023-04-10T10:00 to 2023-04-10T11:00 "
        + "at \"Conference Room\"";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    String[] args = result.getArgs();
    assertEquals("Conference Room", args[5]);
  }

  @Test
  public void testPrivateEvent() {
    String commandString = "create event \"Confidential Meeting\" from 2023-04-10T10:00 to "
        + "2023-04-10T11:00 private";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);

    String[] args = result.getArgs();
    assertEquals("false", args[6]);
  }

  @Test
  public void testComplexEvent() {
    String commandString = "create event --autoDecline \"Project Meeting\" from 2023-04-10T10:00 "
        + "to 2023-04-10T11:00 desc \"Quarterly project review\" at \"Room 101\" private";

    CommandParser.CommandWithArgs result = parser.parseCommand(commandString);
    String[] args = result.getArgs();
    assertEquals("Project Meeting", args[1]);
    assertEquals("Quarterly project review", args[4]);
    assertEquals("Room 101", args[5]);
    assertEquals("false", args[6]);
    assertEquals("true", args[7]);
  }
}