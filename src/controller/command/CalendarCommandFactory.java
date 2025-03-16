package controller.command;

import java.util.HashMap;
import java.util.Map;

import model.calendar.CalendarManager;
import view.ICalendarView;

public class CalendarCommandFactory {

  private final Map<String, CalendarCommandHandler> commands;
  private final CalendarManager calendarManager;
  private final ICalendarView view;

  public CalendarCommandFactory(CalendarManager calendarManager, ICalendarView view) {
    if (calendarManager == null) {
      throw new IllegalArgumentException("CalendarManager cannot be null");
    }

    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commands = new HashMap<>();
    this.calendarManager = calendarManager;
    this.view = view;

    registerCommands();
  }

  private void registerCommands() {
    commands.put("create", this::executeCreateCalendarCommand);
    commands.put("edit", this::executeEditCalendarCommand);
    commands.put("use", this::executeUseCalendarCommand);
    commands.put("copy", this::executeCopyCommand);
  }

  private String executeCopyCommand(String[] strings) {
  }

  private String executeUseCalendarCommand(String[] strings) {
  }

  private String executeEditCalendarCommand(String[] strings) {
  }

  private String executeCreateCalendarCommand(String[] strings) {
  }
}
