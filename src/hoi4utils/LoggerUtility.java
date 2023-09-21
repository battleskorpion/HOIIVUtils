package hoi4utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtility {
    private static final Logger logger = Logger.getLogger(LoggerUtility.class.getName());
    private static FileHandler fileHandler = null;

    public static void configureLogger() {
        try {
            // Create a FileHandler with a pattern to create log files dynamically
            fileHandler = new FileHandler("mylog%g.log", 1024 * 1024, 10, true);
            
            // Create a SimpleFormatter to format log messages
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            
            // Add the FileHandler to the logger
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)
            logger.setLevel(java.util.logging.Level.INFO);

        } catch (IOException e) {
            logger.severe("Error configuring logger: " + e.getMessage());
        }
    }

    public static void closeLogger() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}