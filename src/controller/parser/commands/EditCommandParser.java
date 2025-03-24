package controller.parser.commands;

import controller.ICommandFactory;
import controller.parser.AbstractCommandParser;
import controller.parser.CommandParser;

public class EditCommandParser extends AbstractCommandParser {
    public EditCommandParser(ICommandFactory commandFactory) {
        super(commandFactory);
    }

    @Override
    public CommandParser.CommandWithArgs parseCommand(String commandString) {
        String[] parts = commandString.trim().split("\\s+");
        
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid edit command format");
        }

        String subCommand = parts[1].toLowerCase();
        if (!subCommand.equals("event")) {
            throw new IllegalArgumentException("Invalid edit command format");
        }

        if (parts.length < 3) {
            throw new IllegalArgumentException("Event name cannot be empty");
        }

        String eventName = parts[2];
        return new CommandParser.CommandWithArgs(commandFactory.getCommand("edit"), new String[]{eventName});
    }
}
