package com.github.lawena.views.menu;

import com.github.lawena.Messages;
import com.github.lawena.config.LawenaProperties;
import com.github.lawena.service.fx.LaunchService;
import com.github.lawena.views.about.AboutView;
import com.github.lawena.views.launchers.LaunchersPresenter;
import com.github.lawena.views.launchers.LaunchersView;
import com.github.lawena.views.updates.UpdatesPresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Component
public class MenuPresenter {

    private static final Logger log = LoggerFactory.getLogger(MenuPresenter.class);

    @Autowired
    private LawenaProperties properties;
    @Autowired
    private LaunchersView launchersView;
    @Autowired
    private LaunchService launchService;
    @Autowired
    private AboutView aboutView;
    @Autowired
    private UpdatesPresenter updatesPresenter;

    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem configureLaunchers;

    @FXML
    private void initialize() {
        configureLaunchers.disableProperty().bind(launchService.runningProperty());
    }

    @FXML
    public void configureLaunchers(ActionEvent event) {
        Parent view = launchersView.getView();
        LaunchersPresenter presenter = (LaunchersPresenter) launchersView.getPresenter();
        presenter.load();
        boolean done = false;
        while (!done) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(Messages.getString("ui.launchers.title"));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(view);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.APPLY) {
                done = presenter.isValid(); // invalid result will enter loop again
                if (done) {
                    presenter.save();
                }
            } else {
                done = true;
            }
        }
        presenter.clear();
    }

    @FXML
    public void checkForUpdates(ActionEvent event) {
        properties.setLastSkippedVersion(0);
        updatesPresenter.clearCache();
        updatesPresenter.checkForUpdates(true);
    }

    @FXML
    public void about(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(Messages.getString("ui.about.title"));
        dialog.setDialogPane((DialogPane) aboutView.getView());
        dialog.showAndWait();
    }

    @FXML
    private void exit(ActionEvent event) {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    public void importSettings(ActionEvent event) {

    }

    public Map<String, String> getImportedSettings() {
        Map<String, String> map = null;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("(Experimental) Select a file to import settings from");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Lawena v4.1 settings file", "lwf"));
        File selected = chooser.showOpenDialog(menuBar.getScene().getWindow());
        if (selected != null) {
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(selected.toPath())) {
                map = new LinkedHashMap<>();
                properties.load(input);
                // Lawena v4.1 only supports TF2
                // TODO: import more settings
                String importedGamePath = properties.getProperty("TfDir");
                if (importedGamePath != null && !importedGamePath.isEmpty()) {
                    map.put("gamePath", importedGamePath);
                }
            } catch (IOException e) {
                log.warn("Could not read from properties file: {}", e.toString());
            }
        }
        return map;
    }
}
