# Calendar Application

A command-line calendar application that allows users to manage multiple calendars and events with
support for different timezones, recurring events, and event management operations.

## Design Changes

### 1. Multi-Calendar Support

- **Added `CalendarManager` class**: Serves as the central coordinator for managing multiple
  calendars
- **Added `CalendarRegistry` class**: Stores and indexes calendars by name, maintaining the active
  calendar reference
- **Added `ICalendar` interface**: Provides an abstraction for calendar operations to support
  polymorphism
- **Justification**: These changes create a clear separation of concerns where `CalendarRegistry`
  handles storage while `CalendarManager` manages high-level operations, following Single
  Responsibility Principle

### 2. Timezone Support

- **Added `TimeZoneHandler` class**: Validates timezones and converts times between different
  timezones
- **Added `TimezoneConverter` interface**: Provides a functional way to convert times between
  timezones
- **Enhanced `Calendar` class**: Added timezone property and methods to handle timezone-specific
  operations
- **Justification**: This approach encapsulates timezone logic and makes it reusable across the
  application

### 3. Command System Enhancement

- **Created `CalendarCommandFactory`**: Dedicated factory for calendar-specific commands
- **Added `CalendarCommandHandler` interface**: Functional interface for calendar commands
- **Updated `CalendarController`**: Now routes commands to either event or calendar command
  factories
- **Justification**: This design allows for independent extension of both event and calendar
  commands

### 4. Copy Functionality

- **Added `CopyEventCommand` class**: Unified command for all copy operations
- **Implemented Strategy Pattern**: Created `CopyStrategy` interface with implementations:
    - `SingleEventCopyStrategy`
    - `DayEventsCopyStrategy`
    - `RangeEventsCopyStrategy`
- **Added `CopyStrategyFactory`**: Selects appropriate copy strategy based on command
- **Justification**: Strategy pattern allows for extensible and maintainable copy operations with
  different behaviors

### 5. Builder Pattern for Recurring Events

- **Implemented Builder in `RecurringEvent`**: Allows for step-by-step construction with validation
- **Moved validation to build time**: Ensures objects are created in a valid state
- **Added fluent interface**: Makes code more readable when creating recurring events
- **Justification**: Builder pattern improves the construction of complex objects with many optional
  parameters

### 6. Additional Exception Types

- **Added `CalendarNotFoundException`**: For operations on non-existent calendars
- **Added `DuplicateCalendarException`**: For attempts to create calendars with duplicate names
- **Added `InvalidTimezoneException`**: For operations with invalid timezone specifications
- **Justification**: Specialized exceptions improve error handling and provide clearer feedback

### 7. Enhanced Validation

- **Added `CalendarNameValidator`**: Centralizes calendar name validation logic
- **Improved command validation**: More robust parameter checking in command handlers
- **Enhanced error reporting**: More descriptive error messages
- **Justification**: Better validation improves reliability and user experience

## Features

### Calendar Management

- Create multiple calendars with different timezones
- Switch between calendars using the `use calendar` command
- Edit calendar properties (e.g., timezone)
- Export calendar events to CSV files

### Event Management

- Create one-time and recurring events
- Support for different repeat patterns (MWF, TR, SU)
- Edit event properties (subject, location)
- Copy events between calendars
- Show event status at specific times

### Command Modes

- Interactive Mode: User can input commands directly
- Headless Mode: Execute commands from a file

## Command Syntax

### Calendar Commands

```
create calendar --name <name> --timezone <timezone>
use calendar --name <name>
edit calendar --name <name> --property <property> <value>
export cal <filename>.csv
```

### Event Commands

```
create event "<subject>" from <start-time> to <end-time> [desc "<description>"] [at "<location>"] [repeats <pattern> for <count> times]
print events [on <date>] [from <start-date> to <end-date>]
copy event "<subject>" on <date-time> --target <calendar> to <target-date-time>
copy events [on <date>] [between <start-date> and <end-date>] --target <calendar> to <target-date>
edit event subject "<subject>" from <date-time> with "<new-subject>"
edit event location "<subject>" from <date-time> with "<new-location>"
show status on <date-time>
```

### Date/Time Format

- Date format: YYYY-MM-DD
- Time format: hh:mm
- Combined format: YYYY-MM-DDThh:mm

### Repeat Patterns

- MWF: Monday, Wednesday, Friday
- TR: Tuesday, Thursday
- SU: Sunday

## Running the Application

### Using Maven (Recommended with Java 11+)

### Using Java directly

```bash
# From project root directory
mvn compile
java -cp target/classes CalendarApp --mode interactive
java -cp target/classes CalendarApp --mode headless src/resources/headlessCmd.txt
```

### Application using .jar

- please move .jar file to root directory before executing the following commands.

### Interactive Mode

To run the application in interactive mode:

```bash
java -jar target/calendar-app.jar --mode interactive
```

### Headless Mode

To run the application in headless mode with a command file:

```bash
java -jar target/calendar-app.jar --mode headless src/resources/headlessCmd.txt
```

## Example Command Files

- `resources/headlessCmd.txt`: Contains valid commands for testing
- `resources/invalidCommands.txt`: Contains invalid commands for error handling testing

## Error Handling

The application handles various error cases including:

- Invalid date/time formats
- Missing required parameters
- Non-existent calendars
- Invalid repeat patterns
- Invalid timezones
- End time before start time

## Dependencies

- Java 11 or higher
- JUnit 4 for testing

## Project Structure

```
src/
├── controller/
│   ├── command/
│   │   ├── calendar/
│   │   └── event/
│   └── parser/
├── model/
│   ├── calendar/
│   └── exceptions/
├── view/
└── CalendarApp.java
```

## Feature Status

### Working Features

- Multiple calendar creation and management
- Timezone support for calendars
- Event creation (single, recurring, all-day)
- Event editing and querying
- Copy events between calendars (single event, day, range)
- Interactive and headless modes
- CSV export

## Team Contribution

- Calendar Management & Timezone Support: [Gaurav Bidani]
- Copy Functionality & Command System: [Yash Lad]
- UI Improvements & Testing: [Gaurav Bidani]
- Documentation & Bug Fixes: [Yash Lad]

## Exit

To exit the application, use the `exit` command.
