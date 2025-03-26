package view;

import java.util.Scanner;

/**
 * A simple console-based implementation of the ICalendarView interface.
 * This class provides functionality for reading user input from the console
 * and displaying messages and errors to the user through standard output
 * and standard error streams.
 */
public class ConsoleView implements ICalendarView {

  /**
   * Scanner object used to read user input from the console.
   * This scanner is initialized with System.in to capture keyboard input.
   */
  private final Scanner scanner;

  /**
   * Constructs a new ConsoleView.
   * Initializes the scanner to read from System.in for capturing user input.
   * This constructor should be called when creating a new instance of the view.
   */
  public ConsoleView() {
    this.scanner = new Scanner(System.in);
  }

  /**
   * Reads a command from the user via the console.
   * Displays a prompt ("> ") to indicate that the system is waiting for input,
   * then reads and returns the entire line entered by the user.
   *
   * @return The command entered by the user as a String
   */
  @Override
  public String readCommand() {
    System.out.print("> ");
    return scanner.nextLine();
  }

  /**
   * Displays an informational message to the user.
   * This method prints the given message to the standard output (System.out)
   * followed by a newline character.
   *
   * @param message The message to be displayed to the user
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays an error message to the user.
   * This method prints the given error message to the standard error (System.err)
   * prefixed with "ERROR: " to clearly indicate that it's an error message,
   * followed by a newline character.
   *
   * @param errorMessage The error message to be displayed to the user
   */
  @Override
  public void displayError(String errorMessage) {
    System.err.println("ERROR: " + errorMessage);
  }

  /**
   * Closes the scanner when the view is no longer needed.
   * This method should be called when the application is shutting down
   * to properly release system resources associated with the scanner.
   * Failure to close the scanner may result in resource leaks.
   */
  public void close() {
    scanner.close();
  }
}