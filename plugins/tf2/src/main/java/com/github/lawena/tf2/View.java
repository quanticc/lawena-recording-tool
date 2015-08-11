package com.github.lawena.tf2;

import com.github.lawena.files.Resource;
import com.github.lawena.game.Group;
import com.github.lawena.tf2.skybox.Skybox;
import com.github.lawena.tf2.util.ZeroDoubleStringConverter;
import com.github.lawena.tf2.util.ZeroIntegerStringConverter;
import com.github.lawena.util.ExternalString;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class View {

    // internal controls
    private HBox _resolution;
    private HBox _dxlevel;
    private HBox _advanced;
    private TextInputDialog _dialog;
    private HBox _record;
    private VBox _recInfo;
    private HBox _hud;
    private HBox _skybox;
    private HBox _viewmodels;
    private HBox _config;
    private HBox _resInfo;
    private HBox _resources;

    // exposed controls
    private Spinner<Integer> width;
    private Spinner<Integer> height;
    private ComboBox<ExternalString> dxlevel;
    private Button advanced;
    private ComboBox<ExternalString> captureMode;
    private Spinner<Integer> fps;
    private Spinner<Integer> quality;
    private Button cfgSrcDemo;
    private ComboBox<ExternalString> hud;
    private ComboBox<Skybox> skybox;
    private ComboBox<ExternalString> vmSwitch;
    private Spinner<Double> vmFov;
    private TableView<Group> groupTable;
    private TableView<Resource> resourcesTable;

    // extra properties
    private StringProperty advLaunchProperty = new SimpleStringProperty(""); //$NON-NLS-1$

    // other data
    private Map<ExternalString, Pane> infoCards = new HashMap<>();

    private ValidationSupport validation = new ValidationSupport();

    private static void restrictInput(Node node, String allowedChars) {
        node.addEventFilter(KeyEvent.KEY_TYPED, ke -> {
            if (!allowedChars.contains(ke.getCharacter())) {
                ke.consume();
            }
        });
    }

    private static void patternBinding(Pattern pattern, BooleanProperty target, StringProperty source) {
        BooleanBinding binding =
                Bindings.createBooleanBinding(() -> pattern.matcher(source.get()).matches(), source);
        target.bind(binding);
    }

    protected static String local(String key) {
        return Messages.getString(key);
    }

    private static void configureColumn(TableColumn<?, ?> column, TableView<?> table, double prop) {
        column.prefWidthProperty().bind(table.widthProperty().multiply(prop).subtract(1));
        column.maxWidthProperty().bind(column.prefWidthProperty());
        column.setResizable(false);
    }

    private static void showAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(Messages.getString("View.ConfigureSrcDemoTitle")); //$NON-NLS-1$
        alert.setHeaderText(null);
        alert.setContentText(Messages.getString("View.ConfigureSrcDemoContent")); //$NON-NLS-1$
        alert.showAndWait();
    }

    public ValidationSupport getValidation() {
        return validation;
    }

    public HBox getResolutionBox() {
        if (_resolution == null) {
            Label label = new Label(Messages.getString("View.Resolution")); //$NON-NLS-1$
            label.setPrefWidth(70);
            HBox.setHgrow(label, Priority.NEVER);

            Label by = new Label(Messages.getString("View.ResolutionBy")); //$NON-NLS-1$
            HBox.setHgrow(by, Priority.NEVER);

            width = new Spinner<>(640, Integer.MAX_VALUE, 1280, 1);
            width.setEditable(true);
            width.setPrefWidth(75);
            width.setMaxWidth(Double.MAX_VALUE);
            width.getValueFactory().setConverter(new ZeroIntegerStringConverter());

            height = new Spinner<>(480, Integer.MAX_VALUE, 720, 1);
            height.setEditable(true);
            height.setPrefWidth(75);
            height.setMaxWidth(Double.MAX_VALUE);
            height.getValueFactory().setConverter(new ZeroIntegerStringConverter());

            _resolution = new HBox(5, label, width, by, height);
            _resolution.setAlignment(Pos.CENTER_LEFT);

            String allowed = "0123456789"; //$NON-NLS-1$
            restrictInput(width, allowed);
            restrictInput(height, allowed);

            Pattern widthPattern = Pattern.compile("^.*(-w|-width) [0-9]+.*$"); //$NON-NLS-1$
            Pattern heightPattern = Pattern.compile("^.*(-h|-height) [0-9]+.*$"); //$NON-NLS-1$
            patternBinding(widthPattern, width.disableProperty(), advLaunchProperty);
            patternBinding(heightPattern, height.disableProperty(), advLaunchProperty);

            Validator<String> widthVal =
                    Validator.createEmptyValidator(Messages.getString("View.WidthInvalidEmpty")); //$NON-NLS-1$
            validation.registerValidator(width.getEditor(), false, widthVal);
            Validator<String> heightVal =
                    Validator.createEmptyValidator(Messages.getString("View.HeightInvalidEmpty")); //$NON-NLS-1$
            validation.registerValidator(height.getEditor(), false, heightVal);
        }
        return _resolution;
    }

    public HBox getDxlevelBox() {
        if (_dxlevel == null) {
            Label label = new Label(Messages.getString("View.DxLevel")); //$NON-NLS-1$
            label.setPrefWidth(70);
            dxlevel = new ComboBox<>(FXCollections.observableArrayList(TF2Plugin.DXLEVELS));
            _dxlevel = new HBox(5, label, dxlevel);
            dxlevel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(dxlevel, Priority.ALWAYS);
            _dxlevel.setAlignment(Pos.CENTER_LEFT);

            Pattern dxlevelPattern = Pattern.compile("^.*-dxlevel (80|81|90|95|98).*$"); //$NON-NLS-1$
            patternBinding(dxlevelPattern, dxlevel.disableProperty(), advLaunchProperty);
        }
        return _dxlevel;
    }

    public HBox getAdvancedBox() {
        if (_advanced == null) {
            advanced = new Button(Messages.getString("View.AdvancedBtn")); //$NON-NLS-1$
            advanced.setOnAction(evt -> getLaunchOptionsDialog().showAndWait().ifPresent(
                    advLaunchProperty::set));
            _advanced = new HBox(5, advanced);
            VBox.setVgrow(_advanced, Priority.ALWAYS);
            _advanced.setAlignment(Pos.BOTTOM_RIGHT);
        }
        return _advanced;
    }

    public TextInputDialog getLaunchOptionsDialog() {
        if (_dialog == null) {
            _dialog = new TextInputDialog(advLaunchProperty.get());
            _dialog.setTitle(Messages.getString("View.AdvancedLaunchTitle")); //$NON-NLS-1$
            _dialog.setHeaderText(Messages.getString("View.AdvancedLaunchHeader")); //$NON-NLS-1$
            _dialog.setContentText(Messages.getString("View.AdvancedLaunchContent")); //$NON-NLS-1$
        }
        return _dialog;
    }

    public HBox getRecordOptionsBox() {
        if (_record == null) {
            captureMode = new ComboBox<>(FXCollections.observableArrayList(TF2Plugin.CAPTURES));
            captureMode.setMaxWidth(Double.MAX_VALUE);
            Label at = new Label(Messages.getString("View.FramerateAt")); //$NON-NLS-1$
            fps = new Spinner<>(24, Integer.MAX_VALUE, 120);
            fps.setEditable(true);
            fps.setPrefWidth(70);
            restrictInput(fps, "0123456789"); //$NON-NLS-1$
            fps.getValueFactory().setConverter(new ZeroIntegerStringConverter());
            Label fpsLabel = new Label(Messages.getString("View.FPS")); //$NON-NLS-1$
            _record = new HBox(5, captureMode, at, fps, fpsLabel);
            _record.setAlignment(Pos.CENTER_LEFT);

            Label tgaInfo = new Label(Messages.getString("View.CaptureTGA")); //$NON-NLS-1$
            tgaInfo.setWrapText(true);
            tgaInfo.setAlignment(Pos.TOP_LEFT);
            tgaInfo.setMaxHeight(Double.MAX_VALUE);

            Label jpgInfo = new Label(Messages.getString("View.CaptureJPEG")); //$NON-NLS-1$
            jpgInfo.setWrapText(true);
            jpgInfo.setAlignment(Pos.TOP_LEFT);
            jpgInfo.setMaxHeight(Double.MAX_VALUE);
            Label qualityLabel = new Label(Messages.getString("View.JPEGQuality")); //$NON-NLS-1$
            quality = new Spinner<>(1, 100, 90);
            quality.setEditable(true);
            quality.setPrefWidth(70);
            restrictInput(quality, "0123456789"); //$NON-NLS-1$
            quality.getValueFactory().setConverter(new ZeroIntegerStringConverter());
            HBox jpgHBox = new HBox(5, qualityLabel, quality);
            jpgHBox.setAlignment(Pos.CENTER_RIGHT);

            Label manInfo = new Label(Messages.getString("View.CaptureSrcDemoManaged")); //$NON-NLS-1$
            manInfo.setWrapText(true);
            manInfo.setAlignment(Pos.TOP_LEFT);
            manInfo.setMaxHeight(Double.MAX_VALUE);
            cfgSrcDemo = new Button(Messages.getString("View.ConfigureSrcDemo")); //$NON-NLS-1$
            cfgSrcDemo.setOnAction((evt) -> showAlert());
            HBox srcHBox = new HBox(5, cfgSrcDemo);
            srcHBox.setAlignment(Pos.CENTER_RIGHT);

            Label stdInfo = new Label(Messages.getString("View.CaptureSrcDemoStandalone")); //$NON-NLS-1$
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

            infoCards.put(TF2Plugin.CAPTURES.get(0), tgaVBox);
            infoCards.put(TF2Plugin.CAPTURES.get(1), jpgVBox);
            infoCards.put(TF2Plugin.CAPTURES.get(2), manVBox);
            infoCards.put(TF2Plugin.CAPTURES.get(3), stdVBox);

            Validator<String> fpsVal =
                    Validator.createEmptyValidator(Messages.getString("View.FpsInvalidEmpty")); //$NON-NLS-1$
            validation.registerValidator(fps.getEditor(), false, fpsVal);
            Validator<String> qualityVal =
                    Validator.createEmptyValidator(Messages.getString("View.QualityInvalidEmpty")); //$NON-NLS-1$
            validation.registerValidator(quality.getEditor(), false, qualityVal);
        }
        return _record;
    }

    public Pane getRecordInfoBox() {
        if (_recInfo == null) {
            _recInfo = new VBox();
            VBox.setVgrow(_recInfo, Priority.ALWAYS);
        }
        return _recInfo;
    }

    public HBox getHudBox() {
        if (_hud == null) {
            Label hudLabel = new Label(Messages.getString("View.HUD")); //$NON-NLS-1$
            hudLabel.setPrefWidth(70);

            hud = new ComboBox<>(FXCollections.observableArrayList(TF2Plugin.HUDS));
            hud.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(hud, Priority.ALWAYS);

            _hud = new HBox(5, hudLabel, hud);
            _hud.setAlignment(Pos.CENTER_LEFT);
        }
        return _hud;
    }

    public HBox getSkyboxBox() {
        if (_skybox == null) {
            Label skyLabel = new Label(Messages.getString("View.Skybox")); //$NON-NLS-1$
            skyLabel.setPrefWidth(70);
            HBox.setHgrow(skyLabel, Priority.NEVER);
            skybox = new ComboBox<>();
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
            skybox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(skybox, Priority.ALWAYS);

            _skybox = new HBox(5, skyLabel, skybox);
            _skybox.setAlignment(Pos.CENTER_LEFT);
        }
        return _skybox;
    }

    public HBox getViewmodelsBox() {
        if (_viewmodels == null) {
            Label vmodels = new Label(Messages.getString("View.Viewmodels")); //$NON-NLS-1$
            vmodels.setPrefWidth(70);
            HBox.setHgrow(vmodels, Priority.NEVER);

            vmSwitch = new ComboBox<>(FXCollections.observableArrayList(TF2Plugin.VIEWMODELS));
            vmSwitch.setTooltip(new Tooltip(Messages.getString("View.ViewmodelsTooltip"))); //$NON-NLS-1$
            vmSwitch.setMaxWidth(Double.MAX_VALUE);

            Label with = new Label(Messages.getString("View.ViewmodelFovWith")); //$NON-NLS-1$
            HBox.setHgrow(with, Priority.NEVER);

            vmFov = new Spinner<>(0.1, 179.899994, 75, 1.0);
            vmFov.setEditable(true);
            vmFov.setPrefWidth(70);
            vmFov.setMaxWidth(Double.MAX_VALUE);
            vmFov.getValueFactory().setConverter(new ZeroDoubleStringConverter());
            vmFov.setTooltip(new Tooltip(Messages.getString("View.ViewmodelFovTooltip"))); //$NON-NLS-1$
            restrictInput(vmFov, ".0123456789"); //$NON-NLS-1$
            vmFov.disableProperty().bind(vmSwitch.valueProperty().isEqualTo(TF2Plugin.VIEWMODELS.get(1)));
            Validator<String> vmFovVal =
                    Validator.createEmptyValidator(Messages.getString("View.VmFovInvalidEmpty")); //$NON-NLS-1$
            validation.registerValidator(vmFov.getEditor(), false, vmFovVal);

            Label vmFovLabel = new Label(Messages.getString("View.ViewmodelFov")); //$NON-NLS-1$
            HBox.setHgrow(vmFovLabel, Priority.NEVER);

            _viewmodels = new HBox(5, vmodels, vmSwitch, with, vmFov, vmFovLabel);
            _viewmodels.setAlignment(Pos.CENTER_LEFT);
        }
        return _viewmodels;
    }

    public HBox getGameConfigBox() {
        if (_config == null) {
            groupTable = new TableView<>();
            groupTable.setEditable(true);
            HBox.setHgrow(groupTable, Priority.ALWAYS);

            TableColumn<Group, Boolean> enabledCol = new TableColumn<>(""); //$NON-NLS-1$
            enabledCol.setCellValueFactory(new PropertyValueFactory<>("enabled")); //$NON-NLS-1$
            enabledCol.setCellFactory(CheckBoxTableCell.forTableColumn(enabledCol));
            enabledCol.setEditable(true);

            TableColumn<Group, String> nameCol =
                    new TableColumn<>(Messages.getString("View.GroupNameColumn")); //$NON-NLS-1$
            nameCol.setCellValueFactory(g -> new ReadOnlyObjectWrapper<>(local(g.getValue().getName())));
            nameCol.setEditable(false);

            groupTable.getColumns().add(enabledCol);
            groupTable.getColumns().add(nameCol);
            configureColumn(enabledCol, groupTable, (double) 1 / 11);
            configureColumn(nameCol, groupTable, (double) 10 / 11);
            _config = new HBox(5, groupTable);
        }
        return _config;
    }

    public HBox getResInfoBox() {
        if (_resInfo == null) {
            Label info = new Label(Messages.getString("View.ResourcesInfo")); //$NON-NLS-1$
            info.setWrapText(true);
            info.setAlignment(Pos.TOP_LEFT);
            _resInfo = new HBox(5, info);
        }
        return _resInfo;
    }

    public HBox getResourcesBox() {
        if (_resources == null) {
            resourcesTable = new TableView<>();
            resourcesTable.setPrefHeight(250);
            resourcesTable.setEditable(true);
            HBox.setHgrow(resourcesTable, Priority.ALWAYS);
            VBox.setVgrow(resourcesTable, Priority.ALWAYS);

            TableColumn<Resource, Boolean> enabledCol = new TableColumn<>(""); //$NON-NLS-1$
            enabledCol.setCellValueFactory(new PropertyValueFactory<>("enabled")); //$NON-NLS-1$
            enabledCol.setCellFactory(CheckBoxTableCell.forTableColumn(enabledCol));
            enabledCol.setEditable(true);

            TableColumn<Resource, String> nameCol =
                    new TableColumn<>(Messages.getString("View.ResourcesNameColumn")); //$NON-NLS-1$
            nameCol.setCellValueFactory(r -> new ReadOnlyObjectWrapper<>(local(r.getValue().getName())));
            nameCol.setEditable(false);

            TableColumn<Resource, String> tagsCol =
                    new TableColumn<>(Messages.getString("View.ResourcesTagsColumn")); //$NON-NLS-1$
            tagsCol.setCellValueFactory(r -> {
                Set<String> tags = r.getValue().getTags();
                String value = tags.toString().replace("[", "").replace("]", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                return new ReadOnlyObjectWrapper<>(value);
            });
            tagsCol.setEditable(false);

            resourcesTable.getColumns().add(enabledCol);
            resourcesTable.getColumns().add(nameCol);
            resourcesTable.getColumns().add(tagsCol);
            configureColumn(enabledCol, resourcesTable, (double) 1 / 11);
            configureColumn(nameCol, resourcesTable, (double) 7 / 11);
            configureColumn(tagsCol, resourcesTable, (double) 3 / 11);
            _resources = new HBox(5, resourcesTable);
        }
        return _resources;
    }

    public ObjectProperty<Integer> widthProperty() {
        return width.getValueFactory().valueProperty();
    }

    public ObjectProperty<Integer> heightProperty() {
        return height.getValueFactory().valueProperty();
    }

    public ObjectProperty<ExternalString> dxlevelProperty() {
        return dxlevel.valueProperty();
    }

    public StringProperty advLaunchProperty() {
        return advLaunchProperty;
    }

    public ObjectProperty<ExternalString> captureModeProperty() {
        return captureMode.valueProperty();
    }

    public ObjectProperty<Integer> fpsProperty() {
        return fps.getValueFactory().valueProperty();
    }

    public ObjectProperty<Integer> qualityProperty() {
        return quality.getValueFactory().valueProperty();
    }

    public ObjectProperty<ExternalString> hudProperty() {
        return hud.valueProperty();
    }

    public ObjectProperty<Skybox> skyboxProperty() {
        return skybox.valueProperty();
    }

//    public Pane getInfoCard(ExternalString key) {
//        Objects.requireNonNull(key);
//        return infoCards.get(key);
//    }

    public Optional<Pane> getInfoCard(ExternalString key) {
        Objects.requireNonNull(key);
        return Optional.ofNullable(infoCards.get(key));
    }

    public ObjectProperty<ObservableList<Skybox>> skyboxItemsProperty() {
        return skybox.itemsProperty();
    }

    public ObjectProperty<ExternalString> vmSwitchProperty() {
        return vmSwitch.valueProperty();
    }

    public ObjectProperty<Double> vmFovProperty() {
        return vmFov.getValueFactory().valueProperty();
    }

    public ObjectProperty<ObservableList<Group>> groupItemsProperty() {
        return groupTable.itemsProperty();
    }

    public ObjectProperty<ObservableList<Resource>> resourceItemsProperty() {
        return resourcesTable.itemsProperty();
    }

}
