package com.hoi4utils.ui.log_viewer;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.scene.control.CheckBox;

public class LogViewerController extends HOIIVUtilsWindow {

    @FXML
    private ListView<String> logListView;
    @FXML
    private CheckBox startupCheck;

    private static ObservableList<String> logList = FXCollections.observableArrayList();

    public LogViewerController() {
        setFxmlResource("LogViewer.fxml");
        setTitle("Log Viewer");
    }

    @FXML
    public void initialize() {
        logListView.setItems(logList);
    }

    public static void addLog(String log) {
        logList.add(log);
    }

    @FXML
    private void clearLogs() {
        logList.clear();
    }

    @FXML
    private void startupCheck() {
        System.out.println("Startup check: " + startupCheck.isSelected());
    }
}
