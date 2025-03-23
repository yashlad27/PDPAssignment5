package view;

import java.util.Scanner;

/**
 * A simple console-based implementation of the calendar view.
 */
public class ConsoleView implements ICalendarView {

  private final Scanner scanner;

  /**
   * Constructs a new ConsoleView.
   */
  public ConsoleView() {
    this.scanner = new Scanner(System.in);
  }

  @Override
  public String readCommand() {
    System.out.print("> ");
    return scanner.nextLine();
  }

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String errorMessage) {
    System.err.println("ERROR: " + errorMessage);
  }

  /**
   * Closes the scanner when the view is no longer needed.
   */
  public void close() {
    scanner.close();
  }
}