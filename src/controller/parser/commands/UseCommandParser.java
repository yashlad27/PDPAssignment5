package controller.parser.commands;

import controller.ICommandFactory;
import controller.parser.AbstractCommandParser;
import controller.parser.CommandParser;

public class UseCommandParser extends AbstractCommandParser {
    public UseCommandParser(ICommandFactory commandFactory) {
        super(commandFactory);
    }

    @Override
    public CommandWithArgs parseCommand(String commandString) {
        String[] parts = commandString.trim().split("\\s+");
        
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid use command format");
        }

        if (!parts[1].equals("calendar")) {
            throw new IllegalArgumentException("Invalid use command format");
        }

        if (!parts[2].equals("--name")) {
            throw new IllegalArgumentException("Invalid use command format");
        }

        if (parts.length < 4) {
            throw new IllegalArgumentException("Calendar name cannot be empty");
        }

        String calendarName = parts[3];
        return new CommandParser.CommandWithArgs(commandFactory.getCommand("use"), new String[]{calendarName});
    }
}
