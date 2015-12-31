package com.github.lawena.task;

import com.github.lawena.util.LwrtUtils;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public abstract class LawenaTask<T> extends Task<T> {

    public static ImageView getGenericImageView() {
        ImageView icon = new ImageView(LwrtUtils.localImage("/fugue/gear.png"));
        RotateTransition rt = new RotateTransition(Duration.millis(1000), icon);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();
        return icon;
    }

    public Group getGroup() {
        return Group.GENERIC;
    }

    public ImageView getImageView() {
        return getGenericImageView();
    }

    public enum Group {
        GENERIC, RESOURCE, LAUNCH;
    }

}
