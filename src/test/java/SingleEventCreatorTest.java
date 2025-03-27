import org.junit.Test;

import java.time.LocalDateTime;

import controller.command.create.strategy.SingleEventCreator;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for SingleEventCreator.
 */
public class SingleEventCreatorTest {

  @Test
  public void testCreateEventSuccess() throws InvalidEventException {
    String[] args = {"single", "Team Meeting", "2023-05-15T10:00", "2023-05-15T11:00",
        "Weekly team sync", "Conference Room A", "true", "false"};
    SingleEventCreator creator = new SingleEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Team Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), event.getEndDateTime());
    assertEquals("Weekly team sync", event.getDescription());
    assertEquals("Conference Room A", event.getLocation());
    assertTrue(event.isPublic());
    assertFalse(event.isAllDay());
  }

  @Test
  public void testCreateEventWithMinimalParameters() throws InvalidEventException {
    String[] args = {"single", "Quick Meeting", "2023-05-15T14:00", "2023-05-15T15:00"};
    SingleEventCreator creator = new SingleEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Quick Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 14, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 15, 0), event.getEndDateTime());
    assertEquals("", event.getDescription());
    assertEquals("", event.getLocation());
    assertTrue(event.isPublic());
    assertFalse(event.isAllDay());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithNullArgs() {
    new SingleEventCreator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithInsufficientArgs() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00"};
    new SingleEventCreator(args);
  }

  @Test(expected = InvalidEventException.class)
  public void testCreateEventWithNullEventName() throws InvalidEventException {
    String[] args = {"single", null, "2023-05-15T10:00", "2023-05-15T11:00"};
    SingleEventCreator creator = new SingleEventCreator(args);
    creator.createEvent();
  }

  @Test(expected = InvalidEventException.class)
  public void testCreateEventWithEmptyEventName() throws InvalidEventException {
    String[] args = {"single", "", "2023-05-15T10:00", "2023-05-15T11:00"};
    SingleEventCreator creator = new SingleEventCreator(args);
    creator.createEvent();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithInvalidDateTimeFormat() {
    String[] args = {"single", "Meeting", "invalid-date", "2023-05-15T11:00"};
    new SingleEventCreator(args);
  }

  @Test
  public void testCreateEventWithAutoDecline()
      throws InvalidEventException, ConflictingEventException {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
        "true", "true"};
    SingleEventCreator creator = new SingleEventCreator(args);
    model.calendar.Calendar calendar = new model.calendar.Calendar();
    String result = creator.executeCreation(calendar);

    assertTrue("Should return success message",
        result.contains("Event 'Meeting' created successfully"));
    assertEquals("Should have one event in calendar", 1, calendar.getAllEvents().size());

    Event event = calendar.getAllEvents().get(0);
    assertEquals("Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), event.getEndDateTime());
    assertTrue(event.isPublic());
  }

  @Test
  public void testCreatePrivateEvent() throws InvalidEventException {
    String[] args = {"single", "Private Meeting", "2023-05-15T10:00", "2023-05-15T11:00",
        "Confidential discussion", "Private Room", "false", "false"};
    SingleEventCreator creator = new SingleEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Private Meeting", event.getSubject());
    assertEquals("Confidential discussion", event.getDescription());
    assertEquals("Private Room", event.getLocation());
    assertFalse(event.isPublic());
  }

  @Test
  public void testCreateEventWithQuotedDescription() throws InvalidEventException {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00",
        "\"Team sync meeting\"", "\"Main conference room\"", "true", "false"};
    SingleEventCreator creator = new SingleEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Team sync meeting", event.getDescription());
    assertEquals("Main conference room", event.getLocation());
  }

  @Test
  public void testCreateEventWithEndTimeBeforeStartTime() {
    String[] args = {"single", "Meeting", "2023-05-15T11:00", "2023-05-15T10:00"};
    try {
      SingleEventCreator creator = new SingleEventCreator(args);
      creator.createEvent();
      fail("Should throw IllegalArgumentException when end time is before start time");
    } catch (IllegalArgumentException e) {
      assertTrue("Exception message should indicate invalid time order",
          e.getMessage().contains("End date/time cannot be before start date/time"));
    } catch (InvalidEventException e) {
      throw new RuntimeException(e);
    }
  }
}