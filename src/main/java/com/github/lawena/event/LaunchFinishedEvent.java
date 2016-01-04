package com.github.lawena.event;

import com.github.lawena.task.LaunchTask;
import org.controlsfx.validation.ValidationResult;
import org.springframework.context.ApplicationEvent;

public class LaunchFinishedEvent extends ApplicationEvent {

    private final ValidationResult result;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LaunchFinishedEvent(LaunchTask source, ValidationResult result) {
        super(source);
        this.result = result;
    }

    @Override
    public LaunchTask getSource() {
        return (LaunchTask) super.getSource();
    }

    public ValidationResult getResult() {
        return result;
    }
}
