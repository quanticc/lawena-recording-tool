package com.github.lawena.util;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class StatusAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

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
          label.setText(message);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

}
