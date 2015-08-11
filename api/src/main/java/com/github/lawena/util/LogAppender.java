package com.github.lawena.util;

/**
 * Represents a logging appender that can communicate with a {@link LogController} to redirect
 * messages to a view component.
 *
 * @author Ivan
 */
public interface LogAppender {

    /**
     * Starts the appender mechanism. Must be called when the target controller is ready to accept
     * the forwarding of logging messages.
     */
    public void startAppender();

    /**
     * Stops the appender mechanism. Messages will not reach the target controller.
     */
    public void stopAppender();

    /**
     * Obtain the controller for message forwarding.
     */
    public LogController getController();

    /**
     * Define a controller for message forwarding.
     */
    public void setController(LogController controller);

}
