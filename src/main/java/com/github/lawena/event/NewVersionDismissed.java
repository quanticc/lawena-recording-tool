package com.github.lawena.event;

import org.springframework.context.ApplicationEvent;

/**
 * Indicates that the new version was dismissed for the moment.
 */
public class NewVersionDismissed extends ApplicationEvent {
    /**
     * Create a NewVersionDismissed event.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public NewVersionDismissed(Object source) {
        super(source);
    }
}
