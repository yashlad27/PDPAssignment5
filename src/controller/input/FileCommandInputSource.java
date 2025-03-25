package controller.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Implementation of CommandInputSource that reads commands from a file.
 */
public class FileCommandInputSource implements CommandInputSource {
    private final BufferedReader reader;

    public FileCommandInputSource(String filePath) throws IOException {
        this.reader = new BufferedReader(new FileReader(filePath));
    }

    @Override
    public String readNextCommand() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean hasNextCommand() {
        try {
            return reader.ready();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            // Ignore close errors
        }
    }
} 