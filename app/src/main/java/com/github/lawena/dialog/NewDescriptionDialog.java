package com.github.lawena.dialog;

import com.github.lawena.game.GameDescription;
import com.github.lawena.util.ZeroIntegerStringConverter;

import org.controlsfx.control.CheckListView;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Created by Ivan on 30-07-2015.
 */
public class NewDescriptionDialog extends Dialog<GameDescription> {

    private final List<String> modulesList;

    public NewDescriptionDialog(List<String> modulesList) {
        this.modulesList = modulesList;
        init();
    }

    private void init() {
        setTitle("Create New Source Game Launch Profile");
        setHeaderText("Configure a new launch profile for a Source game you want to launch the tool with");

        // Set the button types.
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Enter name");
        TextField shortName = new TextField();
        shortName.setPromptText("Enter short name");
        TextField processName = new TextField();
        processName.setPromptText("Enter process name");
        TextField folderName = new TextField();
        folderName.setPromptText("Enter folder name (not a file path)");
        TextField localPath = new TextField();
        localPath.setPromptText("Enter path (e.g. lwrt/tf)");

        Spinner<Integer> appId = new Spinner<>(0, Integer.MAX_VALUE, 440, 1);
        appId.setEditable(true);
        appId.setPrefWidth(75);
        appId.setMaxWidth(Double.MAX_VALUE);
        appId.getValueFactory().setConverter(new ZeroIntegerStringConverter());

        CheckListView<String> modules = new CheckListView<>(FXCollections.observableArrayList(modulesList));

        grid.add(new Label("Game Name"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Abbreviation"), 0, 1);
        grid.add(shortName, 1, 1);
        grid.add(new Label("Process"), 0, 2);
        grid.add(processName, 1, 2);
        grid.add(new Label("Folder Name"), 0, 2);
        grid.add(processName, 1, 2);

        // Enable/Disable login button depending on whether a username was entered.
        Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // TODO: configure validation
//        ValidationSupport validation = new ValidationSupport();
//        Validator<String> nameValidator =
//                Validator.combine(Validator.createEmptyValidator("Profile name must not be empty"),
//                        Validator.createPredicateValidator(n -> !profiles.containsByName(n),
//                                "Profile name must be unique"));
//        validation.registerValidator(name, false, nameValidator);
//        createButton.disableProperty().bind(validation.invalidProperty());

        getDialogPane().setContent(grid);

        // Request focus on the name field by default.
        Platform.runLater(name::requestFocus);

        // Convert the result to a game-name-pair when the create button is clicked.
//        setResultConverter(dialogButton -> {
//            if (dialogButton == createButtonType) {
//                return new Pair<>(apps.getValue(), name.getText());
//            }
//            return null;
//        });
    }

}
