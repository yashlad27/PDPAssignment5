package model.calendar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import utilities.CSVExporter;
import utilities.DateTimeUtil;
import utilities.EventPropertyUpdater;

/**
 * Implementation of the ICalendar interface that manages a calendar's events and operations. This
 * class provides comprehensive functionality for managing both single and recurring events,
 * including creation, modification, querying, and conflict detection.
 *
 * <p>Key Features: Manages both single and recurring events, Supports event conflict detection,
 * Provides flexible event querying (by date, range, or custom filters), Handles event property
 * updates, Supports calendar data export to CSV
 *
 * <p>The calendar maintains separate collections for single and recurring events,
 * with UUID-based indexing for efficient event lookup.
 */
public class Calendar implements ICalendar {

  private final List<Event> events;
  private final List<RecurringEvent> recurringEvents;
  private final Map<UUID, Event> eventById;
  private final Map<UUID, RecurringEvent> recurringEventById;
  private String name;
  private String timezone;
  private final Map<String, EventPropertyUpdater> propertyUpdaters;

  /**
   * Constructs a new Calendar instance with default settings. Initializes empty event collections
   * and sets default values: - Name: "Default" - Timezone: "America/New_York" - Empty events and
   * recurring events lists - Empty event ID mappings - Initialized property updaters for event
   * modification
   */
  public Calendar() {
    this.events = new ArrayList<>();
    this.recurringEvents = new ArrayList<>();
    this.eventById = new HashMap<>();
    this.recurringEventById = new HashMap<>();
    this.name = "Default";
    this.timezone = "America/New_York";

    this.propertyUpdaters = new HashMap<>();
    initializePropertyUpdaters();
  }

  /**
   * Adds a single event to the calendar with conflict checking.
   * <p> The method performs the following steps:
   * 1. Validates the event is not null 2. Checks for conflicts with existing events 3. If
   * autoDecline is true, throws exception on conflict 4. If autoDecline is false, returns false on
   * conflict 5. Adds the event to both the events list and ID mapping
   * </p>
   *
   * @param event       The event to add, must not be null
   * @param autoDecline If true, throws exception on conflict; if false, returns false
   * @return true If event was added successfully, false otherwise
   * @throws ConflictingEventException if autoDecline is true and event conflicts with existing
   *                                   events
   */
  @Override
  public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (hasConflict(event)) {
      if (autoDecline) {
        throw new ConflictingEventException(
            "Cannot add event '" + event.getSubject() + "' due to conflict with an existing event");
      }
      return false;
    }

    events.add(event);
    eventById.put(event.getId(), event);
    return true;
  }

  /**
   * Adds a recurring event to the calendar with conflict checking for all occurrences.
   *
   * <p>The method performs the following steps:
   * 1. Validates the recurring event is not null 2. Generates all event occurrences 3. Checks each
   * occurrence for conflicts 4. If autoDecline is true, throws exception on any conflict 5. If
   * autoDecline is false, returns false on any conflict 6. Adds the recurring event and all its
   * occurrences to respective collections
   *
   * @param recurringEvent The recurring event to add, must not be null
   * @param autoDecline    If true, throws exception on conflict; if false, returns false
   * @return true if event was added successfully, false otherwise
   */
  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
      throws ConflictingEventException {
    if (recurringEvent == null) {
      throw new IllegalArgumentException("Recurring event cannot be null");
    }

    List<Event> occurrences = recurringEvent.getAllOccurrences();

    for (Event occurrence : occurrences) {
      if (hasConflict(occurrence)) {
        if (autoDecline) {
          throw new ConflictingEventException(
              "Cannot add recurring event '" + recurringEvent.getSubject()
                  + "' due to conflict with an existing event");
        }
        return false;
      }
    }

    recurringEvents.add(recurringEvent);
    recurringEventById.put(recurringEvent.getId(), recurringEvent);

    for (Event occurrence : occurrences) {
      events.add(occurrence);
      eventById.put(occurrence.getId(), occurrence);
    }

    return true;
  }

  /**
   * Creates a recurring event that repeats on specified weekdays until a given end date.
   *
   * <p>Example weekdays format: "MWF" for Monday, Wednesday, Friday
   * Valid weekday codes: M (Monday), T (Tuesday), W (Wednesday), R (Thursday), F (Friday), S
   * (Saturday), U (Sunday)
   *
   * @param name        Event name/subject
   * @param start       Start date and time of the first occurrence
   * @param end         End date and time of the first occurrence
   * @param weekdays    String specifying which days of the week the event repeats on
   * @param untilDate   The last date on which the event can occur
   * @param autoDecline If true, throws exception on conflict; if false, returns false
   * @return true if event was created successfully
   * @throws ConflictingEventException if autoDecline is true and any occurrence conflicts
   */
  @Override
  public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
      String weekdays, LocalDate untilDate, boolean autoDecline) throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, start, end,
          repeatDays).isPublic(true).endDate(untilDate).build();

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
      int occurrences, boolean autoDecline, String description, String location, boolean isPublic)
      throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay, endOfDay,
          repeatDays).description(description).location(location).isPublic(isPublic)
          .occurrences(occurrences).isAllDay(true).build();

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
      LocalDate untilDate, boolean autoDecline, String description, String location,
      boolean isPublic) throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay, endOfDay,
          repeatDays).description(description).location(location).isPublic(isPublic)
          .endDate(untilDate).isAllDay(true).build();

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Finds an event by its subject and start date/time.
   *
   * <p>This method searches through all events (both single and recurring occurrences)
   * to find an exact match of both subject and start date/time.
   *
   * @param subject       The event subject to search for
   * @param startDateTime The exact start date and time to match
   * @return The matching Event object, or null if no match is found
   * @throws IllegalArgumentException if either subject or startDateTime is null
   */
  @Override
  public Event findEvent(String subject, LocalDateTime startDateTime) {
    if (subject == null || startDateTime == null) {
      throw new IllegalArgumentException("Subject and start date/time cannot be null");
    }

    return events.stream()
        .filter(e -> e.getSubject().equals(subject) && e.getStartDateTime().equals(startDateTime))
        .findFirst().orElse(null);
  }

  /**
   * Retrieves all events in calendar.
   *
   * @return List of all events.
   */
  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Edits a specific event in calendar.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to edit (name, startTime, endTime, etc.)
   * @param newValue      the new value for the property
   * @return true if the operation is successful
   */
  @Override
  public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
      String newValue) {
    Event eventToEdit = findEvent(subject, startDateTime);

    if (eventToEdit == null) {
      return false;
    }

    return updateEventProperty(eventToEdit, property, newValue);
  }

  /**
   * Edits a specific event in calendar for a given date.
   *
   * @param subject       the subject of the recurring events to edit
   * @param startDateTime the start date/time to begin editing from
   * @param property      the property to edit
   * @param newValue      the new value for the property
   * @return the number of events edited
   */
  @Override
  public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
      String newValue) {
    int count = 0;

    List<Event> matchingEvents = events.stream().filter(
        e -> e.getSubject().equals(subject) && !e.getStartDateTime().isBefore(startDateTime))
        .collect(Collectors.toList());

    for (Event event : matchingEvents) {
      if (updateEventProperty(event, property, newValue)) {
        count++;
      }
    }

    return count;
  }

  /**
   * Edits multiple events at once.
   *
   * @param subject  the subject of the events to edit
   * @param property the property to edit
   * @param newValue the new value for the property
   * @return number of occurrences edited
   */
  @Override
  public int editAllEvents(String subject, String property, String newValue) {
    int count = 0;

    List<Event> matchingEvents = events.stream().filter(e -> e.getSubject().equals(subject))
        .collect(Collectors.toList());

    for (Event event : matchingEvents) {
      if (updateEventProperty(event, property, newValue)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Retrieves all recurring events in the calendar.
   *
   * @return a list of all recurring events
   */
  @Override
  public List<RecurringEvent> getAllRecurringEvents() {
    return new ArrayList<>(recurringEvents);
  }

  /**
   * Export all events of the calendar to a CSV file.
   *
   * @param filePath the path where the CSV file should be created
   * @return filePath of exported csv
   * @throws IOException if an I/O error occurs
   */
  @Override
  public String exportToCSV(String filePath) throws IOException {
    return CSVExporter.exportToCSV(filePath, events);
  }

  /**
   * Checks if an event conflicts with any existing event in the calendar.
   *
   * @param event the event to check for conflicts
   * @return true if there is a conflict, false otherwise
   */
  private boolean hasConflict(Event event) {
    return events.stream().anyMatch(event::conflictsWith);
  }

  /**
   * Updates a specific property of an event.
   *
   * @param event    the event to update
   * @param property the property to update
   * @param newValue the new value for the property
   * @return true if the update was successful, otherwise false
   */
  private boolean updateEventProperty(Event event, String property, String newValue) {
    if (property == null || newValue == null) {
      return false;
    }

    EventPropertyUpdater updater = propertyUpdaters.get(property.toLowerCase());
    return updater != null && updater.update(event, newValue);
  }

  @Override
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date cannot be null");
    }

    return getFilteredEvents(event -> {
      if (event.getStartDateTime() != null) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
        LocalDate eventEndDate =
            (event.getEndDateTime() != null) ? event.getEndDateTime().toLocalDate()
                : eventStartDate;

        return !(eventEndDate.isBefore(startDate) || eventStartDate.isAfter(endDate));
      } else if (event.getDate() != null) {
        return !event.getDate().isBefore(startDate) && !event.getDate().isAfter(endDate);
      }
      return false;
    });
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }

    EventFilter busyFilter = event -> {
      if (event.getStartDateTime() != null && event.getEndDateTime() != null) {
        return !dateTime.isBefore(event.getStartDateTime()) && !dateTime.isAfter(
            event.getEndDateTime());
      }

      if (event.getDate() != null) {
        LocalDate targetDate = dateTime.toLocalDate();
        return event.getDate().equals(targetDate);
      }

      return false;
    };

    return events.stream().anyMatch(busyFilter::matches);
  }

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this calendar.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the timezone of this calendar.
   *
   * @return the timezone
   */
  public String getTimezone() {
    return timezone;
  }

  /**
   * Sets the timezone of this calendar.
   *
   * @param timezone the new timezone
   */
  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  /**
   * Initializes the map of property updaters with lambda expressions for each property.
   */
  private void initializePropertyUpdaters() {
    EventPropertyUpdater subjectUpdater = (event, value) -> {
      try {
        event.setSubject(value);
        return true;
      } catch (IllegalArgumentException e) {
        return false;
      }
    };

    propertyUpdaters.put("subject", subjectUpdater);
    propertyUpdaters.put("name", subjectUpdater);

    propertyUpdaters.put("description", (event, value) -> {
      event.setDescription(value);
      return true;
    });

    propertyUpdaters.put("location", (event, value) -> {
      event.setLocation(value);
      return true;
    });

    EventPropertyUpdater startTimeUpdater = (event, value) -> {
      try {
        LocalDateTime newStartTime;
        if (value.contains("T")) {
          newStartTime = DateTimeUtil.parseDateTime(value);
        } else {
          LocalTime newTime = LocalTime.parse(value);
          newStartTime = LocalDateTime.of(event.getStartDateTime().toLocalDate(), newTime);
        }
        event.setStartDateTime(newStartTime);
        return true;
      } catch (Exception e) {
        return false;
      }
    };
    propertyUpdaters.put("start", startTimeUpdater);
    propertyUpdaters.put("starttime", startTimeUpdater);
    propertyUpdaters.put("startdatetime", startTimeUpdater);

    EventPropertyUpdater endTimeUpdater = (event, value) -> {
      try {
        LocalDateTime newEndTime;
        if (value.contains("T")) {
          newEndTime = DateTimeUtil.parseDateTime(value);
        } else {
          LocalTime newTime = LocalTime.parse(value);
          newEndTime = LocalDateTime.of(event.getEndDateTime().toLocalDate(), newTime);
        }
        event.setEndDateTime(newEndTime);
        return true;
      } catch (Exception e) {
        return false;
      }
    };
    propertyUpdaters.put("end", endTimeUpdater);
    propertyUpdaters.put("endtime", endTimeUpdater);
    propertyUpdaters.put("enddatetime", endTimeUpdater);

    EventPropertyUpdater visibilityUpdater = (event, value) -> {
      boolean isPublic = value.equalsIgnoreCase("public") || value.equalsIgnoreCase("true");
      event.setPublic(isPublic);
      return true;
    };
    propertyUpdaters.put("visibility", visibilityUpdater);
    propertyUpdaters.put("ispublic", visibilityUpdater);
    propertyUpdaters.put("public", visibilityUpdater);

    propertyUpdaters.put("private", (event, value) -> {
      boolean isPrivate = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("private");
      event.setPublic(!isPrivate);
      return true;
    });
  }

  /**
   * Gets events that match a specific filter.
   *
   * @param filter the filter to apply
   * @return a list of events that match the filter
   */
  public List<Event> getFilteredEvents(EventFilter filter) {
    return events.stream().filter(filter::matches).collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }

    return getFilteredEvents(event -> {
      if (event.getStartDateTime() != null) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();

        if (event.getEndDateTime() != null) {
          LocalDate eventEndDate = event.getEndDateTime().toLocalDate();
          return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate);
        } else {
          return eventStartDate.equals(date);
        }
      } else if (event.getDate() != null) {
        return event.getDate().equals(date);
      }
      return false;
    });
  }
}