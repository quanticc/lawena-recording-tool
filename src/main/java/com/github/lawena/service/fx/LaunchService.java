package com.github.lawena.service.fx;

import com.github.lawena.service.TaskService;
import com.github.lawena.task.LaunchTask;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class LaunchService extends javafx.concurrent.Service<Boolean> {

    private final ApplicationContext context;
    private final TaskService taskService;

    @Autowired
    public LaunchService(ApplicationContext context, TaskService taskService) {
        this.context = context;
        this.taskService = taskService;
    }

    @Override
    protected Task<Boolean> createTask() {
        Task<Boolean> task = context.getBean(LaunchTask.class);
        taskService.addToProgressView(task);
        return task;
    }
}
