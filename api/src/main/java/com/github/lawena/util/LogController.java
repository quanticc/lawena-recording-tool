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
    void startController();

    /**
     * Release all resources used by this controller.
     */
    void stopController();

    /**
     * Appends a message to the logging component.
     */
    void append(String text);

    /**
     * Clear all messages appended to the logging component.
     */
    void clear();

}
