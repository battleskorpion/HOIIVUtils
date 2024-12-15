@echo off
REM Get the path to the JAR file
set JAR_PATH="HOIIVUtils-jar-with-dependencies.jar.jar"

REM Open a new terminal and run the JAR
start cmd /k java -jar %JAR_PATH%