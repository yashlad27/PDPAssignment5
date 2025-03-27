import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

import controller.command.create.strategy.RecurringUntilEventCreator;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for RecurringUntilEventCreator.
 */
public class RecurringUntilEventCreatorTest {

  /**
   * Mock Event class for testing specific scenarios.
   */
  private static class MockEvent extends Event {

    private boolean isPublic;
    private String description;
    private String location;
    private boolean isAllDay;

    public MockEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
        String description, String location, boolean isPublic) {
      super(subject, startDateTime, endDateTime, description, location, isPublic);
      this.isPublic = isPublic;
      this.description = description;
      this.location = location;
      this.isAllDay = false;
    }

    @Override
    public boolean isPublic() {
      return isPublic;
    }

    @Override
    public void setPublic(boolean isPublic) {
      this.isPublic = isPublic;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public void setDescription(String description) {
      this.description = description;
    }

    @Override
    public String getLocation() {
      return location;
    }

    @Override
    public void setLocation(String location) {
      this.location = location;
    }

    @Override
    public boolean isAllDay() {
      return isAllDay;
    }

    @Override
    public void setAllDay(boolean allDay) {
      isAllDay = allDay;
    }
  }

  /**
   * Mock class for testing recurring event specific scenarios.
   */
  private static class MockRecurringEvent extends Event {

    private final Set<DayOfWeek> repeatDays;
    private final LocalDateTime endDateTime;

    public MockRecurringEvent(String subject, LocalDateTime startDateTime,
        LocalDateTime endDateTime, Set<DayOfWeek> repeatDays) {
      super(subject, startDateTime, endDateTime, null, null, true);
      this.repeatDays = repeatDays;
      this.endDateTime = endDateTime;
    }

    public Set<DayOfWeek> getRepeatDays() {
      return repeatDays;
    }

    @Override
    public LocalDateTime getEndDateTime() {
      return endDateTime;
    }
  }

  @Test
  public void testCreateEventSuccess() throws InvalidEventException {
    String[] args = {"recurring-until", "Weekly Team Meeting", "2023-05-15T10:00",
        "2023-05-15T11:00", "MWF", "2023-06-15", "false", "Weekly team sync", "Conference Room A",
        "true"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Weekly Team Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), event.getEndDateTime());
    assertEquals("Weekly team sync", event.getDescription());
    assertEquals("Conference Room A", event.getLocation());
    assertTrue(event.isPublic());
    assertFalse(event.isAllDay());
    assertTrue(event instanceof RecurringEvent);
  }

  @Test
  public void testCreateEventWithMinimalParameters() throws InvalidEventException {
    String[] args = {"recurring-until", "Quick Meeting", "2023-05-15T14:00", "2023-05-15T15:00",
        "M", "2023-06-15", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Quick Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 14, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 15, 0), event.getEndDateTime());
    assertEquals("", event.getDescription());
    assertEquals("", event.getLocation());
    assertTrue(event.isPublic());
    assertFalse(event.isAllDay());
    assertTrue(event instanceof RecurringEvent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithNullArgs() {
    new RecurringUntilEventCreator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithInsufficientArgs() {
    String[] args = {"recurring-until", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "M"};
    new RecurringUntilEventCreator(args);
  }

  @Test(expected = InvalidEventException.class)
  public void testCreateEventWithNullEventName() throws InvalidEventException {
    String[] args = {"recurring-until", null, "2023-05-15T10:00", "2023-05-15T11:00", "M",
        "2023-06-15", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    creator.createEvent();
  }

  @Test(expected = InvalidEventException.class)
  public void testCreateEventWithEmptyEventName() throws InvalidEventException {
    String[] args = {"recurring-until", "", "2023-05-15T10:00", "2023-05-15T11:00", "M",
        "2023-06-15", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    creator.createEvent();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithInvalidDateTimeFormat() {
    String[] args = {"recurring-until", "Meeting", "invalid-date", "2023-05-15T11:00", "M",
        "2023-06-15", "false"};
    new RecurringUntilEventCreator(args);
  }

  @Test(expected = InvalidEventException.class)
  public void testCreateEventWithEndTimeBeforeStartTime() throws InvalidEventException {
    String[] args = {"recurring-until", "Meeting", "2023-05-15T11:00", "2023-05-15T10:00", "M",
        "2023-06-15", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    creator.createEvent();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithEmptyRepeatDays() {
    String[] args = {"recurring-until", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "",
        "2023-06-15", "false"};
    new RecurringUntilEventCreator(args);
  }

  @Test
  public void testCreatePrivateEvent() throws InvalidEventException {
    String[] args = {"recurring-until", "Private Meeting", "2023-05-15T10:00", "2023-05-15T11:00",
        "M", "2023-06-15", "false", "Confidential discussion", "Private Room", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Private Meeting", event.getSubject());
    assertEquals("Confidential discussion", event.getDescription());
    assertEquals("Private Room", event.getLocation());
    assertFalse(event.isPublic());
  }

  @Test
  public void testCreateEventWithQuotedDescription() throws InvalidEventException {
    String[] args = {"recurring-until", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "M",
        "2023-06-15", "false", "\"Team sync meeting\"", "\"Main conference room\"", "true"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    assertEquals("Team sync meeting", event.getDescription());
    assertEquals("Main conference room", event.getLocation());
  }

  @Test
  public void testCreateEventWithMultipleRepeatDays() throws InvalidEventException {
    String[] args = {"recurring-until", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF",
        "2023-06-15", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    assertTrue(event instanceof RecurringEvent);
    RecurringEvent recurringEvent = (RecurringEvent) event;
    Set<DayOfWeek> repeatDays = recurringEvent.getRepeatDays();
    assertEquals(3, repeatDays.size());
    assertTrue(repeatDays.contains(DayOfWeek.MONDAY));
    assertTrue(repeatDays.contains(DayOfWeek.WEDNESDAY));
    assertTrue(repeatDays.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testCreateEventWithMockEvent() throws InvalidEventException {
    String[] args = {"recurring-until", "Mock Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "M",
        "2023-06-15", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    MockEvent mockEvent = new MockEvent("Mock Meeting", LocalDateTime.of(2023, 5, 15, 10, 0),
        LocalDateTime.of(2023, 5, 15, 11, 0), "", "", true);

    assertEquals(mockEvent.getSubject(), event.getSubject());
    assertEquals(mockEvent.getStartDateTime(), event.getStartDateTime());
    assertEquals(mockEvent.getEndDateTime(), event.getEndDateTime());
    assertEquals(mockEvent.getDescription(), event.getDescription());
    assertEquals(mockEvent.getLocation(), event.getLocation());
    assertEquals(mockEvent.isPublic(), event.isPublic());
    assertEquals(mockEvent.isAllDay(), event.isAllDay());
  }

  @Test
  public void testCreateEventWithMockRecurringEvent() throws InvalidEventException {
    String[] args = {"recurring-until", "Recurring Mock Meeting", "2023-05-15T10:00",
        "2023-05-15T11:00", "MWF", "2023-06-15", "false"};
    RecurringUntilEventCreator creator = new RecurringUntilEventCreator(args);
    Event event = creator.createEvent();

    MockRecurringEvent mockRecurringEvent = new MockRecurringEvent("Recurring Mock Meeting",
        LocalDateTime.of(2023, 5, 15, 10, 0), LocalDateTime.of(2023, 5, 15, 11, 0),
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

    assertTrue(event instanceof RecurringEvent);
    assertEquals(mockRecurringEvent.getSubject(), event.getSubject());
    assertEquals(mockRecurringEvent.getStartDateTime(), event.getStartDateTime());
    assertEquals(mockRecurringEvent.getEndDateTime(), event.getEndDateTime());
    assertEquals(mockRecurringEvent.getRepeatDays(), ((RecurringEvent) event).getRepeatDays());
  }
}