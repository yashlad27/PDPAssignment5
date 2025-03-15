package controller.command;

import model.calendar.ICalendar;
import view.ICalendarView;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and registering commands.
 */
public class CommandFactory {

  private final Map<String, ICommand> commands;
  private final ICalendar calendar;
  private final ICalendarView view;


  /**
   * Constructs a new CommandFactory and registers all available commands.
   *
   * @param calendar the calendar model
   * @param view     the view for user interaction
   */
  public CommandFactory(ICalendar calendar, ICalendarView view) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }

    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commands = new HashMap<>();

    // Register all available commands
    registerCommand(new CreateEventCommand(calendar));
    registerCommand(new PrintEventsCommand(calendar));
    registerCommand(new ShowStatusCommand(calendar));
    registerCommand(new ExportCalendarCommand(calendar));
    registerCommand(new EditEventCommand(calendar));
    registerCommand(new ExitCommand());

    this.calendar = calendar;
    this.view = view;
  }

  /**
   * Registers a command with the factory.
   *
   * @param command the command to register
   */
  private void registerCommand(ICommand command) {
    commands.put(command.getName(), command);
  }

  /**
   * Gets a command by name.
   *
   * @param name the name of the command
   * @return the command, or null if not found
   */
  public ICommand getCommand(String name) {
    return commands.get(name);
  }

  /**
   * Checks if a command is registered.
   *
   * @param name the name of the command
   * @return true if the command is registered, false otherwise
   */
  public boolean hasCommand(String name) {
    return commands.containsKey(name);
  }

  /**
   * Gets all available command names.
   *
   * @return a list of command names
   */
  public Iterable<String> getCommandNames() {
    return commands.keySet();
  }

  /**
   * Gets the calendar instance.
   *
   * @return the calendar instance
   */
  public ICalendar getCalendar() {
    return calendar;
  }

  /**
   * Gets the view instance.
   *
   * @return the view instance
   */
  public ICalendarView getView() {
    return view;
  }
}