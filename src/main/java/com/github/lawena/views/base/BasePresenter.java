package com.github.lawena.views.base;

import com.github.lawena.Messages;
import com.github.lawena.config.Constants;
import com.github.lawena.domain.Launcher;
import com.github.lawena.domain.Profile;
import com.github.lawena.service.Profiles;
import com.github.lawena.service.Resources;
import com.github.lawena.service.TaskService;
import com.github.lawena.service.fx.LaunchService;
import com.github.lawena.task.ScanTask;
import com.github.lawena.util.LwrtUtils;
import com.github.lawena.views.GameView;
import com.github.lawena.views.dialog.NewProfileDialog;
import com.github.lawena.views.launch.LaunchView;
import com.github.lawena.views.menu.MenuView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.When;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class BasePresenter {

    private static final Logger log = LoggerFactory.getLogger(BasePresenter.class);

    @Autowired
    private ApplicationContext context;
    @Autowired
    private Resources resources;
    @Autowired
    private Profiles profiles;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskProgressView taskProgressView;
    @Autowired
    private LaunchService launchService;
    @Autowired
    private LaunchView launchView;
    @Autowired
    private MenuView menuView;

    @FXML
    private MenuButton menu;
    @FXML
    private ComboBox<Profile> profilesComboBox;
    @FXML
    private Label leftStatus;
    @FXML
    private Label rightStatus;
    @FXML
    private Button launchButton;
    @FXML
    private TabPane tabs;
    @FXML
    private Tab setupTab;
    @FXML
    private Tab renderingTab;
    @FXML
    private Tab demosTab;
    @FXML
    private Tab tasksTab;
    @FXML
    private Pane tasksPane;
    @FXML
    private VBox mainContainer;
    @FXML
    private SplitMenuButton profileSplitMenuButton;

    private DirectoryChooser directoryChooser = new DirectoryChooser();

    /**
     * Listener for changes to the Resource folders, like "lwrt/tf2/custom"
     */
    private ListChangeListener<Path> resourceFolderListener = ch -> {
        while (ch.next()) {
            if (ch.wasAdded()) {
                log.debug("Scanning for resources inside the added folder: {}", ch.getAddedSubList());
                List<? extends Path> added = ch.getAddedSubList();
                for (final Path path : added) {
                    taskService.submitTask(new ScanTask(resources, path));
                }
            }
            if (ch.wasRemoved()) {
                log.debug("Removing all resources inside the removed folder: {}", ch.getRemoved());
                Platform.runLater(() -> resources.getResourceList()
                        .removeIf(r -> ch.getRemoved().contains(r.getPath().getParent())));
            }
        }
    };

    @FXML
    private void initialize() {
        log.debug("Initializing FX UI");
        VBox.setVgrow(taskProgressView, Priority.ALWAYS);
        mainContainer.getChildren().add(0, menuView.getView());
        tasksPane.getChildren().add(taskProgressView);
        Platform.runLater(() -> {
            tabs.getScene().getWindow().setOnCloseRequest(e -> exit());
            resources.foldersProperty().addListener(resourceFolderListener);
            bindProfileList();
            bindTaskStatus(true);

            renderingTab.setContent(launchView.getView());

//            String url = "https://github.com/iabarca/lawena-recording-tool";
//            WebView webView = new WebView();
//            webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) ->
//                    System.out.println(url + " " + oldState + " -> " + newState));
//            webView.getEngine().load(url);
//            foldersPane.getChildren().add(webView);
        });
    }

    private void bindTaskStatus(boolean wasNotInterrupted) {
        // Task running status indicator
        IntegerBinding sizeBinding = Bindings.size(taskProgressView.getTasks());
        ProgressIndicator taskIndicator = new ProgressIndicator(-1);
        taskIndicator.setMaxSize(16, 16);
        When isEmpty = Bindings.when(Bindings.equal(0, sizeBinding));
        String plural = Messages.getString("ui.base.tasks.tasksRunning");
        String singular = Messages.getString("ui.base.tasks.taskRunning");
        rightStatus.setOnMouseClicked(e -> tabs.getSelectionModel().select(tasksTab));
        rightStatus.setOnMouseExited(e -> rightStatus.setUnderline(false));
        rightStatus.setOnMouseEntered(e -> rightStatus.setUnderline(true));
        rightStatus.visibleProperty().bind(Bindings.notEqual(0, sizeBinding));
        rightStatus.graphicProperty().bind(isEmpty.then((ProgressIndicator) null).otherwise(taskIndicator));
        rightStatus.textProperty().bind(
                Bindings.concat(isEmpty.then("").otherwise(sizeBinding.asString()), isEmpty.then("")
                        .otherwise(Bindings.when(
                                Bindings.greaterThan(1, sizeBinding)).then(plural)
                                .otherwise(singular))));
    }

    // ***********************************************************************
    //  PROFILE MVP (LOAD FROM MODEL TO VIEW, SAVE FROM VIEW TO MODEL)
    //  - Initialize the profile list from model layer (repository.Profiles)
    //  - Load a profile from model and send data to proper presenter
    //      - That presenter will reflect the model onto the view
    //        mapping profile keys (from the json) to a JavaFx property
    //        and binding it with the corresponding control (if needed)
    //  - Save the view layer data to the model
    //      - Again, delegate this to the profile's presenter
    //      - Perform save-to-file action here (serialize to json)
    // ***********************************************************************

    private void bindProfileList() {
        log.debug("Binding profiles to UI");
        Bindings.bindContentBidirectional(profilesComboBox.itemsProperty().get(), profiles
                .profilesProperty().get());
        profilesComboBox.setValue(profiles.getSelected());
        setProfileCellFactory();

        Bindings.bindBidirectional(profilesComboBox.valueProperty(), profiles.selectedProperty());
        profiles.selectedProperty().addListener((obs, pre, cur) -> {
            log.debug("Selected profile change: {} -> {}", pre.getName(), cur.getName());
            unbindProfile(pre);
            bindProfile(cur);
        });
        bindProfile(profiles.getSelected());
    }

    private void setProfileCellFactory() {
        Callback<ListView<Profile>, ListCell<Profile>> cellFactory = listView -> new ProfileCell(profiles);
        profilesComboBox.setButtonCell(cellFactory.call(null));
        profilesComboBox.setCellFactory(cellFactory);
    }

    private void unbindProfile(Profile profile) {
        log.info("Saving profile: {}", profile.getName());
        try {
            // get the launcher defined in the given profile
            Launcher app = profiles.getLauncher(profile).get();
            GameView view = profiles.getView(app);

            // unbind
            Platform.runLater(() -> view.getPresenter().unbind(profile));
        } catch (NoSuchElementException e) {
            log.warn("Invalid profile definition of {}: {}", profile.getName(), e.toString());
        }
    }

    private void bindProfile(Profile profile) {
        log.info("Loading profile: {}", profile.getName());
        try {
            // get the launcher defined in the given profile
            Launcher launcher = profiles.getLauncher(profile).get();
            GameView view = profiles.getView(launcher);
            syncResourceFolders(launcher);
            // perform binding and display view
            Platform.runLater(() -> {
                view.getPresenter().bind(profile);
                setupTab.setContent(view.getView());
            });
        } catch (NoSuchElementException e) {
            log.warn("Invalid profile definition of {}: {}", profile.getName(), e.toString());
        }
    }

    private void syncResourceFolders(Launcher current) {
        ObservableList<Path> folders = resources.getFolders();
        List<Path> candidates = new ArrayList<>(folders);
        // try to get a valid basePath or get it from the user now
        Path base = LwrtUtils.tryGetPath(current.getBasePath())
                .map(Constants.LWRT_PATH::resolve)
                .filter(this::isValidBasePath)
                .orElseGet(() -> requestBasePath(current));
        if (base != null) {
            // add the default folder (e.g. lwrt/tf2/custom for "tf2" as basePath)
            candidates.add(base.resolve(Constants.CUSTOM_FOLDER_NAME));
            // exclude current launcher-defined folders that are inside lwrt base path (e.g. lwrt/tf2)
            current.getResourceFolders().stream().map(Paths::get).filter(p -> p.startsWith(base)).forEach(candidates::add);
        }
        // exclude current launcher-defined folders that are already in the folder list
        candidates.stream().filter(p -> !folders.contains(p)).forEach(folders::add);
        // remove previous launcher-defined folders that are not in the "candidates" folder list
        folders.removeIf(p -> !candidates.contains(p));
    }

    private Path requestBasePath(Launcher launcher) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Messages.getString("ui.base.basePathRequest.title"));
        alert.setHeaderText(Messages.getString("ui.base.basePathRequest.header"));
        alert.setContentText(Messages.getString("ui.base.basePathRequest.content"));
        if (alert.showAndWait().filter(t -> t == ButtonType.OK).isPresent()) {
            directoryChooser.setInitialDirectory(Constants.LWRT_PATH.toFile());
            directoryChooser.setTitle("ui.base.basePathRequest.chooser");
            File file = null;
            while (!isValidBasePath(file)) {
                file = directoryChooser.showDialog(null);
                if (!isValidBasePath(file)) {
                    Alert bad = new Alert(Alert.AlertType.ERROR);
                    bad.setTitle(Messages.getString("ui.base.invalidBasePath.title"));
                    bad.setHeaderText(null);
                    bad.setContentText(Messages.getString("ui.base.invalidBasePath.content"));
                } else {
                    Path result = file.toPath().toAbsolutePath().relativize(Constants.LWRT_PATH.toAbsolutePath());
                    launcher.setBasePath(result.toString());
                    return result;
                }
            }
        }
        return null;
    }

    private boolean isValidBasePath(String str) {
        return LwrtUtils.tryGetPath(str).map(this::isValidBasePath).orElse(false);
    }

    private boolean isValidBasePath(File file) {
        return file != null && isValidBasePath(file.toPath());
    }

    private boolean isValidBasePath(Path path) {
        return path != null && Files.isDirectory(path) && path.startsWith(Constants.LWRT_PATH);
    }

    // ***********************************************************************
    //  LAUNCH RELATED ACTIONS/METHODS
    // ***********************************************************************

    @FXML
    private void launch(ActionEvent event) {
        log.debug("Launch game button pressed");
        Platform.runLater(() -> {
            launchService.restart();
            tabs.getSelectionModel().select(renderingTab);
        });
    }

    public Button getLaunchButton() {
        return launchButton;
    }

    /**
     * Enable or disable all controls that affect the launch process.
     *
     * @param value disable controls with <code>true</code>, enable them with <code>false</code>
     */
    public void disable(boolean value) {
        profilesComboBox.setDisable(value);
        profileSplitMenuButton.setDisable(value);
        launchButton.setDisable(value);
        setupTab.getContent().setDisable(value);
    }

    private void exit() {
        log.debug("Exiting controller");
        unbindProfile(profiles.getSelected());
        Platform.exit();
    }

    // ***********************************************************************
    //  PROFILE MANAGEMENT BUTTONS
    // ***********************************************************************

    @FXML
    private void newProfile(ActionEvent event) {
        NewProfileDialog newProfileDialog = context.getBean(NewProfileDialog.class);
        Optional<Pair<Launcher, String>> result = newProfileDialog.showAndWait();
        result.ifPresent(response -> Platform.runLater(
                () -> profiles.create(response.getKey().getName(), response.getValue())));
    }

    @FXML
    private void renameProfile(ActionEvent event) {
        Profile profile = profiles.getSelected();
        TextInputDialog dialog = new TextInputDialog(profile.getName());
        dialog.setTitle(Messages.getString("ui.base.renameProfile.title"));
        dialog.setHeaderText(Messages.getString("ui.base.renameProfile.header"));
        dialog.setContentText(Messages.getString("ui.base.renameProfile.content"));

        // The dialog result is valid if, when the name exists, it belongs to this same profile
        ValidationSupport validation = new ValidationSupport();
        Validator<String> nameValidator = Validator.combine(
                Validator.createEmptyValidator(
                        Messages.getString("ui.base.renameProfile.nameEmpty")),
                Validator.createPredicateValidator(
                        n -> !profiles.findByName(n).filter(p -> !p.equals(profile)).isPresent(),
                        Messages.getString("ui.base.renameProfile.nameNotUnique")));
        validation.registerValidator(dialog.getEditor(), false, nameValidator);

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (validation.isInvalid()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(Messages.getString("ui.base.renameProfile.failedTitle"));
                alert.setHeaderText(Messages.getString("ui.base.renameProfile.failedHeader"));
                alert.setContentText(Messages.getString("ui.base.renameProfile.failedContent"));
                alert.showAndWait();
            } else {
                Platform.runLater(() -> {
                    profiles.rename(profile, name);
                    setProfileCellFactory();
                });
            }
        });
    }

    @FXML
    private void duplicateProfile(ActionEvent event) {
        Profile profile = profiles.getSelected();
        Platform.runLater(() -> profiles.duplicate(profile));
    }

    @FXML
    private void deleteProfile(ActionEvent event) {
        Profile profile = profiles.getSelected();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Messages.getString("ui.base.deleteProfile.title"));
        alert.setHeaderText(Messages.getString("ui.base.deleteProfile.header", profile));
        alert.setContentText(Messages.getString("ui.base.deleteProfile.content"));
        alert.showAndWait().filter(t -> t == ButtonType.OK)
                .ifPresent(x -> Platform.runLater(() -> profiles.remove(profile)));
    }
}
