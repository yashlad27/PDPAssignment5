package model.calendar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.event.Event;
import model.event.RecurringEvent;

/**
 * Interface for a calendar that can manage events.
 */
public interface ICalendar {

  /**
   * Adds a single event to the calendar.
   *
   * @param event       the event to add
   * @param autoDecline if true, the addition will be declined if it conflicts with existing events
   * @return true if the event was added, false if it was declined due to conflicts
   * @throws IllegalArgumentException if the event is null
   */
  boolean addEvent(Event event, boolean autoDecline);

  /**
   * Adds a recurring event to the calendar.
   *
   * @param recurringEvent the recurring event to add
   * @param autoDecline    if true, the addition will be declined if any occurrence conflicts
   * @return true if the event was added, false if it was declined due to conflicts
   * @throws IllegalArgumentException if the recurring event is null
   */
  boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline);

  /**
   * Creates a recurring event that repeats until a specific date.
   *
   * @param name        the name of the event
   * @param start       the start date and time
   * @param end         the end date and time
   * @param weekdays    the days of the week to repeat on (e.g., "MWF")
   * @param untilDate   the date until which to repeat (inclusive)
   * @param autoDecline whether to automatically decline if there's a conflict
   * @return true if the event was created successfully, false otherwise
   */
  boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
      String weekdays, LocalDate untilDate, boolean autoDecline);

  /**
   * Creates an all-day recurring event.
   *
   * @param name        the name of the event
   * @param date        the date of the event
   * @param weekdays    the days of the week to repeat on (e.g., "MWF")
   * @param occurrences the number of occurrences
   * @param autoDecline whether to automatically decline if there's a conflict
   * @return true if the event was created successfully, false otherwise
   */
  boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays, int occurrences,
      boolean autoDecline, String description, String location, boolean isPublic);

  /**
   * Creates an all-day recurring event that repeats until a specific date.
   *
   * @param name        the name of the event
   * @param date        the date of the event
   * @param weekdays    the days of the week to repeat on (e.g., "MWF")
   * @param untilDate   the date until which to repeat (inclusive)
   * @param autoDecline whether to automatically decline if there's a conflict
   * @return true if the event was created successfully, false otherwise
   */
  boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
      LocalDate untilDate, boolean autoDecline, String description, String location,
      boolean isPublic);

  /**
   * Gets all events occurring on a specific date.
   *
   * @param date the date to query
   * @return a list of events on the given date
   * @throws IllegalArgumentException if the date is null
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Gets all events occurring within a date range (inclusive).
   *
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   * @return a list of events within the given date range
   * @throws IllegalArgumentException if any date is null or if endDate is before startDate
   */
  List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate);

  /**
   * Checks if there are any events at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if there is at least one event at the given date and time, false otherwise
   * @throws IllegalArgumentException if the dateTime is null
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Finds an event by its subject and start date/time.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @return the matching event, or null if not found
   * @throws IllegalArgumentException if any argument is null
   */
  Event findEvent(String subject, LocalDateTime startDateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events in the calendar
   */
  List<Event> getAllEvents();

  /**
   * Edits a single event identified by subject and start date/time.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to edit (name, startTime, endTime, etc.)
   * @param newValue      the new value for the property
   * @return true if the event was found and edited, false otherwise
   */
  boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
      String newValue);

  /**
   * Edits all events in a recurring series starting from a specific date.
   *
   * @param subject       the subject of the recurring events to edit
   * @param startDateTime the start date/time to begin editing from
   * @param property      the property to edit
   * @param newValue      the new value for the property
   * @return the number of events that were edited
   */
  int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
      String newValue);

  /**
   * Edits all events with a specific subject.
   *
   * @param subject  the subject of the events to edit
   * @param property the property to edit
   * @param newValue the new value for the property
   * @return the number of events that were edited
   */
  int editAllEvents(String subject, String property, String newValue);

  /**
   * Gets all recurring events in the calendar.
   *
   * @return a list of all recurring events in the calendar
   */
  List<RecurringEvent> getAllRecurringEvents();

  /**
   * Exports the calendar to a CSV file.
   *
   * @param filePath the path where the CSV file should be created
   * @return true if the export was successful, false otherwise
   * @throws IllegalArgumentException if the filePath is null or empty
   */
  String exportToCSV(String filePath) throws IOException;
}