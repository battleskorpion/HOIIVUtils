#!/bin/bash
# Get the path to the JAR file
JAR_PATH="target/HOIIVUtils-jar-with-dependencies.jar"

# Open a new terminal and run the JAR
if command -v gnome-terminal >/dev/null 2>&1; then
    gnome-terminal -- bash -c "java -jar \"$JAR_PATH\"; exec bash"
elif command -v konsole >/dev/null 2>&1; then
    konsole -e bash -c "java -jar \"$JAR_PATH\"; exec bash"
elif command -v xfce4-terminal >/dev/null 2>&1; then
    xfce4-terminal -e "bash -c \"java -jar '$JAR_PATH'; exec bash\""
else
    echo "No supported terminal emulator found. Run this manually: java -jar \"$JAR_PATH\""
fi
