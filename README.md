# Event Planning Application

A comprehensive event planning application built with **Kotlin** and **Scala**, featuring a JavaFX desktop GUI.

## Features

### Kotlin Components (Domain, GUI, Persistence)

- **Domain Classes** - Event, Venue, Participant, Session with proper separation of concerns
- **Event Creation** - Create events with title, date, time, venue, and capacity
- **Participant Registration** - Register participants with capacity validation
- **Persistence** - JSON file-based data storage

### Scala Algorithms (Functional Programming)

- **Slot Finder** - Finds the first available venue for a planned size and earliest start date
- **Event Scheduler** - Creates conflict-free schedules for multiple events

## Project Structure

```
event-planning-app/
├── kotlin-app/                    # Kotlin module
│   ├── src/main/kotlin/com/eventplanner/
│   │   ├── domain/                # Domain classes
│   │   │   ├── Event.kt
│   │   │   ├── Venue.kt
│   │   │   ├── Participant.kt
│   │   │   └── Session.kt
│   │   ├── persistence/           # Data persistence
│   │   │   └── DataStore.kt
│   │   ├── service/               # Business logic
│   │   │   └── ValidationUtils.kt
│   │   ├── ui/                    # JavaFX GUI
│   │   │   ├── MainView.kt
│   │   │   ├── EventView.kt
│   │   │   ├── VenueView.kt
│   │   │   ├── ParticipantView.kt
│   │   │   ├── RegistrationView.kt
│   │   │   ├── SessionView.kt
│   │   │   ├── SlotFinderView.kt
│   │   │   └── SchedulerView.kt
│   │   └── Main.kt
│   └── src/test/kotlin/           # Unit tests
│       └── com/eventplanner/
│           ├── domain/
│           │   ├── EventTest.kt
│           │   ├── VenueTest.kt
│           │   └── ParticipantTest.kt
│           └── service/
│               └── ValidationUtilsTest.kt
├── scala-algorithms/              # Scala module
│   ├── src/main/scala/com/eventplanner/algorithms/
│   │   ├── SlotFinder.scala
│   │   └── EventScheduler.scala
│   └── src/test/scala/            # Scala tests
│       └── com/eventplanner/algorithms/
│           ├── SlotFinderTest.scala
│           └── EventSchedulerTest.scala
├── build.gradle.kts
└── settings.gradle.kts
```

## Requirements

- JDK 17 or higher
- Gradle 8.x

## Building and Running

### Build the project
```bash
./gradlew build
```

### Run the application
```bash
./gradlew :kotlin-app:run
```

### Run all tests
```bash
./gradlew test
```

### Run Kotlin tests only
```bash
./gradlew :kotlin-app:test
```

### Run Scala tests only
```bash
./gradlew :scala-algorithms:test
```

## Usage Guide

1. **Venues Tab** - Start by adding venues with name, capacity, and location
2. **Events Tab** - Create events and assign them to venues
3. **Participants Tab** - Add participants with contact information
4. **Registration Tab** - Register participants to events (enforces capacity limits)
5. **Slot Finder Tab** - Find available venue slots using the Scala algorithm
6. **Event Scheduler Tab** - Generate conflict-free schedules

## Architecture Highlights

### Code Quality
- Low coupling and high cohesion
- Separation of concerns (UI logic separate from domain)
- Input validation and error handling
- Clean, readable, self-documenting code

### Design Patterns
- MVC architecture for GUI
- Repository pattern for data access
- Immutable data classes (Kotlin data classes, Scala case classes)

### Functional Programming (Scala)
- Pure functions with no side effects
- Immutable data structures
- Higher-order functions (map, filter, fold)
- Pattern matching

## Technologies

- **Kotlin 1.9.20** - Main application language
- **Scala 2.13.12** - Algorithm implementation
- **JavaFX 21** - Desktop GUI framework
- **Kotlinx Serialization** - JSON persistence
- **Gradle** - Build system

## Data Persistence

Data is saved to `event_planner_data.json` in the application directory. This includes:
- All venues
- All events
- All participants
- Registration information

## License

Educational project for software engineering coursework.
