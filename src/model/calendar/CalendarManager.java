package model.calendar;

import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages multiple calendars with unique names and timezones.
 * Provides functionality to create, access, modify, and switch between calendars.
 */
public class CalendarManager {

  private final Map<String, Calendar> calendars;
  private final TimeZoneHandler timezoneHandler;
  private String activeCalendarName;

  /**
   * Private constructor used by the builder to create a CalendarManager instance.
   */
  private CalendarManager(Builder builder) {
    this.calendars = new HashMap<>();
    this.timezoneHandler = builder.timezoneHandler;
    this.activeCalendarName = null;
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
      // Default timezone handler
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
    // Validate name
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }

    // Check for duplicate name
    if (calendars.containsKey(name)) {
      throw new DuplicateCalendarException("Calendar with name '" + name + "' already exists");
    }

    // Validate timezone
    if (!timezoneHandler.isValidTimezone(timezone)) {
      throw new InvalidTimezoneException("Invalid timezone: " + timezone);
    }

    // Create the calendar with the specified timezone
    Calendar calendar = new Calendar();
    calendar.setName(name);
    calendar.setTimezone(timezone);

    // Add to map
    calendars.put(name, calendar);

    // If this is the first calendar, make it active
    if (activeCalendarName == null) {
      activeCalendarName = name;
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
    if (!calendars.containsKey(name)) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    return calendars.get(name);
  }

  /**
   * Gets the currently active calendar.
   *
   * @return the active calendar
   * @throws CalendarNotFoundException if no calendar is currently active
   */
  public Calendar getActiveCalendar() throws CalendarNotFoundException {
    if (activeCalendarName == null) {
      throw new CalendarNotFoundException("No active calendar set");
    }
    return calendars.get(activeCalendarName);
  }

  /**
   * Sets the active calendar by name.
   *
   * @param name the name of the calendar to set as active
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public void setActiveCalendar(String name) throws CalendarNotFoundException {
    if (!calendars.containsKey(name)) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    activeCalendarName = name;
  }

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the name of the active calendar, or null if no calendar is active
   */
  public String getActiveCalendarName() {
    return activeCalendarName;
  }

  /**
   * Checks if the specified calendar name exists.
   *
   * @param name the calendar name to check
   * @return true if a calendar with the specified name exists, false otherwise
   */
  public boolean hasCalendar(String name) {
    return calendars.containsKey(name);
  }

  /**
   * Gets all calendar names.
   *
   * @return a set of all calendar names
   */
  public Set<String> getCalendarNames() {
    return calendars.keySet();
  }

  /**
   * Gets the number of calendars.
   *
   * @return the number of calendars
   */
  public int getCalendarCount() {
    return calendars.size();
  }

  /**
   * Edits a calendar's name.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws CalendarNotFoundException   if no calendar with the specified name exists
   * @throws DuplicateCalendarException if a calendar with the new name already exists
   */
  public void editCalendarName(String oldName, String newName)
          throws CalendarNotFoundException, DuplicateCalendarException {
    // Validate parameters
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("New calendar name cannot be null or empty");
    }

    // Check if old name exists
    if (!calendars.containsKey(oldName)) {
      throw new CalendarNotFoundException("Calendar not found: " + oldName);
    }

    // Check if new name already exists
    if (!oldName.equals(newName) && calendars.containsKey(newName)) {
      throw new DuplicateCalendarException("Calendar with name '" + newName + "' already exists");
    }

    // Get the calendar
    Calendar calendar = calendars.get(oldName);

    // Update name in calendar object
    calendar.setName(newName);

    // Remove from map with old name and add with new name
    calendars.remove(oldName);
    calendars.put(newName, calendar);

    // Update active calendar name if necessary
    if (oldName.equals(activeCalendarName)) {
      activeCalendarName = newName;
    }
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

    // Check if calendar exists
    if (!calendars.containsKey(calendarName)) {
      throw new CalendarNotFoundException("Calendar not found: " + calendarName);
    }

    // Get the calendar and update timezone
    Calendar calendar = calendars.get(calendarName);
    calendar.setTimezone(newTimezone);
  }

  /**
   * Removes a calendar.
   *
   * @param name the name of the calendar to remove
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public void removeCalendar(String name) throws CalendarNotFoundException {
    if (!calendars.containsKey(name)) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }

    calendars.remove(name);

    // Update active calendar if necessary
    if (name.equals(activeCalendarName)) {
      if (!calendars.isEmpty()) {
        activeCalendarName = calendars.keySet().iterator().next();
      } else {
        activeCalendarName = null;
      }
    }
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