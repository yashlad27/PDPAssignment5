package controller.command.edit;

import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;

@FunctionalInterface
public interface EditStrategy {
  String execute(String[] args) throws ConflictingEventException, InvalidEventException, EventNotFoundException;

  // Default method to check if strategy can handle the args
  default boolean canHandle(String[] args) {
    return args.length >= 1 && matchesCommand(args[0]);
  }

  // Default method to check if the strategy matches a command type
  default boolean matchesCommand(String commandType) {
    return false;
  }
}