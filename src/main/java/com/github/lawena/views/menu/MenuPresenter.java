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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MenuPresenter {

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
    private void configureLaunchers(ActionEvent event) {
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
    private void checkForUpdates(ActionEvent event) {
        properties.setLastSkippedVersion(0);
        updatesPresenter.clearCache();
        updatesPresenter.checkForUpdates();
    }

    @FXML
    private void about(ActionEvent event) {
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
}
