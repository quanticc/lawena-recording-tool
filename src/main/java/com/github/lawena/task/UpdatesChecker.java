package com.github.lawena.task;

import com.github.lawena.domain.UpdateResult;
import com.github.lawena.service.VersionService;

public class UpdatesChecker extends LawenaTask<UpdateResult> {

    private final VersionService versionService;

    public UpdatesChecker(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    protected UpdateResult call() throws Exception {
        updateTitle("Lawena Recording Tool");
        updateMessage("Checking for updates...");
        return versionService.checkForUpdates();
    }
}
