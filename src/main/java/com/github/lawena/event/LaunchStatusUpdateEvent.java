package com.github.lawena.event;

import org.springframework.context.ApplicationEvent;

public class LaunchStatusUpdateEvent extends ApplicationEvent {

    public static LaunchStatusUpdateEvent updateEvent(Object source, String message) {
        return new LaunchStatusUpdateEvent(source).message(message);
    }

    public static LaunchStatusUpdateEvent updateEvent(Object source, String message, long workDone, long max) {
        return new LaunchStatusUpdateEvent(source).message(message).withProgress(workDone, max);
    }

    private String title;
    private String message;
    private double workDone = Double.NaN;
    private double max = Double.NaN;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public LaunchStatusUpdateEvent(Object source) {
        super(source);
    }

    public LaunchStatusUpdateEvent title(String title) {
        this.title = title;
        return this;
    }

    public LaunchStatusUpdateEvent message(String message) {
        this.message = message;
        return this;
    }

    public LaunchStatusUpdateEvent withProgress(long workDone, long max) {
        this.workDone = (double) workDone;
        this.max = (double) max;
        return this;
    }

    public LaunchStatusUpdateEvent withProgress(double workDone, double max) {
        this.workDone = workDone;
        this.max = max;
        return this;
    }

    public LaunchStatusUpdateEvent indeterminate() {
        this.workDone = -1;
        this.max = -1;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public double getWorkDone() {
        return workDone;
    }

    public double getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "LaunchStatusUpdateEvent [" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", workDone=" + workDone +
                ", max=" + max +
                ']';
    }
}
