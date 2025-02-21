package com.hoi4utils.ui.log_viewer;

import javafx.application.Platform;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import java.nio.charset.StandardCharsets;

public class LogViewerAppender extends AbstractAppender {

    public LogViewerAppender(String name, Layout<?> layout) {
        super(name, null, layout, false, null);
    }

    @Override
    public void append(LogEvent event) {
        String message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);

        // Ensure UI updates run on JavaFX thread
        Platform.runLater(() -> LogViewerController.addLog(message));
    }
}
