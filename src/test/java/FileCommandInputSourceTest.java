import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import controller.input.CommandInputSource;
import controller.input.FileCommandInputSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for FileCommandInputSource.
 * Tests the functionality of reading commands from a file.
 */
public class FileCommandInputSourceTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private File commandFile;
  private FileCommandInputSource inputSource;

  /**
   * Set up the test environment before each test.
   */
  @Before
  public void setUp() throws IOException {
    commandFile = tempFolder.newFile("test_commands.txt");
  }

  /**
   * Clean up after each test.
   */
  @After
  public void tearDown() {
    if (inputSource != null) {
      inputSource.close();
    }
  }

  /**
   * Test that FileCommandInputSource implements CommandInputSource.
   */
  @Test
  public void testImplementsCommandInputSource() throws IOException {
    writeToFile(commandFile, "test");
    inputSource = new FileCommandInputSource(commandFile.getPath());
    assertTrue(inputSource instanceof CommandInputSource);
  }

  /**
   * Test that constructor accepts a valid file path.
   */
  @Test
  public void testConstructorWithValidPath() throws IOException {
    writeToFile(commandFile, "test command");
    inputSource = new FileCommandInputSource(commandFile.getPath());
    assertNotNull(inputSource);
  }

  /**
   * Test that constructor throws appropriate exception with invalid file path.
   */
  @Test(expected = IOException.class)
  public void testConstructorWithInvalidPath() throws IOException {
    inputSource = new FileCommandInputSource("non_existent_file.txt");
  }

  /**
   * Test reading a single command from a file.
   */
  @Test
  public void testReadCommand() throws IOException {
    String testCommand = "create calendar MyCalendar";
    writeToFile(commandFile, testCommand);

    inputSource = new FileCommandInputSource(commandFile.getPath());
    String command = inputSource.readNextCommand();

    assertEquals(testCommand, command);
  }

  /**
   * Test reading multiple commands from a file.
   */
  @Test
  public void testReadMultipleCommands() throws IOException {
    String[] commands = {
            "create calendar MyCalendar",
            "use calendar MyCalendar",
            "exit"
    };

    writeToFile(commandFile, String.join("\n", commands));

    inputSource = new FileCommandInputSource(commandFile.getPath());

    assertEquals(commands[0], inputSource.readNextCommand());
    assertEquals(commands[1], inputSource.readNextCommand());
    assertEquals(commands[2], inputSource.readNextCommand());
  }

  /**
   * Test reading a file with empty lines.
   */
  @Test
  public void testReadFileWithEmptyLines() throws IOException {
    String content = "line1\n\nline2\n\n\nline3";
    writeToFile(commandFile, content);

    inputSource = new FileCommandInputSource(commandFile.getPath());

    assertEquals("line1", inputSource.readNextCommand());
    assertEquals("", inputSource.readNextCommand()); // Empty line
    assertEquals("line2", inputSource.readNextCommand());
    assertEquals("", inputSource.readNextCommand()); // Empty line
    assertEquals("", inputSource.readNextCommand()); // Empty line
    assertEquals("line3", inputSource.readNextCommand());
  }

  /**
   * Test the hasNextCommand method when more commands are available.
   */
  @Test
  public void testHasNextCommandWhenMoreAvailable() throws IOException {
    String content = "command1\ncommand2";
    writeToFile(commandFile, content);

    inputSource = new FileCommandInputSource(commandFile.getPath());

    assertTrue(inputSource.hasNextCommand());
    inputSource.readNextCommand(); // Read command1
    assertTrue(inputSource.hasNextCommand());
  }

  /**
   * Test the hasNextCommand method when no more commands are available.
   */
  @Test
  public void testHasNextCommandWhenNoneAvailable() throws IOException {
    String content = "command1";
    writeToFile(commandFile, content);

    inputSource = new FileCommandInputSource(commandFile.getPath());

    assertTrue(inputSource.hasNextCommand());
    inputSource.readNextCommand(); // Read the only command
    assertFalse(inputSource.hasNextCommand());
  }

  /**
   * Test reading from an empty file.
   */
  @Test
  public void testReadFromEmptyFile() throws IOException {
    // Create an empty file
    writeToFile(commandFile, "");

    inputSource = new FileCommandInputSource(commandFile.getPath());

    assertFalse(inputSource.hasNextCommand());
  }

  /**
   * Test closing the input source.
   */
  @Test
  public void testClose() throws IOException {
    writeToFile(commandFile, "command");

    inputSource = new FileCommandInputSource(commandFile.getPath());
    inputSource.close();

    // Trying to read after close should either throw exception or return null
    // depending on implementation
    try {
      String result = inputSource.readNextCommand();
      // If no exception, the result should be null
      assertNull(result);
    } catch (Exception e) {
      // If exception, this is also acceptable behavior after closing
      assertTrue(e instanceof IllegalStateException || e instanceof IOException);
    }
  }

  /**
   * Test reading file with Windows-style line endings (CRLF).
   */
  @Test
  public void testReadWithWindowsLineEndings() throws IOException {
    String content = "command1\r\ncommand2\r\ncommand3";
    writeToFile(commandFile, content);

    inputSource = new FileCommandInputSource(commandFile.getPath());

    assertEquals("command1", inputSource.readNextCommand());
    assertEquals("command2", inputSource.readNextCommand());
    assertEquals("command3", inputSource.readNextCommand());
  }

  /**
   * Test reading file with comments (lines starting with #).
   */
  @Test
  public void testReadWithComments() throws IOException {
    String content = "# This is a comment\ncommand1\n# Another comment\ncommand2";
    writeToFile(commandFile, content);

    inputSource = new FileCommandInputSource(commandFile.getPath());

    // Depending on implementation, comments might be skipped or returned
    // This test assumes comments are returned as normal lines
    assertEquals("# This is a comment", inputSource.readNextCommand());
    assertEquals("command1", inputSource.readNextCommand());
    assertEquals("# Another comment", inputSource.readNextCommand());
    assertEquals("command2", inputSource.readNextCommand());
  }

  /**
   * Test reading a large file.
   */
  @Test
  public void testReadLargeFile() throws IOException {
    StringBuilder largeContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeContent.append("command").append(i).append("\n");
    }

    writeToFile(commandFile, largeContent.toString());

    inputSource = new FileCommandInputSource(commandFile.getPath());

    for (int i = 0; i < 1000; i++) {
      assertTrue(inputSource.hasNextCommand());
      assertEquals("command" + i, inputSource.readNextCommand());
    }

    assertFalse(inputSource.hasNextCommand());
  }

  /**
   * Helper method to write content to a file.
   *
   * @param file    The file to write to.
   * @param content The content to write.
   * @throws IOException If an I/O error occurs.
   */
  private void writeToFile(File file, String content) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(content);
    }
  }
} 