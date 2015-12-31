package com.github.lawena.util;

/**
 * An exception representing a failed launch process.
 */
public class LaunchException extends Exception {

    public LaunchException(Throwable cause) {
        super(cause);
    }

    public LaunchException() {
    }

    public LaunchException(String message) {
        super(message);
    }

    public LaunchException(String message, Throwable cause) {
        super(message, cause);
    }
}
