package controller.command.calendar;

import controller.CalendarController;
import io.CalendarExporter;

/**
 * Command implementation for exporting a calendar to CSV format.
 * 
 * <p>This command is responsible for exporting a calendar's events to a CSV file.
 * It delegates the actual export operation to the CalendarController, which handles
 * the file I/O operations and error handling.
 * 
 * <p>The command follows the Command pattern by:
 * - Encapsulating the export operation as a separate object
 * - Using the CalendarController for the actual implementation
 * - Providing a simple execute() interface
 * 
 * <p>Example usage:
 * <pre>
 * ExportCalendarCommand command = new ExportCalendarCommand("MyCalendar", "output.csv", controller);
 * String result = command.execute();
 * </pre>
 * 
 * @see ICalendarCommand
 * @see CalendarController
 * @see CalendarExporter
 */
public class ExportCalendarCommand implements ICalendarCommand {
    private final String calendarName;
    private final String filePath;
    private final CalendarController controller;

    /**
     * Creates a new ExportCalendarCommand.
     * 
     * @param calendarName The name of the calendar to export
     * @param filePath The path where the CSV file should be created
     * @param controller The CalendarController that will handle the export operation (can be null during initialization)
     * @throws IllegalArgumentException if calendarName or filePath is null or empty
     */
    public ExportCalendarCommand(String calendarName, String filePath, CalendarController controller) {
        if (calendarName == null || calendarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Calendar name cannot be null or empty");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        this.calendarName = calendarName;
        this.filePath = filePath;
        this.controller = controller;
    }

    /**
     * Executes the export command.
     * 
     * <p>This method delegates the export operation to the CalendarController,
     * which handles the actual file I/O and error handling.
     * 
     * @return A string message describing the result of the export operation
     */
    @Override
    public String execute() {
        if (controller == null) {
            return "Error: Controller not initialized";
        }
        return controller.exportCalendarToCSV(calendarName, filePath);
    }
} 