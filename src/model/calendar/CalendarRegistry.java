package model.calendar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;

/**
 * Manages registration and retrieval of calendars by name.
 * This class is responsible for storing calendars and providing
 * access to them by name, following the Single Responsibility Principle.
 */
public class CalendarRegistry {

  private final Map<String, Calendar> calendars;
  private String activeCalendarName;

  /**
   * Constructs a new CalendarRegistry with no calendars.
   */
  public CalendarRegistry() {
    this.calendars = new HashMap<>();
    this.activeCalendarName = null;
  }

  /**
   * Gets a calendar by name.
   *
   * @param name the name of the calendar
   * @return the calendar with the specified name
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public Calendar getCalendarByName(String name) throws CalendarNotFoundException {
    if (!calendars.containsKey(name)) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    return calendars.get(name);
  }

  /**
   * Registers a calendar with the specified name.
   *
   * @param name     the unique name for the calendar
   * @param calendar the calendar to register
   * @throws DuplicateCalendarException if a calendar with the specified name already exists
   */
  public void registerCalendar(String name, Calendar calendar)
          throws DuplicateCalendarException {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }

    if (calendars.containsKey(name)) {
      throw new DuplicateCalendarException("Calendar with name '" + name + "' already exists");
    }

    calendars.put(name, calendar);

    if (activeCalendarName == null) {
      activeCalendarName = name;
    }
  }

  /**
   * Removes a calendar from the registry.
   *
   * @param name the name of the calendar to remove
   * @throws CalendarNotFoundException if no calendar with the specified name exists
   */
  public void removeCalendar(String name) throws CalendarNotFoundException {
    if (!calendars.containsKey(name)) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }

    calendars.remove(name);

    if (name.equals(activeCalendarName)) {
      if (!calendars.isEmpty()) {
        activeCalendarName = calendars.keySet().iterator().next();
      } else {
        activeCalendarName = null;
      }
    }
  }

  /**
   * Renames a calendar in the registry.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws CalendarNotFoundException  if no calendar with the specified name exists
   * @throws DuplicateCalendarException if a calendar with the new name already exists
   */
  public void renameCalendar(String oldName, String newName)
          throws CalendarNotFoundException, DuplicateCalendarException {
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("New calendar name cannot be null or empty");
    }

    if (!calendars.containsKey(oldName)) {
      throw new CalendarNotFoundException("Calendar not found: " + oldName);
    }

    if (!oldName.equals(newName) && calendars.containsKey(newName)) {
      throw new DuplicateCalendarException("Calendar with name '" + newName + "' already exists");
    }

    Calendar calendar = calendars.get(oldName);

    calendar.setName(newName);

    calendars.remove(oldName);
    calendars.put(newName, calendar);

    if (oldName.equals(activeCalendarName)) {
      activeCalendarName = newName;
    }
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
   * Applies a consumer to a calendar by name.
   *
   * @param calendarName the name of the calendar
   * @param consumer     the consumer to apply
   * @throws CalendarNotFoundException if the calendar cannot be found
   */
  public void applyToCalendar(String calendarName, Consumer<Calendar> consumer)
          throws CalendarNotFoundException {
    Calendar calendar = getCalendarByName(calendarName);
    consumer.accept(calendar);
  }

  /**
   * Applies a consumer to the active calendar.
   *
   * @param consumer the consumer to apply
   * @throws CalendarNotFoundException if there is no active calendar
   */
  public void applyToActiveCalendar(Consumer<Calendar> consumer)
          throws CalendarNotFoundException {
    Calendar calendar = getActiveCalendar();
    consumer.accept(calendar);
  }
}