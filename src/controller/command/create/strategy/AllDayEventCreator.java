package controller.command.create.strategy;

import java.time.LocalDate;

import model.event.Event;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Strategy for creating an all-day event.
 * Extends AbstractEventCreator to inherit common functionality.
 */
public class AllDayEventCreator extends AbstractEventCreator {

  private final String eventName;
  private final LocalDate date;
  private final boolean autoDecline;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Constructs a strategy for creating an all-day event.
   *
   * @param args the arguments for event creation
   * @throws IllegalArgumentException if args is null or has insufficient arguments
   */
  public AllDayEventCreator(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 4) {
      throw new IllegalArgumentException("Insufficient arguments for creating an all-day event");
    }

    try {
      this.eventName = args[1];
      this.date = DateTimeUtil.parseDate(args[2]);
      this.autoDecline = Boolean.parseBoolean(args[3]);

      this.description = args.length > 4 ? removeQuotes(args[4]) : null;
      this.location = args.length > 5 ? removeQuotes(args[5]) : null;
      this.isPublic = args.length > 6 ? Boolean.parseBoolean(args[6]) : true;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  @Override
  public Event createEvent() throws InvalidEventException {
    validateEventParameters(eventName);

    if (date == null) {
      throw new InvalidEventException("Date cannot be null");
    }

    return Event.createAllDayEvent(
            eventName, date, description, location, isPublic);
  }

  @Override
  protected boolean getAutoDecline() {
    return autoDecline;
  }

  @Override
  protected String getSuccessMessage(Event event) {
    return "All-day event '" + eventName + "' created successfully.";
  }
}