import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.function.Consumer;

import model.calendar.Calendar;
import model.calendar.CalendarRegistry;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the CalendarRegistry class.
 */
public class CalendarRegistryTest {

  private CalendarRegistry registry;
  private Calendar workCalendar;
  private Calendar personalCalendar;
  private Calendar familyCalendar;

  @Before
  public void setUp() {
    registry = new CalendarRegistry();
    
    // Create sample calendars
    workCalendar = new Calendar();
    workCalendar.setName("Work");
    workCalendar.setTimezone("America/New_York");
    
    personalCalendar = new Calendar();
    personalCalendar.setName("Personal");
    personalCalendar.setTimezone("America/Los_Angeles");
    
    familyCalendar = new Calendar();
    familyCalendar.setName("Family");
    familyCalendar.setTimezone("Europe/London");
  }

  /**
   * Test constructor creates a valid object.
   */
  @Test
  public void testConstructor() {
    CalendarRegistry newRegistry = new CalendarRegistry();
    assertNotNull(newRegistry);
    assertEquals(0, newRegistry.getCalendarCount());
    assertNull(newRegistry.getActiveCalendarName());
  }

  /**
   * Test registering a calendar.
   */
  @Test
  public void testRegisterCalendar() throws DuplicateCalendarException {
    registry.registerCalendar("Work", workCalendar);
    
    assertEquals(1, registry.getCalendarCount());
    assertTrue(registry.hasCalendar("Work"));
    assertEquals("Work", registry.getActiveCalendarName());
  }
  
  /**
   * Test registering multiple calendars.
   */
  @Test
  public void testRegisterMultipleCalendars() throws DuplicateCalendarException {
    registry.registerCalendar("Work", workCalendar);
    registry.registerCalendar("Personal", personalCalendar);
    registry.registerCalendar("Family", familyCalendar);
    
    assertEquals(3, registry.getCalendarCount());
    assertTrue(registry.hasCalendar("Work"));
    assertTrue(registry.hasCalendar("Personal"));
    assertTrue(registry.hasCalendar("Family"));
    
    // First calendar registered should be active
    assertEquals("Work", registry.getActiveCalendarName());
  }
  
  /**
   * Test registering a calendar with a null name.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRegisterCalendarWithNullName() throws DuplicateCalendarException {
    registry.registerCalendar(null, workCalendar);
  }
  
  /**
   * Test registering a calendar with an empty name.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRegisterCalendarWithEmptyName() throws DuplicateCalendarException {
    registry.registerCalendar("", workCalendar);
  }
  
  /**
   * Test registering a duplicate calendar.
   */
  @Test(expected = DuplicateCalendarException.class)
  public void testRegisterDuplicateCalendar() throws DuplicateCalendarException {
    registry.registerCalendar("Work", workCalendar);
    registry.registerCalendar("Work", personalCalendar);
  }
  
  /**
   * Test getting a calendar by name.
   */
  @Test
  public void testGetCalendarByName() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    
    Calendar retrieved = registry.getCalendarByName("Work");
    assertNotNull(retrieved);
    assertEquals("Work", retrieved.getName());
  }
  
  /**
   * Test getting a calendar by name that doesn't exist.
   */
  @Test(expected = CalendarNotFoundException.class)
  public void testGetCalendarByNameNotFound() throws CalendarNotFoundException {
    registry.getCalendarByName("NonExistentCalendar");
  }
  
  /**
   * Test removing a calendar.
   */
  @Test
  public void testRemoveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    
    // Verify calendar was added
    assertTrue(registry.hasCalendar("Work"));
    
    // Remove the calendar
    registry.removeCalendar("Work");
    
    // Verify calendar was removed
    assertFalse(registry.hasCalendar("Work"));
    assertEquals(0, registry.getCalendarCount());
    assertNull(registry.getActiveCalendarName());
  }
  
  /**
   * Test removing a calendar that doesn't exist.
   */
  @Test(expected = CalendarNotFoundException.class)
  public void testRemoveCalendarNotFound() throws CalendarNotFoundException {
    registry.removeCalendar("NonExistentCalendar");
  }
  
  /**
   * Test removing a calendar affects active calendar selection.
   */
  @Test
  public void testRemoveActiveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    registry.registerCalendar("Personal", personalCalendar);
    
    // Verify Work is active
    assertEquals("Work", registry.getActiveCalendarName());
    
    // Remove the active calendar
    registry.removeCalendar("Work");
    
    // Verify another calendar becomes active
    assertEquals("Personal", registry.getActiveCalendarName());
    
    // Remove the last calendar
    registry.removeCalendar("Personal");
    
    // Verify no active calendar
    assertNull(registry.getActiveCalendarName());
  }
  
  /**
   * Test renaming a calendar.
   */
  @Test
  public void testRenameCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    
    // Rename the calendar
    registry.renameCalendar("Work", "Job");
    
    // Verify rename was successful
    assertFalse(registry.hasCalendar("Work"));
    assertTrue(registry.hasCalendar("Job"));
    assertEquals("Job", registry.getActiveCalendarName());
    
    // Verify the calendar object was updated
    Calendar renamed = registry.getCalendarByName("Job");
    assertEquals("Job", renamed.getName());
  }
  
  /**
   * Test renaming a calendar to a name that already exists.
   */
  @Test(expected = DuplicateCalendarException.class)
  public void testRenameCalendarToDuplicateName() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    registry.registerCalendar("Personal", personalCalendar);
    
    registry.renameCalendar("Work", "Personal");
  }
  
  /**
   * Test renaming a calendar that doesn't exist.
   */
  @Test(expected = CalendarNotFoundException.class)
  public void testRenameCalendarNotFound() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.renameCalendar("NonExistentCalendar", "NewName");
  }
  
  /**
   * Test renaming a calendar to null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testRenameCalendarToNullName() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    registry.renameCalendar("Work", null);
  }
  
  /**
   * Test checking if a calendar exists.
   */
  @Test
  public void testHasCalendar() throws DuplicateCalendarException {
    assertFalse(registry.hasCalendar("Work"));
    
    registry.registerCalendar("Work", workCalendar);
    
    assertTrue(registry.hasCalendar("Work"));
  }
  
  /**
   * Test getting all calendar names.
   */
  @Test
  public void testGetCalendarNames() throws DuplicateCalendarException {
    registry.registerCalendar("Work", workCalendar);
    registry.registerCalendar("Personal", personalCalendar);
    
    Set<String> names = registry.getCalendarNames();
    
    assertEquals(2, names.size());
    assertTrue(names.contains("Work"));
    assertTrue(names.contains("Personal"));
  }
  
  /**
   * Test getting calendar count.
   */
  @Test
  public void testGetCalendarCount() throws DuplicateCalendarException, CalendarNotFoundException {
    assertEquals(0, registry.getCalendarCount());
    
    registry.registerCalendar("Work", workCalendar);
    assertEquals(1, registry.getCalendarCount());
    
    registry.registerCalendar("Personal", personalCalendar);
    assertEquals(2, registry.getCalendarCount());
    
    registry.removeCalendar("Work");
    assertEquals(1, registry.getCalendarCount());
  }
  
  /**
   * Test getting active calendar.
   */
  @Test
  public void testGetActiveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    
    Calendar active = registry.getActiveCalendar();
    assertNotNull(active);
    assertEquals("Work", active.getName());
  }
  
  /**
   * Test getting active calendar when none exists.
   */
  @Test(expected = CalendarNotFoundException.class)
  public void testGetActiveCalendarNoneSet() throws CalendarNotFoundException {
    registry.getActiveCalendar();
  }
  
  /**
   * Test setting active calendar.
   */
  @Test
  public void testSetActiveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    registry.registerCalendar("Personal", personalCalendar);
    
    // By default, first calendar is active
    assertEquals("Work", registry.getActiveCalendarName());
    
    // Set a different active calendar
    registry.setActiveCalendar("Personal");
    
    assertEquals("Personal", registry.getActiveCalendarName());
    assertEquals("Personal", registry.getActiveCalendar().getName());
  }
  
  /**
   * Test setting a non-existent calendar as active.
   */
  @Test(expected = CalendarNotFoundException.class)
  public void testSetActiveCalendarNotFound() throws CalendarNotFoundException {
    registry.setActiveCalendar("NonExistentCalendar");
  }
  
  /**
   * Test apply to calendar.
   */
  @Test
  public void testApplyToCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    
    // Apply a change to the calendar using a consumer
    registry.applyToCalendar("Work", calendar -> calendar.setTimezone("Europe/Berlin"));
    
    // Verify the change was applied
    Calendar modified = registry.getCalendarByName("Work");
    assertEquals("Europe/Berlin", modified.getTimezone());
  }
  
  /**
   * Test apply to calendar that doesn't exist.
   */
  @Test(expected = CalendarNotFoundException.class)
  public void testApplyToCalendarNotFound() throws CalendarNotFoundException {
    registry.applyToCalendar("NonExistentCalendar", calendar -> {});
  }
  
  /**
   * Test apply to active calendar.
   */
  @Test
  public void testApplyToActiveCalendar() throws DuplicateCalendarException, CalendarNotFoundException {
    registry.registerCalendar("Work", workCalendar);
    
    // Apply a change to the active calendar
    registry.applyToActiveCalendar(calendar -> calendar.setTimezone("Europe/Berlin"));
    
    // Verify the change was applied
    Calendar active = registry.getActiveCalendar();
    assertEquals("Europe/Berlin", active.getTimezone());
  }
  
  /**
   * Test apply to active calendar when none is set.
   */
  @Test(expected = CalendarNotFoundException.class)
  public void testApplyToActiveCalendarNoneSet() throws CalendarNotFoundException {
    registry.applyToActiveCalendar(calendar -> {});
  }
  
  /**
   * Test operations maintain consistency.
   */
  @Test
  public void testRegistryConsistency() throws DuplicateCalendarException, CalendarNotFoundException {
    // Register calendars
    registry.registerCalendar("Work", workCalendar);
    registry.registerCalendar("Personal", personalCalendar);
    
    // Rename a calendar
    registry.renameCalendar("Work", "Job");
    
    // Remove a calendar
    registry.removeCalendar("Personal");
    
    // Verify state
    assertEquals(1, registry.getCalendarCount());
    assertTrue(registry.hasCalendar("Job"));
    assertFalse(registry.hasCalendar("Work"));
    assertFalse(registry.hasCalendar("Personal"));
    assertEquals("Job", registry.getActiveCalendarName());
    
    // Register a new calendar
    registry.registerCalendar("Family", familyCalendar);
    
    // Change active calendar
    registry.setActiveCalendar("Family");
    
    // Verify active calendar
    assertEquals("Family", registry.getActiveCalendarName());
    
    // Remove active calendar
    registry.removeCalendar("Family");
    
    // Verify another calendar becomes active
    assertEquals("Job", registry.getActiveCalendarName());
  }
} 