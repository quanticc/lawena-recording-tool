package com.github.lawena.views.updates;

import com.github.lawena.Messages;
import com.github.lawena.config.LawenaProperties;
import com.github.lawena.domain.Branch;
import com.github.lawena.domain.Build;
import com.github.lawena.domain.UpdateResult;
import com.github.lawena.event.NewVersionAvailable;
import com.github.lawena.event.NewVersionDismissed;
import com.github.lawena.repository.ImageRepository;
import com.github.lawena.service.TaskService;
import com.github.lawena.service.VersionService;
import com.github.lawena.task.DownloadTask;
import com.github.lawena.task.UpdateSetupTask;
import com.github.lawena.task.UpdatesChecker;
import com.github.lawena.task.WebViewLoader;
import com.github.lawena.util.FXUtils;
import com.threerings.getdown.data.Resource;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.github.lawena.util.LwrtUtils.interceptAnchors;

@Component
public class UpdatesPresenter {

    private static final Logger log = LoggerFactory.getLogger(UpdatesPresenter.class);
    private static final String HOME_URL = "https://raw.githubusercontent.com/wiki/iabarca/lawena-recording-tool/Home.md";
    private static final String LOG_URL = "https://raw.githubusercontent.com/wiki/iabarca/lawena-recording-tool/Changelog.md";

    @Autowired
    private VersionService versionService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HostServices hostServices;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private LawenaProperties lawenaProperties;
    @Autowired
    private ApplicationEventPublisher publisher;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab infoTab;
    @FXML
    private WebView infoWebView;
    @FXML
    private WebView changesWebView;

    private NotificationPane resultPane;
    private Action check;
    private Action restart;
    private Action rollback;

    @FXML
    private void initialize() {
        // setup updates sliding notification pane
        resultPane = new NotificationPane(infoWebView);
        infoTab.setContent(resultPane);

        // intercept <a> tags to open links externally
        infoWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                interceptAnchors(infoWebView.getEngine().getDocument(), href -> hostServices.showDocument(href));
            }
        });
        changesWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                interceptAnchors(changesWebView.getEngine().getDocument(), href -> hostServices.showDocument(href));
            }
        });
        WebViewLoader.LoadSpec loadSpec = new WebViewLoader.LoadSpec();
        Map<WebView, String> map = new LinkedHashMap<>();
        map.put(infoWebView, HOME_URL);
        map.put(changesWebView, LOG_URL);
        loadSpec.getViewUrlMap().putAll(map);
        loadSpec.getStylesheets().add(getClass().getResource("../markdownpad-github.css"));
        taskService.submitTask(new WebViewLoader(loadSpec));

        if (lawenaProperties.getLastSkippedVersion() >= 0) {
            checkForUpdates(false);
        }
        resultPane.setOnHiding(e -> publisher.publishEvent(new NewVersionDismissed(UpdatesPresenter.this)));
    }

    public void checkForUpdates(boolean alwaysShow) {
        UpdatesChecker checker = new UpdatesChecker(versionService);
        taskService.submitTask(checker);
        CompletableFuture.supplyAsync(() -> {
            UpdateResult result;
            try {
                result = checker.get();
            } catch (InterruptedException | ExecutionException e) {
                result = UpdateResult.notFound(e.toString());
            }
            return result;
        }).thenAcceptAsync(r -> processUpdateResult(r, alwaysShow), Platform::runLater);
    }

    public void clearCache() {
        versionService.clear();
    }

    private void processUpdateResult(UpdateResult result, boolean alwaysShow) {
        log.debug("{}", result.toString());
        if (result.getStatus() == UpdateResult.Status.UPDATE_AVAILABLE) {
            Build target = result.getDetails();
            String appbase = versionService.getAppbase();
            long targetVersion = target.getTimestamp();
            if (lawenaProperties.getLastSkippedVersion() == targetVersion) {
                log.info("Skipping this version: {}", targetVersion);
                return;
            }
            URL url;
            try {
                url = new URL(appbase.replace("%VERSION%", "" + targetVersion));
            } catch (MalformedURLException e) {
                log.error("Bad url format: {}", e.toString());
                return;
            }

            check = new Action(Messages.getString("ui.updates.retry"),
                    event -> performUpdate(url, targetVersion)
            );
            restart = new Action(Messages.getString("ui.updates.restart"), event -> {
                Stage stage = (Stage) tabPane.getScene().getWindow();
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                taskService.scheduleOnShutdown(() -> versionService.upgradeApplication(target));
            });
            Action updateNow = new Action(Messages.getString("ui.updates.updateNow"),
                    event -> performUpdate(url, targetVersion)
            );
            Action skip = new Action(Messages.getString("ui.updates.skip"), event -> {
                lawenaProperties.setLastSkippedVersion(targetVersion);
                resultPane.hide();
            });

            resultPane.setText(result.getMessage());
            resultPane.setGraphic(new ImageView(imageRepository.image("/com/github/lawena/fugue/exclamation-24.png")));
            resultPane.getActions().addAll(updateNow, skip);
            resultPane.show();
            publisher.publishEvent(new NewVersionAvailable(this));
        } else if (result.getStatus() == UpdateResult.Status.ALREADY_LATEST_VERSION) {
            if (alwaysShow) {
                rollback = new Action(Messages.getString("ui.updates.rollback"), event -> {
                    ChoiceDialog<Pair<Branch, Build>> dialog = createBuildChooserDialog();
                    dialog.showAndWait().ifPresent(pair -> {
                        Branch chosenBranch = pair.getKey();
                        Build chosenBuild = pair.getValue();
                        log.debug("Rollback choice: {}", pair);
                        if (!chosenBranch.equals(versionService.getCurrentBranch())) {
                            try {
                                String appbase = versionService.switchBranch(chosenBranch, chosenBuild);
                                long targetVersion = chosenBuild.getTimestamp();
                                URL url = new URL(appbase.replace("%VERSION%", "" + targetVersion));
                                performUpdate(url, targetVersion);
                            } catch (IOException e) {
                                log.error("Switch operation failed", e);
                                FXUtils.showWarning(Messages.getString("ui.updates.failedSwitch.title"),
                                        Messages.getString("ui.updates.failedSwitch.header"),
                                        Messages.getString("ui.updates.failedSwitch.content"));
                            }
                        } else if (chosenBuild.getTimestamp() != versionService.getLongCurrentVersion()) {
                            try {
                                String appbase = versionService.getAppbase();
                                long targetVersion = chosenBuild.getTimestamp();
                                URL url = new URL(appbase.replace("%VERSION%", "" + targetVersion));
                                performUpdate(url, targetVersion);
                            } catch (MalformedURLException e) {
                                log.error("Bad appbase URL: {}", e.toString());
                                FXUtils.showWarning(Messages.getString("ui.updates.badAppbase.title"),
                                        Messages.getString("ui.updates.badAppbase.header"),
                                        Messages.getString("ui.updates.badAppbase.content"));
                            }
                        } else {
                            FXUtils.showAlert(Messages.getString("ui.updates.sameVersion.title"),
                                    Messages.getString("ui.updates.sameVersion.header"),
                                    Messages.getString("ui.updates.sameVersion.content"));
                        }
                    });
                });
                resultPane.setText(result.getMessage());
                resultPane.setGraphic(new ImageView(imageRepository.image("/com/github/lawena/fugue/tick-24.png")));
                resultPane.getActions().add(rollback);
                resultPane.show();
                publisher.publishEvent(new NewVersionAvailable(this));
            }
        } else if (result.getStatus() == UpdateResult.Status.NO_UPDATES_FOUND) {
            if (alwaysShow) {
                resultPane.setText(result.getMessage());
                resultPane.setGraphic(new ImageView(imageRepository.image("/com/github/lawena/fugue/exclamation-24.png")));
                resultPane.show();
                publisher.publishEvent(new NewVersionAvailable(this));
            }
        }
    }

    private ChoiceDialog<Pair<Branch, Build>> createBuildChooserDialog() {
        List<Branch> branches = versionService.getBranches();
        ChoiceDialog<Pair<Branch, Build>> dialog = new ChoiceDialog<>();
        branches.stream().map(this::buildPairsFromBranch).flatMap(Collection::stream).forEach(pair -> dialog.getItems().add(pair));
        dialog.setSelectedItem(versionService.getCurrentBuild());
        dialog.setTitle(Messages.getString("ui.updates.buildChooser.title"));
        dialog.setHeaderText(Messages.getString("ui.updates.buildChooser.header"));
        dialog.setContentText(Messages.getString("ui.updates.buildChooser.content"));
        dialog.initOwner(tabPane.getScene().getWindow());
        return dialog;
    }

    private List<BranchBuildPair> buildPairsFromBranch(Branch branch) {
        return branch.getBuilds().stream().sorted().map(build -> new BranchBuildPair(branch, build)).collect(Collectors.toList());
    }

    private void performUpdate(URL url, long targetVersion) {
        // Start the background update process
        resultPane.getActions().clear();
        UpdateSetupTask setupTask = new UpdateSetupTask(versionService.getGetdown(), url, targetVersion);
        ProgressIndicator indicator = new ProgressIndicator(-1);
        indicator.setPrefSize(24, 24);
        indicator.setMaxSize(24, 24);
        taskService.submitTask(setupTask);
        resultPane.textProperty().bind(setupTask.messageProperty());
        resultPane.setGraphic(indicator);
        indicator.progressProperty().bind(setupTask.progressProperty());
        CompletableFuture.supplyAsync(() -> {
            try {
                return setupTask.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(Messages.getString("ui.updates.setupInterrupted"), e);
            }
        }).thenApplyAsync(list -> {
            indicator.progressProperty().unbind();
            resultPane.textProperty().unbind();
            DownloadTask downloadTask = new DownloadTask(list);
            taskService.submitTask(downloadTask);
            resultPane.textProperty().bind(downloadTask.messageProperty());
            indicator.progressProperty().bind(downloadTask.progressProperty());
            return downloadTask;
        }, Platform::runLater).thenApplyAsync(task -> {
            try {
                task.get();
                return task;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(Messages.getString("ui.updates.downloadInterrupted"), e);
            }
        }).thenAcceptAsync(task -> {
            try {
                List<Resource> list = task.get(); // can be null if task failed due to I/O
                if (list != null && !list.isEmpty()) {
                    resultPane.textProperty().unbind();
                    resultPane.setText(Messages.getString("ui.updates.updateReady"));
                    resultPane.setGraphic(new ImageView(imageRepository.image("/com/github/lawena/fugue/exclamation-24.png")));
                    resultPane.getActions().add(restart);
                } else {
                    log.warn("Task failed with exception", task.getException());
                    resultPane.textProperty().unbind();
                    resultPane.setText(Messages.getString("ui.updates.updateFailed"));
                    resultPane.setGraphic(new ImageView(imageRepository.image("/com/github/lawena/fugue/exclamation-24.png")));
                    resultPane.getActions().add(check);
                }
            } catch (InterruptedException | ExecutionException e) {
                // we should not get here normally
                // exceptions on task cancel/interrupt are handled by exceptionally()
                throw new RuntimeException(e);
            }
        }, Platform::runLater).exceptionally(t -> {
            log.info("Update aborted with exception", t);
            resultPane.textProperty().unbind();
            resultPane.setText(Messages.getString("ui.updates.updateAborted", t.getMessage()));
            resultPane.setGraphic(new ImageView(imageRepository.image("/com/github/lawena/fugue/exclamation-24.png")));
            resultPane.getActions().add(check);
            return null;
        });
    }

    static class BranchBuildPair extends Pair<Branch, Build> {

        /**
         * Creates a new pair
         *
         * @param key   The key for this pair
         * @param value The value to use for this pair
         */
        public BranchBuildPair(Branch key, Build value) {
            super(key, value);
        }

        @Override
        public String toString() {
            Branch branch = getKey();
            Build build = getValue();
            return branch.getName() + "/" + build.getTimestamp() + " (" + build.getVersion() + ")";
        }
    }
}
