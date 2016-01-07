package com.github.lawena.event;

import com.github.lawena.task.LaunchTask;
import org.springframework.context.ApplicationEvent;

public class LaunchNextStateEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LaunchNextStateEvent(LaunchTask source) {
        super(source);
    }

    @Override
    public LaunchTask getSource() {
        return (LaunchTask) super.getSource();
    }
}