/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * Based on LoggingAppender from https://code.google.com/p/quackedcube/
 */
package com.github.lawena.util;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class LoggingAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  private static final Logger log = LoggerFactory.getLogger(LoggingAppender.class);

  private final JTextPane pane;
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
  private boolean printStackTrace = false;
  private Level level = Level.DEBUG;

  public LoggingAppender(JTextPane pane, JScrollPane scroll, LoggerContext context) {
    this.pane = pane;
    setName("LoggingAppender");
    setContext(context);
    start();
    DefaultCaret caret = (DefaultCaret) pane.getCaret();
    caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    pane.getDocument().addDocumentListener(new ScrollingDocumentListener(pane, scroll));
  }

  public boolean isPrintStackTrace() {
    return printStackTrace;
  }

  public void setPrintStackTrace(boolean printStackTrace) {
    this.printStackTrace = printStackTrace;
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  @Override
  public void append(ILoggingEvent event) {
    if (!event.getLoggerName().equals("status") && event.getLevel().isGreaterOrEqual(level)) {
      if (event.getMarker() == null || !event.getMarker().contains("no-ui-log")) {
        SwingUtilities.invokeLater(new WriteOutput(event));
      }
    }
  }

  public class WriteOutput implements Runnable {

    private StyledDocument doc;
    private ILoggingEvent event;

    public WriteOutput(ILoggingEvent event) {
      this.doc = pane.getStyledDocument();
      this.event = event;

      if (doc.getStyle("Class") == null) {
        doc.addStyle("Normal", null);
        StyleConstants.setForeground(doc.addStyle("Class", null), Color.BLUE);
        StyleConstants.setForeground(doc.addStyle("Error", null), Color.RED);
        StyleConstants.setForeground(doc.addStyle("Debug", null), Color.GRAY);
        StyleConstants.setItalic(doc.addStyle("Level", null), true);
      }
    }

    @Override
    public void run() {
      try {
        String message = event.getFormattedMessage().trim();
        if (name.trim().equals(""))
          return;
        Style msgStyle = null;
        Level level = event.getLevel();
        if (level.isGreaterOrEqual(Level.WARN)) {
          msgStyle = doc.getStyle("Error");
        } else if (level.isGreaterOrEqual(Level.INFO)) {
          msgStyle = doc.getStyle("Normal");
        } else {
          msgStyle = doc.getStyle("Debug");
        }
        String time = dateFormatter.format(event.getTimeStamp());
        String logger = last(event.getLoggerName());
        int prevLength = doc.getLength();
        doc.insertString(doc.getLength(), "[" + time + "] ", doc.getStyle("Normal"));
        doc.insertString(doc.getLength(), "[" + logger + "] ", doc.getStyle("Class"));
        doc.insertString(doc.getLength(), formatMsg(event, message), msgStyle);
        doc.insertString(doc.getLength(), "\n", doc.getStyle("Normal"));
        pane.setCaretPosition(prevLength);
      } catch (Exception e) {
        log.warn(StatusAppender.NOGUI, "Problem while appending to textPane", e);
      }
    }

    public String formatMsg(ILoggingEvent event, String message) {
      ThrowableProxy throwArr = (ThrowableProxy) event.getThrowableProxy();
      if (throwArr == null)
        return message;
      StringBuilder builder = new StringBuilder(message);
      Throwable ex = throwArr.getThrowable();
      if (printStackTrace) {
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

  private static String last(String str) {
    return str.substring(Math.max(0, str.lastIndexOf('.') + 1), str.length());
  }
}
