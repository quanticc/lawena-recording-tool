
package com.github.iabarca.lwrt.util;

public class LawenaException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public LawenaException(String msg) {
        super(msg);
    }

    public LawenaException(String msg, Throwable t) {
        super(msg, t);
    }

}
