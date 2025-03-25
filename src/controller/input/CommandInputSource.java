package controller.input;

/**
 * Interface for command input sources.
 * This abstraction allows the program to work with various input sources.
 */
public interface CommandInputSource {
    /**
     * Reads the next command from the input source.
     *
     * @return The next command as a string, or null if no more commands
     */
    String readNextCommand();

    /**
     * Checks if there are more commands available.
     *
     * @return true if there are more commands to read
     */
    boolean hasNextCommand();

    /**
     * Closes the input source and releases any resources.
     */
    void close();
} 