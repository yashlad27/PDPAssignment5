package controller.command.calendar;

/**
 * Command implementation for handling error cases in calendar operations.
 * 
 * <p>This command is used to encapsulate error conditions as commands,
 * allowing for consistent error handling across the calendar system.
 * It provides a simple way to return error messages in the same format
 * as successful command results.
 * 
 * <p>Example usage:
 * <pre>
 * ErrorCommand error = new ErrorCommand("Invalid calendar name");
 * String result = error.execute(); // Returns "Error: Invalid calendar name"
 * </pre>
 * 
 * @see ICalendarCommand
 */
public class ErrorCommand implements ICalendarCommand {
  private final String errorMessage;

  /**
   * Creates a new ErrorCommand with the specified error message.
   * 
   * @param errorMessage The error message to be returned when the command is executed
   * @throws IllegalArgumentException if errorMessage is null or empty
   */
  public ErrorCommand(String errorMessage) {
    if (errorMessage == null || errorMessage.trim().isEmpty()) {
      throw new IllegalArgumentException("Error message cannot be null or empty");
    }
    this.errorMessage = errorMessage;
  }

  /**
   * Executes the error command.
   * 
   * <p>This method simply returns the error message prefixed with "Error: ".
   * This ensures consistent error message formatting across the system.
   * 
   * @return A formatted error message string
   */
  @Override
  public String execute() {
    return "Error: " + errorMessage;
  }
} 