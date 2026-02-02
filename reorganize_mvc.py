import os
import shutil
import re
from pathlib import Path

base_path = Path("m:\\Java\\Damose-0.3\\src\\main\\java\\damose")

# Define the file moves: (source, destination, old_package, new_package)
file_moves = [
    # From data/model to model
    ("data\\model\\Route.java", "model\\Route.java", "damose.data.model", "damose.model"),
    ("data\\model\\Stop.java", "model\\Stop.java", "damose.data.model", "damose.model"),
    ("data\\model\\StopTime.java", "model\\StopTime.java", "damose.data.model", "damose.model"),
    ("data\\model\\Trip.java", "model\\Trip.java", "damose.data.model", "damose.model"),
    ("data\\model\\TripServiceCalendar.java", "model\\TripServiceCalendar.java", "damose.data.model", "damose.model"),
    ("data\\model\\TripUpdateRecord.java", "model\\TripUpdateRecord.java", "damose.data.model", "damose.model"),
    ("data\\model\\VehiclePosition.java", "model\\VehiclePosition.java", "damose.data.model", "damose.model"),
    # model/*.java already in model (just need package updates)
    ("model\\BusWaypoint.java", "model\\BusWaypoint.java", "damose.model", "damose.model"),
    ("model\\ConnectionMode.java", "model\\ConnectionMode.java", "damose.model", "damose.model"),
    ("model\\StopWaypoint.java", "model\\StopWaypoint.java", "damose.model", "damose.model"),
    ("model\\VehicleType.java", "model\\VehicleType.java", "damose.model", "damose.model"),
    # From database to model (User.java)
    ("database\\User.java", "model\\User.java", "damose.database", "damose.model"),
    # From ui to view  
    ("ui\\MainView.java", "view\\MainView.java", "damose.ui", "damose.view"),
    ("ui\\component\\ConnectionButton.java", "view\\component\\ConnectionButton.java", "damose.ui.component", "damose.view.component"),
    ("ui\\component\\FloatingArrivalPanel.java", "view\\component\\FloatingArrivalPanel.java", "damose.ui.component", "damose.view.component"),
    ("ui\\component\\SearchOverlay.java", "view\\component\\SearchOverlay.java", "damose.ui.component", "damose.view.component"),
    ("ui\\component\\ServiceQualityPanel.java", "view\\component\\ServiceQualityPanel.java", "damose.ui.component", "damose.view.component"),
]

# Files that reference the old packages and need import updates
files_to_update_imports = [
    "app\\DamoseApp.java",
    "config\\AppConstants.java",
    "controller\\MainController.java",
    "data\\loader\\CalendarLoader.java",
    "data\\loader\\CsvParser.java",
    "data\\loader\\RoutesLoader.java",
    "data\\loader\\StopsLoader.java",
    "data\\loader\\StopTimesLoader.java",
    "data\\loader\\TripsLoader.java",
    "data\\mapper\\StopTripMapper.java",
    "data\\mapper\\TripIdUtils.java",
    "data\\mapper\\TripMatcher.java",
    "database\\DatabaseManager.java",
    "database\\SessionManager.java",
    "database\\UserService.java",
    "service\\ArrivalService.java",
    "service\\FavoritesService.java",
    "service\\GtfsParser.java",
    "service\\RealtimeService.java",
    "service\\RouteService.java",
    "service\\ServiceQualityTracker.java",
    "service\\StaticSimulator.java",
]

print("MVC Reorganization Script")
print("=" * 50)

# 1. Update imports in all files
print("\nPhase 1: Updating imports...")
for file_path in files_to_update_imports:
    full_path = base_path / file_path
    if full_path.exists():
        with open(full_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        # Update imports
        content = content.replace("import damose.data.model.", "import damose.model.")
        content = content.replace("import damose.ui;", "import damose.view;")
        content = content.replace("import damose.ui.", "import damose.view.")
        content = content.replace("import damose.database.User;", "import damose.model.User;")
        content = content.replace("import damose.data.model.Stop;", "import damose.model.Stop;")
        content = content.replace("import damose.data.model.Route;", "import damose.model.Route;")
        content = content.replace("import damose.data.model.Trip;", "import damose.model.Trip;")
        content = content.replace("import damose.data.model.StopTime;", "import damose.model.StopTime;")
        content = content.replace("import damose.data.model.VehiclePosition;", "import damose.model.VehiclePosition;")
        content = content.replace("import damose.data.model.TripUpdateRecord;", "import damose.model.TripUpdateRecord;")
        content = content.replace("import damose.data.model.TripServiceCalendar;", "import damose.model.TripServiceCalendar;")
        
        if content != original_content:
            with open(full_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"  Updated: {file_path}")

print("Phase 1 Complete!")
print("\nFiles updated successfully. Next, you should:")
print("1. Review the reorganized structure")
print("2. Build the project to check for any remaining issues")
print("3. Update test files if needed")
