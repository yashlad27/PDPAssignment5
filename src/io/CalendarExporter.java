package io;

import java.io.IOException;

import model.calendar.Calendar;

/**
 * Interface for calendar export operations.
 * This abstraction allows for different export formats and destinations.
 */
public interface CalendarExporter {
  /**
   * Exports calendar data to a file.
   *
   * @param calendar The calendar to export
   * @param filePath The path to the output file
   * @return true if export was successful
   */
  boolean export(Calendar calendar, String filePath);

  /**
   * Exports calendar data to any Appendable destination.
   *
   * @param calendar    The calendar to export
   * @param destination The Appendable destination to write to
   * @return true if export was successful
   * @throws IOException if writing to the destination fails
   */
  boolean exportToAppendable(Calendar calendar, Appendable destination) throws IOException;
} 
