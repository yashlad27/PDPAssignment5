package controller.command.create;

import java.time.LocalDate;
import java.time.LocalDateTime;

import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;
import utilities.DateTimeUtil;

/**
 * Strategy for creating an all-day recurring event with a specific number of occurrences.
 * Extends AbstractEventCreationStrategy to inherit common functionality.
 */
public class AllDayRecurringEventCreationStrategy extends AbstractEventCreationStrategy {

  private final String eventName;
  private final LocalDate date;
  private final String weekdays;
  private final int occurrences;
  private final boolean autoDecline;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Constructs a strategy for creating an all-day recurring event.
   *
   * @param args the arguments for event creation
   */
  public AllDayRecurringEventCreationStrategy(String[] args) {
    if (args.length < 6) {
      throw new IllegalArgumentException("Insufficient arguments for all-day recurring event");
    }

    try {
      this.eventName = args[1];
      this.date = DateTimeUtil.parseDate(args[2]);
      this.weekdays = args[3];
      this.occurrences = Integer.parseInt(args[4]);
      this.autoDecline = Boolean.parseBoolean(args[5]);

      this.description = args.length > 6 ? removeQuotes(args[6]) : null;
      this.location = args.length > 7 ? removeQuotes(args[7]) : null;
      this.isPublic = args.length > 8 ? Boolean.parseBoolean(args[8]) : true;
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
    if (occurrences <= 0) {
      throw new InvalidEventException("Occurrences must be positive");
    }

    // For all-day recurring events, we need to delegate to the calendar
    // because the model doesn't provide a direct way to create them
    return null;
  }

  @Override
  public String executeCreation(ICalendar calendar) throws ConflictingEventException, InvalidEventException {
    validateEventParameters(eventName);

    if (date == null) {
      throw new InvalidEventException("Date cannot be null");
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      throw new InvalidEventException("Weekdays cannot be empty");
    }
    if (occurrences <= 0) {
      throw new InvalidEventException("Occurrences must be positive");
    }

    try {
      boolean success = calendar.createAllDayRecurringEvent(
              eventName, date, weekdays, occurrences,
              autoDecline, description, location, isPublic);

      if (!success) {
        throw new InvalidEventException("Failed to create all-day recurring event");
      }

      return getSuccessMessage(null);
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidEventException("Error creating all-day recurring event: " + e.getMessage());
    }
  }

  @Override
  protected boolean getAutoDecline() {
    return autoDecline;
  }

  @Override
  protected String getSuccessMessage(Event event) {
    return "All-day recurring event '" + eventName + "' created successfully with "
            + occurrences + " occurrences.";
  }
}