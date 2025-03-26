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
    CalendarCommandHandler throwingHandler = args -> {
      throw new CalendarNotFoundException("Test calendar not found");
    };

    CalendarCommandHandler wrappedHandler = throwingHandler.withExceptionHandling();
    String result = wrappedHandler.execute(new String[]{});
    assertTrue(result.contains("Error: Test calendar not found"));
  }

  @Test
  public void testExceptionHandlingWrapperWithMultipleExceptions() throws CalendarNotFoundException,
          InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler throwingHandler = args -> {
      if (args.length == 0) {
        throw new CalendarNotFoundException("Calendar not found");
      } else if (args.length == 1) {
        throw new DuplicateCalendarException("Calendar already exists");
      } else {
        throw new InvalidTimezoneException("Invalid timezone");
      }
    };

    CalendarCommandHandler wrappedHandler = throwingHandler.withExceptionHandling();

    String result1 = wrappedHandler.execute(new String[]{});
    assertTrue(result1.contains("Error: Calendar not found"));

    String result2 = wrappedHandler.execute(new String[]{"test"});
    assertTrue(result2.contains("Error: Calendar already exists"));

    String result3 = wrappedHandler.execute(new String[]{"test", "test"});
    assertTrue(result3.contains("Error: Invalid timezone"));
  }

  @Test
  public void testExceptionHandlingWrapperWithUnexpectedException()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler throwingHandler = args -> {
      throw new RuntimeException("Unexpected error");
    };

    CalendarCommandHandler wrappedHandler = throwingHandler.withExceptionHandling();

    String result = wrappedHandler.execute(new String[]{});
    assertTrue(result.contains("Unexpected error: Unexpected error"));
  }

  @Test
  public void testExceptionHandlingWrapperWithSuccessfulExecution()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler successfulHandler = args -> "Success";

    CalendarCommandHandler wrappedHandler = successfulHandler.withExceptionHandling();

    String result = wrappedHandler.execute(new String[]{});
    assertEquals("Success", result);
  }

  @Test
  public void testExceptionHandlingWrapperWithNullArgs()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler nullHandler = args -> {
      if (args == null) {
        throw new IllegalArgumentException("Args cannot be null");
      }
      return "Success";
    };

    CalendarCommandHandler wrappedHandler = nullHandler.withExceptionHandling();

    String result = wrappedHandler.execute(null);
    assertTrue(result.contains("Unexpected error: Args cannot be null"));
  }

  @Test
  public void testExceptionHandlingWrapperReturnsNull()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler nullReturningHandler = args -> null;

    CalendarCommandHandler wrapped = nullReturningHandler.withExceptionHandling();

    String result = wrapped.execute(new String[]{});
    assertEquals(null, result);
  }

  @Test
  public void testExceptionHandlingWrapperReturnsWhitespace()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler whitespaceHandler = args -> "   Success with space   ";

    CalendarCommandHandler wrapped = whitespaceHandler.withExceptionHandling();

    String result = wrapped.execute(new String[]{});
    assertEquals("   Success with space   ", result);
  }

  @Test
  public void testExceptionHandlingWrapperWithLargeOutput()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    String largeOutput = "Success".repeat(1000);
    CalendarCommandHandler handler = args -> largeOutput;

    CalendarCommandHandler wrapped = handler.withExceptionHandling();

    String result = wrapped.execute(new String[]{});
    assertEquals(largeOutput, result);
  }

  @Test
  public void testExceptionHandlingWrapperWithSpecialCharacters()
          throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler handler = args -> "✓ Success @";

    CalendarCommandHandler wrapped = handler.withExceptionHandling();

    String result = wrapped.execute(new String[]{});
    assertEquals("✓ Success @", result);
  }
} 