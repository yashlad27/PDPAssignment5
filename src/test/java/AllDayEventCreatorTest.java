import org.junit.Test;

import java.time.LocalDateTime;

import controller.command.create.strategy.AllDayEventCreator;
import controller.command.create.strategy.AllDayRecurringUntilEventCreator;
import model.event.Event;
import model.event.EventAction;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for AllDayEventCreator. Tests the creation of single all-day events.
 */
public class AllDayEventCreatorTest {

  @Test
  public void testConstructorWithNullArgs() {
    try {
      new AllDayEventCreator(null);
      fail("Should throw IllegalArgumentException for null args");
    } catch (IllegalArgumentException e) {
      assertEquals("Arguments array cannot be null", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInsufficientArgs() {
    String[] args = {"allday", "Meeting", "2023-05-15"};
    try {
      new AllDayEventCreator(args);
      fail("Should throw IllegalArgumentException for insufficient args");
    } catch (IllegalArgumentException e) {
      assertEquals("Insufficient arguments for creating an all-day event", e.getMessage());
    }
  }

  @Test
  public void testConstructorWithInvalidDate() {
    String[] args = {"allday", "Meeting", "invalid-date", "false"};
    try {
      new AllDayEventCreator(args);
      fail("Should throw IllegalArgumentException for invalid date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Error parsing arguments"));
    }
  }

  @Test
  public void testCreateEventWithNullUntilDate() {
    String[] args = {"allday-recurring-until", "Meeting", "2023-05-15", "MWF", null, "false"};
    try {
      new AllDayRecurringUntilEventCreator(args);
      fail("Should throw IllegalArgumentException for null until date");
    } catch (IllegalArgumentException e) {
      assertEquals("Error parsing arguments: text", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithNullEventName() {
    String[] args = {"allday", null, "2023-05-15", "false"};
    AllDayEventCreator creator = new AllDayEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for null event name");
    } catch (InvalidEventException e) {
      assertEquals("Event name cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventWithEmptyEventName() {
    String[] args = {"allday", "", "2023-05-15", "false"};
    AllDayEventCreator creator = new AllDayEventCreator(args);
    try {
      creator.createEvent();
      fail("Should throw InvalidEventException for empty event name");
    } catch (InvalidEventException e) {
      assertEquals("Event name cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testCreateEventSuccess() throws InvalidEventException {
    String[] args = {"allday", "Meeting", "2023-05-15", "false", "Team meeting", "Conference Room",
        "true"};
    AllDayEventCreator creator = new AllDayEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 0, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 23, 59, 59), event.getEndDateTime());
    assertEquals("Team meeting", event.getDescription());
    assertEquals("Conference Room", event.getLocation());
    assertTrue(event.isPublic());
    assertTrue(event.isAllDay());
  }

  @Test
  public void testApplyEventAction() {
    LocalDateTime eventTime = LocalDateTime.of(2025, 3, 26, 10, 0);
    LocalDateTime eventEndTime = LocalDateTime.of(2025, 3, 26, 10, 10);
    Event event = new Event("Meeting", eventTime, eventEndTime, "Conference Room",
        "Discuss project updates", true);

    EventAction updateSubject = e -> e.setSubject("Updated Subject");
    EventAction updateLocation = e -> e.setLocation("New Location");

    updateSubject.execute(event);
    assertEquals("Updated Subject", event.getSubject());

    updateLocation.execute(event);
    assertEquals("New Location", event.getLocation());
  }

  @Test(expected = NullPointerException.class)
  public void testNullEventThrowsException() {
    EventAction updateSubject = e -> e.setSubject("Updated");
    updateSubject.execute(null);
  }

}