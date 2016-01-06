package com.github.lawena.views.dialog;

import com.github.lawena.Messages;
import com.github.lawena.views.dialog.data.CustomSettingsData;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Window;

public class CustomSettingsDialog extends Dialog<CustomSettingsData> {

    private TextArea textArea;

    public CustomSettingsDialog(Window owner) {
        setTitle(Messages.getString("ui.dialog.customSettings.title"));
        setHeaderText(Messages.getString("ui.dialog.customSettings.header"));

        setResizable(true);

        // Set the button types.
        ButtonType saveButtonType = new ButtonType(Messages.getString("ui.dialog.customSettings.ok"), ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        VBox container = new VBox(5);

        textArea = new TextArea();
        textArea.setFont(new Font("Courier New", 12));
        container.getChildren().add(textArea);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        getDialogPane().setContent(container);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                CustomSettingsData result = new CustomSettingsData();
                result.setContent(textArea.getText());
                result.setDimension(new Dimension2D(getWidth(), getHeight()));
                return result;
            }
            textArea.clear();
            return null;
        });
    }

    public CustomSettingsDialog withInputData(CustomSettingsData input) {
        textArea.clear();
        textArea.appendText(input.getContent());
        Dimension2D dimension = input.getDimension();
        setWidth(dimension.getWidth());
        setHeight(dimension.getHeight());
        return this;
    }
}
