package com.github.lawena.task;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;

public abstract class LawenaTask<T> extends Task<T> {

    public static Node getGenericImageView() {
        return GlyphsDude.createIconLabel(FontAwesomeIcon.COG, "", "1.5em", null, ContentDisplay.LEFT);
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
