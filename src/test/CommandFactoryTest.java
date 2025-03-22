import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import controller.ICommandFactory;
import controller.command.ICommand;
import controller.command.event.CommandFactory;
import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Junit test file to test command factory.
 */
public class CommandFactoryTest {

  /**
   * Manual mock implementation of ICalendar.
   */
  private static class MockCalendar implements ICalendar {
    /**
     * Minimal implementation with no functionality.
     */
    @Override
    public boolean addEvent(model.event.Event event, boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean addRecurringEvent(model.event.RecurringEvent recurringEvent,
                                     boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, java.time.LocalDateTime start,
                                             java.time.LocalDateTime end, String weekdays, java.time.LocalDate untilDate,
                                             boolean autoDecline) throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, java.time.LocalDate date,
                                              String weekdays, int occurrences, boolean autoDecline, String description, String location,
                                              boolean isPublic) throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, java.time.LocalDate date,
                                                   String weekdays, java.time.LocalDate untilDate, boolean autoDecline, String description,
                                                   String location, boolean isPublic) throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public java.util.List<model.event.Event> getEventsOnDate(java.time.LocalDate date) {
      return null;
    }

    @Override
    public java.util.List<model.event.Event> getEventsInRange(java.time.LocalDate startDate,
                                                              java.time.LocalDate endDate) {
      return null;
    }

    @Override
    public boolean isBusy(java.time.LocalDateTime dateTime) {
      return false;
    }

    @Override
    public model.event.Event findEvent(String subject, java.time.LocalDateTime startDateTime) throws EventNotFoundException {
      return null;
    }

    @Override
    public java.util.List<model.event.Event> getAllEvents() {
      return null;
    }

    @Override
    public boolean editSingleEvent(String subject, java.time.LocalDateTime startDateTime,
                                   String property, String newValue) throws EventNotFoundException, InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, java.time.LocalDateTime startDateTime,
                                  String property, String newValue) throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public java.util.List<model.event.RecurringEvent> getAllRecurringEvents() {
      return null;
    }

    @Override
    public String exportToCSV(String filePath) throws java.io.IOException {
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
      // Empty implementation for testing
    }

    @Override
    public void displayError(String errorMessage) {
      // Empty implementation for testing
    }
  }

  private ICalendar calendar;
  private ICalendarView view;
  private ICommandFactory factory;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    view = new MockCalendarView();
    factory = new CommandFactory(calendar, view);
  }

  @Test
  public void testGetCommandWithValidName() {
    ICommand createCommand = factory.getCommand("create");
    ICommand printCommand = factory.getCommand("print");
    ICommand showCommand = factory.getCommand("show");
    ICommand exportCommand = factory.getCommand("export");
    ICommand editCommand = factory.getCommand("edit");
    ICommand exitCommand = factory.getCommand("exit");

    // Check if commands are not null (the command types have changed)
    assertNotNull(createCommand);
    assertNotNull(printCommand);
    assertNotNull(showCommand);
    assertNotNull(exportCommand);
    assertNotNull(editCommand);
    assertNotNull(exitCommand);

    // Check command names
    assertEquals("create", createCommand.getName());
    assertEquals("print", printCommand.getName());
    assertEquals("show", showCommand.getName());
    assertEquals("export", exportCommand.getName());
    assertEquals("edit", editCommand.getName());
    assertEquals("exit", exitCommand.getName());
  }

  @Test
  public void testGetCommandWithInvalidName() {
    ICommand command = factory.getCommand("nonexistent");
    assertNull(command);
  }

  @Test
  public void testHasCommandWithValidName() {
    assertTrue(factory.hasCommand("create"));
    assertTrue(factory.hasCommand("print"));
    assertTrue(factory.hasCommand("show"));
    assertTrue(factory.hasCommand("export"));
    assertTrue(factory.hasCommand("edit"));
    assertTrue(factory.hasCommand("exit"));
  }

  @Test
  public void testHasCommandWithInvalidName() {
    assertFalse(factory.hasCommand("nonexistent"));
    assertFalse(factory.hasCommand(""));
    assertFalse(factory.hasCommand(null));
  }

  @Test
  public void testGetCommandNames() {
    Iterable<String> commandNames = ((CommandFactory) factory).getCommandNames();

    Set<String> nameSet = new HashSet<>();
    for (String name : commandNames) {
      nameSet.add(name);
    }

    assertTrue(nameSet.contains("create"));
    assertTrue(nameSet.contains("print"));
    assertTrue(nameSet.contains("show"));
    assertTrue(nameSet.contains("export"));
    assertTrue(nameSet.contains("edit"));
    assertTrue(nameSet.contains("exit"));
    // Check for calendar-related commands that should be forwarded
    assertTrue(nameSet.contains("use"));
    assertTrue(nameSet.contains("copy"));

    // Should now have 8 commands (added "use" and "copy" to the original 6)
    assertEquals(8, nameSet.size());
  }

  @Test
  public void testGetCalendar() {
    assertSame(calendar, ((CommandFactory) factory).getCalendar());
  }

  @Test
  public void testGetView() {
    assertSame(view, ((CommandFactory) factory).getView());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new CommandFactory(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullView() {
    new CommandFactory(calendar, null);
  }

  @Test
  public void testCommandInitialization() {
    ICommand createCommand = factory.getCommand("create");
    ICommand printCommand = factory.getCommand("print");
    ICommand showCommand = factory.getCommand("show");
    ICommand exportCommand = factory.getCommand("export");
    ICommand editCommand = factory.getCommand("edit");

    assertEquals("create", createCommand.getName());
    assertEquals("print", printCommand.getName());
    assertEquals("show", showCommand.getName());
    assertEquals("export", exportCommand.getName());
    assertEquals("edit", editCommand.getName());
  }

  @Test
  public void testCalendarCommandForwarding() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    // Create and use commands should be forwarded to CalendarCommandFactory
    ICommand useCommand = factory.getCommand("use");
    String result = useCommand.execute(new String[]{"test"});
    assertEquals("Command forwarded to CalendarCommandFactory", result);

    // Copy command should also be forwarded
    ICommand copyCommand = factory.getCommand("copy");
    result = copyCommand.execute(new String[]{"test"});
    assertEquals("Command forwarded to CalendarCommandFactory", result);
  }

  @Test
  public void testRegisterDuplicateCommand() {
    CommandFactory factoryImpl = (CommandFactory) factory;
    int initialCommandCount = 0;
    for (String name : factoryImpl.getCommandNames()) {
      initialCommandCount++;
    }

    CommandFactory newFactory = new CommandFactory(calendar, view);

    int newCommandCount = 0;
    for (String name : newFactory.getCommandNames()) {
      newCommandCount++;
    }

    assertEquals(initialCommandCount, newCommandCount);
  }

  @Test
  public void testNullCommandName() {
    ICommand command = factory.getCommand(null);
    assertNull(command);

    assertFalse(factory.hasCommand(null));
  }

  @Test
  public void testRegisterCustomCommand() throws ConflictingEventException, InvalidEventException, EventNotFoundException {
    CommandFactory factoryImpl = (CommandFactory) factory;
    // Register a custom command
    factoryImpl.registerCommand("custom", args -> "Custom command executed");

    // Verify the command was registered
    assertTrue(factoryImpl.hasCommand("custom"));

    // Execute the command
    ICommand customCommand = factoryImpl.getCommand("custom");
    assertEquals("Custom command executed", customCommand.execute(new String[]{}));
  }
}