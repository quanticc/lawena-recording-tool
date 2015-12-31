package com.github.lawena.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.controlsfx.control.TaskProgressView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();


    @Autowired
    private TaskProgressView<Task<?>> taskProgressView;

    public Future<?> submitTask(Task<?> task) {
        taskProgressView.getTasks().add(task);
        return executor.submit(task);
    }

    public Future<?> submitTask(Callable<?> callable) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    callable.call();
                } catch (Exception e) {
                    log.warn("Could not complete task", e);
                }
                return null;
            }
        };
        return submitTask(task);
    }

    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    public <T> Task<T> addToProgressView(Task<T> task) {
        taskProgressView.getTasks().add(task);
        return task;
    }

    /**
     * Schedule an action after a set time in milliseconds. The action will run in the JavaFX Application Thread.
     *
     * @param millis the time in milliseconds to wait before the action is dispatched
     * @param action a consumer with the action to schedule, specifying if the waiting was completed or interrupted
     */
    public void scheduleLater(long millis, Consumer<Boolean> action) {
        // watch if used too much, could end up blocking the common thread Pool (switch to a cached Executor)
        CompletableFuture.supplyAsync(() -> catchThySleep(millis), executor).thenAcceptAsync(action, Platform::runLater);
    }

    private static boolean catchThySleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
