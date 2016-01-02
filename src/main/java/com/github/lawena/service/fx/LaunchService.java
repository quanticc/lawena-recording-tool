package com.github.lawena.service.fx;

import com.github.lawena.service.TaskService;
import com.github.lawena.task.LaunchTask;
import javafx.concurrent.Task;
import org.controlsfx.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class LaunchService extends javafx.concurrent.Service<ValidationResult> {

    private final ApplicationContext context;
    private final TaskService taskService;

    @Autowired
    public LaunchService(ApplicationContext context, TaskService taskService) {
        this.context = context;
        this.taskService = taskService;
    }

    @Override
    protected Task<ValidationResult> createTask() {
        Task<ValidationResult> task = context.getBean(LaunchTask.class);
        taskService.addToProgressView(task);
        return task;
    }
}
