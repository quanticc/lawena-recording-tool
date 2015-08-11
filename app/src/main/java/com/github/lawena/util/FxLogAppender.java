package com.github.lawena.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Queue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import javafx.application.Platform;

@SuppressWarnings("nls")
public class FxLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> implements LogAppender {

    private static final Logger log = LoggerFactory.getLogger(FxLogAppender.class);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    private boolean _started = false;
    private boolean printingTrace = false;
    private Level minLevel = Level.DEBUG;
    private Queue<ILoggingEvent> queue = new ArrayDeque<>();
    private LogController controller;

    public FxLogAppender(LoggerContext context) {
        setName("LoggingAppender");
        setContext(context);
        start();
    }

    static String last(String str) {
        return str.substring(Math.max(0, str.lastIndexOf('.') + 1), str.length());
    }

    @Override
    public void startAppender() {
        log.debug("Starting Log Appender");
        _started = true;
        while (!queue.isEmpty()) {
            Platform.runLater(new WriteOutput(queue.poll()));
        }
    }

    @Override
    public void stopAppender() {
        _started = false;
    }

    public boolean isPrintingTrace() {
        return printingTrace;
    }

    public void setPrintingTrace(boolean printingTrace) {
        this.printingTrace = printingTrace;
    }

    public Level getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(Level minLevel) {
        this.minLevel = minLevel;
    }

    @Override
    public void append(ILoggingEvent event) {
        if (!event.getLoggerName().equals("status") && event.getLevel().isGreaterOrEqual(minLevel)) {
            if (event.getMarker() == null || !event.getMarker().contains("no-ui-log")) {
                if (_started) {
                    Platform.runLater(new WriteOutput(event));
                } else {
                    queue(event);
                }
            }
        }
    }

    private void queue(ILoggingEvent event) {
        queue.add(event);
    }

    @Override
    public LogController getController() {
        return controller;
    }

    @Override
    public void setController(LogController controller) {
        this.controller = controller;
    }

    public class WriteOutput implements Runnable {

        private ILoggingEvent _event;

        public WriteOutput(ILoggingEvent event) {
            this._event = event;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void run() {
            try {
                String message = _event.getFormattedMessage().trim();
                if (name.trim().equals(""))
                    return;
                Level level = _event.getLevel();
                String time = dateFormatter.format(_event.getTimeStamp());
                String logger = last(_event.getLoggerName());
                controller.append(String.format("%s %s [%s] %s\n", time, level, logger,
                        formatMsg(_event, message)));
            } catch (Exception e) {
                log.warn(MarkerFactory.getMarker("no-ui-log"), "Problem while appending to UI", e);
            }
        }

        @SuppressWarnings("synthetic-access")
        public String formatMsg(ILoggingEvent event, String message) {
            ThrowableProxy throwArr = (ThrowableProxy) event.getThrowableProxy();
            if (throwArr == null)
                return message;
            StringBuilder builder = new StringBuilder(message);
            Throwable ex = throwArr.getThrowable();
            if (printingTrace) {
                builder.append("\n");
                StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            } else {
                String exString = ex.toString();
                if (exString != null) {
                    builder.append("\n");
                    builder.append("----- Caused by " + exString);
                }
            }
            return builder.toString();
        }
    }
}
