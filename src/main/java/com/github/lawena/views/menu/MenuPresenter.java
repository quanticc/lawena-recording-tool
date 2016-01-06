package com.github.lawena.views.menu;

import com.github.lawena.Messages;
import com.github.lawena.service.fx.LaunchService;
import com.github.lawena.views.launchers.LaunchersPresenter;
import com.github.lawena.views.launchers.LaunchersView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MenuPresenter {

    @Autowired
    private LaunchersView launchersView;
    @Autowired
    private LaunchService launchService;

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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(Messages.getString("ui.launchers.title"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(launchersView.getView());
        LaunchersPresenter presenter = (LaunchersPresenter) launchersView.getPresenter();
        presenter.load();
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            ButtonType buttonType = result.get();
            if (buttonType == ButtonType.APPLY) {
                presenter.save();
            }
        }
        presenter.clear();
    }

    @FXML
    private void exit(ActionEvent event) {
        Stage stage = (Stage) menuBar.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
