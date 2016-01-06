package com.github.lawena.views.launchers;

import com.github.lawena.Messages;
import com.github.lawena.config.Constants;
import com.github.lawena.config.LawenaProperties;
import com.github.lawena.event.LaunchersUpdatedEvent;
import com.github.lawena.util.ExternalString;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
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

    @FXML
    private ListView<FxLauncher> launchersList;

    @FXML
    private TextField name;

    @FXML
    private TextField icon;

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
    private Pane launchModePane;

    private final ListProperty<FxLauncher> fxLaunchers = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final Map<FxLauncher, String> renames = new LinkedHashMap<>();

    private TextField modName;
    private TextField steamPath;
    private TextField appId;
    private Map<ExternalString, Node> launchModeCards = new HashMap<>();
    private ChangeListener<String> refreshLauncherCells = (o, oldValue, newValue) -> {
        if (newValue != null) {
            setLaunchersCellFactory();
        }
    };

    @FXML
    private void initialize() {
        Bindings.bindContentBidirectional(launchersList.itemsProperty().get(), fxLaunchers.get());
        launchersList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            unbindLauncher(oldValue);
            bindLauncher(newValue);
        });
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

        setLaunchersCellFactory();

        keyFlagColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyFlagColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        defaultEnabledColumn.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
        defaultEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(defaultEnabledColumn));
        enabledValueColumn.setCellValueFactory(new PropertyValueFactory<>("enabledValue"));
        enabledValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        disabledValueColumn.setCellValueFactory(new PropertyValueFactory<>("disabledValue"));
        disabledValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        description.setText(Messages.getString("ui.launchers.description"));
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
    }

    private void unbindLauncher(FxLauncher launcher) {
        if (launcher == null) {
            return;
        }
        Bindings.unbindBidirectional(name.textProperty(), launcher.nameProperty());
        Bindings.unbindBidirectional(icon.textProperty(), launcher.iconProperty());
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
        lawenaProperties.getLaunchers().stream()
                .map(FxLauncher::launcherToFxLauncher)
                .forEach(fxLaunchers::add);
        launchersList.getSelectionModel().selectFirst();
    }

    public void save() {
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

    public void clear() {
        fxLaunchers.clear();
        renames.clear();
    }

    @FXML
    private void addFlag(ActionEvent event) {

    }

    @FXML
    private void addSource(ActionEvent event) {

    }

    @FXML
    private void chooseBasePath(ActionEvent event) {

    }

    @FXML
    private void chooseGamePath(ActionEvent event) {

    }

    private void chooseSteamPath(ActionEvent event) {

    }

    @FXML
    private void deleteSelectedLauncher(ActionEvent event) {

    }

    @FXML
    private void newLauncher(ActionEvent event) {

    }

    @FXML
    private void removeSelectedFlag(ActionEvent event) {

    }

    @FXML
    private void removeSelectedSource(ActionEvent event) {

    }

    @FXML
    private void rename(ActionEvent event) {
        setLaunchersCellFactory();
    }
}
