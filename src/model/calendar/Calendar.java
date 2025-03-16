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
 * Implementation of the ICalendar interface that manages calendar events.
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
   * method to add event to the calendar.
   *
   * @param event       the event to add
   * @param autoDecline if true, the addition will be declined if it conflicts with existing events
   * @return
   */
  @Override
  public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    if (autoDecline && hasConflict(event)) {
      throw new ConflictingEventException("Cannot add event '" + event.getSubject() + "' due to conflict with an existing event");
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
   * @return
   */
  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
    if (recurringEvent == null) {
      throw new IllegalArgumentException("Recurring event cannot be null");
    }

    List<Event> occurrences = recurringEvent.getAllOccurrences();

    if (autoDecline) {
      for (Event occurrence : occurrences) {
        if (hasConflict(occurrence)) {
          return false;
        }
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

  @Override
  public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                           String weekdays, LocalDate untilDate, boolean autoDecline) {
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
                                            int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay, endOfDay, repeatDays)
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
                                                 LocalDate untilDate, boolean autoDecline, String description, String location,
                                                 boolean isPublic) {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay, endOfDay, repeatDays)
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
   * @return true if the operation is successful.
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
   * @return true if the operation is successful.
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
   * @return number of occurrences edited.
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
   * retrieves oll reccurring events in the calendar.
   *
   * @return a list of all reccuring events.
   */
  @Override
  public List<RecurringEvent> getAllRecurringEvents() {
    return new ArrayList<>(recurringEvents);
  }

  /**
   * Export all events of the calendar to a  csv file.
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
  private boolean updateEventProperty(Event event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
      case "name":
        event.setSubject(newValue);
        return true;
      case "description":
        event.setDescription(newValue);
        return true;
      case "location":
        event.setLocation(newValue);
        return true;
      case "start":
      case "starttime":
      case "startdatetime":
        try {
          LocalDateTime newStartTime;
          if (newValue.contains("T")) {
            newStartTime = DateTimeUtil.parseDateTime(newValue);
          } else {
            LocalTime newTime = LocalTime.parse(newValue);
            newStartTime = LocalDateTime.of(event.getStartDateTime().toLocalDate(), newTime);
          }
          event.setStartDateTime(newStartTime);
          return true;
        } catch (Exception e) {
          return false;
        }
      case "end":
      case "endtime":
      case "enddatetime":
        try {
          LocalDateTime newEndTime;
          if (newValue.contains("T")) {
            newEndTime = DateTimeUtil.parseDateTime(newValue);
          } else {
            LocalTime newTime = LocalTime.parse(newValue);
            newEndTime = LocalDateTime.of(event.getEndDateTime().toLocalDate(), newTime);
          }
          event.setEndDateTime(newEndTime);
          return true;
        } catch (Exception e) {
          return false;
        }
      case "visibility":
      case "ispublic":
      case "public":
      case "private":
        boolean isPublic = newValue.equalsIgnoreCase("public") || (newValue.equalsIgnoreCase("true")
                && !property.equals("private"));
        event.setPublic(isPublic);
        return true;
      default:
        return false;
    }
  }

//  @Override
//  public List<Event> getEventsOnDate(LocalDate date) {
//    return events.stream().filter(event -> {
//      if (event.getStartDateTime() != null) {
//        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
//
//        if (event.getEndDateTime() != null) {
//          LocalDate eventEndDate = event.getEndDateTime().toLocalDate();
//          return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate);
//        } else {
//          return eventStartDate.equals(date);
//        }
//      } else if (event.getDate() != null) {
//        return event.getDate().equals(date);
//      }
//      return false;
//    }).collect(Collectors.toList());
//  }

  @Override
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    return events.stream().filter(event -> {
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
    }).collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return events.stream().anyMatch(event -> {
      if (event.getStartDateTime() != null && event.getEndDateTime() != null) {
        return !dateTime.isBefore(event.getStartDateTime()) && !dateTime.isAfter(
                event.getEndDateTime());
      }

      if (event.getDate() != null) {
        LocalDate targetDate = dateTime.toLocalDate();
        return event.getDate().equals(targetDate);
      }

      return false;
    });
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTimezone() {
    return timezone;
  }

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
      boolean isPublic = value.equalsIgnoreCase("public") || value.equalsIgnoreCase("true");
      event.setPublic(isPublic);
      return true;
    };
    propertyUpdaters.put("visibility", visibilityUpdater);
    propertyUpdaters.put("ispublic", visibilityUpdater);
    propertyUpdaters.put("public", visibilityUpdater);

    // Special case for "private" - inverts the logic
    propertyUpdaters.put("private", (event, value) -> {
      boolean isPrivate = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("private");
      event.setPublic(!isPrivate);
      return true;
    });

  }

  /**
   * Updates a specific property of an event.
   *
   * @param event    the event to update
   * @param property the property to update
   * @param newValue the new value for the property
   * @return true if the update was successful, otherwise false
   */
  private boolean UpdateEventProperty(Event event, String property, String newValue) {
    if (property == null || newValue == null) {
      return false;
    }

    EventPropertyUpdater updater = propertyUpdaters.get(property.toLowerCase());
    return updater != null && updater.update(event, newValue);
  }

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