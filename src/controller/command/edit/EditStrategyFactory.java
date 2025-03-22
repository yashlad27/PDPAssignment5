package controller.command.edit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.calendar.ICalendar;
import utilities.DateTimeUtil;

public class EditStrategyFactory {
  private final List<EditStrategy> strategies;

  public EditStrategyFactory(ICalendar calendar) {
    strategies = new ArrayList<>();

    // Single event edit strategy using lambda
    strategies.add(createSingleEventStrategy(calendar));

    // Series from date edit strategy using lambda
    strategies.add(createSeriesFromDateStrategy(calendar));

    // All events edit strategy using lambda
    strategies.add(createAllEventsStrategy(calendar));
  }

  private EditStrategy createSingleEventStrategy(ICalendar calendar) {
    return new EditStrategy() {
      @Override
      public String execute(String[] args) throws ConflictingEventException, InvalidEventException, EventNotFoundException {
        if (args.length < 5) {
          throw new InvalidEventException("Insufficient arguments for editing a single event");
        }

        String property = args[1];
        String subject = args[2];
        LocalDateTime startDateTime = DateTimeUtil.parseDateTime(args[3]);
        String newValue = removeQuotes(args[4]);

        boolean success = calendar.editSingleEvent(subject, startDateTime, property, newValue);
        if (success) {
          return "Successfully edited event '" + subject + "'.";
        } else {
          throw new EventNotFoundException("Event not found: " + subject + " at " + startDateTime);
        }
      }

      @Override
      public boolean matchesCommand(String commandType) {
        return "single".equals(commandType);
      }
    };
  }

  private EditStrategy createSeriesFromDateStrategy(ICalendar calendar) {
    return new EditStrategy() {
      @Override
      public String execute(String[] args) throws ConflictingEventException, InvalidEventException {
        // Implementation similar to SeriesFromDateEditStrategy
        // ...
        return "Series events edited";
      }

      @Override
      public boolean matchesCommand(String commandType) {
        return "series_from_date".equals(commandType);
      }
    };
  }

  private EditStrategy createAllEventsStrategy(ICalendar calendar) {
    return new EditStrategy() {
      @Override
      public String execute(String[] args) throws ConflictingEventException, InvalidEventException {
        // Implementation similar to AllEventsEditStrategy
        // ...
        return "All events edited";
      }

      @Override
      public boolean matchesCommand(String commandType) {
        return "all".equals(commandType);
      }
    };
  }

  public EditStrategy getStrategy(String[] args) {
    if (args.length < 1) {
      return null;
    }

    return strategies.stream()
            .filter(strategy -> strategy.canHandle(args))
            .findFirst()
            .orElse(null);
  }

  private String removeQuotes(String value) {
    if (value != null && value.length() >= 2) {
      if ((value.startsWith("\"") && value.endsWith("\"")) ||
              (value.startsWith("'") && value.endsWith("'"))) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }
}