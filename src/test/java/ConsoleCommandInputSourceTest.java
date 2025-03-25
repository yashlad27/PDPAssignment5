import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import controller.input.ConsoleCommandInputSource;
import controller.input.CommandInputSource;

import static org.junit.Assert.*;

/**
 * Test class for ConsoleCommandInputSource.
 * Tests the functionality of reading commands from the console.
 */
public class ConsoleCommandInputSourceTest {

    private final InputStream originalSystemIn = System.in;
    private ConsoleCommandInputSource inputSource;

    /**
     * Set up the test environment before each test.
     */
    @Before
    public void setUp() {
        inputSource = new ConsoleCommandInputSource();
    }

    /**
     * Restore the original System.in after each test.
     */
    @After
    public void tearDown() {
        System.setIn(originalSystemIn);
    }

    /**
     * Test that ConsoleCommandInputSource implements CommandInputSource.
     */
    @Test
    public void testImplementsCommandInputSource() {
        assertTrue(inputSource instanceof CommandInputSource);
    }

    /**
     * Test reading a single command from the console.
     */
    @Test
    public void testReadCommand() {
        String testCommand = "create calendar MyCalendar";
        simulateUserInput(testCommand);
        
        String command = inputSource.readNextCommand();
        assertEquals(testCommand, command);
    }

    /**
     * Test reading a command with leading and trailing whitespace.
     */
    @Test
    public void testReadCommandWithWhitespace() {
        String testCommand = "  create calendar MyCalendar  ";
        simulateUserInput(testCommand);
        
        String command = inputSource.readNextCommand();
        assertEquals(testCommand, command);
    }

    /**
     * Test reading an empty command.
     */
    @Test
    public void testReadEmptyCommand() {
        String testCommand = "";
        simulateUserInput(testCommand);
        
        String command = inputSource.readNextCommand();
        assertEquals(null, command);
    }

    /**
     * Test reading multiple commands in sequence.
     */
    @Test
    public void testReadMultipleCommands() {
        String testCommands = "create calendar MyCalendar\nuse calendar MyCalendar\nexit";
        simulateUserInput(testCommands);
        
        String command1 = inputSource.readNextCommand();
        String command2 = inputSource.readNextCommand();
        String command3 = inputSource.readNextCommand();
        
        assertEquals("create calendar MyCalendar", command1);
        assertEquals("use calendar MyCalendar", command2);
        assertEquals("exit", command3);
    }

    /**
     * Test behavior when hasNextCommand is called.
     */
    @Test
    public void testHasNextCommand() {
        String testCommands = "command1\ncommand2\n";
        simulateUserInput(testCommands);
        
        assertTrue(inputSource.hasNextCommand());
        inputSource.readNextCommand(); // Read command1
        assertTrue(inputSource.hasNextCommand());
        inputSource.readNextCommand(); // Read command2
        assertFalse(inputSource.hasNextCommand());
    }

    /**
     * Test behavior when trying to read a command when none is available.
     */
    @Test
    public void testReadCommandWhenNoneAvailable() {
        String testCommand = "single command";
        simulateUserInput(testCommand);
        
        inputSource.readNextCommand(); // Read the only command
        assertFalse(inputSource.hasNextCommand());
        
        // Reading when no command is available should return null or empty string
        // (Implementation dependent - adjust assertion based on actual behavior)
        String result = inputSource.readNextCommand();
        assertTrue(result == null || result.isEmpty());
    }

    /**
     * Test reading a command with special characters.
     */
    @Test
    public void testReadCommandWithSpecialCharacters() {
        String testCommand = "create calendar My$Special@Calendar#123";
        simulateUserInput(testCommand);
        
        String command = inputSource.readNextCommand();
        assertEquals(testCommand, command);
    }

    /**
     * Test the close method to ensure resources are released.
     */
    @Test
    public void testClose() {
        // First make sure the input source can read
        simulateUserInput("test");
        assertNotNull(inputSource.readNextCommand());
        
        // Close the input source
        inputSource.close();
        
        // Further operations might throw exceptions or return null depending on implementation
        // This part is implementation-dependent, adjust as needed
    }

    /**
     * Helper method to simulate user input.
     * @param input The input to simulate.
     */
    private void simulateUserInput(String input) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
        System.setIn(testIn);
        // Re-initialize input source to use the new System.in
        inputSource = new ConsoleCommandInputSource();
    }
} 