package controller.input;

import java.util.Scanner;

/**
 * Implementation of CommandInputSource that reads commands from the console.
 */
public class ConsoleCommandInputSource implements CommandInputSource {
    private final Scanner scanner;

    public ConsoleCommandInputSource() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String readNextCommand() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return null;
    }

    @Override
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    @Override
    public void close() {
        scanner.close();
    }
} 