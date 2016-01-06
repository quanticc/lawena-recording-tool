package com.github.lawena.views.launchers;

import com.github.lawena.repository.ImageRepository;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.lawena.util.FXUtils.asyncToFxThread;

public class FxLauncherCell extends ListCell<FxLauncher> {

    private final ImageRepository images;

    @Autowired
    public FxLauncherCell(ImageRepository images) {
        this.images = images;
    }

    @Override
    protected void updateItem(FxLauncher item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            asyncToFxThread(() -> images.image(item.iconProperty().get()), image -> setGraphic(fitImageView(image)));
            setText(item.nameProperty().get());
        }
    }

    private ImageView fitImageView(Image image) {
        ImageView icon = new ImageView(image);
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        return icon;
    }
}
