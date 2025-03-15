import controller.CalendarController;
import controller.command.CommandFactory;
import model.calendar.Calendar;
import model.calendar.ICalendar;
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
    ICalendar calendar = new Calendar();
    ICalendarView view = new ConsoleView();

    CommandFactory commandFactory = new CommandFactory(calendar, view);

    CalendarController controller = new CalendarController(commandFactory, view);

    if (args.length < 2) {
      view.displayError(
          "Insufficient arguments. Usage: --mode [interactive|headless filename.txt]");
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
        view.displayError("Headless mode requires a filename. Usage: --mode headless filename.txt");
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