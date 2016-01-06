package com.github.lawena.event;

import org.springframework.context.ApplicationEvent;

public class LaunchersUpdatedEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LaunchersUpdatedEvent(Object source) {
        super(source);
    }
}
