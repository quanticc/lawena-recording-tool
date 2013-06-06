
package config;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class StartLogger {

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
                    String message = ex.getLocalizedMessage();
                    if (message != null) {
                        builder.append(" -- " + message);
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
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (component != null) {
                        component.setText(message);
                    }
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
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    Document doc = component.getDocument();
                    if (doc != null) {
                        try {
                            doc.insertString(doc.getLength(), message, null);
                        } catch (BadLocationException e) {
                        }
                    }
                }
            });
        }

    }

    private static final String newLine = System.getProperty("line.separator");
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final SimpleDateFormat DATETIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

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
        String pattern = logger.getName() + "%u.log";
        File logFolder = new File(pattern).getParentFile();
        try {
            if (logFolder != null && !logFolder.exists()) {
                logFolder.mkdir();
            }
            FileHandler localFileHandler = new FileHandler(pattern, 16384, 1, true);
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

}
