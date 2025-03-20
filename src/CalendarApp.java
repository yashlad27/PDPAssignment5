import controller.CalendarController;
import controller.command.CalendarCommandFactory;
import controller.command.CommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import utilities.TimeZoneHandler;
import view.ConsoleView;
import view.ICalendarView;

/**
 * Main class for the Calendar Application.
 */
public class CalendarApp {

  /**
   * Main method to run the application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {

    ICalendarView view = new ConsoleView();

    TimeZoneHandler timezoneHandler = new TimeZoneHandler();

    CalendarManager calendarManager = new CalendarManager.Builder()
            .timezoneHandler(timezoneHandler)
            .build();

    try {
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

    // Create command factories
    CommandFactory commandFactory = new CommandFactory(activeCalendar, view);
    CalendarCommandFactory calendarCommandFactory = new CalendarCommandFactory(calendarManager,
            view);

    // Create controller with dynamic calendar access
    CalendarController controller = new CalendarController(commandFactory,
            calendarCommandFactory, calendarManager, view);

    if (args.length < 2) {
      view.displayError(
              "Insufficient arguments. Usage: --mode "
                      + "[interactive|headless filename.txt]");
      return;
    }

    String modeArg = args[0].toLowerCase();
    String modeValue = args[1].toLowerCase();

    if (!modeArg.equals("--mode")) {
      view.displayError("Invalid argument. Expected: --mode");
      return;
    }

    if (modeValue.equals("interactive")) {
      controller.startInteractiveMode();
    } else if (modeValue.equals("headless")) {
      if (args.length < 3) {
        view.displayError("Headless mode requires a filename. "
                + "Usage: --mode headless filename.txt");
        return;
      }

      String filename = args[2];
      boolean success = controller.startHeadlessMode(filename);

      if (!success) {
        view.displayError("Headless mode execution failed.");
        System.exit(1);
      }
    } else {
      view.displayError("Invalid mode. Expected: interactive or headless");
    }

    if (view instanceof ConsoleView) {
      ((ConsoleView) view).close();
    }
  }
}