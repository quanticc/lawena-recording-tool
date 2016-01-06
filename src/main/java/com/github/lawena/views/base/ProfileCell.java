package com.github.lawena.views.base;

import com.github.lawena.domain.Launcher;
import com.github.lawena.domain.Profile;
import com.github.lawena.repository.ImageRepository;
import com.github.lawena.service.Profiles;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.github.lawena.util.FXUtils.asyncToFxThread;

public class ProfileCell extends ListCell<Profile> {

    private final Profiles profiles;
    private final ImageRepository images;

    @Autowired
    public ProfileCell(Profiles profiles, ImageRepository images) {
        this.profiles = profiles;
        this.images = images;
    }

    @Override
    protected void updateItem(Profile item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setGraphic(null);
            setText(null);
        } else {
            Optional<Launcher> o = profiles.getLauncher(item);
            if (o.isPresent()) {
                asyncToFxThread(() -> images.image(o.get().getIcon()), image -> setGraphic(fitImageView(image)));
                setText(item.getName());
            }
        }
    }

    private ImageView fitImageView(Image image) {
        ImageView icon = new ImageView(image);
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        return icon;
    }
}
