import os
from pathlib import Path

base_path = Path("m:\\Java\\Damose-0.3\\src\\main\\java\\damose")

# Files still in old ui directory that need import updates
old_ui_files = [
    "ui\\MainView.java",
    "ui\\component\\ConnectionButton.java",
    "ui\\component\\FloatingArrivalPanel.java",
    "ui\\component\\SearchOverlay.java",
    "ui\\component\\ServiceQualityPanel.java",
    "ui\\dialog\\LoadingDialog.java",
    "ui\\dialog\\LoginDialog.java",
    "ui\\map\\GeoUtils.java",
    "ui\\map\\MapAnimator.java",
    "ui\\map\\MapFactory.java",
    "ui\\map\\MapOverlayManager.java",
    "ui\\render\\BusWaypointRenderer.java",
    "ui\\render\\RoutePainter.java",
    "ui\\render\\StopWaypointRenderer.java",
]

# Other files that might still have old imports
other_files = [
    "app\\DamoseApp.java",
    "controller\\MainController.java",
]

all_files = old_ui_files + other_files

print("Fixing imports in remaining files...")
print("=" * 60)

for file_path in all_files:
    full_path = base_path / file_path
    if full_path.exists():
        with open(full_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Fix old ui imports to new view
        content = content.replace("import damose.ui.", "import damose.view.")
        content = content.replace("import damose.ui;", "import damose.view;")
        content = content.replace("package damose.ui.", "package damose.view.")
        content = content.replace("package damose.ui;", "package damose.view;")
        
        # Fix old data.model imports to model
        content = content.replace("import damose.data.model.", "import damose.model.")
        content = content.replace("damose.data.model.", "damose.model.")
        
        # Fix specific type references
        content = content.replace("List<damose.data.model.Stop>", "List<damose.model.Stop>")
        content = content.replace("List<damose.model.Stop>", "List<damose.model.Stop>")
        
        if content != original_content:
            with open(full_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ Updated: {file_path}")
        else:
            print(f"- No changes: {file_path}")

print("\n" + "=" * 60)
print("Import fixes complete!")
