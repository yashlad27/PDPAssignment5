package controller.command;

import java.util.HashMap;
import java.util.Map;

import controller.ICommandFactory;
import model.calendar.ICalendar;
import view.ICalendarView;

/**
 * Factory for creating and registering commands using functional interfaces.
 * Implements ICommandFactory to allow for dependency inversion.
 */
public class CommandFactory implements ICommandFactory {

  private final Map<String, CommandExecutor> commands;
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
    this.calendar = calendar;
    this.view = view;

    // Register all available commands as functional interfaces
    registerCommands();
  }

  /**
   * Registers all command executors.
   */
  private void registerCommands() {
    // Create event command
    registerCreateCommand();

    // Edit event command
    registerEditCommand();

    // Print events command
    commands.put("print", new PrintEventsCommand(calendar)::execute);

    // Show status command
    commands.put("show", new ShowStatusCommand(calendar)::execute);

    // Export calendar command
    commands.put("export", new ExportCalendarCommand(calendar)::execute);

    // Exit command
    commands.put("exit", args -> "Exiting application.");
  }

  /**
   * Registers the create command with all its subcommands.
   */
  private void registerCreateCommand() {
    // Use a single CreateEventCommand instance for consistency
    CreateEventCommand createCmd = new CreateEventCommand(calendar);

    commands.put("create", createCmd::execute);
  }

  /**
   * Registers the edit command with all its subcommands.
   */
  private void registerEditCommand() {
    // Use a single EditEventCommand instance
    EditEventCommand editCmd = new EditEventCommand(calendar);

    commands.put("edit", editCmd::execute);
  }

  /**
   * Gets a command executor by name.
   *
   * @param name the name of the command
   * @return the command executor, or null if not found
   */
  public CommandExecutor getCommandExecutor(String name) {
    return commands.get(name);
  }

  /**
   * Checks if a command is registered.
   *
   * @param name the name of the command
   * @return true if the command is registered, false otherwise
   */
  @Override
  public boolean hasCommand(String name) {
    return commands.containsKey(name);
  }

  /**
   * Gets a command by name.
   *
   * @param commandName the name of the command
   * @return the command, or null if not found
   */
  @Override
  public ICommand getCommand(String commandName) {
    CommandExecutor executor = getCommandExecutor(commandName);
    if (executor == null) {
      return null;
    }
    return new CommandAdapter(commandName, executor);
  }

  /**
   * Gets all available command names.
   *
   * @return a set of command names
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

  /**
   * Registers a custom command executor.
   *
   * @param name     the name of the command
   * @param executor the command executor
   */
  public void registerCommand(String name, CommandExecutor executor) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Command name cannot be null or empty");
    }
    if (executor == null) {
      throw new IllegalArgumentException("Command executor cannot be null");
    }

    commands.put(name, executor);
  }
}