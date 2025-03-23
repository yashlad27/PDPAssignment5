import org.junit.Test;

import controller.command.calendar.CalendarCommandHandler;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalendarCommandHandlerTest {

  @Test
  public void testExceptionHandlingWrapper() throws CalendarNotFoundException,
          InvalidTimezoneException, DuplicateCalendarException {
    // Create a handler that throws an exception
    CalendarCommandHandler throwingHandler = args -> {
      throw new CalendarNotFoundException("Test calendar not found");
    };

    // Wrap it with exception handling
    CalendarCommandHandler wrappedHandler = throwingHandler.withExceptionHandling();

    // Execute and verify the error message
    String result = wrappedHandler.execute(new String[]{});
    assertTrue(result.contains("Error: Test calendar not found"));
  }

  @Test
  public void testExceptionHandlingWrapperWithMultipleExceptions() throws CalendarNotFoundException,
          InvalidTimezoneException, DuplicateCalendarException {
    // Create a handler that throws different exceptions
    CalendarCommandHandler throwingHandler = args -> {
      if (args.length == 0) {
        throw new CalendarNotFoundException("Calendar not found");
      } else if (args.length == 1) {
        throw new DuplicateCalendarException("Calendar already exists");
      } else {
        throw new InvalidTimezoneException("Invalid timezone");
      }
    };

    // Wrap it with exception handling
    CalendarCommandHandler wrappedHandler = throwingHandler.withExceptionHandling();

    // Test CalendarNotFoundException
    String result1 = wrappedHandler.execute(new String[]{});
    assertTrue(result1.contains("Error: Calendar not found"));

    // Test DuplicateCalendarException
    String result2 = wrappedHandler.execute(new String[]{"test"});
    assertTrue(result2.contains("Error: Calendar already exists"));

    // Test InvalidTimezoneException
    String result3 = wrappedHandler.execute(new String[]{"test", "test"});
    assertTrue(result3.contains("Error: Invalid timezone"));
  }

  @Test
  public void testExceptionHandlingWrapperWithUnexpectedException()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    // Create a handler that throws an unexpected exception
    CalendarCommandHandler throwingHandler = args -> {
      throw new RuntimeException("Unexpected error");
    };

    // Wrap it with exception handling
    CalendarCommandHandler wrappedHandler = throwingHandler.withExceptionHandling();

    // Execute and verify the error message
    String result = wrappedHandler.execute(new String[]{});
    assertTrue(result.contains("Unexpected error: Unexpected error"));
  }

  @Test
  public void testExceptionHandlingWrapperWithSuccessfulExecution()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    // Create a handler that returns successfully
    CalendarCommandHandler successfulHandler = args -> "Success";

    // Wrap it with exception handling
    CalendarCommandHandler wrappedHandler = successfulHandler.withExceptionHandling();

    // Execute and verify the success message
    String result = wrappedHandler.execute(new String[]{});
    assertEquals("Success", result);
  }

  @Test
  public void testExceptionHandlingWrapperWithNullArgs()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    // Create a handler that handles null args
    CalendarCommandHandler nullHandler = args -> {
      if (args == null) {
        throw new IllegalArgumentException("Args cannot be null");
      }
      return "Success";
    };

    // Wrap it with exception handling
    CalendarCommandHandler wrappedHandler = nullHandler.withExceptionHandling();

    // Execute with null args and verify the error message
    String result = wrappedHandler.execute(null);
    assertTrue(result.contains("Unexpected error: Args cannot be null"));
  }
} 