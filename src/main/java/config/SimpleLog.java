
package config;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SimpleLog {

    public static class LogFormatter extends Formatter {
        private final SimpleDateFormat date;

        public LogFormatter(SimpleDateFormat date) {
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

    private static final String newLine = System.getProperty("line.separator");
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final SimpleDateFormat DATETIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

    public static String getLogName(String loggerName) {
        return "logs/" + loggerName + "_" + ((int) (System.currentTimeMillis() / 1000L)) + ".log";
    }

    private Logger logger;
    private String pattern;

    public SimpleLog(String loggerName) {
        this(loggerName, getLogName(loggerName));
    }

    public SimpleLog(String loggerName, String patronLocal) {
        this(loggerName, patronLocal, new LogFormatter(DATETIME));
    }

    public SimpleLog(String loggerName, String localPattern, Formatter localFormatter) {
        logger = Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.INFO);
        pattern = localPattern;
    }

    public Logger getLogger() {
        return logger;
    }

    public void startLoggingToConsole() {
        startLoggingToConsole(Level.INFO, new LogFormatter(DATETIME));
    }

    public void startLoggingToConsole(Level level, Formatter formatter) {
        ConsoleHandler localConsoleHandler = new ConsoleHandler();
        localConsoleHandler.setFormatter(formatter);
        localConsoleHandler.setLevel(level);
        logger.addHandler(localConsoleHandler);
    }

    public void startLoggingToFile() {
        startLoggingToFile(Level.INFO, new LogFormatter(DATETIME));
    }

    private void startLoggingToFile(Level level, Formatter formatter) {
        File logFolder = new File(pattern).getParentFile();
        try {
            if (logFolder != null && !logFolder.exists()) {
                logFolder.mkdir();
            }
            FileHandler localFileHandler = new FileHandler(pattern, true);
            localFileHandler.setFormatter(formatter);
            localFileHandler.setLevel(level);
            logger.addHandler(localFileHandler);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to start logging to file", e);
        }
    }

}
