import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

import model.calendar.Calendar;
import model.calendar.CalendarRegistry;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test class for Calendar Registry class.
 */
public class CalendarRegistryTest {

  private CalendarRegistry calendarRegistry;
  private CalendarMock mockCalendar;

  @Before
  public void setUp() {
    calendarRegistry = new CalendarRegistry();
    mockCalendar = new CalendarMock("Test Calendar");
  }

  @Test
  public void testRegisterCalendar() throws DuplicateCalendarException {
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
    assertTrue(calendarRegistry.hasCalendar("Test Calendar"));
  }

  @Test(expected = DuplicateCalendarException.class)
  public void testRegisterDuplicateCalendar() throws DuplicateCalendarException {
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
  }

  @Test
  public void testGetCalendarByName() throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
    Calendar retrievedCalendar = calendarRegistry.getCalendarByName("Test Calendar");
    assertEquals(mockCalendar, retrievedCalendar);
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testGetCalendarByNameNotFound() throws CalendarNotFoundException {
    calendarRegistry.getCalendarByName("Non-existent Calendar");
  }

  @Test
  public void testRemoveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
    calendarRegistry.removeCalendar("Test Calendar");
    assertFalse(calendarRegistry.hasCalendar("Test Calendar"));
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testRemoveCalendarNotFound() throws CalendarNotFoundException {
    calendarRegistry.removeCalendar("Non-existent Calendar");
  }

  @Test
  public void testRenameCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Old Name", mockCalendar);
    calendarRegistry.renameCalendar("Old Name", "New Name");
    assertTrue(calendarRegistry.hasCalendar("New Name"));
    assertFalse(calendarRegistry.hasCalendar("Old Name"));
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testRenameCalendarNotFound()
          throws CalendarNotFoundException, DuplicateCalendarException {
    calendarRegistry.renameCalendar("Non-existent Calendar", "New Name");
  }

  @Test(expected = DuplicateCalendarException.class)
  public void testRenameCalendarToExistingName()
          throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Calendar1", mockCalendar);
    Calendar anotherCalendar = new CalendarMock("Calendar2");
    calendarRegistry.registerCalendar("Calendar2", anotherCalendar);
    calendarRegistry.renameCalendar("Calendar1", "Calendar2");
  }

  @Test
  public void testGetActiveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
    Calendar activeCalendar = calendarRegistry.getActiveCalendar();
    assertEquals(mockCalendar, activeCalendar);
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testGetActiveCalendarNotSet() throws CalendarNotFoundException {
    calendarRegistry.getActiveCalendar();
  }

  @Test
  public void testSetActiveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Calendar1", mockCalendar);
    Calendar anotherCalendar = new CalendarMock("Calendar2");
    calendarRegistry.registerCalendar("Calendar2", anotherCalendar);
    calendarRegistry.setActiveCalendar("Calendar2");
    assertEquals("Calendar2", calendarRegistry.getActiveCalendarName());
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testSetActiveCalendarNotFound() throws CalendarNotFoundException {
    calendarRegistry.setActiveCalendar("Non-existent Calendar");
  }

  @Test
  public void testApplyToCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
    Consumer<Calendar> mockConsumer = calendar -> calendar.setName("Updated Calendar");
    calendarRegistry.applyToCalendar("Test Calendar", mockConsumer);
    assertEquals("Updated Calendar", mockCalendar.getName());
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testApplyToCalendarNotFound() throws CalendarNotFoundException {
    calendarRegistry.applyToCalendar("Non-existent Calendar", calendar -> {
    });
  }

  @Test
  public void testApplyToActiveCalendar()
          throws DuplicateCalendarException, CalendarNotFoundException {
    calendarRegistry.registerCalendar("Test Calendar", mockCalendar);
    calendarRegistry.setActiveCalendar("Test Calendar");
    Consumer<Calendar> mockConsumer = calendar ->
            calendar.setName("Updated Active Calendar");
    calendarRegistry.applyToActiveCalendar(mockConsumer);
    assertEquals("Updated Active Calendar", mockCalendar.getName());
  }

  @Test(expected = CalendarNotFoundException.class)
  public void testApplyToActiveCalendarNotFound() throws CalendarNotFoundException {
    calendarRegistry.applyToActiveCalendar(calendar -> {
    });
  }

  private class CalendarMock extends Calendar {

    private String name;

    public CalendarMock(String name) {
      this.name = name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }
  }
}
