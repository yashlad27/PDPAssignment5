package controller.command.create.strategy;

import java.time.LocalDate;

import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Strategy for creating an all-day recurring event that repeats until a specific date.
 * Extends AbstractEventCreator to inherit common functionality.
 */
public class AllDayRecurringUntilEventCreator extends AbstractEventCreator {

  private final String eventName;
  private final LocalDate date;
  private final String weekdays;
  private final LocalDate untilDate;
  private final boolean autoDecline;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Constructs a strategy for creating an all-day recurring event that repeats until a specific
   * date.
   *
   * @param args the arguments for event creation
   */
  public AllDayRecurringUntilEventCreator(String[] args) {
    if (args == null) {
      throw new IllegalArgumentException("Arguments array cannot be null");
    }
    if (args.length < 6) {
      throw new IllegalArgumentException("Insufficient arguments for all-day recurring event "
              + "until date");
    }

    try {
      this.eventName = args[1];
      this.date = DateTimeUtil.parseDate(args[2]);
      this.weekdays = args[3];
      this.untilDate = DateTimeUtil.parseDate(args[4]);
      this.autoDecline = Boolean.parseBoolean(args[5]);

      this.description = args.length > 6 ? removeQuotes(args[6]) : null;
      this.location = args.length > 7 ? removeQuotes(args[7]) : null;
      this.isPublic = args.length > 8 ? Boolean.parseBoolean(args[8]) : true;

      if (untilDate != null && date != null && untilDate.isBefore(date)) {
        throw new IllegalArgumentException("Error parsing arguments: "
                + "Until date must be after start date");
      }
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
    if (weekdays == null || weekdays.trim().isEmpty()) {
      throw new InvalidEventException("Weekdays cannot be empty");
    }
    if (untilDate == null) {
      throw new InvalidEventException("Until date cannot be null");
    }
    return null;
  }

  @Override
  public String executeCreation(ICalendar calendar) throws ConflictingEventException,
          InvalidEventException {
    validateEventParameters(eventName);

    if (date == null) {
      throw new InvalidEventException("Date cannot be null");
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      throw new InvalidEventException("Weekdays cannot be empty");
    }
    if (untilDate == null) {
      throw new InvalidEventException("Until date cannot be null");
    }

    try {
      boolean success = calendar.createAllDayRecurringEventUntil(
              eventName, date, weekdays, untilDate,
              autoDecline, description, location, isPublic);

      if (!success) {
        throw new InvalidEventException("Failed to create all-day recurring event until date");
      }

      return getSuccessMessage(null);
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidEventException("Error creating all-day recurring event until date: "
              + e.getMessage());
    }
  }

  @Override
  protected boolean getAutoDecline() {
    return autoDecline;
  }

  @Override
  protected String getSuccessMessage(Event event) {
    return "All-day recurring event '" + eventName + "' created successfully until "
            + DateTimeUtil.formatDate(untilDate) + ".";
  }
}