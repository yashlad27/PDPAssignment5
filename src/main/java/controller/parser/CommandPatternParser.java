package controller.parser;

import java.util.regex.Matcher;

/**
 * Functional interface for parsing commands based on regex matcher.
 */
@FunctionalInterface
public interface CommandPatternParser {
  CommandParser.CommandWithArgs parse(Matcher matcher);
}