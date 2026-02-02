import shutil
import os
from pathlib import Path

base_path = Path("m:\\Java\\Damose-0.3\\src\\main\\java\\damose")

# File copy operations with package replacements
copy_operations = [
    ("ui\\MainView.java", "view\\MainView.java", "damose.ui", "damose.view"),
    ("ui\\component\\ConnectionButton.java", "view\\component\\ConnectionButton.java", "damose.ui.component", "damose.view.component"),
    ("ui\\component\\FloatingArrivalPanel.java", "view\\component\\FloatingArrivalPanel.java", "damose.ui.component", "damose.view.component"),
    ("ui\\component\\SearchOverlay.java", "view\\component\\SearchOverlay.java", "damose.ui.component", "damose.view.component"),
    ("ui\\component\\ServiceQualityPanel.java", "view\\component\\ServiceQualityPanel.java", "damose.ui.component", "damose.view.component"),
]

print("Copying UI files to View with updated packages...")
print("=" * 60)

for src_file, dest_file, old_pkg, new_pkg in copy_operations:
    src_path = base_path / src_file
    dest_path = base_path / dest_file
    
    if src_path.exists():
        # Read source file
        with open(src_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace package declaration
        content = content.replace(f"package {old_pkg};", f"package {new_pkg};")
        
        # Also fix any internal imports
        content = content.replace("import damose.data.model.", "import damose.model.")
        content = content.replace("import damose.ui.", "import damose.view.")
        content = content.replace("import damose.model.ConnectionMode;", "import damose.model.ConnectionMode;")
        
        # Create destination directory if it doesn't exist
        dest_path.parent.mkdir(parents=True, exist_ok=True)
        
        # Write to destination
        with open(dest_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✓ Created: {dest_file}")
    else:
        print(f"✗ Source not found: {src_file}")

print("\n" + "=" * 60)
print("View layer files created successfully!")
print("\nNext steps:")
print("1. Review the view layer structure")
print("2. Build the project to verify all imports are correct")
print("3. Test the application")
