package model.calendar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
 * Implementation of the ICalendar interface that manages calendar events.
 * Each calendar has its own timezone context.
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
   * Constructs a new Calendar with no events.
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
   * Constructs a new Calendar with a specific name and timezone.
   *
   * @param name     the name of the calendar
   * @param timezone the timezone of the calendar
   */
  public Calendar(String name, String timezone) {
    this();
    this.name = name;
    this.timezone = timezone;
  }

  /**
   * method to add event to the calendar.
   *
   * @param event       the event to add
   * @param autoDecline if true, the addition will be declined if it conflicts with existing events
   * @return true if the event was added successfully, false otherwise
   */
  @Override
  public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    // Always check for conflicts regardless of autoDecline flag
    if (hasConflict(event)) {
      if (autoDecline) {
        throw new ConflictingEventException("Cannot add event '" + event.getSubject() +
                "' due to conflict with an existing event");
      }
      return false;
    }

    events.add(event);
    eventById.put(event.getId(), event);
    return true;
  }

  /**
   * Method to add recurring event to the calendar.
   *
   * @param recurringEvent the recurring event to add
   * @param autoDecline    if true, the addition will be declined if any occurrence conflicts
   * @return true if the recurring event was added successfully, false otherwise
   */
  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
          throws ConflictingEventException {
    if (recurringEvent == null) {
      throw new IllegalArgumentException("Recurring event cannot be null");
    }

    List<Event> occurrences = recurringEvent.getAllOccurrences();

    // Always check for conflicts for all occurrences
    for (Event occurrence : occurrences) {
      if (hasConflict(occurrence)) {
        if (autoDecline) {
          throw new ConflictingEventException("Cannot add recurring event '" +
                  recurringEvent.getSubject() + "' due to conflict with an existing event");
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
   * Creates and adds a deep copy of an event to this calendar.
   *
   * @param sourceEvent the event to copy
   * @param autoDecline if true, the addition will be declined if it conflicts
   * @return true if the event was copied successfully, false otherwise
   */
  public boolean addEventCopy(Event sourceEvent, boolean autoDecline) throws ConflictingEventException {
    if (sourceEvent == null) {
      throw new IllegalArgumentException("Source event cannot be null");
    }

    // Create a deep copy of the event
    Event newEvent = new Event(
            sourceEvent.getSubject(),
            sourceEvent.getStartDateTime(),
            sourceEvent.getEndDateTime(),
            sourceEvent.getDescription(),
            sourceEvent.getLocation(),
            sourceEvent.isPublic()
    );

    if (sourceEvent.isAllDay()) {
      newEvent.setAllDay(true);
      if (sourceEvent.getDate() != null) {
        newEvent.setDate(sourceEvent.getDate());
      }
    }

    return addEvent(newEvent, autoDecline);
  }

  @Override
  public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                           String weekdays, LocalDate untilDate,
                                           boolean autoDecline)
          throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, start, end, repeatDays)
              .isPublic(true)
              .endDate(untilDate)
              .build();

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                            int occurrences, boolean autoDecline,
                                            String description,
                                            String location, boolean isPublic)
          throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay,
              endOfDay, repeatDays)
              .description(description)
              .location(location)
              .isPublic(isPublic)
              .occurrences(occurrences)
              .build();

      recurringEvent.setAllDay(true);

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                 LocalDate untilDate, boolean autoDecline,
                                                 String description, String location,
                                                 boolean isPublic)
          throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay, endOfDay,
              repeatDays)
              .description(description)
              .location(location)
              .isPublic(isPublic)
              .endDate(untilDate)
              .build();

      recurringEvent.setAllDay(true);

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Method to find event on a certain date.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @return Event type object.
   */
  @Override
  public Event findEvent(String subject, LocalDateTime startDateTime) {
    if (subject == null || startDateTime == null) {
      throw new IllegalArgumentException("Subject and start date/time cannot be null");
    }

    return events.stream()
            .filter(e -> e.getSubject().equals(subject)
                    && e.getStartDateTime().equals(startDateTime))
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
   * @return true if the operation is successful.
   */
  @Override
  public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
                                 String newValue)
          throws ConflictingEventException {
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
   * @return true if the operation is successful.
   */
  @Override
  public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
                                String newValue)
          throws ConflictingEventException {
    int count = 0;

    List<Event> matchingEvents = events.stream()
            .filter(e -> e.getSubject().equals(subject) &&
                    !e.getStartDateTime().isBefore(startDateTime))
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
   * @return number of occurrences edited.
   */
  @Override
  public int editAllEvents(String subject, String property, String newValue)
          throws ConflictingEventException {
    int count = 0;

    List<Event> matchingEvents = events.stream()
            .filter(e -> e.getSubject().equals(subject))
            .collect(Collectors.toList());

    for (Event event : matchingEvents) {
      if (updateEventProperty(event, property, newValue)) {
        count++;
      }
    }
    return count;
  }

  /**
   * retrieves all recurring events in the calendar.
   *
   * @return a list of all recurring events.
   */
  @Override
  public List<RecurringEvent> getAllRecurringEvents() {
    return new ArrayList<>(recurringEvents);
  }

  /**
   * Export all events of the calendar to a csv file.
   *
   * @param filePath the path where the CSV file should be created
   * @return filePath of exported csv.
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
   * @param event    the event to update.
   * @param property the property to update
   * @param newValue the new value for the property
   * @return true if the update was successful, otherwise false.
   */
  private boolean updateEventProperty(Event event, String property, String newValue)
          throws ConflictingEventException {
    if (property == null || newValue == null) {
      return false;
    }

    // Create a temporary copy to check for conflicts after property update
    Event tempEvent = new Event(
            event.getSubject(),
            event.getStartDateTime(),
            event.getEndDateTime(),
            event.getDescription(),
            event.getLocation(),
            event.isPublic()
    );
    tempEvent.setAllDay(event.isAllDay());

    // Apply the update to the temporary event
    EventPropertyUpdater updater = propertyUpdaters.get(property.toLowerCase());
    if (updater == null || !updater.update(tempEvent, newValue)) {
      return false;
    }

    // Check if the update would create conflicts
    if (wouldCreateConflict(tempEvent, event)) {
      throw new ConflictingEventException("Updating " + property + " would create a conflict");
    }

    // If we got here, apply the update to the actual event
    return updater.update(event, newValue);
  }

  /**
   * Checks if updating an event would create a conflict.
   *
   * @param updatedEvent  the event with updated properties
   * @param originalEvent the original event before updates
   * @return true if the update would create a conflict, false otherwise
   */
  private boolean wouldCreateConflict(Event updatedEvent, Event originalEvent) {
    // Skip comparing against the original event
    return events.stream()
            .filter(e -> !e.getId().equals(originalEvent.getId()))
            .anyMatch(updatedEvent::conflictsWith);
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
        return !dateTime.isBefore(event.getStartDateTime()) &&
                !dateTime.isAfter(event.getEndDateTime());
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
   * @return the name of the calendar
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this calendar.
   *
   * @param name the new name for the calendar
   */
  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    this.name = name;
  }

  /**
   * Gets the timezone of this calendar.
   *
   * @return the timezone string (IANA format)
   */
  public String getTimezone() {
    return timezone;
  }

  /**
   * Sets the timezone of this calendar.
   * All events in the calendar will maintain their local times
   * but be interpreted in the context of the new timezone.
   *
   * @param timezone the new timezone string (IANA format)
   */
  public void setTimezone(String timezone) {
    if (timezone == null || timezone.trim().isEmpty()) {
      throw new IllegalArgumentException("Timezone cannot be null or empty");
    }

    // Validate that this is a valid timezone
    try {
      ZoneId.of(timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }

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

    // Start date/time updaters
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

    // End date/time updaters
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

    // Visibility/privacy updaters
    EventPropertyUpdater visibilityUpdater = (event, value) -> {
      boolean isPublic = value.equalsIgnoreCase("public")
              || value.equalsIgnoreCase("true");
      event.setPublic(isPublic);
      return true;
    };
    propertyUpdaters.put("visibility", visibilityUpdater);
    propertyUpdaters.put("ispublic", visibilityUpdater);
    propertyUpdaters.put("public", visibilityUpdater);

    // Special case for "private" - inverts the logic
    propertyUpdaters.put("private", (event, value) -> {
      boolean isPrivate = value.equalsIgnoreCase("true")
              || value.equalsIgnoreCase("private");
      event.setPublic(!isPrivate);
      return true;
    });
  }

  /**
   * Get events that match a specific filter.
   *
   * @param filter the filter to apply
   * @return a list of events that match the filter
   */
  public List<Event> getFilteredEvents(EventFilter filter) {
    return events.stream()
            .filter(filter::matches)
            .collect(Collectors.toList());
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