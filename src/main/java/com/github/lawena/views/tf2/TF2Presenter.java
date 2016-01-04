package com.github.lawena.views.tf2;

import com.github.lawena.Messages;
import com.github.lawena.config.Constants;
import com.github.lawena.domain.*;
import com.github.lawena.event.NewResourceEvent;
import com.github.lawena.service.Profiles;
import com.github.lawena.service.Resources;
import com.github.lawena.service.TaskService;
import com.github.lawena.util.*;
import com.github.lawena.views.GamePresenter;
import com.github.lawena.util.TagsCell;
import com.github.lawena.views.dialog.CustomSettingsDialog;
import com.github.lawena.views.dialog.ResourceFilterDialog;
import com.github.lawena.views.dialog.SimpleCustomSettingsDialog;
import com.github.lawena.views.dialog.data.CustomSettingsData;
import com.github.lawena.views.tf2.skybox.PreviewTask;
import com.github.lawena.views.tf2.skybox.Skybox;
import com.github.lawena.views.tf2.skybox.SkyboxStore;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.lawena.util.BindUtils.*;

@Component
public class TF2Presenter implements GamePresenter {

    private static final Logger log = LoggerFactory.getLogger(TF2Presenter.class);

    @Autowired
    private Resources resources;
    @Autowired
    private Profiles profiles;
    @Autowired
    private TaskService taskService;

    @FXML
    private TitledPane launchPane;
    @FXML
    private TitledPane recorderPane;
    @FXML
    private TitledPane configPane;
    @FXML
    private TitledPane resourcesPane;

    /////////////////////////////////////////////////////////////////////////
    // Launch pane controls
    @FXML
    private Spinner<Integer> width;
    @FXML
    private Spinner<Integer> height;
    @FXML
    private ComboBox<ExternalString> dxlevel;
    @FXML
    private CheckBox insecure;
    @FXML
    private CheckBox defaultLaunch;

    private StringProperty advLaunchProperty = new SimpleStringProperty("");

    private TextInputDialog advLaunchDialog;

    /////////////////////////////////////////////////////////////////////////
    // Record options controls
    @FXML
    private ComboBox<ExternalString> captureMode;
    @FXML
    private Spinner<Integer> fps;
    @FXML
    private TextField framesPath;
    @FXML
    private Spinner<Integer> quality;
    @FXML
    private VBox recorderInfo;

    private Map<ExternalString, Pane> recorderInfoCards = new HashMap<>();

    private DirectoryChooser framesPathChooser = new DirectoryChooser();

    /////////////////////////////////////////////////////////////////////////
    // Game configuration controls
    @FXML
    private ComboBox<ExternalString> hud;
    @FXML
    private ComboBox<Skybox> skybox;
    @FXML
    private ComboBox<ExternalString> vmSwitch;
    @FXML
    private Label withLabel;
    @FXML
    private Label vmFovLabel;
    @FXML
    private Spinner<Double> vmFov;
    @FXML
    private TableView<ConfigFlag> configFlagTable;

    /////////////////////////////////////////////////////////////////////////
    // Custom resources controls
    @FXML
    private TableView<Resource> resourcesTable;

    /////////////////////////////////////////////////////////////////////////
    // other fields
    private ValidationSupport validationSupport = new ValidationSupport();
    private TF2Properties config = new TF2Properties();
    private SkyboxStore skyboxes = new SkyboxStore();
    private CustomSettingsDialog customSettingsDialog;
    private ResourceFilterDialog resourceFilterDialog;

    @FXML
    private void initialize() {
        configureLaunchOptions();
        configureRecorderOptions();
        configureRecorderInfoCards();
        configureGameConfigOptions();
        configureResourcesOptions();
        configureSkyboxRepository();

        Validator<String> qualityValidator = Validator.createEmptyValidator(Messages.getString("ui.tf2.recorder.qualityInvalidEmpty"));
        Validator<String> fpsValidator = Validator.createEmptyValidator(Messages.getString("ui.tf2.recorder.fpsInvalidEmpty"));
        Validator<String> framesPathValidator = Validator.combine(
                Validator.createEmptyValidator(Messages.getString("ui.tf2.recorder.recordingPathInvalidEmpty")),
                Validator.createPredicateValidator(this::isValidFolder, Messages.getString("ui.tf2.recorder.recordingPathInvalidFolder")));
        Validator<String> widthValidator = Validator.createEmptyValidator(Messages.getString("ui.tf2.launch.widthInvalidEmpty"));
        Validator<String> heightValidator = Validator.createEmptyValidator(Messages.getString("ui.tf2.launch.heightInvalidEmpty"));
        Validator<String> vmFovValidator = Validator.createEmptyValidator(Messages.getString("ui.tf2.config.vmFovInvalidEmpty"));

        Predicate<ExternalString> hudTableEnabled = (x -> resourcesTable.getItems().stream()
                .anyMatch(r -> r.isEnabled() && r.getTags().stream().map(Tag::getName).anyMatch(name -> name.equals("hud"))));
        Predicate<ExternalString> hudComboSelected = (x -> x == null || !x.equals(Constants.HUDS.get(2)));
        Validator<ExternalString> customHudValidator = Validator.createPredicateValidator(
                xor(hudTableEnabled, hudComboSelected),
                Messages.getString("ui.tf2.config.customHudNotMatching"), Severity.WARNING);

        validationSupport.registerValidator(quality.getEditor(), false, qualityValidator);
        validationSupport.registerValidator(fps.getEditor(), false, fpsValidator);
        validationSupport.registerValidator(framesPath, false, framesPathValidator);
        validationSupport.registerValidator(width.getEditor(), false, widthValidator);
        validationSupport.registerValidator(height.getEditor(), false, heightValidator);
        validationSupport.registerValidator(vmFov.getEditor(), false, vmFovValidator);
        validationSupport.registerValidator(hud, false, customHudValidator);
    }

    private <T> Predicate<T> xor(Predicate<T> a, Predicate<T> b) {
        return a.and(b.negate()).or(a.negate().and(b));
    }

    private void configureLaunchOptions() {
        SpinnerValueFactory<Integer> widthValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Constants.MINIMUM_WIDTH, Integer.MAX_VALUE, Constants.INITIAL_WIDTH);
        widthValueFactory.setConverter(new ZeroIntegerStringConverter());
        width.setValueFactory(widthValueFactory);

        SpinnerValueFactory<Integer> heightValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Constants.MINIMUM_HEIGHT, Integer.MAX_VALUE, Constants.INITIAL_HEIGHT);
        heightValueFactory.setConverter(new ZeroIntegerStringConverter());
        height.setValueFactory(heightValueFactory);

        FXUtils.restrictInput(width, Constants.ALLOWED_NUMERIC);
        FXUtils.restrictInput(height, Constants.ALLOWED_NUMERIC);

        Pattern widthPattern = Pattern.compile("^.*(-w|-width) [0-9]+.*$");
        Pattern heightPattern = Pattern.compile("^.*(-h|-height) [0-9]+.*$");
        FXUtils.patternBinding(widthPattern, width.disableProperty(), advLaunchProperty);
        FXUtils.patternBinding(heightPattern, height.disableProperty(), advLaunchProperty);

        dxlevel.setItems(FXCollections.observableArrayList(Constants.DIRECTX_LEVELS));

        Pattern dxlevelPattern = Pattern.compile("^.*-dxlevel (80|81|90|95|98).*$");
        FXUtils.patternBinding(dxlevelPattern, dxlevel.disableProperty(), advLaunchProperty);

        Pattern insecurePattern = Pattern.compile("^.*-insecure.*$");
        FXUtils.patternBinding(insecurePattern, insecure.disableProperty(), advLaunchProperty);

        Pattern defaultPattern = Pattern.compile("^.*-default.*$");
        FXUtils.patternBinding(defaultPattern, defaultLaunch.disableProperty(), advLaunchProperty);
    }

    private void configureRecorderOptions() {
        SpinnerValueFactory<Integer> fpsValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Constants.MINIMUM_FPS, Integer.MAX_VALUE, Constants.INITIAL_FPS);
        fpsValueFactory.setConverter(new ZeroIntegerStringConverter());
        fps.setValueFactory(fpsValueFactory);

        FXUtils.restrictInput(fps, Constants.ALLOWED_NUMERIC);

        captureMode.setItems(FXCollections.observableArrayList(Constants.CAPTURE_MODES));

        // setup recording method (captureMode) combo-box changes
        captureMode.valueProperty().addListener((obs, oldKey, newKey) -> {
            if (oldKey != null) {
                Optional.ofNullable(recorderInfoCards.get(oldKey))
                        .ifPresent(c -> recorderInfo.getChildren().remove(c));
            }
            Optional.ofNullable(recorderInfoCards.get(newKey))
                    .ifPresent(c -> recorderInfo.getChildren().add(c));
        });
    }

    private void configureRecorderInfoCards() {
        Label tgaInfo = new Label(Messages.getString("ui.tf2.recorder.captureTGA"));
        tgaInfo.setWrapText(true);
        tgaInfo.setAlignment(Pos.TOP_LEFT);
        tgaInfo.setMaxHeight(Double.MAX_VALUE);

        Label jpgInfo = new Label(Messages.getString("ui.tf2.recorder.captureJPEG"));
        jpgInfo.setWrapText(true);
        jpgInfo.setAlignment(Pos.TOP_LEFT);
        jpgInfo.setMaxHeight(Double.MAX_VALUE);
        Label qualityLabel = new Label(Messages.getString("ui.tf2.recorder.qualityJPEG"));
        quality = new Spinner<>(Constants.MINIMUM_JPEG_QUALITY, Constants.MAXIMUM_JPEG_QUALITY, Constants.INITIAL_JPEG_QUALITY);
        quality.setEditable(true);
        quality.setPrefWidth(85);
        FXUtils.restrictInput(quality, Constants.ALLOWED_NUMERIC);
        quality.getValueFactory().setConverter(new ZeroIntegerStringConverter());
        HBox jpgHBox = new HBox(5, qualityLabel, quality);
        jpgHBox.setAlignment(Pos.CENTER_RIGHT);

        Label manInfo = new Label(Messages.getString("ui.tf2.recorder.captureSrcDemoManaged"));
        manInfo.setWrapText(true);
        manInfo.setAlignment(Pos.TOP_LEFT);
        manInfo.setMaxHeight(Double.MAX_VALUE);
        Button cfgSrcDemo = new Button(Messages.getString("ui.tf2.recorder.configureSrcDemo"));
        // TODO: SrcDemo2 integration pending
        cfgSrcDemo.setDisable(true);
        //cfgSrcDemo.setOnAction(evt -> FXUtils.showAlert(title, header, content));
        HBox srcHBox = new HBox(5, cfgSrcDemo);
        srcHBox.setAlignment(Pos.CENTER_RIGHT);

        Label stdInfo = new Label(Messages.getString("ui.tf2.recorder.captureSrcDemoStandalone"));
        stdInfo.setWrapText(true);
        stdInfo.setAlignment(Pos.TOP_LEFT);
        stdInfo.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(tgaInfo, Priority.ALWAYS);
        VBox.setVgrow(jpgInfo, Priority.ALWAYS);
        VBox.setVgrow(manInfo, Priority.ALWAYS);
        VBox.setVgrow(stdInfo, Priority.ALWAYS);

        VBox tgaVBox = new VBox(5, tgaInfo);
        VBox jpgVBox = new VBox(5, jpgInfo, jpgHBox);
        VBox manVBox = new VBox(5, manInfo, srcHBox);
        VBox stdVBox = new VBox(5, stdInfo);

        VBox.setVgrow(tgaVBox, Priority.ALWAYS);
        VBox.setVgrow(jpgVBox, Priority.ALWAYS);
        VBox.setVgrow(manVBox, Priority.ALWAYS);
        VBox.setVgrow(stdVBox, Priority.ALWAYS);

        recorderInfoCards.put(Constants.CAPTURE_MODES.get(0), tgaVBox);
        recorderInfoCards.put(Constants.CAPTURE_MODES.get(1), jpgVBox);
        recorderInfoCards.put(Constants.CAPTURE_MODES.get(2), manVBox);
        recorderInfoCards.put(Constants.CAPTURE_MODES.get(3), stdVBox);
    }

    private void configureGameConfigOptions() {
        hud.setItems(FXCollections.observableArrayList(Constants.HUDS));

        Callback<ListView<Skybox>, ListCell<Skybox>> cellFactory = param -> new ListCell<Skybox>() {
            @Override
            protected void updateItem(Skybox item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(new ImageView(item.getPreview()));
                    setText(item.getName());
                }
            }
        };
        skybox.setButtonCell(cellFactory.call(null));
        skybox.setCellFactory(cellFactory);

        vmSwitch.setItems(FXCollections.observableArrayList(Constants.VIEWMODELS));

        SpinnerValueFactory<Double> vmFovValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                Constants.MINIMUM_VM_FOV, Constants.MAXIMUM_VM_FOV, Constants.INITIAL_VM_FOV);
        vmFovValueFactory.setConverter(new ZeroDoubleStringConverter());
        vmFov.setValueFactory(vmFovValueFactory);

        FXUtils.restrictInput(vmFov, Constants.ALLOWED_DECIMAL);

        // disable fov spinner when the vmSwitch has "Force OFF" as selected value
        BooleanBinding forceOffBinding = vmSwitch.valueProperty().isEqualTo(Constants.VIEWMODELS.get(1));
        vmFov.disableProperty().bind(forceOffBinding);
        withLabel.disableProperty().bind(forceOffBinding);
        vmFovLabel.disableProperty().bind(forceOffBinding);

        TableColumn<ConfigFlag, Boolean> enabledCol = new TableColumn<>(Messages.getString("ui.tf2.config.enabledColumn"));
        enabledCol.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabledCol.setCellFactory(CheckBoxTableCell.forTableColumn(enabledCol));
        enabledCol.setEditable(true);

        TableColumn<ConfigFlag, String> nameCol =
                new TableColumn<>(Messages.getString("ui.tf2.config.keyColumn"));
        nameCol.setCellValueFactory(g -> new ReadOnlyObjectWrapper<>(local(g.getValue().getKey())));
        nameCol.setEditable(false);

        configFlagTable.getColumns().add(enabledCol);
        configFlagTable.getColumns().add(nameCol);
        FXUtils.configureColumn(enabledCol, configFlagTable, 0.12, 10);
        FXUtils.configureColumn(nameCol, configFlagTable, 0.88, 10);

        // add context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem selectAll = new MenuItem(Messages.getString("ui.tf2.config.selectAll"));
        selectAll.setOnAction(e -> config.getFlagItems().forEach(configFlag -> configFlag.setEnabled(true)));
        MenuItem selectNone = new MenuItem(Messages.getString("ui.tf2.config.selectNone"));
        selectNone.setOnAction(e -> config.getFlagItems().forEach(configFlag -> configFlag.setEnabled(false)));
        MenuItem selectDefault = new MenuItem(Messages.getString("ui.tf2.config.selectDefaults"));
        selectDefault.setOnAction(e -> config.getFlagItems().forEach(configFlag -> configFlag.setEnabled(configFlag.getDefaultValue())));
        contextMenu.getItems().addAll(selectAll, selectNone, selectDefault);
        configFlagTable.setContextMenu(contextMenu);
    }

    private void configureResourcesOptions() {
        TableColumn<Resource, Boolean> enabledCol = new TableColumn<>(Messages.getString("ui.tf2.resources.enabledColumn"));
        enabledCol.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabledCol.setCellFactory(CheckBoxTableCell.forTableColumn(enabledCol));
        enabledCol.setEditable(true);

        TableColumn<Resource, String> nameCol =
                new TableColumn<>(Messages.getString("ui.tf2.resources.nameColumn"));
        nameCol.setCellValueFactory(r -> new ReadOnlyObjectWrapper<>(local(r.getValue().getName())));
        nameCol.setEditable(false);

        TableColumn<Resource, ObservableSet<Tag>> tagsCol =
                new TableColumn<>(Messages.getString("ui.tf2.resources.tagsColumn"));
        tagsCol.setCellValueFactory(new PropertyValueFactory<>("tags"));
        tagsCol.setCellFactory(param -> new TagsCell());
        tagsCol.setEditable(false);

        resourcesTable.getColumns().add(enabledCol);
        resourcesTable.getColumns().add(nameCol);
        resourcesTable.getColumns().add(tagsCol);
        FXUtils.configureColumn(enabledCol, resourcesTable, 0.1, 7);
        FXUtils.configureColumn(nameCol, resourcesTable, 0.60, 7);
        FXUtils.configureColumn(tagsCol, resourcesTable, 0.29, 7);

        // setup context menus
        // -- first, for the [enabled] column header
        ContextMenu contextMenu = new ContextMenu();
        MenuItem selectAll = new MenuItem(Messages.getString("ui.tf2.resources.selectAll"));
        selectAll.setOnAction(e -> resources.enableAll());
        MenuItem selectNone = new MenuItem(Messages.getString("ui.tf2.resources.selectNone"));
        selectNone.setOnAction(e -> resources.disableAll());
        contextMenu.getItems().addAll(selectAll, selectNone);
        resourcesTable.setContextMenu(contextMenu);
        // -- then, for each of the rows of the table
        resourcesTable.setRowFactory(
                tableView -> {
                    final TableRow<Resource> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    MenuItem refreshItem = new MenuItem(Messages.getString("ui.tf2.resources.menuRefresh"));
                    refreshItem.setOnAction(e -> refreshSelectedResource());
                    MenuItem removeItem = new MenuItem(Messages.getString("ui.tf2.resources.menuRemove"));
                    removeItem.setOnAction(e -> removeSelectedResource());
                    MenuItem filterItem = new MenuItem(Messages.getString("ui.tf2.resources.menuFilter"));
                    filterItem.setOnAction(e -> filterSelectedResource());
                    rowMenu.getItems().addAll(refreshItem, removeItem, filterItem);

                    // only display context menu for non-null items:
                    row.contextMenuProperty().bind(
                            Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                    .then(rowMenu)
                                    .otherwise((ContextMenu) null));
                    return row;
                });

        // bind table view with data
        Bindings.bindContentBidirectional(resourcesTable.itemsProperty().get(), resources.getResourceList());
    }

    private void configureSkyboxRepository() {
        Profile profile = profiles.getSelected();
        Launcher launcher = profiles.getLauncher(profile).get();
        Path base = LwrtUtils.tryGetPath(launcher.getBasePath())
                .map(Constants.LWRT_PATH::resolve)
                .get();
        List<Skybox> toPreviewTask = new ArrayList<>();
        Path dir = base.resolve("skybox/vtf");
        if (Files.exists(dir)) {
            log.debug("Loading skybox folder");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
                stream.forEach(p -> toPreviewTask.add(new Skybox(p)));
            } catch (IOException e) {
                log.warn("Problem while loading skybox folder", e);
            }
        }
        skyboxes.add(Skybox.DEFAULT);
        Path cache = base.resolve("skybox/skybox.ser");
        skyboxes.load(cache);
        toPreviewTask.removeIf(s -> skyboxes.getItems().contains(s));
        Path vtf = Constants.VTFCMD_PATH.toAbsolutePath();
        PreviewTask task = new PreviewTask(vtf, toPreviewTask);
        if (!toPreviewTask.isEmpty()) {
            skyboxes.getItems().addAll(toPreviewTask);
            taskService.submitTask(task);
            CompletableFuture.supplyAsync(() -> {
                try {
                    return task.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("Skybox preview task was interrupted: {}", e.toString());
                }
                return task.getPartialResults();
            }).thenAcceptAsync(list -> {
                list.addAll(0, skyboxes.getItems().stream().filter(s -> !list.contains(s)).collect(Collectors.toList()));
                skyboxes.setItems(list);
                skyboxes.save(cache);
            }, Platform::runLater);
        }
        skybox.itemsProperty().bind(skyboxes.itemsProperty());
    }

    private boolean isValidFolder(String str) {
        try {
            Path path = Paths.get(str);
            return Files.exists(path) && Files.isDirectory(path);
        } catch (InvalidPathException e) {
            return false;
        }
    }

    private void refreshSelectedResource() {
        log.debug("Refreshing table selected resource");
        final ObservableList<TablePosition> posList = resourcesTable.getSelectionModel().getSelectedCells();
        posList.forEach(p -> {
            Resource resource = resourcesTable.getItems().get(p.getRow());
            resources.refreshResource(resource.getPath());
        });
    }

    private void removeSelectedResource() {
        log.debug("Removing table selected resource");
        final ObservableList<TablePosition> posList = resourcesTable.getSelectionModel().getSelectedCells();
        posList.forEach(p -> {
            Resource resource = resourcesTable.getItems().get(p.getRow());
            resources.deleteResource(resource.getPath());
        });
    }

    private void filterSelectedResource() {
        log.debug("Filtering table selected resource");
        final ObservableList<TablePosition> posList = resourcesTable.getSelectionModel().getSelectedCells();
        posList.stream().findFirst().ifPresent(p -> {
            Resource resource = resourcesTable.getItems().get(p.getRow());
            getResourceFilterDialog()
                    .withInputData(resources.getResourceContents(resource), resource.getExcludedPaths())
                    .showAndWait()
                    .ifPresent(excluded -> resource.getExcludedPaths().setAll(excluded));
        });
    }

    private ResourceFilterDialog getResourceFilterDialog() {
        if (resourceFilterDialog == null) {
            resourceFilterDialog = new ResourceFilterDialog(resourcesTable.getScene().getWindow());
        }
        return resourceFilterDialog;
    }

    static String local(String key) {
        return Messages.getStringWithFallback(key, key);
    }

    @FXML
    private void showLaunchOptionsDialog(ActionEvent event) {
        getLaunchOptionsDialog().showAndWait().ifPresent(advLaunchProperty::set);
    }

    private TextInputDialog getLaunchOptionsDialog() {
        if (advLaunchDialog == null) {
            advLaunchDialog = new TextInputDialog(advLaunchProperty.get());
            advLaunchDialog.setTitle(Messages.getString("ui.tf2.launch.advancedDialog.title"));
            advLaunchDialog.setHeaderText(Messages.getString("ui.tf2.launch.advancedDialog.header"));
            advLaunchDialog.setContentText(Messages.getString("ui.tf2.launch.advancedDialog.content"));
        }
        return advLaunchDialog;
    }

    @FXML
    private void showFramesPathChooser(ActionEvent event) {
        LwrtUtils.tryGetPath(framesPath.getText())
                .filter(LwrtUtils::isNotEmptyPath)
                .filter(Files::isDirectory)
                .ifPresent(current -> framesPathChooser.setInitialDirectory(current.toFile()));
        File selectedFile = framesPathChooser.showDialog(null);
        if (selectedFile != null) {
            framesPath.setText(selectedFile.getAbsolutePath());
        }
    }

    private Window windowFromEvent(ActionEvent event) {
        return ((Node) event.getSource()).getScene().getWindow();
    }

    @FXML
    private void showCustomSettingsDialog(ActionEvent event) {
        if (customSettingsDialog == null) {
            customSettingsDialog = new SimpleCustomSettingsDialog(windowFromEvent(event));
        }
        CustomSettingsData input = new CustomSettingsData();
        input.setContent(config.customSettingsProperty().get());
        input.setDimension(config.customSettingsDialogProperty().get());
        customSettingsDialog.withInputData(input).showAndWait().ifPresent(data -> {
            config.customSettingsProperty().set(data.getContent());
            config.customSettingsDialogProperty().set(data.getDimension());
        });
    }

    @EventListener
    public void handleNewResource(NewResourceEvent event) {
        Profile profile = profiles.getSelected();
        List<String> selected = getSelected(profile);
        Map<String, List<String>> excluded = getExcluded(profile);
        Resource resource = event.getSource();
        updateResourceFields(resource, selected, excluded);
    }

    @Override
    public void bind(Profile profile) {
        load(profile, config.widthProperty(), width.getValueFactory().valueProperty());
        load(profile, config.heightProperty(), height.getValueFactory().valueProperty());
        load(profile, config.insecureProperty(), insecure.selectedProperty());
        load(profile, config.defaultLaunchProperty(), defaultLaunch.selectedProperty());
        load(profile, config.launchOptionsProperty(), advLaunchProperty);
        load(profile, config.fpsProperty(), fps.getValueFactory().valueProperty());
        load(profile, config.qualityProperty(), quality.getValueFactory().valueProperty());
        load(profile, config.framesPathProperty(), framesPath.textProperty());
        load(profile, config.viewmodelFovProperty(), vmFov.getValueFactory().valueProperty());

        loadAsString(profile, config.dxlevelProperty(), dxlevel.valueProperty(), this::keyToDxLevel);
        loadAsString(profile, config.captureModeProperty(), captureMode.valueProperty(), this::keyToCaptureMode);
        loadAsString(profile, config.hudProperty(), hud.valueProperty(), this::keyToHud);
        loadAsString(profile, config.skyboxProperty(), skybox.valueProperty(), this::getSkybox);
        loadAsString(profile, config.viewmodelSwitchProperty(), vmSwitch.valueProperty(), this::keyToViewmodel);

        load(profile, config.customSettingsProperty());
        load(profile, config.customSettingsDialogProperty());

        Launcher launcher = profiles.getLauncher(profile).get();
        launcher.getFlags().stream().forEach(f ->
                load(profile, config.getProperty(f.getKey(), f.getDefaultValue())));
        Bindings.bindContentBidirectional(configFlagTable.itemsProperty().get(), config.flagItemsProperty()
                .get());
        config.getFlagItems().setAll(launcher.getFlags());
        config.getFlagItems().forEach(f -> f.setEnabled(config.getPropertyMap().get(f.getKey()).get()));

        log.debug("Refreshing resource selection after profile load");
        refreshResourceSelection(profile);

        taskService.scheduleLater(1000, b -> validationSupport.initInitialDecoration());
    }

    @Override
    public void unbind(Profile profile) {
        save(profile, config.widthProperty(), width.getValueFactory().valueProperty());
        save(profile, config.heightProperty(), height.getValueFactory().valueProperty());
        save(profile, config.insecureProperty(), insecure.selectedProperty());
        save(profile, config.defaultLaunchProperty(), defaultLaunch.selectedProperty());
        save(profile, config.launchOptionsProperty(), advLaunchProperty);
        save(profile, config.fpsProperty(), fps.getValueFactory().valueProperty());
        save(profile, config.qualityProperty(), quality.getValueFactory().valueProperty());
        save(profile, config.framesPathProperty(), framesPath.textProperty());
        save(profile, config.viewmodelFovProperty(), vmFov.getValueFactory().valueProperty());

        save(profile, config.dxlevelProperty(), dxlevel.valueProperty(), ExternalString::getKey);
        save(profile, config.captureModeProperty(), captureMode.valueProperty(), ExternalString::getKey);
        save(profile, config.hudProperty(), hud.valueProperty(), ExternalString::getKey);
        save(profile, config.skyboxProperty(), skybox.valueProperty(), Skybox::getName);
        save(profile, config.viewmodelSwitchProperty(), vmSwitch.valueProperty(), ExternalString::getKey);

        save(profile, config.customSettingsProperty());
        save(profile, config.customSettingsDialogProperty());

        Launcher launcher = profiles.getLauncher(profile).get();
        launcher.getFlags().stream().forEach(f -> {
                    BooleanProperty property = config.getProperty(f.getKey(), f.getDefaultValue());
                    property.set(f.getEnabled());
                    save(profile, property);
                }
        );
        Bindings.unbindContentBidirectional(configFlagTable.itemsProperty().get(), config.flagItemsProperty()
                .get());

        profile.set(
                TF2Properties.RESOURCES_KEY,
                resources.getResourceList().stream()
                        .filter(Resource::isEnabled)
                        .map(Resource::getName)
                        .collect(Collectors.toList()));
        profile.set(TF2Properties.EXCLUDED_KEY,
                resources.getResourceList().stream()
                        .filter(r -> !r.getExcludedPaths().isEmpty())
                        .collect(Collectors.toMap(Resource::getName, this::excludedList)));
    }

    private List<String> excludedList(Resource resource) {
        List<String> list = new ArrayList<>();
        list.addAll(resource.getExcludedPaths());
        return list;
    }

    public ExternalString keyToDxLevel(String key) {
        return exists(Constants.DIRECTX_LEVELS, key).orElse(config.dxlevelProperty().get());
    }

    public ExternalString keyToCaptureMode(String key) {
        return exists(Constants.CAPTURE_MODES, key).orElse(config.captureModeProperty().get());
    }

    public Skybox getSkybox(String key) {
        return skyboxes.getSkybox(key).orElse(Skybox.DEFAULT);
    }

    public ExternalString keyToHud(String key) {
        return exists(Constants.HUDS, key).orElse(config.hudProperty().get());
    }

    public ExternalString keyToViewmodel(String key) {
        return exists(Constants.VIEWMODELS, key).orElse(config.viewmodelSwitchProperty().get());
    }

    private void refreshResourceSelection(Profile profile) {
        List<String> selected = getSelected(profile);
        Map<String, List<String>> excluded = getExcluded(profile);
        resources.getResourceList().forEach(resource -> updateResourceFields(resource, selected, excluded));
    }

    private List<String> getSelected(Profile profile) {
        return profile.get(TF2Properties.RESOURCES_KEY)
                .filter(o -> o instanceof Collection<?>) // to avoid a throwing lambda
                .map(o -> checkedList((Collection<?>) o))
                .orElse(new ArrayList<>(TF2Properties.DEFAULT_RESOURCES));
    }

    private Map<String, List<String>> getExcluded(Profile profile) {
        return profile.get(TF2Properties.EXCLUDED_KEY)
                .filter(o -> o instanceof Map<?, ?>) // to avoid a throwing lambda
                .map(o -> checkedMap((Map<?, ?>) o))
                .orElseGet(HashMap::new);
    }

    private void updateResourceFields(Resource resource, List<String> selected, Map<String, List<String>> excluded) {
        resource.setEnabled(selected.contains(resource.getName()));
        if (excluded.containsKey(resource.getName())) {
            resource.getExcludedPaths().setAll(excluded.get(resource.getName()));
        }
    }

    private List<String> checkedList(Collection<?> collection) {
        List<String> list = new ArrayList<>();
        for (Object o : collection) {
            try {
                list.add((String) o);
            } catch (ClassCastException e) {
                log.warn("Ignoring invalid element in List<String>: {}", o);
            }
        }
        return list;
    }

    private Map<String, List<String>> checkedMap(Map<?, ?> raw) {
        Map<String, List<String>> map = new HashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            try {
                Object key = entry.getKey();
                Object value = entry.getValue();
                map.put((String) key, checkedList((Collection<?>) value));
            } catch (ClassCastException e) {
                log.warn("Ignoring invalid element in Map<String, List<String>>: {} -> {}", entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    @Override
    public ValidationResult validate(Profile profile) {
        ValidationResult result = validationSupport.getValidationResult();

        save(profile, config.widthProperty());
        save(profile, config.heightProperty());
        save(profile, config.insecureProperty());
        save(profile, config.defaultLaunchProperty());
        save(profile, config.launchOptionsProperty());
        save(profile, config.fpsProperty());
        save(profile, config.qualityProperty());
        save(profile, config.framesPathProperty());
        save(profile, config.viewmodelFovProperty());

        save(profile, config.dxlevelProperty(), ExternalString::getKey);
        save(profile, config.captureModeProperty(), ExternalString::getKey);
        save(profile, config.hudProperty(), ExternalString::getKey);
        save(profile, config.skyboxProperty(), Skybox::getName);
        save(profile, config.viewmodelSwitchProperty(), ExternalString::getKey);

        save(profile, config.customSettingsProperty());
        save(profile, config.customSettingsDialogProperty());

        Launcher launcher = profiles.getLauncher(profile).get();
        launcher.getFlags().stream().forEach(f -> {
                    BooleanProperty property = config.getProperty(f.getKey(), f.getDefaultValue());
                    property.set(f.getEnabled());
                    save(profile, property);
                }
        );
        profile.set(
                TF2Properties.RESOURCES_KEY,
                resources.getResourceList().stream()
                        .filter(Resource::isEnabled)
                        .map(Resource::getName)
                        .collect(Collectors.toList()));
        profile.set(TF2Properties.EXCLUDED_KEY,
                resources.getResourceList().stream()
                        .filter(r -> !r.getExcludedPaths().isEmpty())
                        .collect(Collectors.toMap(Resource::getName, this::excludedList)));
        return result;
    }

}
