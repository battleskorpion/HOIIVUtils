# CreatePortableApp.ps1
# Creates a portable version of HOIIVUtils with demo mod and maps included
# Copies necessary files from the project to a desktop folder and optionally creates a zip archive for distribution

# Stops the script if it runs into any issue
$ErrorActionPreference = "Stop"

# Configuration variables
$createZipFile = $false  # Set to $true if you want to create a zip archive

# Define paths
$sourceDir = "$HOME\programming\intelliJ\HOIIVUtils"
$targetDir = "$HOME\Desktop\HOIIVUtils"
$demoModSource = "$sourceDir\demo_mod"
$demoModTarget = "$targetDir\demo_mod"
$mapsSource = "$sourceDir\maps"
$mapsTarget = "$targetDir\maps"
$zipPath = "$HOME\Desktop\HOIIVUtils-Portable.zip"

# Create the main directory if it doesn't exist
if (-not (Test-Path $targetDir)) {
    New-Item -Path $targetDir -ItemType Directory -Force
    Write-Output "Created target directory: $targetDir"
}

# Create the target directory if it doesn't exist
if (-not (Test-Path "$targetDir\target")) {
    New-Item -Path "$targetDir\target" -ItemType Directory -Force
    Write-Output "Created target directory: $targetDir\target"
}

# Copy the main JAR file and scripts
Copy-Item -Path "$sourceDir\target\HOIIVUtils.jar" -Destination "$targetDir\target\HOIIVUtils.jar" -Force
Write-Output "Copied HOIIVUtils.jar"

Copy-Item -Path "$sourceDir\HOIIVUtils.bat" -Destination "$targetDir\HOIIVUtils.bat" -Force
Write-Output "Copied HOIIVUtils.bat"

Copy-Item -Path "$sourceDir\HOIIVUtils.sh" -Destination "$targetDir\HOIIVUtils.sh" -Force
Write-Output "Copied HOIIVUtils.sh"

# Handle the demo_mod directory recursively
if (Test-Path $demoModSource) {
    # Remove existing demo_mod directory if it exists to avoid stale files
    if (Test-Path $demoModTarget) {
        Remove-Item -Path $demoModTarget -Recurse -Force
        Write-Output "Removed existing demo_mod directory"
    }

    # Copy the entire demo_mod directory recursively
    Copy-Item -Path $demoModSource -Destination $targetDir -Recurse -Force
    Write-Output "Copied demo_mod directory recursively"
} else {
    Write-Output "Warning: Source demo_mod directory not found at $demoModSource"
}

# Handle the maps directory recursively
if (Test-Path $mapsSource) {
    # Remove existing maps directory if it exists to avoid stale files
    if (Test-Path $mapsTarget) {
        Remove-Item -Path $mapsTarget -Recurse -Force
        Write-Output "Removed existing maps directory"
    }

    # Copy the entire maps directory recursively
    Copy-Item -Path $mapsSource -Destination $targetDir -Recurse -Force
    Write-Output "Copied maps directory recursively"
} else {
    Write-Output "Warning: Source maps directory not found at $mapsSource"
}

# Create a ZIP file for distribution if enabled
if ($createZipFile) {
    if (Test-Path $zipPath) {
        Remove-Item -Path $zipPath -Force
        Write-Output "Removed existing ZIP file"
    }

    # Create the zip file using Compress-Archive (PowerShell 5.0+)
    Compress-Archive -Path $targetDir\* -DestinationPath $zipPath
    Write-Output "Created portable ZIP file at: $zipPath"
}

Write-Output "DONE! Portable app created successfully."