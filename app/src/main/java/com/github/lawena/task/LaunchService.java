package com.github.lawena.task;

import com.github.lawena.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LaunchService extends Service<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(LaunchService.class);

    // properties

    // dependencies
    private final Controller controller;

    public LaunchService(Controller controller) {
        this.controller = controller;
    }

    @Override
    protected Task<Boolean> createTask() {
        Task<Boolean> task = new LaunchTask(controller);
        controller.getTasks().add(task);
        return task;
    }

    @Override
    protected void ready() {
        log.debug("Ready");
        super.ready();
    }

    @Override
    protected void scheduled() {
        log.debug("Scheduled");
        super.scheduled();
    }

    @Override
    protected void running() {
        log.debug("Running");
        super.running();
    }

    @Override
    protected void succeeded() {
        log.debug("Succeeded");
        super.succeeded();
    }

    @Override
    protected void failed() {
        log.debug("Failed");
        super.failed();
    }

    @Override
    protected void cancelled() {
        log.debug("Cancelled");
        super.cancelled();
    }
}
