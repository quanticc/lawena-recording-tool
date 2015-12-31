package com.github.lawena.views.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import javafx.application.Platform;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;

public class LogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final Logger log = LoggerFactory.getLogger(LogAppender.class);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    private static String last(String str) {
        return str.substring(Math.max(0, str.lastIndexOf('.') + 1), str.length());
    }

    private final StyleClassedTextArea textArea;

    @Autowired
    public LogAppender(LoggerContext context, StyleClassedTextArea textArea) {
        this.textArea = textArea;
        setName("LoggingAppender");
        setContext(context);
        start();
    }

    @Override
    public void append(ILoggingEvent event) {
        if (!event.getLoggerName().equals("status")
                && (event.getMarker() == null || !event.getMarker().contains("no-ui-log"))) {
            Platform.runLater(() -> appendToTextArea(event));
        }
    }

    private void appendToTextArea(ILoggingEvent event) {
        try {
            String message = event.getFormattedMessage().trim();
            if (name.trim().equals("")) {
                return;
            }
            Level level = event.getLevel();
            String time = dateFormatter.format(event.getTimeStamp());
            String logger = last(event.getLoggerName());
            String line = String.format("%s %s [%s] %s\n", time, level, logger, format(event, message));
            textArea.appendText(line);
        } catch (Exception e) {
            log.warn(MarkerFactory.getMarker("no-ui-log"), "Problem while appending to UI", e);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private String format(ILoggingEvent event, String message) {
        ThrowableProxy throwArr = (ThrowableProxy) event.getThrowableProxy();
        if (throwArr == null) {
            return message;
        }
        StringBuilder builder = new StringBuilder(message);
        String exString = throwArr.getThrowable().toString();
        if (exString != null) {
            builder.append("\n").append("----- Caused by ").append(exString);
        }
        return builder.toString();
    }

}
