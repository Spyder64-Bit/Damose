import shutil
from pathlib import Path

base_path = Path("m:\\Java\\Damose-0.3\\src\\main\\java\\damose")

# Additional UI files to copy to view
additional_files = [
    ("ui\\dialog\\LoadingDialog.java", "view\\dialog\\LoadingDialog.java", "damose.ui.dialog", "damose.view.dialog"),
    ("ui\\dialog\\LoginDialog.java", "view\\dialog\\LoginDialog.java", "damose.ui.dialog", "damose.view.dialog"),
    ("ui\\map\\GeoUtils.java", "view\\map\\GeoUtils.java", "damose.ui.map", "damose.view.map"),
    ("ui\\map\\MapAnimator.java", "view\\map\\MapAnimator.java", "damose.ui.map", "damose.view.map"),
    ("ui\\map\\MapFactory.java", "view\\map\\MapFactory.java", "damose.ui.map", "damose.view.map"),
    ("ui\\map\\MapOverlayManager.java", "view\\map\\MapOverlayManager.java", "damose.ui.map", "damose.view.map"),
    ("ui\\render\\BusWaypointRenderer.java", "view\\render\\BusWaypointRenderer.java", "damose.ui.render", "damose.view.render"),
    ("ui\\render\\RoutePainter.java", "view\\render\\RoutePainter.java", "damose.ui.render", "damose.view.render"),
    ("ui\\render\\StopWaypointRenderer.java", "view\\render\\StopWaypointRenderer.java", "damose.ui.render", "damose.view.render"),
]

print("Moving remaining UI files to View layer...")
print("=" * 60)

for src_file, dest_file, old_pkg, new_pkg in additional_files:
    src_path = base_path / src_file
    dest_path = base_path / dest_file
    
    if src_path.exists():
        # Read source file
        with open(src_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace package declaration
        content = content.replace(f"package {old_pkg};", f"package {new_pkg};")
        
        # Also fix any internal imports
        content = content.replace("import damose.ui.", "import damose.view.")
        content = content.replace("import damose.ui;", "import damose.view;")
        content = content.replace("import damose.data.model.", "import damose.model.")
        content = content.replace("import damose.model.Route;", "import damose.model.Route;")
        content = content.replace("import damose.model.Stop;", "import damose.model.Stop;")
        
        # Create destination directory if it doesn't exist
        dest_path.parent.mkdir(parents=True, exist_ok=True)
        
        # Write to destination
        with open(dest_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✓ Created: {dest_file}")
    else:
        print(f"✗ Source not found: {src_file}")

print("\n" + "=" * 60)
print("All view layer files created successfully!")
