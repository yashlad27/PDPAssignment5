import controller.CalendarController;
import controller.ICommandFactory;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.core.timezone.TimeZoneHandler;
import model.factory.CalendarFactory;
import view.ConsoleView;
import view.ICalendarView;
import controller.command.calendar.CalendarCommandFactory;

/**
 * Main entry point for the Calendar Application.
 * This class handles both interactive and headless modes of operation.
 */
public class CalendarApp {

  /**
   * Main method that serves as the entry point for the application.
   *
   * @param args Command line arguments:
   *             --mode interactive    : Starts the application in interactive mode
   *             --mode headless file : Starts the application in headless mode with the specified command file
   */
  public static void main(String[] args) {
    CalendarFactory factory = new CalendarFactory();
    ICalendarView view = factory.createView();
    TimeZoneHandler timezoneHandler = factory.createTimeZoneHandler();
    CalendarManager calendarManager = factory.createCalendarManager(timezoneHandler);

    try {
        // Create and set the default calendar
        calendarManager.createCalendar("Default", "America/New_York");
        calendarManager.setActiveCalendar("Default");
        
        // Get the active calendar for the command factory
        ICalendar calendar = calendarManager.getActiveCalendar();
        
        // Create event command factory
        ICommandFactory eventCommandFactory = factory.createEventCommandFactory(calendar, view);
        
        // Create a calendar command factory with null controller initially
        ICommandFactory calendarCommandFactory = factory.createCalendarCommandFactory(calendarManager, view, null);
        
        // Create the controller with the factories
        CalendarController controller = factory.createController(
                eventCommandFactory,
                calendarCommandFactory,
                calendarManager,
                view);
        
        // Now update the command factory with the controller
        ICommandFactory realFactory = factory.createCalendarCommandFactory(calendarManager, view, controller);
        
        // Create a new controller with the updated factory
        controller = factory.createController(
                eventCommandFactory,
                realFactory,
                calendarManager,
                view);
        
        // Process command line arguments
        processArgs(args, controller, view);
    } catch (Exception e) {
        view.displayError("Error: " + e.getMessage());
        System.exit(1);
    } finally {
        if (view instanceof ConsoleView) {
            ((ConsoleView) view).close();
        }
    }
  }
  
  /**
   * Process command line arguments and start the appropriate mode.
   */
  private static void processArgs(String[] args, CalendarController controller, ICalendarView view) throws Exception {
    if (args.length < 2) {
      view.displayError("Usage: java CalendarApp.java --mode [interactive|headless filename]");
      return;
    }

    String modeArg = args[0].toLowerCase();
    String modeValue = args[1].toLowerCase();

    if (!modeArg.equals("--mode")) {
      view.displayError("Invalid argument. Expected: --mode");
      return;
    }

    switch (modeValue) {
      case "interactive":
        controller.startInteractiveMode();
        break;

      case "headless":
        if (args.length < 3) {
          view.displayError("Headless mode requires a filename. Usage: --mode headless filename");
          return;
        }
        String filename = args[2];
        boolean success = controller.startHeadlessMode(filename);
        if (!success) {
          view.displayError("Headless mode execution failed.");
          System.exit(1);
        }
        break;

      default:
        view.displayError("Invalid mode. Expected: interactive or headless");
    }
  }
}