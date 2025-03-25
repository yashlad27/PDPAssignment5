package io;

import model.calendar.Calendar;
import model.event.Event;
import model.event.RecurringEvent;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV implementation of CalendarExporter.
 * Handles the specific details of exporting calendar data to CSV format.
 * Uses Appendable interface for flexible output handling.
 */
public class CSVCalendarExporter implements CalendarExporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public boolean export(Calendar calendar, String filePath) {
        if (calendar == null || filePath == null) {
            throw new IllegalArgumentException("Calendar and file path cannot be null");
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            return exportToAppendable(calendar, writer);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Exports calendar data to any Appendable destination.
     * This method allows for more flexible output handling, such as writing to files,
     * network streams, or StringBuilder.
     *
     * @param calendar The calendar to export
     * @param destination The Appendable destination to write to
     * @return true if export was successful
     * @throws IOException if writing to the destination fails
     */
    public boolean exportToAppendable(Calendar calendar, Appendable destination) throws IOException {
        if (calendar == null || destination == null) {
            throw new IllegalArgumentException("Calendar and destination cannot be null");
        }

        // Write header
        destination.append("Subject,Start Date,Start Time,End Date,End Time,Location,Description,Is Public\n");

        // Write single events
        List<Event> events = calendar.getAllEvents();
        for (Event event : events) {
            writeEventToCSV(destination, event);
        }

        // Write recurring events
        List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
        for (RecurringEvent event : recurringEvents) {
            writeEventToCSV(destination, event);
        }

        return true;
    }

    private void writeEventToCSV(Appendable destination, Event event) throws IOException {
        destination.append(String.format("%s,%s,%s,%s,%s,%s,%s,%b\n",
            escapeCSV(event.getSubject()),
            event.getStartDateTime().format(DATE_FORMATTER),
            event.getStartDateTime().format(TIME_FORMATTER),
            event.getEndDateTime().format(DATE_FORMATTER),
            event.getEndDateTime().format(TIME_FORMATTER),
            escapeCSV(event.getLocation()),
            escapeCSV(event.getDescription()),
            event.isPublic()
        ));
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
} 