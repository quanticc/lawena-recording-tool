package com.github.lawena.util;

/**
 * Generic exception class for Lawena-related actions.
 *
 * @author Ivan
 */
public class LawenaException extends Exception {

    private static final long serialVersionUID = 1L;

    public LawenaException(String msg) {
        super(msg);
    }

    public LawenaException(String msg, Throwable t) {
        super(msg, t);
    }

}
