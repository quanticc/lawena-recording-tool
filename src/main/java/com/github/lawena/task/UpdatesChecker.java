package com.github.lawena.task;

import com.github.lawena.Messages;
import com.github.lawena.domain.UpdateResult;
import com.github.lawena.service.VersionService;

public class UpdatesChecker extends LawenaTask<UpdateResult> {

    private final VersionService versionService;

    public UpdatesChecker(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    protected UpdateResult call() throws Exception {
        updateTitle(Messages.getString("ui.tasks.checker.title"));
        updateMessage(Messages.getString("ui.tasks.checker.message"));
        return versionService.checkForUpdates();
    }
}
