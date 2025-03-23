import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import view.ConsoleView;

/**
 * Tests for the ConsoleView class.
 */
public class ConsoleViewTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;

  private ConsoleView consoleView;

  /**
   * Set up the test environment before each test.
   */
  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  /**
   * Restore the original streams after each test.
   */
  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    System.setIn(originalIn);

    if (consoleView != null) {
      consoleView.close();
    }
  }

  /**
   * Test that readCommand properly reads input.
   */
  @Test
  public void testReadCommand() {
    String input = "create event Test from 2023-05-10T10:00 to 2023-05-10T11:00";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    consoleView = new ConsoleView();
    String command = consoleView.readCommand();

    assertEquals(input, command);
    assertTrue(outContent.toString().contains("> "));
  }

  /**
   * Test that displayMessage properly outputs messages.
   */
  @Test
  public void testDisplayMessage() {
    consoleView = new ConsoleView();
    String message = "Event created successfully.";
    consoleView.displayMessage(message);

    assertEquals(message + System.lineSeparator(), outContent.toString());
  }

  /**
   * Test that displayError properly outputs error messages.
   */
  @Test
  public void testDisplayError() {
    consoleView = new ConsoleView();
    String errorMessage = "Invalid command format.";
    consoleView.displayError(errorMessage);

    assertEquals("ERROR: " + errorMessage + System.lineSeparator(), errContent.toString());
  }

  /**
   * Test that multiple commands can be read in sequence.
   */
  @Test
  public void testMultipleCommands() {
    String input = "command1" + System.lineSeparator() + "command2";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    consoleView = new ConsoleView();
    String command1 = consoleView.readCommand();
    String command2 = consoleView.readCommand();

    assertEquals("command1", command1);
    assertEquals("command2", command2);
  }

  /**
   * Test that empty input is handled correctly.
   */
  @Test(expected = NoSuchElementException.class)
  public void testEmptyInput() {
    String input = "";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
    System.setIn(inputStream);

    consoleView = new ConsoleView();
    consoleView.readCommand();
  }

  /**
   * Test that multi-line messages display correctly.
   */
  @Test
  public void testMultiLineMessage() {
    consoleView = new ConsoleView();
    String message = "Line 1" + System.lineSeparator() + "Line 2";
    consoleView.displayMessage(message);

    assertEquals(message + System.lineSeparator(), outContent.toString());
  }
}