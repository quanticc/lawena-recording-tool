package com.github.lawena.util;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class StatusAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  private static final Logger log = LoggerFactory.getLogger(StatusAppender.class);

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

      @Override
      public void run() {
        try {
          String message = event.getFormattedMessage().trim();
          if (name.trim().equals(""))
            return;
          label.setIcon(null);
          if (event.getMarker() != null) {
            if (event.getMarker().contains("OK")) {
              label.setIcon(Images.get("ui/fugue/tick-circle.png"));
            } else if (event.getMarker().contains("WARN")) {
              label.setIcon(Images.get("ui/fugue/exclamation.png"));
            } else if (event.getMarker().contains("ERROR")) {
              label.setIcon(Images.get("ui/fugue/cross-circle.png"));
            } else if (event.getMarker().contains("INFO")) {
              label.setIcon(Images.get("ui/fugue/information.png"));
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
