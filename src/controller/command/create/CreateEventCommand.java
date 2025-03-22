package controller.command.create;

import controller.command.ICommand;
import model.calendar.ICalendar;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;

/**
 * Command for creating calendar events using the Strategy pattern.
 */
public class CreateEventCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Constructs a new CreateEventCommand with the specified calendar.
   *
   * @param calendar the calendar in which to create events
   */
  public CreateEventCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  /**
   * Executes the create event command with the provided arguments.
   * Uses the strategy pattern to delegate event creation to the appropriate strategy.
   *
   * @param args command arguments, where args[0] is the event type
   * @return a message indicating the result of the operation
   */
  @Override
  public String execute(String[] args) {
    if (args.length < 1) {
      return "Error: Insufficient arguments for create command";
    }

    String eventType = args[0];

    try {
      // Get the appropriate strategy for this event type
      EventCreator creator = EventCreator.forType(eventType, args);

      // Execute the strategy to create and add the event
      return creator.executeCreation(calendar);

    } catch (InvalidEventException e) {
      return "Error: " + e.getMessage();
    } catch (ConflictingEventException e) {
      return "Error: Event conflicts with an existing event - " + e.getMessage();
    } catch (IllegalArgumentException e) {
      return "Error in command arguments: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "create";
  }
}