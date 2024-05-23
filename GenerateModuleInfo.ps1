# Path to the base directory to scan
$baseDir = "src\main\java\com\HOIIVUtils\ui"

# Path to the output module-info.java file
$moduleInfoPath = "src\main\java\module-info.java"

# Start of the module-info.java content
$moduleContent = @"
module com.HOIIVUtils {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires org.apache.logging.log4j;
    requires java.sql;
    requires java.desktop;
    requires org.jetbrains.annotations;
    requires org.jgrapht.core;
    requires org.apache.poi.poi;
    requires aparapi;            // leave aparapi last cause aparapi has terrible config
    requires org.xerial.sqlitejdbc;

    opens com.HOIIVUtils.ui to javafx.fxml;

"@

# Function to scan directories and generate export statements
function Scan-Directory($dir) {
    $subDirs = Get-ChildItem -Path $dir -Directory
    foreach ($subDir in $subDirs) {
        $relativePath = $subDir.FullName.Substring($baseDir.Length + 1)
        $packageName = $relativePath -replace '\\', '.'
        $moduleContent += "    exports com.HOIIVUtils.ui.$packageName`r`n"
        Scan-Directory -dir $subDir.FullName
    }
}

# Initial call to scan the base directory
Scan-Directory -dir $baseDir

# End of the module-info.java content
$moduleContent += "    exports com.HOIIVUtils.ui;
    exports com.HOIIVUtils.clausewitz_parser;
    exports com.HOIIVUtils.hoi4utils;
}"

# Write the content to the module-info.java file
Set-Content -Path $moduleInfoPath -Value $moduleContent
