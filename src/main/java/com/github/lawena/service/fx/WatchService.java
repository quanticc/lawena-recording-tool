package com.github.lawena.service.fx;

import com.github.lawena.service.Resources;
import com.github.lawena.task.WatchTask;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * A JavaFX service to watch for directory changes, extended to have resource refreshing on user file handling capabilities.
 */
@Service
public class WatchService extends javafx.concurrent.Service<Void> {
    private static final Logger log = LoggerFactory.getLogger(WatchService.class);

    private final Resources resources;

    @Autowired
    public WatchService(Resources resources) {
        this.resources = resources;
    }

    @Override
    protected final void failed() {
        log.warn("Directory watcher failed with an exception", getException());
        super.failed();
    }

    @Override
    protected final Task<Void> createTask() {
        return new WatchTask(resources.foldersProperty()) {

            @Override
            public void entryModified(Path child) {
                resources.refreshResource(child);
            }

            @Override
            public void entryDeleted(Path child) {
                resources.deleteResource(child);
            }

            @Override
            public void entryCreated(Path child) {
                resources.newResource(child);
            }
        };

    }

}
