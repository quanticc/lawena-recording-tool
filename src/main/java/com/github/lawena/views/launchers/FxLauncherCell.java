package com.github.lawena.views.launchers;

import com.github.lawena.util.LwrtUtils;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

public class FxLauncherCell extends ListCell<FxLauncher> {

    @Override
    protected void updateItem(FxLauncher item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(getIcon(item.iconProperty().get()));
            setText(item.nameProperty().get());
        }
    }

    private ImageView getIcon(String location) {
        ImageView icon = null;
        if (!LwrtUtils.isNullOrEmpty(location)) {
            icon = new ImageView(LwrtUtils.image(location));
            icon.setFitWidth(16);
            icon.setFitHeight(16);
        }
        return icon;
    }
}
