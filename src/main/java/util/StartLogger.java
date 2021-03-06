package util;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.*;

public class StartLogger {

    private static final String newLine = System.getProperty("line.separator");
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATETIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private Logger logger;

    public StartLogger(String loggerName) {
        logger = Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
    }

    public StartLogger toConsole(Level level) {
        ConsoleHandler localConsoleHandler = new ConsoleHandler();
        localConsoleHandler.setFormatter(new LogFormatter().fullTraces());
        localConsoleHandler.setLevel(level);
        logger.addHandler(localConsoleHandler);
        return this;
    }

    public StartLogger toFile(Level level) {
        String pattern = logger.getName() + ".%u.%g.log";
        File logFolder = new File(pattern).getParentFile();
        try {
            if (logFolder != null && !logFolder.exists()) {
                if (!logFolder.mkdir()) {
                    logger.warning("Could not create log folder");
                }
            }
            FileHandler localFileHandler = new FileHandler(pattern, 1024000, 3, true);
            localFileHandler.setFormatter(new LogFormatter().dateFormat(DATETIME).fullTraces());
            localFileHandler.setLevel(level);
            logger.addHandler(localFileHandler);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to start logging to file", e);
        }
        return this;
    }

    public StartLogger toTextComponent(Level level, JTextComponent component) {
        TextAreaHandler textAreaHandler = new TextAreaHandler(component);
        try {
            textAreaHandler.setFormatter(new LogFormatter());
            textAreaHandler.setLevel(level);
            logger.addHandler(textAreaHandler);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to start logging to textComponent", e);
        }
        return this;
    }

    public StartLogger toLabel(Level level, JLabel label) {
        LabelTextHandler labelTextHandler = new LabelTextHandler(label);
        try {
            labelTextHandler.setFormatter(new LogFormatter().dateFormat(null));
            labelTextHandler.setLevel(level);
            logger.addHandler(labelTextHandler);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to start logging to textComponent", e);
        }
        return this;
    }

    private static class LogFormatter extends Formatter {

        private SimpleDateFormat date = TIME;
        private boolean fullStackTraces = false;

        public LogFormatter() {

        }

        public LogFormatter dateFormat(SimpleDateFormat date) {
            this.date = date;
            return this;
        }

        public LogFormatter fullTraces() {
            fullStackTraces = true;
            return this;
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            Throwable ex = record.getThrown();
            if (date != null) {
                builder.append("[");
                builder.append(date.format(record.getMillis()));
                builder.append("] ");
            }
            builder.append(record.getMessage());
            if (ex != null) {
                if (fullStackTraces) {
                    builder.append(newLine);
                    StringWriter writer = new StringWriter();
                    ex.printStackTrace(new PrintWriter(writer));
                    builder.append(writer);
                } else {
                    String message = ex.toString();
                    if (message != null) {
                        builder.append(newLine);
                        builder.append("Caused by ").append(message);
                    }
                    builder.append(newLine);
                }
            } else {
                builder.append(newLine);
            }
            return builder.toString();
        }

    }

    private static class LabelTextHandler extends Handler {

        private JLabel component;

        public LabelTextHandler(JLabel component) {
            if (component == null) {
                throw new IllegalArgumentException("TextComponent must not be null");
            }
            this.component = component;
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public synchronized void publish(LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }
            final String message = getFormatter().format(record);
            SwingUtilities.invokeLater(() -> {
                if (component != null) {
                    component.setText(message);
                }
            });
        }

    }

    private static class TextAreaHandler extends Handler {

        private JTextComponent component;

        public TextAreaHandler(JTextComponent component) {
            if (component == null) {
                throw new IllegalArgumentException("TextComponent must not be null");
            }
            this.component = component;
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public synchronized void publish(LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }
            final String message = getFormatter().format(record);
            SwingUtilities.invokeLater(() -> {
                Document doc = component.getDocument();
                if (doc != null) {
                    try {
                        doc.insertString(doc.getLength(), message, null);
                    } catch (BadLocationException ignored) {
                    }
                }
            });
        }

    }

}
