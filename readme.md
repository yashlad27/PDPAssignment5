# Calendar Application Assignment 5

## Overview

This is a virtual calendar application that mimics features found in widely used calendar apps such
as Google Calendar or Apple's iCalendar app. The application supports creating, editing, and
querying calendar events, as well as exporting the calendar to a CSV file.

## Features

- Create single calendar events with subject, start date/time, and optional details
- Create recurring calendar events on specific weekdays
- Edit calendar events (single instances or recurring series)
- Query calendar for events on specific dates or date ranges
- Check availability at a specific date and time
- Export calendar to CSV format compatible with Google Calendar
- Interactive and headless operation modes
- Automatic conflict detection for events

## Technical Requirements

- Java version 11
- Maven build system
- PIT and Mutation Testing for quality assurance
- Implementation follows SOLID design principles
- Uses MVC architecture pattern

## How to Run

### Interactive Mode

```
java -jar model.calendar.CalendarApp.jar --mode interactive
```

In this mode, you can enter commands directly and see immediate results.

OR you would need to change your directory to src/ and run

```
java model.calendar.CalendarApp.java --mode interactive
```

### Headless Mode

```
java -jar calendar-app.jar --mode headless resources/commands.txt
```

In this mode, the program reads commands from a text file and executes them sequentially.

OR you would need to change your directory to src/ and run

```
java model.calendar.CalendarApp.java --mode headless resources/commands.txt 
```

## Command Reference

### Create Events

- `create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString>`
-
`create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`
-
`create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`
- `create event [--autoDecline] <eventName> on <dateStringTtimeString>`
- `create event <eventName> on <dateStringTtimeString> repeats <weekdays> for <N> times`
-
`create event <eventName> on <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`

### Edit Events

-
`edit event <property> <eventName> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>`
- `edit events <property> <eventName> from <dateStringTtimeString> with <NewPropertyValue>`
- `edit events <property> <eventName> <NewPropertyValue>`

### Query Events

- `print events on <dateStringTtimeString>`
- `print events from <dateStringTtimeString> to <dateStringTtimeString>`
- `show status on <dateStringTtimeString>`

### Export

- `export cal fileName.csv`

### Exit

- `exit`

## Implementation Details

### Architecture

The application follows the Model-View-Controller (MVC) architecture:

- **Model**: Represents the core calendar data structures and business logic
- **View**: Handles user interface and display formatting
- **Controller**: Processes user commands and updates the model accordingly

# Design Considerations and Architecture

## 7.1 Design Changes and Justifications

### Strategy Pattern Implementation

- **Change**: Introduced the Strategy Pattern for event creation
- **Why**:
    - Separates different event creation algorithms into distinct classes
    - Makes it easy to add new event types without modifying existing code
    - Follows the Open/Closed Principle
- **Example**: `AllDayEventCreator`, `RecurringEventCreator`, `RecurringUntilEventCreator`

### Command Pattern Enhancement

- **Change**: Extended command pattern to handle different event types
- **Why**:
    - Provides a unified interface for all event operations
    - Makes it easy to add new commands without changing existing code
    - Supports undo/redo operations consistently
- **Example**: `CreateCommand` now delegates to specific event creators

### MVC Architecture Adherence

- **Model**:
    - `Event`, `RecurringEvent` classes handle data and business logic
    - `ICalendar` interface defines calendar operations
- **View**:
    - `CalendarView` handles user interface
    - Supports both interactive and headless modes
- **Controller**:
    - `CalendarController` coordinates between model and view
    - `CommandParser` handles command interpretation

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
- SU: Saturday, Sunday

## Running the Application

### Interactive Mode

```bash
java -jar PDPAssignment5.jar --mode interactive
```

### Headless Mode

```bash
java -jar PDPAssignment5.jar --mode headless <command-file-path>
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
- JUnit 4 
- 
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

### Known Issues

- None at this time. All required functionality is implemented and working as specified.

## Team Contribution

- Calendar Management & Timezone Support: [Team Member Name]
- Copy Functionality & Command System: [Team Member Name]
- UI Improvements & Testing: [Team Member Name]
- Documentation & Bug Fixes: [Team Member Name]

## Exit

To exit the application, use the `exit` command.

### Advantages and Limitations

#### Advantages

1. **Modularity**
    - Each component has a single responsibility
    - Easy to test individual components
    - Simple to maintain and modify

2. **Extensibility**
    - New event types can be added without changing existing code
    - New commands can be added easily
    - Calendar operations can be extended

3. **Testability**
    - Clear separation of concerns makes testing easier
    - Mock objects can be used effectively
    - Each component can be tested in isolation

4. **Maintainability**
    - Code is well-organized and follows patterns
    - Changes are localized to specific components
    - Dependencies are clearly defined

#### Limitations

1. **Complexity**
    - Multiple patterns increase initial complexity
    - More classes to maintain
    - Higher learning curve for new developers

2. **Performance**
    - Strategy pattern adds slight overhead
    - Command pattern requires more memory
    - Multiple layers of abstraction

3. **Flexibility**
    - Some patterns might be overkill for simple features
    - Could be simplified for basic use cases
    - Might be too rigid for some future changes

### Future Improvements

1. **Feature Flags**
    - Could add feature flags for gradual rollout
    - Support for different editions (Basic/Pro)
    - A/B testing capability

2. **Performance Optimization**
    - Caching for frequently accessed data
    - Batch processing for multiple events
    - Optimized data structures

3. **User Experience**
    - More interactive UI features
    - Better error messages
    - More configuration options

## Testing

- Targeted 86% Test strength
- 69% for Mutation Coverage

## Working Features

- All features as specified in the requirements document are fully implemented
- The application handles conflicts between events as specified
- Export functionality produces CSV files compatible with Google Calendar

## Limitations and Future Enhancements

- **Multiple Calendars**: Currently supports only a single calendar; future versions could support
  multiple calendars
- **GUI**: Currently only offers a text-based interface
- **Attachments**: No support for adding file attachments to events
- **Search**: cannot search by keywords within event descriptions

## Team Contributions

- **Yash Lad**: Core event management and CSV export functionality
- **Gaurav Bidani**: Recurring events, conflict detection, and query functionality