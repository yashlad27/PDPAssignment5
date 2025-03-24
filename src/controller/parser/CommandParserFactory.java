package controller.parser;

import controller.ICommandFactory;
import controller.parser.commands.CopyCommandParser;
import controller.parser.commands.CreateCommandParser;
import controller.parser.commands.EditCommandParser;
import controller.parser.commands.ExportCommandParser;
import controller.parser.commands.PrintCommandParser;
import controller.parser.commands.ShowCommandParser;
import controller.parser.commands.UseCommandParser;

public class CommandParserFactory {
    private final ICommandFactory commandFactory;

    public CommandParserFactory(ICommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public AbstractCommandParser createParser(String command) {
        switch (command.toLowerCase()) {
            case "create":
                return new CreateCommandParser(commandFactory);
            case "use":
                return new UseCommandParser(commandFactory);
            case "show":
                return new ShowCommandParser(commandFactory);
            case "edit":
                return new EditCommandParser(commandFactory);
            case "copy":
                return new CopyCommandParser(commandFactory);
            case "print":
                return new PrintCommandParser(commandFactory);
            case "export":
                return new ExportCommandParser(commandFactory);
            default:
                throw new IllegalArgumentException("Unknown command: " + command);
        }
    }
}
