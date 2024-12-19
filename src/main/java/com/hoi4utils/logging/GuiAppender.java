package com.hoi4utils.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.Filter;

import com.hoi4utils.ui.log_viewer.LogViewerController;

public class GuiAppender extends AbstractAppender {

    public GuiAppender(String name, Filter filter, PatternLayout layout) {
        super(name, filter, layout, true);
        start(); // Start appender
    }

    @Override
    public void append(LogEvent event) {
        String message = getLayout().toSerializable(event).toString();
        LogViewerController.addLog(message);
    }
}
