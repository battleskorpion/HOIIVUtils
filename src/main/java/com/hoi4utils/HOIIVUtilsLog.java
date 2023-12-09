package hoi4utils;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;  // Import the File class
import java.io.FileWriter;
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.text.SimpleDateFormat;

public class HOIIVUtilsLog {
	static File log;
	static File logsDir;

	/**
	 * format "date and time [message2] "
	 * @param message1 Actual message
	 * @param message2 stuff like WARNING or INFO
	 */
	public static void writeToLog(String message1, String message2) {
		File hoi4utils_file = new File(SettingsManager.HOI4UTILS_PROPERTIES_PATH);
		if (!hoi4utils_file.exists()) {
			return;
//			throw new RuntimeException("HOIIVUtils directory does not exist/could not be created");
		}
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = dateFormat.format(currentDate);

		try (FileWriter fileWriter = new FileWriter(log, true); // true for append mode
			 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			bufferedWriter.write(formattedDate + "    [" + message2 + "]    " + message1 + ".");
			bufferedWriter.newLine();
		} catch (IOException e) {
			writeToLog("writeToLog has caused a error lol, probly cause the log file doesn't exist or something", "ERROR");
			writeToLogError("writeToLog has caused a error lol", e);
			return;
		} catch (NullPointerException exc ) {
			if (log == null) {
				System.err.println("writeToLog has caused a error lol, " +
						"probly cause the log file doesn't exist or something");
			}
			return;
		}
	}

	public static void writeToLogError(String message1, Throwable throwable) {
		File hoi4utils_file = new File(SettingsManager.HOI4UTILS_PROPERTIES_PATH);
		if (!hoi4utils_file.exists()) {
			return;
//			throw new RuntimeException("HOIIVUtils directory does not exist/could not be created");
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);

		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = dateFormat.format(currentDate);

		try (FileWriter fileWriter = new FileWriter(log, true); // true for append mode
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			bufferedWriter.write(formattedDate + "    [ERROR]    " + message1 + ".");
			bufferedWriter.newLine();
			bufferedWriter.write("Here is the stacktrace:");
			bufferedWriter.newLine();
			bufferedWriter.write("------------START-----------");
			bufferedWriter.newLine();
			bufferedWriter.write(sw.toString());
			bufferedWriter.newLine();
			bufferedWriter.write("------------END-------------");
			bufferedWriter.newLine();
		} catch (IOException e) {
			writeToLog("writeToLogError has caused a error lol, probly cause the log file doesn't exist or something", "ERROR");
			writeToLogError("writeToLogError has caused a error lol", e);
		}
	}

	public static void openLogToDesktop() {
		try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (log.exists()) {
                    desktop.open(log);
                } else {
                   writeToLog("File does not exist.", "ERROR");
                }
            } else {
                writeToLog("Desktop is not supported on this platform.", "ERROR");
            }
        } catch (IOException e) {
            writeToLogError("openLogToDesktop has caused a error lol", e);
        }
	}

	static void startLog() {
		logsDir = createLogsDir();
		log = createTheLogFile();
		File hoi4utils_file = new File(SettingsManager.HOI4UTILS_PROPERTIES_PATH);
		if (!hoi4utils_file.exists()) {
			return;
//			throw new RuntimeException("HOIIVUtils directory does not exist/could not be created");
		}
		writeToLog(logsDir.toString(), "INFO");
		writeToLog(log.toString(), "INFO");
	}

	static File createLogsDir() {
		File nlogsDir = new File(SettingsManager.HOI4UTILS_PROPERTIES_PATH + File.separator + "logs");
		File hoi4utils_file = new File(SettingsManager.HOI4UTILS_PROPERTIES_PATH);
		if (!hoi4utils_file.exists()) {
			return null;
//			throw new RuntimeException("HOIIVUtils directory does not exist/could not be created");
		}
		if(!nlogsDir.exists()) {
			if(nlogsDir.mkdir()) {
				writeToLog("Directory created successfully.", "INFO");
			} else {
				writeToLog("Failed to create the directory.", "ERROR");
			}
		}
		return nlogsDir;
	}

	static File createTheLogFile() {
		File hoi4utils_file = new File(SettingsManager.HOI4UTILS_PROPERTIES_PATH);
		if (!hoi4utils_file.exists()) {
			return null;
//			throw new RuntimeException("HOIIVUtils directory does not exist/could not be created");
		}
		writeToLog(logsDir.toString(), "INFO");
		File currentLogFile = new File(logsDir + File.separator + "log.txt");
		try {
			if (currentLogFile.createNewFile()) {
				writeToLog("File created: " + currentLogFile.getName(), "INFO");
				deleteTheLogData(currentLogFile);
			} else {
				writeToLog("File already exists.", "INFO");
				deleteTheLogData(currentLogFile);
			}
		} catch (IOException e) {
            writeToLogError("createTheLogFile has caused a error lol", e);
		}
		return currentLogFile;
	}

	static void deleteTheLogData(File logFile) {
		File hoi4utils_file = new File(SettingsManager.HOI4UTILS_PROPERTIES_PATH);
		if (!hoi4utils_file.exists()) {
			return;
//			throw new RuntimeException("HOIIVUtils directory does not exist/could not be created");
		}
		 try (FileWriter fileWriter = new FileWriter(logFile)) {
			// Write an empty string to the file to clear its content
			fileWriter.write("");
			writeToLog("File content deleted successfully.", "INFO");
		} catch (IOException e) {
            writeToLogError("deleteTheLogData has caused a error lol", e);
		}
	}
}
