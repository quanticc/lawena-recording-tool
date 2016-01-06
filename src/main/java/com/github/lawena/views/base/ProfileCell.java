package com.github.lawena.views.base;

import com.github.lawena.domain.Launcher;
import com.github.lawena.domain.Profile;
import com.github.lawena.service.Profiles;
import com.github.lawena.util.LwrtUtils;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ProfileCell extends ListCell<Profile> {

    private static final Logger log = LoggerFactory.getLogger(ProfileCell.class);

    private final Profiles profiles;

    public ProfileCell(Profiles profiles) {
        this.profiles = profiles;
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
                setGraphic(getIcon(o.get().getIcon()));
                setText(item.getName());
            }
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
