package utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import model.event.Event;

/**
 * Utility class for exporting calendar events to CSV format.
 */
public class CSVExporter {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Exports a list of events to a CSV file.
   *
   * @param filePath the path where the CSV file should be created
   * @param events   the list of events to export
   * @return the path of the created CSV file
   * @throws IOException if there is an error writing to the file
   */
  public static String exportToCSV(String filePath, List<Event> events) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      // Write header
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day,Description,Location,Public\n");

      // Write events using streams
      events.stream()
              .map(CSVExporter::formatEventForCSV)
              .forEach(line -> {
                try {
                  writer.write(line);
                } catch (IOException e) {
                  throw new RuntimeException("Failed to write event to CSV", e);
                }
              });
    }
    return filePath;
  }

  /**
   * Formats a list of events for display.
   *
   * @param events      the list of events to format
   * @param showDetails whether to show detailed information
   * @return a formatted string representation of the events
   */
  public static String formatEventsForDisplay(List<Event> events, boolean showDetails) {
    if (events == null || events.isEmpty()) {
      return "No events found.";
    }

    return events.stream()
            .map(event -> formatEventForDisplay(event, showDetails))
            .collect(Collectors.joining("\n"));
  }

  private static String formatEventForCSV(Event event) {
    return String.format("%s,%s,%s,%s,%s,%b,%s,%s,%b\n",
            escapeCSV(event.getSubject()),
            event.getStartDateTime().format(DATE_FORMATTER),
            event.getStartDateTime().format(TIME_FORMATTER),
            event.getEndDateTime().format(DATE_FORMATTER),
            event.getEndDateTime().format(TIME_FORMATTER),
            event.isAllDay(),
            escapeCSV(event.getDescription()),
            escapeCSV(event.getLocation()),
            event.isPublic());
  }

  private static String formatEventForDisplay(Event event, boolean showDetails) {
    StringBuilder display = new StringBuilder();

    display.append(event.getSubject());

    if (event.isAllDay()) {
      display.append(" (All Day)");
    } else {
      display.append(" from ")
              .append(event.getStartDateTime().format(TIME_FORMATTER))
              .append(" to ")
              .append(event.getEndDateTime().format(TIME_FORMATTER));
    }

    if (showDetails) {
      String description = event.getDescription();
      if (description != null && !description.trim().isEmpty()) {
        display.append("\n  Description: ").append(description);
      }
      String location = event.getLocation();
      if (location != null && !location.trim().isEmpty()) {
        display.append("\n  Location: ").append(location);
      }
      if (!event.isPublic()) {
        display.append("\n  Private");
      }
    }

    return display.toString();
  }

  private static String escapeCSV(String value) {
    if (value == null) {
      return "";
    }

    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
  }
}