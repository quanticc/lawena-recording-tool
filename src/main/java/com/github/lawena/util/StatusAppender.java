package com.github.lawena.util;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

@SuppressWarnings("nls")
public class StatusAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  private static final Logger log = LoggerFactory.getLogger(StatusAppender.class);

  public static final Marker OK = MarkerFactory.getMarker("OK");
  public static final Marker WARN = MarkerFactory.getMarker("WARN");
  public static final Marker ERROR = MarkerFactory.getMarker("ERROR");
  public static final Marker INFO = MarkerFactory.getMarker("INFO");
  public static final Marker NOGUI = MarkerFactory.getMarker("no-ui-log");

  protected final JLabel label;

  public StatusAppender(JLabel label, LoggerContext context) {
    this.label = label;
    setName("StatusAppender");
    setContext(context);
    start();
  }

  @Override
  public void append(final ILoggingEvent event) {
    SwingUtilities.invokeLater(new Runnable() {

      @SuppressWarnings("synthetic-access")
      @Override
      public void run() {
        try {
          String message = event.getFormattedMessage().trim();
          if (name.trim().equals(""))
            return;
          label.setIcon(null);
          if (event.getMarker() != null) {
            if (event.getMarker().contains("OK")) {
              label.setIcon(Images.get("ui/fugue/tick-circle-14.png"));
            } else if (event.getMarker().contains("WARN")) {
              label.setIcon(Images.get("ui/fugue/warn.png"));
            } else if (event.getMarker().contains("ERROR")) {
              label.setIcon(Images.get("ui/fugue/error.png"));
            } else if (event.getMarker().contains("INFO")) {
              label.setIcon(Images.get("ui/fugue/info.png"));
            }
          }
          label.setText(message);
        } catch (Exception e) {
          log.warn(MarkerFactory.getMarker("no-ui-log"), "Problem while appending to statusBar", e);
        }
      }
    });
  }

}
