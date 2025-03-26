import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import controller.CalendarController;
import controller.ICommandFactory;
import controller.command.calendar.CalendarCommandFactory;
import controller.command.event.CommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.factory.CalendarFactory;
import utilities.TimeZoneHandler;
import view.ConsoleView;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for CalendarFactory.
 * Tests the creation of calendar components including views, handlers, and managers.
 */
public class CalendarFactoryTest {

  private CalendarFactory factory;

  @Before
  public void setUp() {
    factory = new CalendarFactory();
  }

  @Test
  public void testCreateView() {
    ICalendarView view = factory.createView();
    assertNotNull("View should not be null", view);
    assertTrue("View should be an instance of ConsoleView", view instanceof ConsoleView);
  }

  @Test
  public void testCreateTimeZoneHandler() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    assertNotNull("TimeZoneHandler should not be null", handler);
    assertTrue("Handler should be an instance of TimeZoneHandler",
            handler instanceof TimeZoneHandler);
  }

  @Test
  public void testCreateCalendarManager() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);

    assertNotNull("CalendarManager should not be null", manager);
    assertTrue("Manager should be an instance of CalendarManager",
            manager instanceof CalendarManager);

    assertEquals("CalendarManager should have the provided timezone handler",
            handler, manager.getTimezoneHandler());
  }

  @Test
  public void testCreateEventCommandFactory() {
    ICalendarView view = factory.createView();
    ICalendar calendar = new MockCalendar();
    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);

    assertNotNull("Event command factory should not be null", eventFactory);
    assertTrue("Factory should be an instance of CommandFactory",
            eventFactory instanceof CommandFactory);
  }

  @Test
  public void testCreateCalendarCommandFactory() {
    ICalendarView view = factory.createView();
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    assertNotNull("Calendar command factory should not be null", calendarFactory);
    assertTrue("Factory should be an instance of CalendarCommandFactory",
            calendarFactory instanceof CalendarCommandFactory);
  }

  @Test
  public void testCreateController() {
    // Create all dependencies
    ICalendarView view = factory.createView();
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    CalendarController controller = factory.createController(
            eventFactory,
            calendarFactory,
            manager,
            view
    );

    assertNotNull("Controller should not be null", controller);
    assertTrue("Controller should be an instance of CalendarController",
            controller instanceof CalendarController);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarManagerWithNullHandler() {
    factory.createCalendarManager(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventCommandFactoryWithNullCalendar() {
    ICalendarView view = factory.createView();
    factory.createEventCommandFactory(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventCommandFactoryWithNullView() {
    ICalendar calendar = new MockCalendar();
    factory.createEventCommandFactory(calendar, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarCommandFactoryWithNullManager() {
    ICalendarView view = factory.createView();
    factory.createCalendarCommandFactory(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarCommandFactoryWithNullView() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    factory.createCalendarCommandFactory(manager, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateControllerWithNullEventFactory() {
    ICalendarView view = factory.createView();
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    factory.createController(null, calendarFactory, manager, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateControllerWithNullCalendarFactory() {
    ICalendarView view = factory.createView();
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);

    factory.createController(eventFactory, null, manager, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateControllerWithNullManager() {
    ICalendarView view = factory.createView();
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(
            factory.createCalendarManager(factory.createTimeZoneHandler()),
            view
    );

    factory.createController(eventFactory, calendarFactory, null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateControllerWithNullView() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar,
            factory.createView());
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager,
            factory.createView());

    factory.createController(eventFactory, calendarFactory, manager, null);
  }

  /**
   * Mock implementation of ICalendar for testing purposes.
   */
  private static class MockCalendar implements ICalendar {

    @Override
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
            throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start,
                                             LocalDateTime end, String weekdays,
                                             LocalDate untilDate, boolean autoDecline)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline,
                                              String description, String location,
                                              boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate, boolean autoDecline,
                                                   String description, String location,
                                                   boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return List.of();
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      return List.of();
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public Event findEvent(String subject, LocalDateTime startDateTime)
            throws EventNotFoundException {
      return null;
    }

    @Override
    public List<Event> getAllEvents() {
      return List.of();
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime,
                                   String property, String newValue)
            throws EventNotFoundException, InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime,
                                  String property, String newValue)
            throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue)
            throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return List.of();
    }

    @Override
    public String exportToCSV(String filePath) throws IOException {
      return "";
    }
  }
}