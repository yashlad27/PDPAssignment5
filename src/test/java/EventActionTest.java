import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.event.Event;
import model.event.EventAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the EventAction interface.
 */
public class EventActionTest {

  private List<Event> events;
  private Event regularEvent;
  private Event allDayEvent;
  private Event multiDayEvent;

  @Before
  public void setUp() {
    events = new ArrayList<>();

    // Regular event
    regularEvent = new Event("Team Meeting",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 30),
            "Weekly team sync", "Conference Room A", true);

    // All-day event
    allDayEvent = Event.createAllDayEvent("Company Holiday",
            LocalDate.of(2023, 5, 29),
            "Memorial Day", null, true);

    // Multi-day event
    multiDayEvent = new Event("Conference",
            LocalDateTime.of(2023, 6, 1, 9, 0),
            LocalDateTime.of(2023, 6, 3, 17, 0),
            "Annual tech conference", "Convention Center", true);

    events.add(regularEvent);
    events.add(allDayEvent);
    events.add(multiDayEvent);
  }

  @Test
  public void testPrintEventAction() {
    EventAction printAction = event -> {
      assertTrue("Event should not be null", event != null);
      assertTrue("Event should have a subject", event.getSubject() != null && !event.getSubject().isEmpty());
    };

    printAction.execute(regularEvent);

    printAction.executeOnList(events);
  }

  @Test
  public void testModifyEventAction() {
    EventAction modifyAction = event -> {
      if (event != null) {
        event.setSubject(event.getSubject() + " (Modified)");
        event.setDescription(event.getDescription() + " (Updated)");
      }
    };

    String originalSubject = regularEvent.getSubject();
    String originalDesc = regularEvent.getDescription();

    modifyAction.execute(regularEvent);

    assertEquals("Event subject should be modified", originalSubject + " (Modified)", regularEvent.getSubject());
    assertEquals("Event description should be modified", originalDesc + " (Updated)", regularEvent.getDescription());

    modifyAction.executeOnList(events);
    assertTrue("All events should have modified subjects",
            events.stream().allMatch(e -> e.getSubject().endsWith(" (Modified)")));
  }

  @Test
  public void testNullEventAction() {
    EventAction nullCheckAction = event -> {
      assertTrue("Event should not be null", event != null);
    };

    try {
      nullCheckAction.execute(null);
      assertFalse("Should have thrown an assertion error", true);
    } catch (AssertionError e) {
      // Expected behavior
    }
  }

  @Test
  public void testEventValidationAction() {
    EventAction validationAction = event -> {
      if (event != null) {
        assertTrue("Event should have valid start time", event.getStartDateTime() != null);
        assertTrue("Event should have valid end time", event.getEndDateTime() != null);
        if (!event.isAllDay()) {
          assertTrue("End time should be after start time",
                  event.getEndDateTime().isAfter(event.getStartDateTime()));
        }
      }
    };

    validationAction.execute(regularEvent);

    validationAction.executeOnList(events);
  }

  @Test
  public void testActionComposition() {
    List<String> actionLog = new ArrayList<>();

    EventAction firstAction = event -> actionLog.add("First action on " + event.getSubject());
    EventAction secondAction = event -> actionLog.add("Second action on " + event.getSubject());

    EventAction composedAction = firstAction.andThen(secondAction);

    composedAction.execute(regularEvent);

    assertEquals("Should have two log entries", 2, actionLog.size());
    assertTrue("First action should be logged", actionLog.get(0).startsWith("First action"));
    assertTrue("Second action should be logged", actionLog.get(1).startsWith("Second action"));
  }

  @Test
  public void testEmptyListAction() {
    EventAction anyAction = event -> {
      // Do nothing
    };

    // Should not throw any exception
    anyAction.executeOnList(new ArrayList<>());
  }

  @Test
  public void testStaticFactoryMethods() {
    String newSubject = "New Subject";
    EventAction.setSubject(newSubject).execute(regularEvent);
    assertEquals("Subject should be updated", newSubject, regularEvent.getSubject());

    String newDescription = "New Description";
    EventAction.setDescription(newDescription).execute(regularEvent);
    assertEquals("Description should be updated", newDescription, regularEvent.getDescription());

    String newLocation = "New Location";
    EventAction.setLocation(newLocation).execute(regularEvent);
    assertEquals("Location should be updated", newLocation, regularEvent.getLocation());

    LocalDateTime newEnd = LocalDateTime.of(2023, 5, 15, 12, 0);
    EventAction.setEndDateTime(newEnd).execute(regularEvent);
    assertEquals("End time should be updated", newEnd, regularEvent.getEndDateTime());

    LocalDateTime newStart = LocalDateTime.of(2023, 5, 15, 11, 0);
    EventAction.setStartDateTime(newStart).execute(regularEvent);
    assertEquals("Start time should be updated", newStart, regularEvent.getStartDateTime());
  }
}