package view;

/**
 * Interface for the calendar view that handles user interaction.
 */
public interface ICalendarView {

  /**
   * Reads a command from the user.
   *
   * @return the command string
   */
  String readCommand();

  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param errorMessage the error message to display
   */
  void displayError(String errorMessage);
}