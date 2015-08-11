package com.github.lawena.files;

import com.github.lawena.i18n.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;

public abstract class ScanTask extends Task<Set<Resource>> {
    private static final Logger log = LoggerFactory.getLogger(ScanTask.class);

    private final List<? extends Path> folders;

    public ScanTask(List<? extends Path> folders) {
        this.folders = folders;
    }

    @Override
    protected Set<Resource> call() throws Exception {
        log.debug("Scanning {}", folders); //$NON-NLS-1$
        updateTitle(Messages.getString("ScanTask.Title")); //$NON-NLS-1$
        updateMessage(Messages.getString("ScanTask.MessageGeneral")); //$NON-NLS-1$
        updateProgress(0, folders.size());
        // scan for the current state of each folder
        Set<Resource> set =
                folders.stream().flatMap(p -> scan(p).stream()).peek(r -> up(r))
                        .collect(Collectors.toSet());
        return set;
    }

    private void up(Resource r) {
        updateMessage(r.getPath().toAbsolutePath().toString());
        Platform.runLater(() -> updateProgress(getWorkDone() + 1, getTotalWork()));
    }

    public abstract Set<Resource> scan(Path dir);

    @Override
    protected void running() {
        log.debug("RUNNING"); //$NON-NLS-1$
        super.running();
    }

    @Override
    protected void done() {
        Platform.runLater(() -> log.debug("{}", getState())); //$NON-NLS-1$
        super.done();
    }
}
