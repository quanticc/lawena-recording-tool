package com.github.lawena.task;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.util.Duration;

public abstract class LawenaTask<T> extends Task<T> {

    public static Node getGenericImageView() {
        Label label = GlyphsDude.createIconLabel(FontAwesomeIcon.COG, "", "1.5em", null, ContentDisplay.LEFT);
        Node node = label.getGraphic();
        RotateTransition rt = new RotateTransition(Duration.millis(2000), node);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();
        return node;
    }

    public Group getGroup() {
        return Group.GENERIC;
    }

    public Node getGraphic() {
        return getGenericImageView();
    }

    public enum Group {
        GENERIC, RESOURCE, LAUNCH;
    }

}
