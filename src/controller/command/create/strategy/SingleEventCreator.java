package controller.command.create.strategy;

import java.time.LocalDateTime;

import model.event.Event;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Strategy for creating a single event.
 * Extends AbstractEventCreator to inherit common functionality.
 */
public class SingleEventCreator extends AbstractEventCreator {

  private final String eventName;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean autoDecline;

  /**
   * Constructs a strategy for creating a single event.
   *
   * @param args the arguments for event creation
   */
  public SingleEventCreator(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 4) {
      throw new IllegalArgumentException("Insufficient arguments for creating a single event");
    }

    try {
      this.eventName = args[1];
      this.startDateTime = DateTimeUtil.parseDateTime(args[2]);
      this.endDateTime = DateTimeUtil.parseDateTime(args[3]);

      this.description = args.length > 4 ? removeQuotes(args[4]) : null;
      this.location = args.length > 5 ? removeQuotes(args[5]) : null;
      this.isPublic = args.length > 6 ? Boolean.parseBoolean(args[6]) : true;
      this.autoDecline = args.length > 7 ? Boolean.parseBoolean(args[7]) : false;
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing arguments: " + e.getMessage(), e);
    }
  }

  @Override
  public Event createEvent() throws InvalidEventException {
    validateEventParameters(eventName);

    if (startDateTime == null) {
      throw new InvalidEventException("Start date/time cannot be null");
    }

    return new Event(
            eventName,
            startDateTime,
            endDateTime,
            description,
            location,
            isPublic
    );
  }

  @Override
  protected boolean getAutoDecline() {
    return autoDecline;
  }

  @Override
  protected String getSuccessMessage(Event event) {
    return "Event '" + eventName + "' created successfully.";
  }
}