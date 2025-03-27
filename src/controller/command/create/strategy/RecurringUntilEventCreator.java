package controller.command.create.strategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Strategy for creating a recurring event that repeats until a specific date.
 * Extends AbstractEventCreator to inherit common functionality.
 */
public class RecurringUntilEventCreator extends AbstractEventCreator {

  private final String eventName;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final Set<DayOfWeek> repeatDays;
  private final LocalDate untilDate;
  private final boolean autoDecline;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Constructs a strategy for creating a recurring event that repeats until a specific date.
   *
   * @param args the arguments for event creation
   */
  public RecurringUntilEventCreator(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 7) {
      throw new IllegalArgumentException("Insufficient arguments for recurring event until date");
    }

    try {
      this.eventName = args[1];
      this.startDateTime = DateTimeUtil.parseDateTime(args[2]);
      this.endDateTime = DateTimeUtil.parseDateTime(args[3]);
      String weekdays = args[4];
      this.repeatDays = DateTimeUtil.parseWeekdays(args[4]);
      this.untilDate = DateTimeUtil.parseDate(args[5]);
      this.autoDecline = Boolean.parseBoolean(args[6]);

      this.description = args.length > 7 ? removeQuotes(args[7]) : null;
      this.location = args.length > 8 ? removeQuotes(args[8]) : null;
      this.isPublic = args.length > 9 ? Boolean.parseBoolean(args[9]) : true;
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
    if (repeatDays == null || repeatDays.isEmpty()) {
      throw new InvalidEventException("Repeat days cannot be empty");
    }
    if (untilDate == null) {
      throw new InvalidEventException("Until date cannot be null");
    }

    try {
      return new RecurringEvent.Builder(
              eventName, startDateTime, endDateTime, repeatDays)
              .description(description)
              .location(location)
              .isPublic(isPublic)
              .endDate(untilDate)
              .build();
    } catch (IllegalArgumentException e) {
      throw new InvalidEventException(e.getMessage());
    }
  }

  @Override
  protected boolean getAutoDecline() {
    return autoDecline;
  }

  @Override
  protected String getSuccessMessage(Event event) {
    return "Recurring event '" + eventName + "' created successfully, repeating until "
            + DateTimeUtil.formatDate(untilDate) + ".";
  }
}