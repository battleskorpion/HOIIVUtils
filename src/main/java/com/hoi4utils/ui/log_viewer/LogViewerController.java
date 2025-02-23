package com.hoi4utils.ui.log_viewer;

import com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;

public class LogViewerController extends HOIIVUtilsWindow {
    private static final String LOG_FILE_PATH = "latest.log";
    private static final String CONFIG_FILE_PATH = "config.properties";

    @FXML private TextFlow logTextFlow;
    @FXML private CheckBox infoCheckbox, debugCheckbox, warnCheckbox, errorCheckbox, fatalCheckbox;
    @FXML private CheckBox openOnStartupCheckbox;

    public LogViewerController() {
        setFxmlResource("LogViewer.fxml");
        setTitle("Log Viewer");
    }
    
    public void initialize() {
        loadConfig();
        loadLogFile();
        watchLogFile();

        // TODO: Add listeners to checkboxes
//        infoCheckbox.setOnAction(e -> filterLogs());
//        debugCheckbox.setOnAction(e -> filterLogs());
//        warnCheckbox.setOnAction(e -> filterLogs());
//        errorCheckbox.setOnAction(e -> filterLogs());
//        fatalCheckbox.setOnAction(e -> filterLogs());

        openOnStartupCheckbox.setOnAction(e -> saveConfig(openOnStartupCheckbox.isSelected()));
    }

    private void loadLogFile() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(LOG_FILE_PATH));
            updateLogDisplay(lines);
        } catch (IOException e) {
            logTextFlow.getChildren().clear();
            logTextFlow.getChildren().add(new Text("Log file not found.\n"));
        }
    }

    private void watchLogFile() {
        Thread logWatcher = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path logDir = Paths.get("logs");
                logDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().equals("latest.log")) {
                            Platform.runLater(this::loadLogFile);
                        }
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        logWatcher.setDaemon(true);
        logWatcher.start();
    }

    private void updateLogDisplay(List<String> logs) {
        logTextFlow.getChildren().clear();
        for (String log : logs) {
            if (shouldDisplayLog(log)) {
                logTextFlow.getChildren().add(formatLogEntry(log));
            }
        }
    }

    private boolean shouldDisplayLog(String log) {
        if (log.contains("INFO") && infoCheckbox.isSelected()) return true;
        if (log.contains("DEBUG") && debugCheckbox.isSelected()) return true;
        if (log.contains("WARN") && warnCheckbox.isSelected()) return true;
        if (log.contains("ERROR") && errorCheckbox.isSelected()) return true;
        if (log.contains("FATAL") && fatalCheckbox.isSelected()) return true;
        return false;
    }

    private Text formatLogEntry(String log) {
        Text logText = new Text(log + "\n");
        if (log.contains("ERROR")) logText.setFill(Color.RED);
        else if (log.contains("WARN")) logText.setFill(Color.ORANGE);
        else if (log.contains("DEBUG")) logText.setFill(Color.GRAY);
        else logText.setFill(Color.BLACK);
        return logText;
    }

    private void loadConfig() {
        Properties props = new Properties();
        try {
            props.load(Files.newInputStream(Paths.get(CONFIG_FILE_PATH)));
            boolean openOnStartup = Boolean.parseBoolean(props.getProperty("openLogViewerOnStartup", "false"));
            openOnStartupCheckbox.setSelected(openOnStartup);
        } catch (IOException e) {
            // File might not exist yet, ignore
        }
    }

    private void saveConfig(boolean openOnStartup) {
        Properties props = new Properties();
        props.setProperty("openLogViewerOnStartup", String.valueOf(openOnStartup));
        try {
            props.store(Files.newOutputStream(Paths.get(CONFIG_FILE_PATH)), "Application Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
