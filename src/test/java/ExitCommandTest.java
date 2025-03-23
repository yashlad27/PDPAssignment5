import org.junit.Before;
import org.junit.Test;

import controller.command.ExitCommand;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link ExitCommand} class.
 */
public class ExitCommandTest {

  private ExitCommand command;

  @Before
  public void setUp() {
    command = new ExitCommand();
  }

  @Test
  public void testGetName() {
    assertEquals("exit", command.getName());
  }

  @Test
  public void testExecuteWithNoArguments() {
    String[] args = {};

    String result = command.execute(args);
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testExecuteWithArguments() {
    String[] args = {"ignored", "arguments"};
    String result = command.execute(args);
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testExecuteWithNullArguments() {
    String[] args = null;
    String result = command.execute(args);
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testMultipleExecutions() {
    String[] args = {};

    String result1 = command.execute(args);
    String result2 = command.execute(args);
    String result3 = command.execute(args);

    assertEquals("Exiting application.", result1);
    assertEquals("Exiting application.", result2);
    assertEquals("Exiting application.", result3);
  }
}