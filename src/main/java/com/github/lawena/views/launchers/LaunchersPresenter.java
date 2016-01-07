package com.github.lawena.views.launchers;

import com.github.lawena.Messages;
import com.github.lawena.config.Constants;
import com.github.lawena.config.LawenaProperties;
import com.github.lawena.event.LaunchersUpdatedEvent;
import com.github.lawena.service.Profiles;
import com.github.lawena.util.ExternalString;
import com.github.lawena.util.FXUtils;
import com.github.lawena.util.LwrtUtils;
import com.github.lawena.views.GameView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LaunchersPresenter {

    private static final Logger log = LoggerFactory.getLogger(LaunchersPresenter.class);

    @Autowired
    private ApplicationContext context;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private LawenaProperties lawenaProperties;
    @Autowired
    private List<GameView> gameViews;
    @Autowired
    private Profiles profiles;

    @FXML
    private ListView<FxLauncher> launchersList;
    @FXML
    private TextField name;
    @FXML
    private TextField icon;
    @FXML
    private TextField viewName;
    @FXML
    private TextField gamePath;
    @FXML
    private ComboBox<ExternalString> launchMode;
    @FXML
    private TextField basePath;
    @FXML
    private CheckBox includeGamePath;
    @FXML
    private ListView<String> sourcesList;
    @FXML
    private TableView<FxConfigFlag> flagsTable;
    @FXML
    private TableColumn<FxConfigFlag, String> keyFlagColumn;
    @FXML
    private TableColumn<FxConfigFlag, Boolean> defaultEnabledColumn;
    @FXML
    private TableColumn<FxConfigFlag, String> enabledValueColumn;
    @FXML
    private TableColumn<FxConfigFlag, String> disabledValueColumn;
    @FXML
    private Label description;
    @FXML
    private VBox launchModePane;
    @FXML
    private Button deleteLauncher;
    @FXML
    private Button removeFlag;
    @FXML
    private Button removeSource;

    private final ListProperty<FxLauncher> fxLaunchers = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final Map<FxLauncher, String> renames = new LinkedHashMap<>();
    private final ValidationSupport validationSupport = new ValidationSupport();
    private TextField modName;
    private TextField steamPath;
    private TextField appId;
    private VBox expandedContainer;
    private ListView<String> validationReport;
    private Map<ExternalString, Node> launchModeCards = new HashMap<>();
    private ChangeListener<String> refreshLauncherCells = (o, oldValue, newValue) -> {
        if (newValue != null) {
            Platform.runLater(this::setLaunchersCellFactory);
        }
    };

    @FXML
    private void initialize() {
        log.debug("Initializing launchers config UI");
        Bindings.bindContentBidirectional(launchersList.itemsProperty().get(), fxLaunchers.get());
        launchersList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                unbindLauncher(oldValue);
                bindLauncher(newValue);
            });
        });
        deleteLauncher.disableProperty().bind(launchersList.getSelectionModel().selectedItemProperty().isNull());
        removeSource.disableProperty().bind(sourcesList.getSelectionModel().selectedItemProperty().isNull());
        removeFlag.disableProperty().bind(flagsTable.getSelectionModel().selectedItemProperty().isNull());
        Platform.runLater(this::setLaunchersCellFactory);

        launchMode.setItems(FXCollections.observableArrayList(Constants.LAUNCH_MODES));
        launchModeCards.put(Constants.LAUNCH_MODES.get(0), configureSteamPane());
        launchModeCards.put(Constants.LAUNCH_MODES.get(1), configureHL2Pane());
        launchMode.valueProperty().addListener((observable, oldKey, newKey) -> {
            if (oldKey != null) {
                Optional.ofNullable(launchModeCards.get(oldKey))
                        .ifPresent(c -> launchModePane.getChildren().remove(c));
            }
            Optional.ofNullable(launchModeCards.get(newKey))
                    .ifPresent(c -> launchModePane.getChildren().add(c));
        });

        keyFlagColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyFlagColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        defaultEnabledColumn.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
        defaultEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(defaultEnabledColumn));
        enabledValueColumn.setCellValueFactory(new PropertyValueFactory<>("enabledValue"));
        enabledValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        disabledValueColumn.setCellValueFactory(new PropertyValueFactory<>("disabledValue"));
        disabledValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        description.setText(Messages.getString("ui.launchers.description", "??", "??"));

        // Validation
        Validator<String> gamePathValidator = Validator.createPredicateValidator(this::isValidGamePath, Messages.getString("ui.launchers.gamePath.invalid"));
        Validator<String> steamPathValidator = Validator.createPredicateValidator(this::isValidSteamPathOrUnneeded, Messages.getString("ui.launchers.steamPath.invalid"));
        Validator<String> appIdValidator = Validator.createPredicateValidator(this::isValidAppIdOrUnneeded, Messages.getString("ui.launchers.appIdInvalid"));
        Validator<String> modNameValidator = Validator.createPredicateValidator(this::isValidModNameOrUnneeded, Messages.getString("ui.launchers.modNameInvalid"));
        Validator<String> basePathValidator = Validator.createPredicateValidator(this::isValidBasePath, Messages.getString("ui.launchers.basePath.invalid"));
        Validator<String> viewNameValidator = Validator.createPredicateValidator(this::isValidViewName, Messages.getString("ui.launchers.viewNameinvalid"));

        validationSupport.registerValidator(gamePath, false, gamePathValidator);
        validationSupport.registerValidator(steamPath, false, steamPathValidator);
        validationSupport.registerValidator(appId, false, appIdValidator);
        validationSupport.registerValidator(modName, false, modNameValidator);
        validationSupport.registerValidator(basePath, false, basePathValidator);
        validationSupport.registerValidator(viewName, false, viewNameValidator);

        expandedContainer = new VBox(5);
        validationReport = new ListView<>(FXCollections.observableArrayList());
        expandedContainer.getChildren().add(validationReport);
    }

    private boolean isValidViewName(String value) {
        return value != null && !value.isEmpty() && gameViews.stream().map(GameView::getName).anyMatch(value::equals);
    }

    private boolean isValidBasePath(String pathStr) {
        return pathStr != null && !pathStr.isEmpty() && LwrtUtils.tryGetPath(pathStr)
                .map(Constants.LWRT_PATH::resolve)
                .filter(Files::isDirectory)
                .isPresent();
    }

    private boolean isValidModNameOrUnneeded(String modNameValue) {
        ExternalString mode = launchMode.getValue();
        return mode == null || !mode.equals(Constants.HL2_LAUNCH_MODE) || !modNameValue.isEmpty();
    }

    private boolean isValidAppIdOrUnneeded(String appIdValue) {
        ExternalString mode = launchMode.getValue();
        return mode == null || !mode.equals(Constants.STEAM_LAUNCH_MODE) || appIdValue.matches("[0-9]+");
    }

    private boolean isValidSteamPathOrUnneeded(String pathStr) {
        ExternalString mode = launchMode.getValue();
        return mode == null || !mode.equals(Constants.STEAM_LAUNCH_MODE) || LwrtUtils.isValidSteamPath(pathStr);
    }

    private boolean isValidGamePath(String pathStr) {
        FxLauncher selected = launchersList.getSelectionModel().getSelectedItem();
        return selected != null && LwrtUtils.isValidGamePath(pathStr, selected.gameExecutableProperty().get());
    }

    private Node configureSteamPane() {
        Label steamPathLabel = new Label(Messages.getString("ui.launchers.steamDir"));
        steamPathLabel.setPrefWidth(100);

        steamPath = new TextField();
        steamPath.setPromptText(Messages.getString("ui.launchers.steamDirPrompt"));
        steamPath.setMaxWidth(Double.MAX_VALUE);

        Button steamPathButton = new Button("...");
        steamPathButton.setOnAction(this::chooseSteamPath);

        Label appIdLabel = new Label(Messages.getString("ui.launchers.appId"));
        appIdLabel.setPrefWidth(100);

        appId = new TextField();
        appId.setPromptText(Messages.getString("ui.launchers.appIdPrompt"));
        appId.setMaxWidth(Double.MAX_VALUE);

        VBox steamPane = new VBox(5);
        HBox steamPathBox = new HBox(5);
        HBox appIdBox = new HBox(5);
        steamPathBox.setAlignment(Pos.CENTER_LEFT);
        appIdBox.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(steamPath, Priority.ALWAYS);
        HBox.setHgrow(appId, Priority.ALWAYS);
        HBox.setHgrow(steamPane, Priority.ALWAYS);
        HBox.setHgrow(steamPathBox, Priority.ALWAYS);
        HBox.setHgrow(appIdBox, Priority.ALWAYS);

        steamPathBox.getChildren().addAll(steamPathLabel, steamPath, steamPathButton);
        appIdBox.getChildren().addAll(appIdLabel, appId);
        steamPane.getChildren().addAll(steamPathBox, appIdBox);
        return steamPane;
    }

    private Node configureHL2Pane() {
        Label modNameLabel = new Label(Messages.getString("ui.launchers.modName"));
        modNameLabel.setPrefWidth(100);

        modName = new TextField();
        modName.setPromptText(Messages.getString("ui.launchers.modNamePrompt"));
        modName.setMaxWidth(Double.MAX_VALUE);

        VBox hl2Pane = new VBox(5);
        HBox modNameBox = new HBox(5);
        modNameBox.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(modName, Priority.ALWAYS);
        HBox.setHgrow(hl2Pane, Priority.ALWAYS);
        HBox.setHgrow(modNameBox, Priority.ALWAYS);

        modNameBox.getChildren().addAll(modNameLabel, modName);
        hl2Pane.getChildren().add(modNameBox);
        return hl2Pane;
    }

    private void setLaunchersCellFactory() {
        launchersList.setCellFactory(listView -> context.getBean(FxLauncherCell.class));
    }

    private void bindLauncher(FxLauncher launcher) {
        if (launcher == null) {
            return;
        }
        Bindings.bindBidirectional(name.textProperty(), launcher.nameProperty());
        Bindings.bindBidirectional(icon.textProperty(), launcher.iconProperty());
        Bindings.bindBidirectional(viewName.textProperty(), launcher.viewNameProperty());
        Bindings.bindBidirectional(gamePath.textProperty(), launcher.gamePathProperty());
        Bindings.bindBidirectional(launchMode.valueProperty(), launcher.launchModeProperty());
        Bindings.bindBidirectional(modName.textProperty(), launcher.modNameProperty());
        Bindings.bindBidirectional(steamPath.textProperty(), launcher.steamPathProperty());
        Bindings.bindBidirectional(appId.textProperty(), launcher.appIdProperty());
        Bindings.bindBidirectional(basePath.textProperty(), launcher.basePathProperty());
        Bindings.bindBidirectional(includeGamePath.selectedProperty(), launcher.includeGamePathProperty());
        Bindings.bindContentBidirectional(sourcesList.itemsProperty().get(), launcher.resourceFoldersProperty());
        Bindings.bindContentBidirectional(flagsTable.itemsProperty().get(), launcher.flagsProperty());
        icon.textProperty().addListener(refreshLauncherCells);
        description.setText(Messages.getString("ui.launchers.description",
                launcher.gameExecutableProperty().get(), launcher.gameProcessProperty().get()));
    }

    private void unbindLauncher(FxLauncher launcher) {
        if (launcher == null) {
            return;
        }
        Bindings.unbindBidirectional(name.textProperty(), launcher.nameProperty());
        Bindings.unbindBidirectional(icon.textProperty(), launcher.iconProperty());
        Bindings.unbindBidirectional(viewName.textProperty(), launcher.viewNameProperty());
        Bindings.unbindBidirectional(gamePath.textProperty(), launcher.gamePathProperty());
        Bindings.unbindBidirectional(launchMode.valueProperty(), launcher.launchModeProperty());
        Bindings.unbindBidirectional(modName.textProperty(), launcher.modNameProperty());
        Bindings.unbindBidirectional(steamPath.textProperty(), launcher.steamPathProperty());
        Bindings.unbindBidirectional(appId.textProperty(), launcher.appIdProperty());
        Bindings.unbindBidirectional(basePath.textProperty(), launcher.basePathProperty());
        Bindings.unbindBidirectional(includeGamePath.selectedProperty(), launcher.includeGamePathProperty());
        Bindings.unbindContentBidirectional(sourcesList.itemsProperty().get(), launcher.resourceFoldersProperty());
        Bindings.unbindContentBidirectional(flagsTable.itemsProperty().get(), launcher.flagsProperty());
        icon.textProperty().removeListener(refreshLauncherCells);
    }

    /**
     * Populate the view with the given data.
     */
    public void load() {
        Platform.runLater(() -> {
            lawenaProperties.getLaunchers().stream()
                    .map(FxLauncher::launcherToFxLauncher)
                    .forEach(fxLaunchers::add);
            launchersList.getSelectionModel().selectFirst();
            validationSupport.initInitialDecoration();
        });
    }

    public void save() {
        if (validationSupport.isInvalid()) {
            ValidationResult result = validationSupport.getValidationResult();
            result.getWarnings().forEach(w -> log.info("Validation {}: {} @ {}", w.getSeverity(), w.getText(), w.getTarget()));
            result.getErrors().forEach(e -> log.info("Validation {}: {} @ {}", e.getSeverity(), e.getText(), e.getTarget()));
            validationReport.getItems().clear();
            result.getMessages().forEach(m -> validationReport.getItems().add(m.getSeverity() + ": " + m.getText()));
            FXUtils.showWarning(
                    Messages.getString("ui.launchers.save.invalidTitle"),
                    Messages.getString("ui.launchers.save.invalidHeader"),
                    Messages.getString("ui.launchers.save.invalidContent"), expandedContainer);
        } else {
            log.debug("Saving launchers from dialog data");
            lawenaProperties.getProfiles().forEach(p -> {
                // find if this profile belongs to a renamed launcher
                // and then rename the profile
                renames.keySet().stream()
                        .filter(k -> k.getLauncher().getName().equals(p.getLauncher()))
                        .forEach(k -> p.setLauncher(renames.get(k)));
            });
            lawenaProperties.setLaunchers(fxLaunchers.stream()
                    .map(FxLauncher::fxLauncherToLauncher)
                    .collect(Collectors.toList()));
            publisher.publishEvent(new LaunchersUpdatedEvent(this));
        }
    }

    public void clear() {
        Platform.runLater(fxLaunchers::clear);
        renames.clear();
    }

    @FXML
    private void addSource(ActionEvent event) {
        FxLauncher launcher = launchersList.getSelectionModel().getSelectedItem();
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Messages.getString("ui.launchers.addSource.title"));
        File selected = chooser.showDialog(launchModePane.getScene().getWindow());
        if (selected != null) {
            // must be a directory, can't start with lwrt, can't be game path
            Path selectedPath = selected.toPath().toAbsolutePath();
            Path gamePath = LwrtUtils.tryGetPath(launcher.gamePathProperty().get()).orElse(Paths.get(""));
            if (selectedPath.equals(gamePath)) {
                includeGamePath.setSelected(true);
            } else if (Files.isDirectory(selectedPath) && !selectedPath.startsWith(Paths.get("").toAbsolutePath())) {
                sourcesList.getItems().add(selectedPath.toString());
            } else {
                FXUtils.showAlert(Messages.getString("ui.launchers.addSource.alertTitle"),
                        Messages.getString("ui.launchers.addSource.alertHeader"),
                        Messages.getString("ui.launchers.addSource.alertContent"));
            }
        }
    }

    @FXML
    private void chooseBasePath(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Messages.getString("ui.launchers.basePath.title"));
        chooser.setInitialDirectory(Constants.LWRT_PATH.toFile());
        File selected = chooser.showDialog(launchModePane.getScene().getWindow());
        if (selected != null) {
            // must be a directory, can't start with lwrt, can't be game path
            Path selectedPath = selected.toPath().toAbsolutePath();
            Path lwrtPath = Constants.LWRT_PATH.toAbsolutePath();
            if (Files.isDirectory(selectedPath) && !selectedPath.startsWith(lwrtPath)) {
                basePath.setText(lwrtPath.relativize(selectedPath).toString());
            } else {
                FXUtils.showAlert(Messages.getString("ui.launchers.basePath.alertTitle"),
                        Messages.getString("ui.launchers.basePath.alertHeader"),
                        Messages.getString("ui.launchers.basePath.alertContent"));
            }
        }
    }

    @FXML
    private void chooseGamePath(ActionEvent event) {
        FxLauncher launcher = launchersList.getSelectionModel().getSelectedItem();
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Messages.getString("ui.launchers.gamePath.title"));
        File initial = LwrtUtils.tryGetPath(gamePath.getText()).map(Path::toAbsolutePath)
                .filter(Files::exists).filter(Files::isDirectory).map(Path::toFile)
                .orElse(null);
        chooser.setInitialDirectory(initial);
        File selected = chooser.showDialog(launchModePane.getScene().getWindow());
        if (selected != null) {
            // must be a directory, can't start with lwrt, can't be game path
            Path selectedPath = selected.toPath().toAbsolutePath();
            String executableName = launcher.gameExecutableProperty().get();
            if (LwrtUtils.isValidGamePath(selectedPath, executableName)) {
                gamePath.setText(selectedPath.toString());
            } else {
                FXUtils.showAlert(Messages.getString("ui.launchers.gamePath.alertTitle"),
                        Messages.getString("ui.launchers.gamePath.alertHeader"),
                        Messages.getString("ui.launchers.gamePath.alertContent", executableName));
            }
        }
    }

    private void chooseSteamPath(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        String steamExecutable = Constants.STEAM_APP_NAME.get();
        chooser.setTitle(Messages.getString("ui.launchers.steamPath.title", steamExecutable));
        File initial = LwrtUtils.tryGetPath(steamPath.getText()).map(Path::toAbsolutePath)
                .filter(Files::exists).filter(Files::isDirectory).map(Path::toFile)
                .orElse(null);
        chooser.setInitialDirectory(initial);
        File selected = chooser.showDialog(launchModePane.getScene().getWindow());
        if (selected != null) {
            // must be a directory, can't start with lwrt, can't be game path
            Path selectedPath = selected.toPath().toAbsolutePath();
            if (LwrtUtils.isValidSteamPath(selectedPath)) {
                steamPath.setText(selectedPath.toString());
            } else {
                FXUtils.showAlert(Messages.getString("ui.launchers.steamPath.alertTitle"),
                        Messages.getString("ui.launchers.steamPath.alertHeader"),
                        Messages.getString("ui.launchers.steamPath.alertContent", steamExecutable));
            }
        }
    }

    @FXML
    private void deleteSelectedLauncher(ActionEvent event) {
        // alert if it's the only one or owns profiles
        FxLauncher launcher = launchersList.getSelectionModel().getSelectedItem();
        if (launcher != null && launchersList.getItems().size() > 1
                && profiles.profilesProperty().get().stream().noneMatch(p -> p.getLauncher().equals(launcher.getName()))) {
            // confirm
            Optional<ButtonType> result = FXUtils.showConfirmation(
                    Messages.getString("ui.launchers.delete.confirmTitle"),
                    Messages.getString("ui.launchers.delete.confirmHeader"),
                    Messages.getString("ui.launchers.delete.confirmContent"));
            if (result.isPresent()) {
                ButtonType buttonType = result.get();
                if (buttonType.equals(ButtonType.OK)) {
                    // and delete
                    launchersList.getItems().remove(launcher);
                }
            }
        } else {
            FXUtils.showWarning(
                    Messages.getString("ui.launchers.delete.alertTitle"),
                    Messages.getString("ui.launchers.delete.alertHeader"),
                    Messages.getString("ui.launchers.delete.alertContent"));
        }
    }

    @FXML
    private void newLauncher(ActionEvent event) {
        // duplicate from current selection
        FxLauncher launcher = launchersList.getSelectionModel().getSelectedItem();
        if (launcher != null) {
            FxLauncher dupe = FxLauncher.duplicate(launcher);
            dupe.setName(findAvailableNameFrom(launcher.getName()));
            launchersList.getItems().add(dupe);
        }
    }

    private String findAvailableNameFrom(String name) {
        String src = name.trim();
        if (!findByName(src).isPresent()) {
            return name;
        }
        String append = " - Copy";
        src = src.contains(append) ? src.substring(0, src.indexOf(append)) : src;
        String dest = src + append;
        int count = 2;
        while (findByName(dest).orElse(null) != null) {
            dest = src + " - Copy (" + count + ")";
            count++;
        }
        return dest;
    }

    private Optional<FxLauncher> findByName(String name) {
        Objects.requireNonNull(name);
        return launchersList.getItems().stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    @FXML
    private void addFlag(ActionEvent event) {
        flagsTable.getItems().add(new FxConfigFlag());
    }

    @FXML
    private void removeSelectedFlag(ActionEvent event) {
        ObservableList<FxConfigFlag> selected = flagsTable.getSelectionModel().getSelectedItems();
        flagsTable.getItems().removeIf(selected::contains);
    }

    @FXML
    private void removeSelectedSource(ActionEvent event) {
        ObservableList<String> selected = sourcesList.getSelectionModel().getSelectedItems();
        sourcesList.getItems().removeIf(selected::contains);
    }

    @FXML
    private void rename(ActionEvent event) {
        FxLauncher current = launchersList.getSelectionModel().getSelectedItem();
        if (current != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(Messages.getString("ui.launchers.rename.title"));
            dialog.setHeaderText(Messages.getString("ui.launchers.rename.header"));
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String value = result.get();
                if (!value.isEmpty() && launchersList.getItems().stream()
                        .noneMatch(item -> !item.equals(current) && item.getName().equals(value))) {
                    current.setName(value);
                    renames.put(current, value);
                    Platform.runLater(this::setLaunchersCellFactory);
                } else {
                    FXUtils.showWarning(
                            Messages.getString("ui.launchers.rename.alertTitle"),
                            Messages.getString("ui.launchers.rename.alertHeader"),
                            Messages.getString("ui.launchers.rename.alertContent"));
                }
            }
        }
    }
}
