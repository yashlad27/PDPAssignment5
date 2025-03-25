import org.junit.Test;

import controller.command.calendar.ErrorCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for ErrorCommand.
 * This class tests the functionality of the ErrorCommand class,
 * which is used to encapsulate error conditions as commands.
 */
public class ErrorCommandTest {

  /**
   * Test that the constructor properly initializes the error message.
   */
  @Test
  public void testConstructorWithValidMessage() {
    ErrorCommand command = new ErrorCommand("Test error message");
    assertEquals("Error: Test error message", command.execute());
  }

  /**
   * Test that the constructor properly handles a message with whitespace.
   */
  @Test
  public void testConstructorWithWhitespaceMessage() {
    ErrorCommand command = new ErrorCommand("   Whitespace message   ");
    assertEquals("Error:    Whitespace message   ", command.execute());
  }

  /**
   * Test that the constructor throws an exception when given a null message.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullMessage() {
    new ErrorCommand(null);
  }

  /**
   * Test that the constructor throws an exception when given an empty message.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptyMessage() {
    new ErrorCommand("");
  }

  /**
   * Test that the constructor throws an exception when given a message with only whitespace.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithOnlyWhitespaceMessage() {
    new ErrorCommand("    ");
  }

  /**
   * Test that the execute method properly formats the error message.
   */
  @Test
  public void testExecuteMethodWithRegularMessage() {
    ErrorCommand command = new ErrorCommand("Something went wrong");
    String result = command.execute();
    assertTrue(result.startsWith("Error: "));
    assertEquals("Error: Something went wrong", result);
  }

  /**
   * Test with a message that already contains "Error:" prefix.
   */
  @Test
  public void testExecuteMethodWithErrorPrefix() {
    ErrorCommand command = new ErrorCommand("Error: Duplicate error");
    String result = command.execute();
    assertEquals("Error: Error: Duplicate error", result);
  }

  /**
   * Test with a message containing special characters.
   */
  @Test
  public void testExecuteMethodWithSpecialCharacters() {
    ErrorCommand command = new ErrorCommand("Error in $%^&*()!@#");
    String result = command.execute();
    assertEquals("Error: Error in $%^&*()!@#", result);
  }

  /**
   * Test with a multi-line message.
   */
  @Test
  public void testExecuteMethodWithMultiLineMessage() {
    ErrorCommand command = new ErrorCommand("Line 1\nLine 2");
    String result = command.execute();
    assertEquals("Error: Line 1\nLine 2", result);
  }
} 