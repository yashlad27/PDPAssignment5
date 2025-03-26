package view;

/**
 * Interface for the calendar view that handles user interaction.
 * This interface defines the contract for any view implementation in the calendar application,
 * establishing a clear separation between the user interface and the underlying business logic.
 * By using this interface, the application can support multiple view implementations
 * (console-based, GUI, web, etc.) without changing the core functionality.
 */
public interface ICalendarView {

  /**
   * Reads a command from the user.
   * This method is responsible for obtaining input from the user through the
   * appropriate input mechanism (console, GUI elements, etc.). It should handle
   * any necessary formatting or preprocessing of the raw input before returning
   * it as a command string.
   *
   * @return the command string entered by the user
   */
  String readCommand();

  /**
   * Displays a message to the user.
   * This method is responsible for showing informational messages to the user
   * through the appropriate output mechanism (console, GUI elements, etc.).
   * It should present the information in a clear and readable format appropriate
   * to the view implementation.
   *
   * @param message the message to display to the user
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   * This method is responsible for showing error messages to the user
   * through the appropriate output mechanism (console, GUI elements, etc.).
   * Error messages should be visually distinct from normal messages to
   * clearly indicate that an error has occurred.
   *
   * @param errorMessage the error message to display to the user
   */
  void displayError(String errorMessage);
}