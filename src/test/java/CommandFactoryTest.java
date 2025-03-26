import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import controller.ICommandFactory;
import controller.command.ExitCommand;
import controller.command.ICommand;
import controller.command.event.CommandFactory;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
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
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent,
                                     boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start,
                                             LocalDateTime end, String weekdays,
                                             LocalDate untilDate,
                                             boolean autoDecline)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date,
                                              String weekdays, int occurrences,
                                              boolean autoDecline, String description,
                                              String location,
                                              boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date,
                                                   String weekdays, LocalDate untilDate,
                                                   boolean autoDecline, String description,
                                                   String location, boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public java.util.List<model.event.Event> getEventsOnDate(LocalDate date) {
      return null;
    }

    @Override
    public java.util.List<model.event.Event> getEventsInRange(LocalDate startDate,
                                                              LocalDate endDate) {
      return null;
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public model.event.Event findEvent(String subject, LocalDateTime startDateTime)
            throws EventNotFoundException {
      return null;
    }

    @Override
    public java.util.List<model.event.Event> getAllEvents() {
      return null;
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime,
                                   String property, String newValue) throws EventNotFoundException,
            InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime,
                                  String property, String newValue) throws InvalidEventException,
            ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue)
            throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public java.util.List<RecurringEvent> getAllRecurringEvents() {
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

    assertNotNull(createCommand);
    assertNotNull(printCommand);
    assertNotNull(showCommand);
    assertNotNull(exportCommand);
    assertNotNull(editCommand);
    assertNotNull(exitCommand);

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
    assertTrue(nameSet.contains("use"));
    assertTrue(nameSet.contains("copy"));

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
  public void testCalendarCommandForwarding() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    ICommand useCommand = factory.getCommand("use");
    String result = useCommand.execute(new String[]{"test"});
    assertEquals("Command forwarded to CalendarCommandFactory", result);

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
  public void testRegisterCustomCommand() throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    CommandFactory factoryImpl = (CommandFactory) factory;
    factoryImpl.registerCommand("custom", args -> "Custom command executed");

    assertTrue(factoryImpl.hasCommand("custom"));

    ICommand customCommand = factoryImpl.getCommand("custom");
    assertEquals("Custom command executed", customCommand.execute(new String[]{}));
  }

  @Test
  public void testExecute() {
    ExitCommand exitCommand = new ExitCommand();
    String result = exitCommand.execute(new String[]{});
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testGetName() {
    ExitCommand exitCommand = new ExitCommand();
    assertEquals("exit", exitCommand.getName());
  }
}