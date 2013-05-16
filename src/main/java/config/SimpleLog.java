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

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class SimpleLog {

    public static class ShortLogFormatter extends Formatter {
        private final SimpleDateFormat date;

        public ShortLogFormatter(SimpleDateFormat date) {
            if (date == null) {
                date = TIME;
            }
            this.date = date;
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            Throwable ex = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append(newLine);

            if (ex != null) {
                StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }

    }

    static class TextAreaHandler extends Handler {
        private JTextArea textArea;

        public TextAreaHandler(JTextArea textArea) {
            if (textArea == null) {
                textArea = new JTextArea();
            }
            this.textArea = textArea;
        }

        public void close() {
        }

        public void flush() {
        }

        public synchronized void publish(LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }
            final String message = getFormatter().format(record);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    textArea.append(message);
                }
            });
        }

    }

    public static class TinyLogFormatter extends Formatter {
        private final SimpleDateFormat date;

        public TinyLogFormatter(SimpleDateFormat date) {
            if (date == null) {
                date = TIME;
            }
            this.date = date;
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            Throwable ex = record.getThrown();

            builder.append("[");
            builder.append(date.format(record.getMillis()));
            builder.append("] ");
            builder.append(record.getMessage());

            if (ex != null) {
                builder.append(" (check log for details)");
            }

            builder.append(newLine);

            return builder.toString();
        }

    }

    private static final String newLine = System.getProperty("line.separator");
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final SimpleDateFormat DATETIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

    public static String getLogName(String loggerName) {
        return "logs/" + loggerName + "_" + ((int) (System.currentTimeMillis() / 1000L)) + ".log";
    }

    private Logger log;
    private String pattern;

    public SimpleLog(String loggerName) {
        this(loggerName, getLogName(loggerName));
    }

    public SimpleLog(String loggerName, String patronLocal) {
        this(loggerName, patronLocal, new ShortLogFormatter(DATETIME));
    }

    public SimpleLog(String loggerName, String localPattern, Formatter localFormatter) {
        log = Logger.getLogger(loggerName);
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINE);
        pattern = localPattern;
    }

    public void fine(String message, Throwable t) {
        log.log(Level.FINE, message, t);
    }

    public Logger getLogger() {
        return log;
    }

    public void info(String message, Throwable t) {
        log.log(Level.INFO, message, t);
    }


    public void startConsoleLog() {
        startConsoleLog(Level.FINE);
    }
    
    public void startConsoleLog(Level level) {
        startConsoleLog(level, new ShortLogFormatter(DATETIME));
    }
    
    public void startConsoleLog(Level level, Formatter formatter) {
        ConsoleHandler localConsoleHandler = new ConsoleHandler();
        localConsoleHandler.setFormatter(formatter);
        localConsoleHandler.setLevel(level);
        log.addHandler(localConsoleHandler);
    }

    private void startLogfileOutput(Level level, Formatter formatter) {
        File logFolder = new File(pattern).getParentFile();
        try {
            if (logFolder != null && !logFolder.exists()) {
                logFolder.mkdir();
            }
            FileHandler localFileHandler = new FileHandler(pattern, true);
            localFileHandler.setFormatter(formatter);
            localFileHandler.setLevel(level);
            log.addHandler(localFileHandler);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to start logging to file", e);
        }
    }

    public void startShortLogfileOutput() {
        startLogfileOutput(Level.FINE, new ShortLogFormatter(DATETIME));
    }

    public void startTextAreaLog(JTextArea element, Level level) {
        TextAreaHandler textAreaHandler = new TextAreaHandler(element);
        try {
            textAreaHandler.setFormatter(new TinyLogFormatter(TIME));
            textAreaHandler.setLevel(level);
            log.addHandler(textAreaHandler);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to start logging to TextArea", e);
        }
    }

    public void startTinyLogfileOutput() {
        startLogfileOutput(Level.INFO, new TinyLogFormatter(TIME));
    }

    public void warn(String message, Throwable t) {
        log.log(Level.WARNING, message, t);
    }
}

