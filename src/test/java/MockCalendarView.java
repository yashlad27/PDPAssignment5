import java.util.ArrayList;
import java.util.List;

import view.ICalendarView;

/**
 * Mock implementation of ICalendarView for testing purposes. Tracks displayed messages and errors
 * for verification in tests.
 */
public class MockCalendarView implements ICalendarView {

  private final List<String> displayedMessages;
  private final List<String> displayedErrors;

  public MockCalendarView() {
    this.displayedMessages = new ArrayList<>();
    this.displayedErrors = new ArrayList<>();
  }

  @Override
  public String readCommand() {
    return "";
  }

  @Override
  public void displayMessage(String message) {
    displayedMessages.add(message);
  }

  @Override
  public void displayError(String error) {
    displayedErrors.add(error);
  }

  public List<String> getDisplayedMessages() {
    return new ArrayList<>(displayedMessages);
  }

  public List<String> getDisplayedErrors() {
    return new ArrayList<>(displayedErrors);
  }

  public void clear() {
    displayedMessages.clear();
    displayedErrors.clear();
  }
} 