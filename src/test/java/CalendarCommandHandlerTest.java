import org.junit.Test;

import controller.command.calendar.CalendarCommandHandler;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Test class for CalendarCommandHandler. Tests the handling of calendar commands and exception
 * handling functionality.
 */
public class CalendarCommandHandlerTest {

  @Test
  public void testExceptionHandlingWrapper()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler throwingHandler = args -> {
      throw new CalendarNotFoundException("Test calendar not found");
    };

    CalendarCommandHandler wrappedHandler = throwingHandler.withExceptionHandling();
    String result = wrappedHandler.execute(new String[]{});
    assertTrue(result.contains("Error: Test calendar not found"));
  }

  @Test
  public void testExceptionHandlingWrapperWithMultipleExceptions()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
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

  @Test
  public void testExceptionHandlingWrapperWithEmptyStringArgs()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler handler = args -> {
      if (args.length > 0 && args[0].isEmpty()) {
        throw new IllegalArgumentException("Empty argument not allowed");
      }
      return "Valid args";
    };

    CalendarCommandHandler wrapped = handler.withExceptionHandling();

    String result = wrapped.execute(new String[]{""});
    assertTrue(result.contains("Unexpected error: Empty argument not allowed"));
  }

  @Test
  public void testNestedExceptionHandling()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler innerHandler = args -> {
      throw new RuntimeException("Inner exception",
          new CalendarNotFoundException("Nested calendar not found"));
    };

    CalendarCommandHandler wrapped = innerHandler.withExceptionHandling();

    String result = wrapped.execute(new String[]{});
    assertTrue(result.contains("Unexpected error: Inner exception"));
  }

  @Test
  public void testChainedHandlerWrapping()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler baseHandler = args -> "Base result";

    CalendarCommandHandler wrapped1 = baseHandler.withExceptionHandling();
    CalendarCommandHandler wrapped2 = wrapped1.withExceptionHandling();
    CalendarCommandHandler wrapped3 = wrapped2.withExceptionHandling();

    String result = wrapped3.execute(new String[]{});
    assertEquals("Base result", result);
  }

  @Test
  public void testHandlerWithMalformedArguments()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler handler = args -> {
      if (args.length > 0 && args[0].contains("invalid")) {
        throw new IllegalArgumentException("Malformed argument detected");
      }
      return "Valid arguments";
    };

    CalendarCommandHandler wrapped = handler.withExceptionHandling();

    String result = wrapped.execute(new String[]{"invalid-format"});
    assertTrue(result.contains("Unexpected error: Malformed argument detected"));

    String successResult = wrapped.execute(new String[]{"valid-format"});
    assertEquals("Valid arguments", successResult);
  }

  @Test
  public void testExceptionMessageFormatConsistency()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {

    CalendarCommandHandler notFoundHandler = args -> {
      throw new CalendarNotFoundException("Calendar missing");
    };

    CalendarCommandHandler duplicateHandler = args -> {
      throw new DuplicateCalendarException("Calendar exists");
    };

    CalendarCommandHandler timezoneHandler = args -> {
      throw new InvalidTimezoneException("Bad timezone");
    };

    String result1 = notFoundHandler.withExceptionHandling().execute(new String[]{});
    String result2 = duplicateHandler.withExceptionHandling().execute(new String[]{});
    String result3 = timezoneHandler.withExceptionHandling().execute(new String[]{});

    assertEquals("Error: Calendar missing", result1);
    assertEquals("Error: Calendar exists", result2);
    assertEquals("Error: Bad timezone", result3);
  }

  @Test
  public void testExceptionHandlingWithEmptyArray()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    CalendarCommandHandler handler = args -> {
      if (args.length == 0) {
        return "No arguments provided";
      }
      return "Arguments provided: " + args.length;
    };

    CalendarCommandHandler wrapped = handler.withExceptionHandling();

    String result = wrapped.execute(new String[]{});
    assertEquals("No arguments provided", result);

    String resultWithArgs = wrapped.execute(new String[]{"arg1", "arg2"});
    assertEquals("Arguments provided: 2", resultWithArgs);
  }

  @Test
  public void testHandlerRetainsOriginalFunctionality()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    StringBuilder operationLog = new StringBuilder();

    CalendarCommandHandler handler = args -> {
      operationLog.append("Handler executed with ").append(args.length).append(" args");
      return "Operation completed";
    };

    CalendarCommandHandler wrapped = handler.withExceptionHandling();

    String result = wrapped.execute(new String[]{"test-arg"});
    assertEquals("Operation completed", result);
    assertEquals("Handler executed with 1 args", operationLog.toString());
  }

  @Test
  public void testExceptionHandlerWithNullReturn()
      throws CalendarNotFoundException, InvalidTimezoneException, DuplicateCalendarException {
    boolean[] handlerCalled = new boolean[1];

    CalendarCommandHandler handler = args -> {
      handlerCalled[0] = true;
      return null;
    };

    CalendarCommandHandler wrapped = handler.withExceptionHandling();

    String result = wrapped.execute(new String[]{"test"});
    assertFalse("Result should not contain error message",
        result != null && result.contains("error"));
    assertEquals(null, result);
    assertTrue("Handler should have been called", handlerCalled[0]);
  }
} 