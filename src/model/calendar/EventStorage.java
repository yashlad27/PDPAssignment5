package model.calendar;

import model.event.Event;
import model.event.RecurringEvent;
import model.core.datetime.DateTimeWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Efficient storage structure for events that provides fast lookups by various criteria.
 */
public class EventStorage {
    private final Map<UUID, Event> eventById;
    private final Map<UUID, RecurringEvent> recurringEventById;
    private final Map<LocalDate, Set<Event>> eventsByDate;
    private final Map<String, Set<Event>> eventsBySubject;
    private final NavigableMap<DateTimeWrapper, Set<Event>> eventsByDateTime;

    public EventStorage() {
        this.eventById = new HashMap<>();
        this.recurringEventById = new HashMap<>();
        this.eventsByDate = new HashMap<>();
        this.eventsBySubject = new HashMap<>();
        this.eventsByDateTime = new TreeMap<>();
    }

    public void addEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        eventById.put(event.getId(), event);
        
        LocalDate date = event.getStartDateTime().toLocalDate();
        eventsByDate.computeIfAbsent(date, k -> new HashSet<>()).add(event);
        
        eventsBySubject.computeIfAbsent(event.getSubject(), k -> new HashSet<>()).add(event);
        
        DateTimeWrapper startTime = new DateTimeWrapper(event.getStartDateTime(), event.getTimezone());
        eventsByDateTime.computeIfAbsent(startTime, k -> new HashSet<>()).add(event);
    }

    public void addRecurringEvent(RecurringEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Recurring event cannot be null");
        }

        recurringEventById.put(event.getId(), event);
        for (Event occurrence : event.getAllOccurrences()) {
            addEvent(occurrence);
        }
    }

    public Event getEventById(UUID id) {
        return eventById.get(id);
    }

    public RecurringEvent getRecurringEventById(UUID id) {
        return recurringEventById.get(id);
    }

    public List<Event> getEventsOnDate(LocalDate date) {
        return new ArrayList<>(eventsByDate.getOrDefault(date, Collections.emptySet()));
    }

    public List<Event> getEventsBySubject(String subject) {
        return new ArrayList<>(eventsBySubject.getOrDefault(subject, Collections.emptySet()));
    }

    public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
        DateTimeWrapper startWrapper = new DateTimeWrapper(start, "UTC");
        DateTimeWrapper endWrapper = new DateTimeWrapper(end, "UTC");

        return eventsByDateTime.subMap(startWrapper, true, endWrapper, true)
                .values()
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toList());
    }

    public void removeEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        eventById.remove(event.getId());
        
        LocalDate date = event.getStartDateTime().toLocalDate();
        Set<Event> dateEvents = eventsByDate.get(date);
        if (dateEvents != null) {
            dateEvents.remove(event);
            if (dateEvents.isEmpty()) {
                eventsByDate.remove(date);
            }
        }

        Set<Event> subjectEvents = eventsBySubject.get(event.getSubject());
        if (subjectEvents != null) {
            subjectEvents.remove(event);
            if (subjectEvents.isEmpty()) {
                eventsBySubject.remove(event.getSubject());
            }
        }

        DateTimeWrapper startTime = new DateTimeWrapper(event.getStartDateTime(), event.getTimezone());
        Set<Event> timeEvents = eventsByDateTime.get(startTime);
        if (timeEvents != null) {
            timeEvents.remove(event);
            if (timeEvents.isEmpty()) {
                eventsByDateTime.remove(startTime);
            }
        }
    }

    public void removeRecurringEvent(RecurringEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Recurring event cannot be null");
        }

        recurringEventById.remove(event.getId());
        for (Event occurrence : event.getAllOccurrences()) {
            removeEvent(occurrence);
        }
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(eventById.values());
    }

    public List<RecurringEvent> getAllRecurringEvents() {
        return new ArrayList<>(recurringEventById.values());
    }
} 