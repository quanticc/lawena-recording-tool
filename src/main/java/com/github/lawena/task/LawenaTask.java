package com.github.lawena.task;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public abstract class LawenaTask<T> extends Task<T> {

    private static final Image gearIcon = new Image("/com/github/lawena/fugue/gear.png");

    public static ImageView getGenericImageView() {
        ImageView icon = new ImageView(gearIcon);
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
