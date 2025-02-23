package com.hoi4utils.ui.log_viewer;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.nio.file.*;

public class LogViewerController extends HOIIVUtilsWindow {
    private static final String LOG_FILE_PATH = "latest.log";

    @FXML
    private TextArea logTextArea;

    public LogViewerController() {
        setFxmlResource("LogViewer.fxml");
        setTitle("Log Viewer");
    }

    public void initialize() {
        loadLogFile();
        watchLogFile();
    }

    private void loadLogFile() {
        try {
            String content = Files.readString(Paths.get(LOG_FILE_PATH));
            logTextArea.setText(content);
        } catch (IOException e) {
            logTextArea.setText("Log file not found.");
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
}
