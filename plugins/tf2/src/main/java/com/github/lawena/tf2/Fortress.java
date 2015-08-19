package com.github.lawena.tf2;

import com.github.lawena.Controller;
import com.github.lawena.exts.ViewProvider;
import com.github.lawena.files.Resource;
import com.github.lawena.game.GameDescription;
import com.github.lawena.game.Group;
import com.github.lawena.profile.Profile;
import com.github.lawena.tf2.skybox.PreviewTask;
import com.github.lawena.tf2.skybox.Skybox;
import com.github.lawena.tf2.skybox.SkyboxStore;

import org.controlsfx.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.util.Pair;
import ro.fortsoft.pf4j.Extension;

import static com.github.lawena.util.BindUtils.load;
import static com.github.lawena.util.BindUtils.save;

@Extension
public class Fortress implements ViewProvider {
    private static final Logger log = LoggerFactory.getLogger(Fortress.class);

    private Controller controller;
    private TF2Plugin plugin;
    private GameDescription app;
    private View view;
    private boolean installed = false;
    private InvalidationListener reloader = o -> refreshResourceSelection(controller.getModel().getProfiles().getSelected());

    private static List<String> split(String str) {
        return Arrays.asList(str.split(";")).stream().map(String::trim).collect(Collectors.toList()); //NON-NLS
    }

    private static String join(List<String> list) {
        return list.toString().replace(",", ";").replaceAll("\\[|\\]", "");
    }

    @Override
    public String getName() {
        return "tf2-view"; //NON-NLS
    }

    @Override
    public void install(Controller parent) {
        if (installed) {
            return;
        }
        log.debug("Installing {}", getName());
        this.controller = parent;
        this.app = controller.getGameFromProfile();
        this.plugin = (TF2Plugin) controller.getModel().getPluginManager().getPlugin("TF2Plugin").getPlugin();
        this.view = new View();

        // configure node groups that will be displayed on various UI locations
        controller.getNodeGroups().getNodes(this, "launch") //NON-NLS
                .addAll(Arrays.asList(view.getResolutionBox(), view.getDxlevelBox(), view.getAdvancedBox()));
        controller.getNodeGroups().getNodes(this, "recorder") //NON-NLS
                .addAll(Arrays.asList(view.getRecordOptionsBox(), view.getRecordOutputBox(), view.getRecordInfoBox()));
        controller.getNodeGroups().getNodes(this, "config") //NON-NLS
                .addAll(Arrays.asList(view.getHudBox(), view.getSkyboxBox(), view.getViewmodelsBox(), view.getGameConfigBox()));
        controller.getNodeGroups().getNodes(this, "resources") //NON-NLS
                .addAll(Arrays.asList(view.getResInfoBox(), view.getResourcesBox()));

        // setup context menu
        view.getResourcesTable().setRowFactory(
                tableView -> {
                    final TableRow<Resource> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    MenuItem refreshItem = new MenuItem("Refresh");
                    refreshItem.setOnAction(e -> refreshSelectedResource());
                    MenuItem removeItem = new MenuItem("Remove");
                    removeItem.setOnAction(e -> removeSelectedResource());
                    rowMenu.getItems().addAll(refreshItem, removeItem);

                    // only display context menu for non-null items:
                    row.contextMenuProperty().bind(
                            Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                    .then(rowMenu)
                                    .otherwise((ContextMenu) null));
                    return row;
                });

        // setup recording method (captureMode) combobox changes
        view.captureModeProperty().addListener((obs, oldValue, newValue) -> {
            if (oldValue != null) {
                view.getInfoCard(oldValue).ifPresent(c -> view.getRecordInfoBox().getChildren().remove(c));
            }
            view.getInfoCard(newValue).ifPresent(c -> view.getRecordInfoBox().getChildren().add(c));
        });

        view.getValidation().initInitialDecoration();

        // setup skybox repository
        List<Skybox> toAdd = new ArrayList<>();
        Path dir = Paths.get("lwrt/tf2/skybox/vtf");
        if (Files.exists(dir)) {
            log.debug("Loading skybox folder");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
                stream.forEach(p -> toAdd.add(new Skybox(p)));
            } catch (IOException e) {
                log.warn("Problem while loading skybox folder", e);
            }
        }
        SkyboxStore skyboxes = plugin.getSkyboxes();
        skyboxes.add(Skybox.DEFAULT);
        Path cache = Paths.get("lwrt/tf2/skybox.ser");
        skyboxes.load(cache);
        toAdd.removeIf(s -> skyboxes.getSkybox(s.getName()).isPresent());
        Path vtf = Paths.get("lwrt/tools/vtfcmd/VTFCmd.exe").toAbsolutePath();
        PreviewTask task = new PreviewTask(vtf, toAdd);
        if (!toAdd.isEmpty()) {
            skyboxes.getItems().addAll(toAdd);
            controller.getTasks().add(task);
            controller.getTasks().add(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateTitle(Messages.getString("TF2Plugin.PreviewTaskTitle"));
                    updateMessage(Messages.getString("TF2Plugin.PreviewTaskMessage"));
                    try {
                        log.debug("Waiting for generator to complete");
                        ObservableList<Skybox> list = task.get();
                        list.add(0, Skybox.DEFAULT);
                        Platform.runLater(() -> {
                            skyboxes.setItems(list);
                            skyboxes.save(cache);
                        });
                    } catch (InterruptedException | ExecutionException e) {
                        log.warn("Could not update skybox list", e);
                    }
                    return null;
                }
            });
        }
        view.skyboxItemsProperty().bind(skyboxes.itemsProperty());

        // setup command groups
        Properties config = plugin.getConfig();
        GameDescription tf = controller.getModel().getGames().get(440);
        Bindings.bindContentBidirectional(view.groupItemsProperty().get(), config.groupItemsProperty()
                .get());
        config.getGroupItems().addAll(tf.getGroups());

        Bindings.bindContentBidirectional(view.resourceItemsProperty().get(), controller.getModel()
                .getResources().resourceListProperty().get());

        installed = true;
    }

    private void refreshSelectedResource() {
        log.debug("Refreshing selected resource");
        final ObservableList<TablePosition> posList = view.getResourcesTable().getSelectionModel().getSelectedCells();
        posList.forEach(p -> {
            Resource resource = view.getResourcesTable().getItems().get(p.getRow());
            controller.getModel().getResources().refreshResource(resource.getPath());
        });
    }

    private void removeSelectedResource() {
        log.debug("Removing selected resource");
        final ObservableList<TablePosition> posList = view.getResourcesTable().getSelectionModel().getSelectedCells();
        posList.forEach(p -> {
            Resource resource = view.getResourcesTable().getItems().get(p.getRow());
            controller.getModel().getResources().deleteResource(resource.getPath());
        });
    }

    @Override
    public void remove(Controller parent) {
        // TODO: perform cleanup
    }

    @Override
    public void bind(Profile profile) {
        log.debug("Loading profile: {}", profile.getName());
        Properties config = plugin.getConfig();
        load(profile, config.widthProperty(), view.widthProperty(), Integer::parseInt);
        load(profile, config.heightProperty(), view.heightProperty(), Integer::parseInt);
        load(profile, config.dxlevelProperty(), view.dxlevelProperty(), plugin::dxlevel);
        load(profile, config.launchOptionsProperty(), view.advLaunchProperty(), Function.identity());
        load(profile, config.captureModeProperty(), view.captureModeProperty(), plugin::capture);
        load(profile, config.fpsProperty(), view.fpsProperty(), Integer::parseInt);
        load(profile, config.qualityProperty(), view.qualityProperty(), Integer::parseInt);
        load(profile, config.hudProperty(), view.hudProperty(), plugin::hud);
        load(profile, config.skyboxProperty(), view.skyboxProperty(), plugin::getSkybox);
        load(profile, config.viewmodelSwitchProperty(), view.vmSwitchProperty(), plugin::vmodel);
        load(profile, config.viewmodelFovProperty(), view.vmFovProperty(), Double::parseDouble);
        load(profile, config.framesPathProperty(), view.framesPathProperty(), Function.identity());
        loadGroup(profile, config.motionBlurProperty());
        loadGroup(profile, config.noDamageNumbersProperty());
        loadGroup(profile, config.noCrosshairSwitchProperty());
        loadGroup(profile, config.noHitsoundsProperty());
        loadGroup(profile, config.noVoiceProperty());
        loadGroup(profile, config.hudMinmodeProperty());
        loadGroup(profile, config.hudPlayerModelProperty());

        controller.getModel().getResources().getResourceList().addListener(reloader);
        refreshResourceSelection(profile);
    }

    // UTILITIES

    @Override
    public void unbind(Profile profile) {
        log.debug("Saving profile: {}", profile.getName());
        Properties config = plugin.getConfig();
        save(profile, config.widthProperty(), view.widthProperty());
        save(profile, config.heightProperty(), view.heightProperty());
        save(profile, config.dxlevelProperty(), view.dxlevelProperty(), Pair::getKey);
        save(profile, config.launchOptionsProperty(), view.advLaunchProperty());
        save(profile, config.captureModeProperty(), view.captureModeProperty(), Pair::getKey);
        save(profile, config.fpsProperty(), view.fpsProperty());
        save(profile, config.qualityProperty(), view.qualityProperty());
        save(profile, config.hudProperty(), view.hudProperty(), Pair::getKey);
        save(profile, config.skyboxProperty(), view.skyboxProperty());
        save(profile, config.viewmodelSwitchProperty(), view.vmSwitchProperty(), Pair::getKey);
        save(profile, config.viewmodelFovProperty(), view.vmFovProperty());
        save(profile, config.framesPathProperty(), view.framesPathProperty());
        saveGroup(profile, config.motionBlurProperty());
        saveGroup(profile, config.noDamageNumbersProperty());
        saveGroup(profile, config.noCrosshairSwitchProperty());
        saveGroup(profile, config.noHitsoundsProperty());
        saveGroup(profile, config.noVoiceProperty());
        saveGroup(profile, config.hudMinmodeProperty());
        saveGroup(profile, config.hudPlayerModelProperty());

        profile.set(
                Properties.RESOURCES_KEY,
                join(controller.getModel().getResources().getResourceList().stream().filter(Resource::isEnabled)
                        .map(Resource::getName).collect(Collectors.toList())));
        controller.getModel().getResources().getResourceList().removeListener(reloader);
    }

    @Override
    public ValidationResult validate(Profile profile) {
        ValidationResult result = view.getValidation().getValidationResult();
        result.getWarnings().forEach(w -> log.warn("Validation {}: {} @ {}", w.getSeverity(), w.getText(), w.getTarget()));
        result.getErrors().forEach(e -> log.warn("Validation {}: {} @ {}", e.getSeverity(), e.getText(), e.getTarget()));
        return result;
    }

    private void refreshResourceSelection(Profile profile) {
        List<String> selected =
                split(profile.get(Properties.RESOURCES_KEY).orElse(join(Properties.DEFAULT_RESOURCES)));
        controller.getModel().getResources().getResourceList().forEach(r -> r.setEnabled(selected.contains(r.getName())));
    }

    private void loadGroup(Profile profile, Property<Boolean> model) {
        try {
            Group group = app.getGroup(model).get();
            load(profile, model, group.enabledProperty(), Boolean::parseBoolean);
        } catch (NoSuchElementException e) {
            log.warn("No group found with key '{}'", model.getName());
        }
    }

    private void saveGroup(Profile profile, Property<Boolean> model) {
        try {
            Group group = app.getGroup(model).get();
            save(profile, model, group.enabledProperty());
        } catch (NoSuchElementException e) {
            log.warn("No group found with key '{}'", model.getName());
        }
    }

}
