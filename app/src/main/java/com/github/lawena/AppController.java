package com.github.lawena;

import com.github.lawena.dialog.NewProfileDialog;
import com.github.lawena.exts.FileProvider;
import com.github.lawena.exts.TagProvider;
import com.github.lawena.exts.ViewProvider;
import com.github.lawena.files.Resource;
import com.github.lawena.files.ScanTask;
import com.github.lawena.game.GameDescription;
import com.github.lawena.i18n.Messages;
import com.github.lawena.profile.Profile;
import com.github.lawena.profile.Profiles;
import com.github.lawena.task.LaunchService;
import com.github.lawena.util.FxLogController;
import com.github.lawena.util.LogController;
import com.github.lawena.util.LwrtUtils;

import org.controlsfx.control.StatusBar;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;

public class AppController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);
    @FXML
    private MenuButton menu;
    @FXML
    private ComboBox<Profile> profilesComboBox;
    @FXML
    private TitledPane launchPane;
    @FXML
    private TitledPane recorderPane;
    @FXML
    private TitledPane configPane;
    @FXML
    private TitledPane resourcesPane;
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
    private Tab foldersTab;
    @FXML
    private Tab renderingTab;
    @FXML
    private Tab demosTab;
    @FXML
    private Tab tasksTab;
    @FXML
    private Tab logTab;
    @FXML
    private Pane tasksPane;
    @FXML
    private Pane foldersPane;
    @FXML
    private Pane renderingPane;
    @FXML
    private Pane demosPane;
    @FXML
    private Pane logPane;
    @FXML
    private VBox mainContainer;
    @FXML
    private SplitMenuButton profileSplitMenuButton;
    // non FXML UI elements
    private Stage stage;
    private StatusBar statusBar;
    private TaskProgressView<Task<?>> taskView = new TaskProgressView<>();

    // model-level components
    private Model model;
    private NodeGroups nodeGroups = new AppNodeGroups();
    private LogController logController;
    private List<ViewProvider> viewProviders;
    private List<TagProvider> tagProviders;
    private List<FileProvider> fileProviders;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ListChangeListener<Path> resourceFolderListener = ch -> {
        while (ch.next()) {
            if (ch.wasAdded()) {
                submitTask(new ScanTask(ch.getAddedSubList()) {

                    @Override
                    public Set<Resource> scan(Path dir) {
                        updateMessage(String.format(Messages.getString("AppResources.ScanningResources"), dir)); //$NON-NLS-1$
                        return model.getResources().scanForResources(dir);
                    }

                });
            }
            if (ch.wasRemoved()) {
                Platform.runLater(() -> model.getResources().getResourceList().removeIf(r -> ch.getRemoved().contains(r.getPath())));
            }
        }
    };
    private Map<String, Pane> locations = new HashMap<>();
    private LaunchService launchService;

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public NodeGroups getNodeGroups() {
        return nodeGroups;
    }

    @Override
    public Pane getLogPane() {
        return logPane;
    }

    @FXML
    void initialize() {
        log.debug("Initializing FX UI");

        // cleanup procedure on close
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                unbindProfile(model.getProfiles().getSelected());
            } catch (Exception e) {
                log.warn("Could not save profiles correctly", e);
            }
            model.getResources().foldersProperty().addListener(resourceFolderListener);
            model.exit();
        }));

        // configure task list controls
        Image image = LwrtUtils.image("/ui/fugue/gear.png"); // NON-NLS
        taskView.setGraphicFactory(t -> {
            ImageView icon = new ImageView(image);
            RotateTransition rt = new RotateTransition(Duration.millis(1000), icon);
            rt.setByAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.play();
            return icon;
        });
        tasksPane.getChildren().add(taskView);

        launchPane.setContent(display("launch", Collections.emptyList())); // NON-NLS
        recorderPane.setContent(display("recorder", Collections.emptyList())); // NON-NLS
        configPane.setContent(display("config", Collections.emptyList())); // NON-NLS
        resourcesPane.setContent(display("resources", Collections.emptyList())); // NON-NLS

        // configure additional controls
        statusBar = new StatusBar();
        mainContainer.getChildren().add(mainContainer.getChildren().size(), statusBar);

        logController = new FxLogController(this);
        Platform.runLater(() -> {
            model.getLogAppender().setController(logController);
            logController.startController();
            model.getLogAppender().startAppender();
            model.getResources().foldersProperty().addListener(resourceFolderListener);
            model.getResources().startWatch();
            bindProfileList();
        });

        launchButton.setText("Launch Game");

        launchService = new LaunchService(this);
    }

    private void bindProfileList() {
        log.debug("Creating UI bindings"); //$NON-NLS-1$
        Profiles profiles = model.getProfiles();
        Bindings.bindContentBidirectional(profilesComboBox.itemsProperty().get(), profiles
                .profilesProperty().get());
        profilesComboBox.setValue(profiles.getSelected());
        Callback<ListView<Profile>, ListCell<Profile>> cellFactory = param -> new ListCell<Profile>() {
            @Override
            protected void updateItem(Profile item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // TODO: add game icon as graphic
                    setGraphic(null);
                    setText(item.getName());
                    // subscribe to this item changes
                    item.addListener(p -> setText(((Profile) p).getName()));
                }
            }
        };
        profilesComboBox.setButtonCell(cellFactory.call(null));
        profilesComboBox.setCellFactory(cellFactory);
        Bindings.bindBidirectional(profilesComboBox.valueProperty(), profiles.selectedProperty());
        profiles.selectedProperty().addListener((obs, _old, _new) -> {
            log.info("Selected profile change: {} -> {}", _old.getName(), _new.getName());
            unbindProfile(_old);
            bindProfile(_new);
        });
        bindProfile(profiles.getSelected());
    }

    private void unbindProfile(Profile profile) {
        try {
            GameDescription app = Optional.ofNullable(model.getGames().get(profile.getAppId())).get();
            // get ui extensions related to this app
            List<ViewProvider> exts =
                    getViewProviders().stream().filter(x -> app.getViews().contains(x.getName()))
                            .collect(Collectors.toList());
            // get all cards created by this extension and unbind them
            exts.stream().forEach(x -> x.unbind(profile));
        } catch (NoSuchElementException e) {
            log.warn("Invalid appid value on the selected profile: {}", profile.getAppId());
        }
    }

    private void bindProfile(Profile profile) {
        try {
            GameDescription app = Optional.ofNullable(model.getGames().get(profile.getAppId())).get();
            // get ui extensions related to this app
            List<ViewProvider> exts =
                    getViewProviders().stream().filter(x -> app.getViews().contains(x.getName()))
                            .collect(Collectors.toList());
            exts.forEach(ViewProvider::install);

            // load resources folders
            Path custom = Paths.get(app.getLocalGamePath()).resolve("custom"); //$NON-NLS-1$
            ObservableList<Path> folders = model.getResources().getFolders();
            if (!folders.contains(custom)) {
                folders.add(custom);
            }

            // get all cards created by this extension, bind and display them
            Platform.runLater(() ->
                    exts.stream().peek(x -> x.bind(profile)).flatMap(x -> nodeGroups.getGroups(x).entrySet().stream())
                            .forEach(e -> display(e.getKey(), e.getValue())));
        } catch (NoSuchElementException e) {
            log.warn("Invalid appid value on the selected profile: {}", profile.getAppId());
        }
    }

    private Pane createEmptyPane() {
        VBox vbox = new VBox(5);
        vbox.setPrefWidth(200);
        return vbox;
    }

    private Pane display(String location, List<Node> nodes) {
        Pane parent = locations.computeIfAbsent(location, key -> createEmptyPane());
        parent.getChildren().setAll(nodes);
        return parent;
    }

    @Override
    public List<ViewProvider> getViewProviders() {
        // TODO: handle runtime changes
        // normally there won't be any need to reload this unless the user loads/unloads an extension
        // during runtime, in which case this list must be invalidated
        if (viewProviders == null) {
            viewProviders = model.getPluginManager().getExtensions(ViewProvider.class);
            viewProviders.forEach(vp -> vp.init(this));
        }
        return viewProviders;
    }

    @Override
    public List<TagProvider> getTagProviders() {
        if (tagProviders == null) {
            tagProviders = model.getPluginManager().getExtensions(TagProvider.class);
        }
        return tagProviders;
    }

    @Override
    public List<FileProvider> getFileProviders() {
        if (fileProviders == null) {
            fileProviders = model.getPluginManager().getExtensions(FileProvider.class);
            fileProviders.forEach(fp -> fp.init(this));
        }
        return fileProviders;
    }

    private void submitTask(Task<?> task) {
        taskView.getTasks().add(task);
        executor.submit(task);
    }

    private void submitTasks(List<? extends Task<?>> tasks) {
        log.debug("Submitting tasks: {}", tasks);
        taskView.getTasks().addAll(tasks);
        tasks.forEach(executor::submit);
    }

    @Override
    public ObservableList<Task<?>> getTasks() {
        return taskView.getTasks();
    }

    @Override
    public Button getLaunchButton() {
        return launchButton;
    }

    @Override
    public void disable(boolean value) {
        launchPane.setDisable(value);
        resourcesPane.setDisable(value);
        configPane.setDisable(value);
        recorderPane.setDisable(value);
        launchButton.setDisable(value);
    }

    @FXML
    void launch(ActionEvent event) {
        log.debug("Launch game button pressed");
        // TODO: not fully implemented yet
        Platform.runLater(launchService::start);
    }

    @Override
    public void saveProfiles() {
        Path path = Paths.get(model.getParameters().get("profiles")); //$NON-NLS-1$
        model.saveProfiles(path);
    }

    @FXML
    void newProfile(ActionEvent event) {
        // show the new profile dialog
        NewProfileDialog dialog = new NewProfileDialog(model.getProfiles(), model.getGames().values());
        Optional<Pair<GameDescription, String>> result = dialog.showAndWait();
        result.ifPresent(response -> Platform.runLater(() -> model.getProfiles().create(response.getKey().getApplaunch(),
                response.getValue())));
    }

    @FXML
    void renameProfile(ActionEvent event) {
        Profile profile = model.getProfiles().getSelected();
        TextInputDialog dialog = new TextInputDialog(profile.getName());
        dialog.setTitle(Messages.getString("AppController.RenameProfileTitle")); //$NON-NLS-1$
        dialog.setHeaderText(Messages.getString("AppController.RenameProfileHeader")); //$NON-NLS-1$
        dialog.setContentText(Messages.getString("AppController.RenameProfileContent")); //$NON-NLS-1$

        // The dialog result is valid if, when the name exists, it belongs to this same profile
        ValidationSupport validation = new ValidationSupport();
        Validator<String> nameValidator =
                Validator
                        .combine(Validator.createEmptyValidator(Messages
                                        .getString("AppController.ProfileNameEmpty")), //$NON-NLS-1$
                                Validator.createPredicateValidator(
                                        n -> !model.getProfiles().findByName(n).filter(p -> !p.equals(profile))
                                                .isPresent(), Messages.getString("AppController.ProfileNameNotUnique"))); //$NON-NLS-1$
        validation.registerValidator(dialog.getEditor(), false, nameValidator);

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (validation.isInvalid()) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle(Messages.getString("AppController.RenameProfileFailTitle")); //$NON-NLS-1$
                alert.setHeaderText(Messages.getString("AppController.RenameProfileFailHeader")); //$NON-NLS-1$
                alert.setContentText(Messages.getString("AppController.RenameProfileFailContent")); //$NON-NLS-1$
                alert.showAndWait();
            } else {
                Platform.runLater(() -> model.getProfiles().rename(profile, name));
            }
        });
    }

    @FXML
    void duplicateProfile(ActionEvent event) {
        Profile profile = model.getProfiles().getSelected();
        Platform.runLater(() -> model.getProfiles().duplicate(profile));
    }

    @FXML
    void deleteProfile(ActionEvent event) {
        Profile profile = model.getProfiles().getSelected();
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Messages.getString("AppController.DeleteProfileTitle")); //$NON-NLS-1$
        alert.setHeaderText(String.format(
                Messages.getString("AppController.DeleteProfileHeader"), profile)); //$NON-NLS-1$
        alert.setContentText(Messages.getString("AppController.DeleteProfileContent")); //$NON-NLS-1$
        alert.showAndWait().filter(t -> t == ButtonType.OK)
                .ifPresent(x -> Platform.runLater(() -> model.getProfiles().remove(profile)));
    }
}
