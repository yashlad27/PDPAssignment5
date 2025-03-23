# Calendar Application

## Overview
This is a virtual calendar application that mimics features found in widely used calendar apps such as Google Calendar or Apple's iCalendar app. The application supports creating, editing, and querying calendar events, as well as exporting the calendar to a CSV file.

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
- `create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`
- `create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`
- `create event [--autoDecline] <eventName> on <dateStringTtimeString>`
- `create event <eventName> on <dateStringTtimeString> repeats <weekdays> for <N> times`
- `create event <eventName> on <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`

### Edit Events
- `edit event <property> <eventName> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>`
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

### Feature Support Strategy
- **Approach**: Implemented all features in a single version
- **Why**:
    - Simpler deployment and maintenance
    - Consistent user experience
    - No need for feature flags or version checks
- **Limitations**:
    - All users get all features
    - No premium/advanced feature separation

### Design Extensions
- **Beyond Requirements**:
    - Added comprehensive error handling
    - Implemented validation at multiple levels
    - Added support for future event types
- **Future Extensibility**:
    - New event types can be added by creating new strategy classes
    - New commands can be added without modifying existing code
    - Calendar operations can be extended through the `ICalendar` interface

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

### Documentation
- All design decisions are documented in code comments
- Test cases demonstrate usage and behavior
- README provides high-level overview
- Architecture diagrams show component relationships

## Testing
- All code is thoroughly tested with JUnit tests
- PIT mutation testing is used to evaluate test quality
- Targeted 86% Test strength
- 69% for Mutation Coverage

## Working Features
- All features as specified in the requirements document are fully implemented
- The application handles conflicts between events as specified
- Export functionality produces CSV files compatible with Google Calendar

## Limitations and Future Enhancements
- **Multiple Calendars**: Currently supports only a single calendar; future versions could support multiple calendars
- **GUI**: Currently only offers a text-based interface
- **Attachments**: No support for adding file attachments to events
- **Search**: cannot search by keywords within event descriptions
- **Timezone Management**: Currently assumes all times are in EST; could add support for multiple timezones
- **Recurrence Exceptions**: Cannot create exceptions to recurring event patterns
- **Builder Pattern**: For recurring event file 
- **Headless Mode**: mocking for recurring event needs to be handled correctly.

## Team Contributions
- **Yash Lad**: Core event management and CSV export functionality
- **Gaurav Bidani**: Recurring events, conflict detection, and query functionality