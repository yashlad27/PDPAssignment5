import controller.CalendarController;
import controller.ICommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.factory.CalendarFactory;
import utilities.TimeZoneHandler;
import view.ConsoleView;
import view.ICalendarView;

/**
 * Main class for the Calendar Application.
 * Refactored to use dependency injection through a factory.
 */
public class CalendarApp {

  /**
   * Main method to run the application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    // Create the factory that will provide all our dependencies
    CalendarFactory factory = new CalendarFactory();

    // Use the factory to create application components
    ICalendarView view = factory.createView();
    TimeZoneHandler timezoneHandler = factory.createTimeZoneHandler();
    CalendarManager calendarManager = factory.createCalendarManager(timezoneHandler);

    try {
      // Initialize with default calendar
      calendarManager.createCalendarWithDefaultTimezone("Default");
      calendarManager.setActiveCalendar("Default");
    } catch (Exception e) {
      view.displayError("Failed to create default calendar: " + e.getMessage());
      return;
    }

    // Get the active calendar reference
    ICalendar activeCalendar;
    try {
      activeCalendar = calendarManager.getActiveCalendar();
    } catch (Exception e) {
      view.displayError("Failed to get active calendar: " + e.getMessage());
      return;
    }

    // Create command factories through the factory
    ICommandFactory eventCommandFactory = factory.createEventCommandFactory(activeCalendar, view);
    ICommandFactory calendarCommandFactory = factory.createCalendarCommandFactory(calendarManager, view);

    // Create controller through the factory
    CalendarController controller = factory.createController(
            eventCommandFactory,
            calendarCommandFactory,
            calendarManager,
            view);

    // Process command line arguments
    if (!processCommandLineArgs(args, controller, view)) {
      return;
    }

    // Close resources if necessary
    if (view instanceof ConsoleView) {
      ((ConsoleView) view).close();
    }
  }

  /**
   * Process command line arguments and start the application in the appropriate mode.
   *
   * @param args       the command line arguments
   * @param controller the calendar controller
   * @param view       the view for user interaction
   * @return true if processing was successful, false otherwise
   */
  private static boolean processCommandLineArgs(String[] args, CalendarController controller, ICalendarView view) {
    if (args.length < 2) {
      view.displayError(
              "Insufficient arguments. Usage: --mode "
                      + "[interactive|headless filename.txt]");
      return false;
    }

    String modeArg = args[0].toLowerCase();
    String modeValue = args[1].toLowerCase();

    if (!modeArg.equals("--mode")) {
      view.displayError("Invalid argument. Expected: --mode");
      return false;
    }

    if (modeValue.equals("interactive")) {
      controller.startInteractiveMode();
      return true;
    } else if (modeValue.equals("headless")) {
      if (args.length < 3) {
        view.displayError("Headless mode requires a filename. "
                + "Usage: --mode headless filename.txt");
        return false;
      }

      String filename = args[2];
      boolean success = controller.startHeadlessMode(filename);

      if (!success) {
        view.displayError("Headless mode execution failed.");
        System.exit(1);
        return false;
      }
      return true;
    } else {
      view.displayError("Invalid mode. Expected: interactive or headless");
      return false;
    }
  }
}