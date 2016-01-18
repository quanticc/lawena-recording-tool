package com.github.lawena.event;

import org.springframework.context.ApplicationEvent;

/**
 * Indicates that a new version has become available.
 */
public class NewVersionAvailable extends ApplicationEvent {
    /**
     * Create a NewVersionAvailable event.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public NewVersionAvailable(Object source) {
        super(source);
    }
}
