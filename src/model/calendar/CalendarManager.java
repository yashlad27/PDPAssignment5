package model.calendar;

import java.util.Set;
import java.util.function.Consumer;

import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;

/**
 * Manages calendar operations and coordinates between the CalendarRegistry and TimeZoneHandler.
 * Following the Single Responsibility Principle, this class is focused on high-level
 * calendar management operations rather than storage details.
 */
public class CalendarManager {

  private final CalendarRegistry calendarRegistry;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Private constructor used by the builder to create a CalendarManager instance.
   */
  protected CalendarManager(Builder builder) {
    this.calendarRegistry = new CalendarRegistry();
    this.timezoneHandler = builder.timezoneHandler;
  }

  /**
   * Builder class for creating CalendarManager instances.
   */
  public static class Builder {
    private TimeZoneHandler timezoneHandler;

    /**
     * Constructor for Builder.
     */
    public Builder() {
      this.timezoneHandler = new TimeZoneHandler();
    }

    /**
     * Sets the timezone handler to use.
     *
     * @param timezoneHandler the timezone handler
     * @return the builder instance
     */
    public Builder timezoneHandler(TimeZoneHandler timezoneHandler) {
      this.timezoneHandler = timezoneHandler;
      return this;
    }

    /**
     * Builds a new CalendarManager with the configured parameters.
     *
     * @return a new CalendarManager instance
     */
    public CalendarManager build() {
      return new CalendarManager(this);
    }
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the unique name for the calendar
   * @param timezone the timezone for the calendar
   * @return the newly created calendar
   * @throws DuplicateCalendarException if a calendar with the specified name already exists
   * @throws InvalidTimezoneException   if the timezone is invalid
   */
  public Calendar createCalendar(String name, String timezone)
          throws DuplicateCalendarException, InvalidTimezoneException {
    // Validate timezone
    if (!timezoneHandler.isValidTimezone(timezone)) {
      throw new InvalidTimezoneException("Invalid timezone: " + timezone);
    }

    // Create the calendar with the specified timezone
    Calendar calendar = new Calendar();
    calendar.setName(name);
    calendar.setTimezone(timezone);

    // Register the calendar
    try {
      calendarRegistry.registerCalendar(name, calendar);
    } catch (DuplicateCalendarException e) {
      throw e;
    }

    return calendar;
  }

  /**
   * Creates a new calendar with the specified name and default timezone.
   *
   * @param name the unique name for the calendar
   * @return the newly created calendar
   * @throws DuplicateCalendarException if a calendar with the specified name already exists
   */
  public Calendar createCalendarWithDefaultTimezone(String name) throws DuplicateCalendarException {
    try {
      return createCalendar(name, timezoneHandler.getDefaultTimezone());
    } catch (InvalidTimezoneException e) {
      // This should never happen with the default timezone
      throw new RuntimeException("Invalid default timezone", e);
    }
  }

  /**
   * Gets a calendar by name.
   *
   * @param name the name of the calendar
   * @return the calendar with the specified name
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public Calendar getCalendar(String name) throws CalendarNotFoundException {
    return calendarRegistry.getCalendarByName(name);
  }

  /**
   * Gets the currently active calendar.
   *
   * @return the active calendar
   * @throws CalendarNotFoundException if no calendar is currently active
   */
  public Calendar getActiveCalendar() throws CalendarNotFoundException {
    return calendarRegistry.getActiveCalendar();
  }

  /**
   * Executes an operation on a calendar by name and returns a result.
   *
   * @param <T>          the result type
   * @param calendarName the name of the calendar
   * @param operation    the operation to execute
   * @return the result of the operation
   * @throws CalendarNotFoundException if the calendar cannot be found
   * @throws Exception                 if the operation throws an exception
   */
  public <T> T executeOnCalendar(String calendarName, CalendarOperation<T> operation)
          throws CalendarNotFoundException, Exception {
    Calendar calendar = calendarRegistry.getCalendarByName(calendarName);
    return operation.execute(calendar);
  }

  /**
   * Executes an operation on the active calendar and returns a result.
   *
   * @param <T>       the result type
   * @param operation the operation to execute
   * @return the result of the operation
   * @throws CalendarNotFoundException if there is no active calendar
   * @throws Exception                 if the operation throws an exception
   */
  public <T> T executeOnActiveCalendar(CalendarOperation<T> operation)
          throws CalendarNotFoundException, Exception {
    Calendar calendar = calendarRegistry.getActiveCalendar();
    return operation.execute(calendar);
  }

  /**
   * Applies a consumer to a calendar by name.
   *
   * @param calendarName the name of the calendar
   * @param consumer     the consumer to apply
   * @throws CalendarNotFoundException if the calendar cannot be found
   */
  public void applyToCalendar(String calendarName, Consumer<Calendar> consumer)
          throws CalendarNotFoundException {
    calendarRegistry.applyToCalendar(calendarName, consumer);
  }

  /**
   * Applies a consumer to the active calendar.
   *
   * @param consumer the consumer to apply
   * @throws CalendarNotFoundException if there is no active calendar
   */
  public void applyToActiveCalendar(Consumer<Calendar> consumer)
          throws CalendarNotFoundException {
    calendarRegistry.applyToActiveCalendar(consumer);
  }

  /**
   * Sets the active calendar by name.
   *
   * @param name the name of the calendar to set as active
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public void setActiveCalendar(String name) throws CalendarNotFoundException {
    calendarRegistry.setActiveCalendar(name);
  }

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the name of the active calendar, or null if no calendar is active
   */
  public String getActiveCalendarName() {
    return calendarRegistry.getActiveCalendarName();
  }

  /**
   * Checks if the specified calendar name exists.
   *
   * @param name the calendar name to check
   * @return true if a calendar with the specified name exists, false otherwise
   */
  public boolean hasCalendar(String name) {
    return calendarRegistry.hasCalendar(name);
  }

  /**
   * Gets all calendar names.
   *
   * @return a set of all calendar names
   */
  public Set<String> getCalendarNames() {
    return calendarRegistry.getCalendarNames();
  }

  /**
   * Gets the number of calendars.
   *
   * @return the number of calendars
   */
  public int getCalendarCount() {
    return calendarRegistry.getCalendarCount();
  }

  /**
   * Edits a calendar's name.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws CalendarNotFoundException  if no calendar with the specified name exists
   * @throws DuplicateCalendarException if a calendar with the new name already exists
   */
  public void editCalendarName(String oldName, String newName)
          throws CalendarNotFoundException, DuplicateCalendarException {
    calendarRegistry.renameCalendar(oldName, newName);
  }

  /**
   * Edits a calendar's timezone.
   *
   * @param calendarName the name of the calendar
   * @param newTimezone  the new timezone for the calendar
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   * @throws InvalidTimezoneException  if the timezone is invalid
   */
  public void editCalendarTimezone(String calendarName, String newTimezone)
          throws CalendarNotFoundException, InvalidTimezoneException {
    // Validate timezone
    if (!timezoneHandler.isValidTimezone(newTimezone)) {
      throw new InvalidTimezoneException("Invalid timezone: " + newTimezone);
    }

    // Update timezone
    calendarRegistry.applyToCalendar(calendarName, calendar -> calendar.setTimezone(newTimezone));
  }

  /**
   * Removes a calendar.
   *
   * @param name the name of the calendar to remove
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public void removeCalendar(String name) throws CalendarNotFoundException {
    calendarRegistry.removeCalendar(name);
  }

  /**
   * Gets the timezone handler.
   *
   * @return the timezone handler
   */
  public TimeZoneHandler getTimezoneHandler() {
    return timezoneHandler;
  }
}