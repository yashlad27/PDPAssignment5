package model.factory;

import controller.CalendarController;
import controller.ICommandFactory;
import controller.command.calendar.CalendarCommandFactory;
import controller.command.event.CommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import utilities.TimeZoneHandler;
import view.ConsoleView;
import view.ICalendarView;

/**
 * Factory class that creates and provides dependencies for the calendar application.
 * This class centralizes the creation of application components and follows the
 * Factory pattern to support Dependency Inversion.
 */
public class CalendarFactory {

  /**
   * Creates a view implementation.
   *
   * @return an implementation of ICalendarView
   */
  public ICalendarView createView() {
    return new ConsoleView();
  }

  /**
   * Creates a timezone handler.
   *
   * @return a TimeZoneHandler instance
   */
  public TimeZoneHandler createTimeZoneHandler() {
    return new TimeZoneHandler();
  }

  /**
   * Creates a calendar manager with the given timezone handler.
   *
   * @param timezoneHandler the timezone handler to use
   * @return a CalendarManager instance
   * @throws IllegalArgumentException if timezoneHandler is null
   */
  public CalendarManager createCalendarManager(TimeZoneHandler timezoneHandler) {
    if (timezoneHandler == null) {
      throw new IllegalArgumentException("TimeZoneHandler cannot be null");
    }
    return new CalendarManager.Builder().timezoneHandler(timezoneHandler).build();
  }

  /**
   * Creates a command factory for event commands.
   *
   * @param calendar the calendar to operate on
   * @param view     the view to interact with
   * @return an ICommandFactory for event commands
   */
  public ICommandFactory createEventCommandFactory(ICalendar calendar, ICalendarView view) {
    return new CommandFactory(calendar, view);
  }

  /**
   * Creates a command factory for calendar management commands.
   *
   * @param calendarManager the calendar manager to operate on
   * @param view            the view to interact with
   * @return an ICommandFactory for calendar commands
   */
  public ICommandFactory createCalendarCommandFactory(CalendarManager calendarManager,
                                                      ICalendarView view) {
    return new CalendarCommandFactory(calendarManager, view);
  }

  /**
   * Creates a calendar controller with all its dependencies.
   *
   * @param eventCommandFactory    the command factory for event commands
   * @param calendarCommandFactory the command factory for calendar commands
   * @param calendarManager        the calendar manager
   * @param view                   the view to interact with
   * @return a CalendarController instance
   */
  public CalendarController createController(ICommandFactory eventCommandFactory,
                                             ICommandFactory calendarCommandFactory,
                                             CalendarManager calendarManager, ICalendarView view) {

    return new CalendarController(eventCommandFactory, calendarCommandFactory, calendarManager,
            view);
  }
}