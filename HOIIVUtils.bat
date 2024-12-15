@echo off
REM Get the path to the JAR file
set JAR_PATH="target/HOIIVUtils-jar-with-dependencies.jar"

REM Open a new terminal and run the JAR
start cmd /k java -jar %JAR_PATH%