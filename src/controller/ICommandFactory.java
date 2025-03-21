package controller;

import controller.command.ICommand;

/**
 * Interface for command factories.
 * This provides an abstraction for different types of command factories.
 */
public interface ICommandFactory {
  /**
   * Checks if a command is registered.
   *
   * @param commandName the name of the command
   * @return true if the command is registered, false otherwise
   */
  boolean hasCommand(String commandName);

  /**
   * Gets a command handler by name.
   *
   * @param commandName the name of the command
   * @return the command handler
   */
  ICommand getCommand(String commandName);
}