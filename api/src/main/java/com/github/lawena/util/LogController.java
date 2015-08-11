package com.github.lawena.util;


/**
 * A manager for logging messages to some view component.
 *
 * @author Ivan
 */
public interface LogController {

    /**
     * Prepare all required components for the logging mechanism to work.
     */
    public void startController();

    /**
     * Release all resources used by this controller.
     */
    public void stopController();

    /**
     * Appends a message to the logging component.
     */
    public void append(String text);

    /**
     * Clear all messages appended to the logging component.
     */
    public void clear();

}
