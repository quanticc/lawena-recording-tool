package com.github.lawena.task;

import com.github.fge.lambdas.ThrownByLambdaException;
import com.github.lawena.Controller;
import com.github.lawena.Messages;
import com.github.lawena.exts.FileProvider;
import com.github.lawena.exts.ViewProvider;
import com.github.lawena.files.BackupMode;
import com.github.lawena.game.GameDescription;
import com.github.lawena.profile.Profile;
import com.github.lawena.util.LawenaException;
import com.github.lawena.util.Util;

import org.controlsfx.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class LaunchTask extends Task<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(LaunchTask.class);

    private final Controller controller;
    private final Profile profile;

    public LaunchTask(Controller controller) {
        this.controller = controller;
        this.profile = controller.getModel().getProfiles().getSelected();
    }

    private static Long size(Path p) {
        try {
            return Util.sizeOfPath(p);
        } catch (IOException e) {
            log.warn("Could not retrieve size of {}: {}", p, e.toString()); //$NON-NLS-1$
            return 0L;
        }
    }

    private static void showFailedValidationDialog(String string) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(Messages.getString("LaunchTask.ValidationFailTitle")); //$NON-NLS-1$
        alert.setHeaderText(Messages.getString("LaunchTask.ValidationFailHeader")); //$NON-NLS-1$
        alert.setContentText(Messages.getString("LaunchTask.ValidationFailContent")); //$NON-NLS-1$
        Label label = new Label(Messages.getString("LaunchTask.ValidationFailErrorList")); //$NON-NLS-1$
        TextArea textArea = new TextArea(string);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    private static BackupMode promptBackupAction(String size) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Messages.getString("LaunchTask.BackupWarnTitle")); //$NON-NLS-1$
        alert.setHeaderText(String.format(Messages.getString("LaunchTask.BackupWarnHeader"), size)); //$NON-NLS-1$
        alert.setContentText(Messages.getString("LaunchTask.BackupWarnContent")); //$NON-NLS-1$

        ButtonType buttonAbort =
                new ButtonType(
                        Messages.getString("LaunchTask.BackupWarnAbortButton"), ButtonData.CANCEL_CLOSE); //$NON-NLS-1$
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, buttonAbort);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES) {
            return BackupMode.ALWAYS;
        } else if (result.get() == ButtonType.NO) {
            return BackupMode.SOMETIMES;
        } else {
            return BackupMode.ABORT;
        }
    }

    private static void closeHandles(Path p) {
        // call OS functions or external applications to remove file handles/locks on the given path
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        if (osName.contains("Windows")) { //$NON-NLS-1$
            String tool = Paths.get("lwrt/tools/handle/handle.exe").toAbsolutePath().toString(); //$NON-NLS-1$
            startProcess(Arrays.asList(tool, p.toString()), 4, line -> {
                String[] columns = line.split("[ ]+type: [A-Za-z]+[ ]+|: |[ ]+pid: "); //$NON-NLS-1$
                if (columns.length == 4) {
                    log.debug("[handle] Closing handle {} opened by {}", columns[3], columns[0]); //$NON-NLS-1$
                    closeHandle(columns[1], columns[2]);
                } else {
                    log.debug("[handle] {}", line); //$NON-NLS-1$
                }
            });
        }
    }

    private static void closeHandle(String pid, String handle) {
        String tool = Paths.get("lwrt/tools/handle/handle.exe").toAbsolutePath().toString(); //$NON-NLS-1$
        startProcess(Arrays.asList(tool, "-c", handle, "-p", pid, "-y"), 7, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                line -> log.debug("[handle] {}", line)); //$NON-NLS-1$
    }

    private static int startProcess(List<String> command, int discard, Consumer<String> lineConsumer) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            Process p = builder.start();
            try (BufferedReader input = Util.newProcessReader(p)) {
                String line;
                int count = 0;
                while ((line = input.readLine()) != null) {
                    if (count > discard)
                        lineConsumer.accept(line);
                    count++;
                }
            }
            return p.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Process could not be completed", e); //$NON-NLS-1$
            return 1;
        }
    }

    @Override
    protected Boolean call() throws Exception {
        try {
            return innerCall();
        } catch (Exception e) {
            log.warn("Exception on launch", e);
        }
        return false;
    }

    private Boolean innerCall() throws Exception {
        updateTitle(Messages.getString("LaunchTask.Title")); //$NON-NLS-1$
        updateMessage(Messages.getString("LaunchTask.MessageStart")); //$NON-NLS-1$
        // disable launch game button, rename it to "Launching..."
        // users looking to abort task can head to Tasks tab
        controller.disable(true);
        controller.getLaunchButton().setText(Messages.getString("LaunchTask.LaunchButtonText"));
        // execute launch validations, requires to call extensions
        GameDescription game = Optional.ofNullable(controller.getModel().getGames().get(profile.getAppId())).get();
        // retrieve all view extensions
        List<ViewProvider> views =
                controller.getViewProviders().stream().filter(x -> game.getViews().contains(x.getName()))
                        .collect(Collectors.toList());
        // and perform validation
        List<ValidationResult> invalid =
                views.stream().map(x -> x.validate(profile)).filter(r -> !r.getErrors().isEmpty())
                        .collect(Collectors.toList());
        if (!invalid.isEmpty()) {
            // validation failed - don't continue!
            showFailedValidationDialog(invalid.stream().flatMap(r -> r.getErrors().stream())
                    .map(m -> m.getText() + "\n") //$NON-NLS-1$
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString());
            return Boolean.FALSE;
        }

        if (isCancelled()) {
            log.info("Launch cancelled by the user (1)"); //$NON-NLS-1$
            return Boolean.FALSE;
        }

        // get all folders used by the extensions
//        List<Path> dependencies =
//                exts.stream().flatMap(x -> x.dependentFolders(profile).stream())
//                        .collect(Collectors.toList());

        // alert in case of large resource folders that could cause OOM errors
//        Long size = dependencies.stream().mapToLong(LaunchTask::size).sum();
//        BackupMode backup = checkSize(size);
//        if (isCancelled() || backup == BackupMode.ABORT) {
//            log.warn("Aborted during folder size verification"); //$NON-NLS-1$
//            return Boolean.FALSE;
//        }
        // if the user selected "No" (don't create a backup)

        // close game handles on folder affected by file replacing
//        if (dependencies.stream().peek(LaunchTask::closeHandles).anyMatch(p -> isCancelled())) {
//            log.info("Launch cancelled by the user (3)"); //$NON-NLS-1$
//            return Boolean.FALSE;
//        }

        // RESTORE files previous to launch
        // TODO: see restore below
        // TODO: restore must be done by each plugin
        // exts.forEach(x -> x.restore(profile));

        // (optional: save profiles to disk - requires a Path)
        controller.saveProfiles();

        List<FileProvider> files =
                controller.getFileProviders().stream().filter(f -> game.getFiles().contains(f.getName()))
                        .collect(Collectors.toList());
        for (FileProvider file : files) {
            try {
                file.copyLaunchFiles(profile);
            } catch (LawenaException e) {
                log.warn("Launch operation failed", e);
            }
        }


        // write all configs for launch - there might be multiple write sources
        // REPLACE files for launch
        // TODO: each plugin will write its own files
        try {
//            List<Path> launchPaths =
//                    exts.stream().flatMap(Throwing.function(x -> x.launchPathList(profile).stream()))
//                            .collect(Collectors.toList());
            // TODO: launch folder must be created if not already done
            Files.createDirectories(Paths.get(game.getLocalGamePath()).resolve("launch"));
            // TODO: create each link or do each copy according to launchPaths
            // TODO: create the backup folder in game's files, send user files to them
            // TODO: finally, create the link or move or copy between the launch folder and the game's
            // folder
            //
        } catch (ThrownByLambdaException e) {
            // replace failed!
            log.warn("Replace failed", e); //$NON-NLS-1$
            return Boolean.FALSE;
        }
        // TODO:

        // EXECUTE steam command line -- OS dependent of course

        if (isCancelled()) {
            // user cancelled task - abort
            return Boolean.FALSE;
        }

        // wait for process to start - set a timeout!

        // game is live, wait until it's done
        // while (gameRuns) Thread.sleep(5000); // -- or some other value

        // game has closed, give a grace waiting time once more
        Thread.sleep(5000);

        // and then close handles again
//        dependencies.forEach(LaunchTask::closeHandles);

        // RESTORE user files to original folders
    /*
     * TODO: the main app does not delegate restore procedure to plugins - this portion must: (1)
     * Restore the game user files and (2) Cleanup the launch folder.
     */

        return Boolean.TRUE;
    }

    @Override
    protected void done() {
        // perform shutdown actions - to revert stuff beyond file replacing, like system dxlevel
        // controller.getViewProviders().stream()
        // .forEach(x -> x.shutdown(profile, valueProperty().get()));
        // re-enable launch button - remember to use Platform.runLater!
        Platform.runLater(() -> {
            controller.getLaunchButton().setText("Launch Game");
            controller.disable(false);
        });
    }

    private BackupMode checkSize(Long bytes) {
        Long limit = 200L; // 200 MB
        try {
            limit = Long.parseLong(profile.get("backup.warningSize").orElse(limit.toString())); //$NON-NLS-1$
        } catch (NumberFormatException ignored) {
        }
        // retrieve from profile only if "backup.rememberMode" is "true"
        boolean remember = Boolean.parseBoolean(profile.get("backup.rememberMode").orElse("false")); //$NON-NLS-1$ //$NON-NLS-2$
        BackupMode mode =
                !remember ? BackupMode.SOMETIMES : BackupMode.from(profile
                        .get("backup.mode").orElse("sometimes").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
        String size = Util.humanReadableByteCount(bytes, true);
        log.debug("Backup folders size: " + size); //$NON-NLS-1$
        // don't prompt if mode is already "ALWAYS" or "NEVER"
        if (mode == BackupMode.SOMETIMES && bytes / 1024 / 1024 > limit) {
            mode = promptBackupAction(size);
        }
        return mode;
    }
}
