package com.github.lawena.views.dialog;

import com.github.lawena.Messages;
import com.github.lawena.domain.Launcher;
import com.github.lawena.service.Profiles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class NewProfileDialog extends Dialog<Pair<Launcher, String>> {

    private final Profiles profiles;

    @Autowired
    public NewProfileDialog(Profiles profiles) {
        this.profiles = profiles;
    }

    @PostConstruct
    private void configure() {
        setTitle(Messages.getString("ui.base.newProfile.title"));
        setHeaderText(Messages.getString("ui.base.newProfile.header"));

        // Set the button types.
        ButtonType createButtonType = new ButtonType(Messages.getString("ui.base.newProfile.ok"), ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Launcher> apps = new ComboBox<>(profiles.getLaunchers());
        apps.getSelectionModel().selectFirst();
        TextField name = new TextField();
        name.setPromptText(Messages.getString("ui.base.newProfile.namePrompt"));

        grid.add(new Label(Messages.getString("ui.base.newProfile.launcher")), 0, 0);
        grid.add(apps, 1, 0);
        grid.add(new Label(Messages.getString("ui.base.newProfile.name")), 0, 1);
        grid.add(name, 1, 1);

        Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        ValidationSupport validation = new ValidationSupport();
        Validator<String> nameValidator =
                Validator.combine(Validator.createEmptyValidator(Messages.getString("ui.base.newProfile.nameEmpty")),
                        Validator.createPredicateValidator(n -> !profiles.containsByName(n),
                                Messages.getString("ui.base.nameProfile.nameNotUnique")));
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
