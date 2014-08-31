/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * Based on LoggingAppender from https://code.google.com/p/quackedcube/
 */
package com.github.lawena.util;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class LoggingAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  private static final Logger log = LoggerFactory.getLogger(LoggingAppender.class);

  private final JTextPane pane;
  private JScrollPane scroll;
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
  private boolean printStackTrace = false;

  public LoggingAppender(JTextPane pane, JScrollPane scroll, LoggerContext context) {
    this.pane = pane;
    this.scroll = scroll;
    // configureScroll();
    setName("LoggingAppender");
    setContext(context);
    start();
  }

  private void configureScroll() {
    scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

      BoundedRangeModel brm = scroll.getVerticalScrollBar().getModel();
      boolean wasAtBottom = true;

      @Override
      public void adjustmentValueChanged(AdjustmentEvent e) {
        if (!brm.getValueIsAdjusting()) {
          if (wasAtBottom) {
            brm.setValue(brm.getMaximum());
          }
        } else {
          wasAtBottom = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
        }
      }
    });
  }

  public boolean isPrintStackTrace() {
    return printStackTrace;
  }

  public void setPrintStackTrace(boolean printStackTrace) {
    this.printStackTrace = printStackTrace;
  }

  @Override
  public void append(ILoggingEvent event) {
    if (!event.getLoggerName().equals("status")) {
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
        ImageIcon icon = null;
        Level level = event.getLevel();
        if (level.isGreaterOrEqual(Level.WARN)) {
          msgStyle = doc.getStyle("Error");
          if (level.isGreaterOrEqual(Level.ERROR)) {
            icon = Images.get("ui/fugue/error.png");
          } else {
            icon = Images.get("ui/fugue/warn.png");
          }
        } else if (level.isGreaterOrEqual(Level.INFO)) {
          msgStyle = doc.getStyle("Normal");
          icon = Images.get("ui/fugue/info.png");
        } else {
          msgStyle = doc.getStyle("Debug");
          icon = Images.get("ui/fugue/debug.png");
        }
        int prevLength = doc.getLength();
        pane.setCaretPosition(prevLength);
        pane.insertIcon(icon);
        doc.insertString(doc.getLength(), " [" + dateFormatter.format(event.getTimeStamp()) + "] ",
            doc.getStyle("Normal"));
        // doc.insertString(doc.getLength(), event.getLevel().toString() + "  ",
        // doc.getStyle("Level"));
        // doc.insertString(doc.getLength(), event.getLoggerName() + " ", doc.getStyle("Class"));
        doc.insertString(doc.getLength(), formatMsg(event, message), msgStyle);
        doc.insertString(doc.getLength(), "\n", doc.getStyle("Normal"));
        pane.setCaretPosition(prevLength);
      } catch (Exception e) {
        log.warn(MarkerFactory.getMarker("no-ui-log"), "Problem while appending to textPane", e);
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
          builder.append("\tCaused by " + exString);
        }
      }
      return builder.toString();
    }
  }
}
