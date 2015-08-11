package com.github.lawena.dialog;

import com.github.lawena.game.GameDescription;
import com.github.lawena.profile.Profiles;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.util.Collection;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class NewProfileDialog extends Dialog<Pair<GameDescription, String>> {

    private final Profiles profiles;
    private final Collection<GameDescription> appCollection;

    public NewProfileDialog(Profiles profiles, Collection<GameDescription> appCollection) {
        this.profiles = profiles;
        this.appCollection = appCollection;
        init();
    }

    private void init() {
        setTitle("Create New Profile");
        setHeaderText("Select the game of the profile and a name");

        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<GameDescription> apps = new ComboBox<>(FXCollections.observableArrayList(appCollection));
        apps.getSelectionModel().selectFirst();
        TextField name = new TextField();
        name.setPromptText("Profile name");

        grid.add(new Label("Game"), 0, 0);
        grid.add(apps, 1, 0);
        grid.add(new Label("Profile Name"), 0, 1);
        grid.add(name, 1, 1);

        Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        ValidationSupport validation = new ValidationSupport();
        Validator<String> nameValidator =
                Validator.combine(Validator.createEmptyValidator("Profile name must not be empty"),
                        Validator.createPredicateValidator(n -> !profiles.containsByName(n),
                                "Profile name must be unique"));
        validation.registerValidator(name, false, nameValidator);
        createButton.disableProperty().bind(validation.invalidProperty());

        getDialogPane().setContent(grid);

        // Request focus on the name field by default.
        Platform.runLater(name::requestFocus);

        // Convert the result to a game-name-pair when the create button is clicked.
        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(apps.getValue(), name.getText());
            }
            return null;
        });
    }
}
