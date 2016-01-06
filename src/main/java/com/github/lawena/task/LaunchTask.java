package com.github.lawena.task;

import com.github.lawena.Messages;
import com.github.lawena.config.Constants;
import com.github.lawena.config.LawenaProperties;
import com.github.lawena.domain.DataValidationMessage;
import com.github.lawena.domain.Launcher;
import com.github.lawena.event.LaunchFinishedEvent;
import com.github.lawena.event.LaunchNextStateEvent;
import com.github.lawena.event.LaunchStartedEvent;
import com.github.lawena.service.FileService;
import com.github.lawena.service.ValidationService;
import com.github.lawena.util.LaunchException;
import com.github.lawena.util.LwrtUtils;
import com.github.lawena.views.base.BasePresenter;
import javafx.application.Platform;
import org.controlsfx.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.github.lawena.util.LwrtUtils.newProcessReader;

public class LaunchTask extends LawenaTask<ValidationResult> {

    private static final Logger log = LoggerFactory.getLogger(LaunchTask.class);

    private final LawenaProperties properties;
    private final FileService fileService;
    private final ValidationService validationService;
    private final BasePresenter basePresenter;
    private final ApplicationEventPublisher publisher;
    private String launchButtonText;

    @Autowired
    public LaunchTask(LawenaProperties properties, FileService fileService, ValidationService validationService,
                      BasePresenter basePresenter, ApplicationEventPublisher publisher) {
        this.properties = properties;
        this.fileService = fileService;
        this.validationService = validationService;
        this.basePresenter = basePresenter;
        this.publisher = publisher;
    }

    @Override
    public Group getGroup() {
        return Group.LAUNCH;
    }

    @Override
    protected ValidationResult call() throws Exception {
        publisher.publishEvent(new LaunchStartedEvent(this));
        updateTitle(Messages.getString("ui.base.tasks.launch.title", validationService.getSelectedLauncher().getName()));
        updateMessage(Messages.getString("ui.base.tasks.launch.start"));
        // disable launch game button, rename it to "Launching..."
        // users looking to abort task can head to Tasks tab
        Platform.runLater(() -> {
            basePresenter.disable(true);
            launchButtonText = basePresenter.getLaunchButton().getText();
            basePresenter.getLaunchButton().setText(Messages.getString("ui.base.tasks.launch.step"));
        });

        // Get current profile to validate first!
        ValidationResult result = validationService.validate(); // and save if successful
        updateValue(result);

        // results
        result.getWarnings().forEach(w -> log.info("Validation {}: {} @ {}", w.getSeverity(), w.getText(), w.getTarget()));
        result.getErrors().forEach(e -> log.info("Validation {}: {} @ {}", e.getSeverity(), e.getText(), e.getTarget()));
        if (!result.getErrors().isEmpty()) {
            log.warn("Aborting due to validation errors");
            throw new LaunchException("Validation errors exist");
        }

        updateMessage(Messages.getString("ui.base.tasks.launch.restoring"));
        fileService.restoreFiles();
        updateMessage(Messages.getString("ui.base.tasks.launch.replacing"));
        publisher.publishEvent(new LaunchNextStateEvent(this));
        fileService.replaceFiles();
        updateMessage(Messages.getString("ui.base.tasks.launch.launching"));
        launchGame();
        int step = 0;
        int maxTimeoutSeconds = properties.getLaunchTimeout();
        int millis = 5000;
        int maxSteps = maxTimeoutSeconds / (millis / 1000);
        updateMessage(Messages.getString("ui.base.tasks.launch.waitingStart"));
        updateProgress(0, maxSteps);
        while (!isCancelled() && !isGameRunning() && (maxTimeoutSeconds == 0 || step < maxSteps)) {
            ++step;
            if (maxTimeoutSeconds > 0) {
                updateProgress(step, maxSteps);
            }
            Thread.sleep(millis);
        }
        if (maxTimeoutSeconds > 0 && step >= maxSteps) {
            int s = step * (millis / 1000);
            log.info("Game launch timed out after {} seconds", s);
            result.add(new DataValidationMessage(DataValidationMessage.Type.timedOutLaunch, s));
            return result;
        }
        if (isCancelled()) {
            return result;
        }
        log.debug("Game process has started");
        updateMessage(Messages.getString("ui.base.tasks.launch.waitingFinish"));
        updateProgress(-1, -1);
        publisher.publishEvent(new LaunchNextStateEvent(this));
        while (!isCancelled() && isGameRunning()) {
            Thread.sleep(millis);
        }
        if (isCancelled()) {
            return result;
        }
        Thread.sleep(millis);
        updateMessage("Returning your folders to their original state"); // TODO: NLS
        log.debug("Game process has stopped");
        publisher.publishEvent(new LaunchNextStateEvent(this));
        fileService.restoreFiles();
        return result;
    }

    @Override
    protected void cancelled() {
        log.info("Launch task cancelled");
        if (isGameRunning()) {
            // TODO: implement process kill
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    protected void failed() {
        Throwable t = getException();
        if (t != null) {
            log.error("Launch task failed with exception", t);
        } else {
            log.error("Launch task failed");
        }
    }

    @Override
    protected void done() {
        // restore files now if game is not running and task ended abruptly
        // getValue must be called on FX app thread, delegate restoration to background thread
        CompletableFuture.supplyAsync(this::getValue, Platform::runLater).thenAcceptAsync(result -> {
            if (isGameRunning()) {
                // TODO: handle improper launch finish correctly
                log.warn("Game is still running, files will not be replaced");
            } else if (result == null || !result.getErrors().isEmpty()) { // result may be null after a FAILED or CANCELLED state
                try {
                    fileService.restoreFiles();
                } catch (LaunchException e) {
                    log.warn("Could not restore files", e);
                }
            }
            publisher.publishEvent(new LaunchFinishedEvent(this, result));
        }).thenRunAsync(() -> {
            basePresenter.getLaunchButton().setText(launchButtonText);
            basePresenter.disable(false);
        }, Platform::runLater);
    }

    private boolean isGameRunning() {
        String processName = validationService.getGameProcess();
        if (LwrtUtils.isWindows()) {
            ProcessBuilder[] builders =
                    {
                            new ProcessBuilder("tasklist", "/fi", "\"imagename eq " + processName + "\"", "/nh", "/fo",
                                    "csv"),
                            new ProcessBuilder("cscript", "//NoLogo", Constants.PROCESS_CHECKER_PATH.toString(), processName)};
            return testAgainstProcess(line -> line.contains(processName), builders);
        } else {
            ProcessBuilder builder = new ProcessBuilder("pgrep", processName);
            return testAgainstProcess(line -> line != null, builder);
        }
    }

    private boolean testAgainstProcess(Predicate<String> linePredicate, ProcessBuilder... builders) {
        String line;
        for (ProcessBuilder pb : builders) {
            try {
                Process p = pb.start();
                try (BufferedReader input = newProcessReader(p)) {
                    while ((line = input.readLine()) != null) {
                        if (linePredicate.test(line)) {
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                log.info("Problem while detecting if game is running", e);
            }
        }
        return false;
    }

    private void launchGame() throws LaunchException {
        Launcher launcher = validationService.getSelectedLauncher();
        Map<String, String> options = new LinkedHashMap<>();
        Launcher.Mode mode = launcher.getLaunchMode();
        String appIdKey = "-applaunch";
        switch (mode) {
            case HL2:
                options.put("-steam", ""); // Enables Steam support (if the game is launched from Steam, this is enabled by default)
                options.put("-game", launcher.getModName()); // Sets game or mod directory to load the game from. Default is "hl2".
                break;
            case STEAM:
                options.put(appIdKey, launcher.getAppId()); // Launches an Game or Application through Steam using its Game/Application ID number
            default:
                throw new LaunchException("Invalid launcher mode");
        }
        // original method returns "DxLevelXX" so we get XX using substring
        String dxlevel = validationService.getString("launch.dxlevel");
        String prefix = "DxLevel";
        options.put("-dxlevel", dxlevel.substring(prefix.length()));
        options.put("-w", validationService.getString("launch.width"));
        options.put("-h", validationService.getString("launch.height"));
        // add custom options, can override previously set values
        String[] params = validationService.getString("launch.custom").trim().replaceAll("\\s+", " ").split(" ");
        for (int i = 0; i < params.length; i++) {
            String key = params[i];
            String value = "";
            if (key.startsWith("-") || key.startsWith("+")) {
                if (i + 1 < params.length) {
                    String next = params[i + 1];
                    if (next.matches("^-?\\d+$") || (!next.startsWith("-") && !next.startsWith("+"))) {
                        value = next;
                        i++;
                    }
                }
                options.put(key, value);
            } else {
                log.warn("Discarding invalid launch parameter: {}", key);
            }
        }
        // Remove -dxlevel if the user provided +mat_dxlevel
        // This won't have the desired effect if launch is done through Steam executable
        if (options.containsKey("+mat_dxlevel")) {
            options.remove("-dxlevel");
        }
        // remove other redundant or mutually exclusive parameters
        if (options.containsKey("-full") || options.containsKey("-fullscreen")) {
            options.remove("-sw");
            options.remove("-window");
            options.remove("-startwindowed");
            options.remove("-windowed");
        } else {
            options.put("-sw", "");
            options.put("-noborder", "");
        }
        if (options.containsKey("-width")) {
            options.remove("-w");
        }
        if (options.containsKey("-height")) {
            options.remove("-h");
        }
        ProcessBuilder processBuilder;
        if (Launcher.Mode.HL2.equals(mode)) {
            processBuilder = new ProcessBuilder(validationService.getGameExecutable().toString());
        } else {
            processBuilder = new ProcessBuilder(validationService.getSteamExecutable().toString());
            processBuilder.command().add(appIdKey);
            processBuilder.command().add(options.get(appIdKey));
        }
        options.remove(appIdKey);
        for (Map.Entry<String, String> e : options.entrySet()) {
            processBuilder.command().add(e.getKey());
            processBuilder.command().add(e.getValue());
        }
        if (validationService.mapAndGet("launch.insecure", o -> (Boolean) o)) {
            processBuilder.command().add("-insecure");
        }
        log.info("Launching: {}", processBuilder.command().toString().replaceAll("\\[|\\]|,", "").replaceAll("\\s\\s+", " "));
        startProcess(processBuilder, Launcher.Mode.STEAM.equals(mode) || !LwrtUtils.isWindows());
    }

    private void startProcess(ProcessBuilder processBuilder, boolean logProcessOutput) throws LaunchException {
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new LaunchException("Could not start process", e);
        }
        if (logProcessOutput) {
            String processName = processBuilder.command().get(0);
            log.debug("Process started: {}", processName);
            try (BufferedReader input = newProcessReader(process)) {
                String line;
                while ((line = input.readLine()) != null) {
                    log.debug("{}", line);
                }
            } catch (IOException e) {
                throw new LaunchException("Could not launch process", e);
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                log.warn("Process was interrupted: {}", e.toString());
            }
            log.debug("Process ended: {}", processName);
        }
    }
}
