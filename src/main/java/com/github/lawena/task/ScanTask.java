package com.github.lawena.task;

import com.github.lawena.Messages;
import com.github.lawena.domain.Resource;
import com.github.lawena.service.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ScanTask extends LawenaTask<Set<Resource>> {
    private static final Logger log = LoggerFactory.getLogger(ScanTask.class);

    private final Resources resources;
    private final Path path;

    public ScanTask(Resources resources, Path path) {
        this.resources = resources;
        this.path = path;
    }

    @Override
    public Group getGroup() {
        return Group.RESOURCE;
    }

    @Override
    protected Set<Resource> call() throws Exception {
        try {
            updateTitle(Messages.getString("ui.base.tasks.scan.title"));
            updateMessage(path.toAbsolutePath().toString());
            return resources.scanForResources(path);
        } catch (Exception e) {
            log.warn("Could not scan folder", e);
        }
        return null;
    }

    @Override
    protected void cancelled() {
        log.debug("Scan of {} was cancelled", path);
        super.cancelled();
    }

    @Override
    protected void failed() {
        log.debug("Scanned {} failed due to {}", path, getException());
        super.failed();
    }

    @Override
    protected void succeeded() {
        try {
            log.debug("Scanned {} and found {}", path, get());
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

}
