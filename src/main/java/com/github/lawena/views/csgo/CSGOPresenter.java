package com.github.lawena.views.csgo;

import com.github.lawena.domain.Profile;
import com.github.lawena.views.GamePresenter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.controlsfx.validation.ValidationResult;
import org.springframework.stereotype.Component;

@Component
public class CSGOPresenter implements GamePresenter {

    @FXML
    private Label testLabel;

    @Override
    public void bind(Profile profile) {
        testLabel.setText("CS:GO !!");
    }

    @Override
    public void unbind(Profile profile) {

    }

    @Override
    public ValidationResult validate(Profile profile) {
        return null;
    }
}
