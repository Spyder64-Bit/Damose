import os
from pathlib import Path

base_path = Path("m:\\Java\\Damose-0.3\\src\\test\\java\\damose")

# Test files that need import updates
test_files = [
    "data\\mapper\\TripIdUtilsTest.java",
    "data\\model\\StopTest.java",
    "data\\model\\StopTimeTest.java",
    "data\\model\\TripServiceCalendarTest.java",
    "data\\model\\TripTest.java",
    "model\\VehicleTypeTest.java",
    "service\\RouteServiceTest.java",
    "ui\\map\\GeoUtilsTest.java",
]

print("Updating test file imports...")
print("=" * 60)

for test_file in test_files:
    file_path = base_path / test_file
    if file_path.exists():
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Update imports
        content = content.replace("import damose.data.model.", "import damose.model.")
        content = content.replace("import damose.ui.", "import damose.view.")
        content = content.replace("import damose.ui;", "import damose.view;")
        content = content.replace("damose.data.model.", "damose.model.")
        
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ Updated: {test_file}")
        else:
            print(f"- No changes needed: {test_file}")
    else:
        print(f"✗ Not found: {test_file}")

print("\n" + "=" * 60)
print("Test files updated successfully!")
