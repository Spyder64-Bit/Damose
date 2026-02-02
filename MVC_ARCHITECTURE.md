# Damose MVC Architecture Reorganization

## Overview
Your project has been reorganized to follow the **Model-View-Controller (MVC)** architectural pattern, which provides better separation of concerns and improved maintainability.

## New Directory Structure

```
damose/
├── app/                    # Application Entry Point
│   └── DamoseApp.java      # Main application class
│
├── config/                 # Configuration & Constants
│   └── AppConstants.java   # App-wide constants
│
├── model/                  # Model Layer (Data Objects)
│   ├── Route.java          # Route entity (from data/model)
│   ├── Stop.java           # Stop entity (from data/model)
│   ├── StopTime.java       # Stop time entity (from data/model)
│   ├── Trip.java           # Trip entity (from data/model)
│   ├── TripServiceCalendar.java
│   ├── TripUpdateRecord.java
│   ├── VehiclePosition.java
│   ├── BusWaypoint.java    # Map waypoint for bus
│   ├── StopWaypoint.java   # Map waypoint for stop
│   ├── VehicleType.java    # Vehicle type enum
│   ├── ConnectionMode.java # Connection status enum
│   └── User.java           # User entity (from database)
│
├── view/                   # View Layer (UI Components)
│   ├── MainView.java       # Main application window
│   ├── component/          # UI Components
│   │   ├── ConnectionButton.java
│   │   ├── FloatingArrivalPanel.java
│   │   ├── SearchOverlay.java
│   │   └── ServiceQualityPanel.java
│   ├── dialog/             # Dialogs (if any)
│   ├── map/                # Map-related UI components
│   └── render/             # Custom rendering components
│
├── controller/             # Controller Layer
│   └── MainController.java # Main app controller
│
├── service/                # Business Logic Services
│   ├── ArrivalService.java
│   ├── FavoritesService.java
│   ├── GtfsParser.java
│   ├── RealtimeService.java
│   ├── RouteService.java
│   ├── ServiceQualityTracker.java
│   ├── StaticSimulator.java
│   └── UserService.java    # (moved from database)
│
├── data/                   # Data Loading & Processing
│   ├── loader/             # Data loaders
│   │   ├── CalendarLoader.java
│   │   ├── CsvParser.java
│   │   ├── RoutesLoader.java
│   │   ├── StopsLoader.java
│   │   ├── StopTimesLoader.java
│   │   └── TripsLoader.java
│   └── mapper/             # Data mapping utilities
│       ├── StopTripMapper.java
│       ├── TripIdUtils.java
│       └── TripMatcher.java
│
├── database/               # Database Operations
│   ├── DatabaseManager.java
│   ├── SessionManager.java
│   └── [UserService moved to service/]
│
└── util/                   # Utility Functions
    └── MemoryManager.java
```

## MVC Pattern Explanation

### Model Layer (`damose.model`)
- **Purpose**: Represents the data and business objects
- **Contains**: All entity classes (Stop, Route, Trip, User, VehiclePosition, etc.)
- **Responsibilities**:
  - Hold application data
  - Validate data integrity
  - Provide getters/setters
  - No UI dependencies

**Classes:**
- `Stop`, `Route`, `Trip`, `StopTime` - GTFS transit data
- `VehiclePosition`, `TripUpdateRecord` - Real-time data
- `BusWaypoint`, `StopWaypoint` - Map waypoints
- `VehicleType`, `ConnectionMode` - Enums for domain values
- `User` - User entity (moved from database package)

### View Layer (`damose.view`)
- **Purpose**: Handles all user interface components
- **Contains**: Swing UI components, panels, dialogs
- **Responsibilities**:
  - Display data to user
  - Capture user input
  - No business logic
  - Depends on Model but not vice versa

**Components:**
- `MainView` - Main application window
- `component/ConnectionButton` - Connection status button
- `component/FloatingArrivalPanel` - Arrivals panel
- `component/SearchOverlay` - Search interface
- `component/ServiceQualityPanel` - Service quality display

### Controller Layer (`damose.controller`)
- **Purpose**: Mediates between Model and View
- **Contains**: `MainController`
- **Responsibilities**:
  - Handle user events
  - Update model based on user actions
  - Update view based on model changes
  - Orchestrate workflow

### Service Layer (`damose.service`)
- **Purpose**: Contains business logic and operations
- **Contains**: Service classes for specific domains
- **Responsibilities**:
  - Implement business rules
  - Handle data processing
  - Manage external operations
  - Can use Model, Data, and Database layers

**Services:**
- `RouteService`, `ArrivalService`, `RealtimeService` - Transit data
- `FavoritesService` - User favorites management
- `ServiceQualityTracker` - Monitoring and metrics
- `GtfsParser` - Data parsing
- `StaticSimulator` - Simulation logic
- `UserService` - User operations (moved from database)

### Data Layer (`damose.data`)
- **Purpose**: Handles data loading and transformation
- **Contains**: Loaders and mappers
- **Responsibilities**:
  - Load data from files/sources
  - Parse CSV and other formats
  - Map external data to internal models

### Database Layer (`damose.database`)
- **Purpose**: Database access and persistence
- **Contains**: Database management classes
- **Responsibilities**:
  - Handle database connections
  - Perform CRUD operations
  - Manage sessions

## Package Import Changes

All imports have been updated to reflect the new structure:

| Old Import | New Import |
|-----------|-----------|
| `damose.data.model.*` | `damose.model.*` |
| `damose.ui.*` | `damose.view.*` |
| `damose.database.User` | `damose.model.User` |

## Files Moved

### From `damose/data/model/` to `damose/model/`:
- Route.java
- Stop.java
- StopTime.java
- Trip.java
- TripServiceCalendar.java
- TripUpdateRecord.java
- VehiclePosition.java

### From `damose/model/` to `damose/model/` (verified as MVC models):
- BusWaypoint.java
- StopWaypoint.java
- VehicleType.java
- ConnectionMode.java

### From `damose/database/` to `damose/model/`:
- User.java (data entity, now in model layer)

### From `damose/ui/` to `damose/view/`:
- MainView.java
- component/ConnectionButton.java
- component/FloatingArrivalPanel.java
- component/SearchOverlay.java
- component/ServiceQualityPanel.java

### From `damose/database/` to `damose/service/`:
- UserService.java (business logic, now in service layer)

## Benefits of This Organization

1. **Clear Separation of Concerns**
   - Model: Data only
   - View: UI only
   - Controller: Orchestration
   - Service: Business logic
   - Data: Loading/parsing

2. **Improved Maintainability**
   - Easy to locate classes by their responsibility
   - Changes to one layer don't affect others

3. **Better Testability**
   - Each layer can be tested independently
   - Mock objects can be easily injected

4. **Scalability**
   - Easy to add new features
   - Multiple developers can work on different layers

5. **Reusability**
   - Services can be reused across different views
   - Models can be used by multiple controllers

## Notes for Development

- **Old directories are still present** (`damose/ui/`, `damose/data/model/`, etc.)
  - Keep them for reference during transition
  - Remove them once you verify everything works
  - Make sure no code still imports from old packages

- **All imports have been updated** in:
  - Main source files
  - Test files
  - Both old and new files coexist temporarily

- **Next steps**:
  1. Build and test the project
  2. Verify no compilation errors
  3. Run all tests
  4. Delete old directories once confident
  5. Update documentation if needed

## Example: MVC Flow

### User searches for a stop:
1. **View** (SearchOverlay) - User types in search field
2. **Controller** (MainController) - Receives search event
3. **Service** (RouteService/ArrivalService) - Processes search logic
4. **Model** (Stop) - Returns matching stops
5. **View** (SearchOverlay) - Displays results to user

### Arrival information updates:
1. **Service** (RealtimeService) - Fetches real-time data
2. **Model** (VehiclePosition, TripUpdateRecord) - Updates data
3. **Controller** (MainController) - Detects model changes
4. **View** (FloatingArrivalPanel) - Displays updated information
5. **User** - Sees new arrival times

---

Generated: 2026-01-31
Project: Damose Transit Application
