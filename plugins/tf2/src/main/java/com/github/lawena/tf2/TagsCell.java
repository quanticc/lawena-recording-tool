package com.github.lawena.tf2;

import com.github.lawena.files.Resource;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

@SuppressWarnings("HardCodedStringLiteral")
public class TagsCell extends TableCell<Resource, ObservableSet<String>> {

    private static final Map<String, Color> colors = new HashMap<>();

    private static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private static Color contrast(Color color) {
        double gamma = 2.2;
        double l = 0.2126 * Math.pow(color.getRed(), gamma)
                + 0.7152 * Math.pow(color.getGreen(), gamma)
                + 0.0722 * Math.pow(color.getBlue(), gamma);
        return l > 0.5 ? Color.BLACK : Color.WHITE;
    }

    static {
        // #e11d21 - red
        // #0052cc - blue
        // #5319e7 - purple
        // #009800 - green
        // #9d5321 - tf2
        // #fbca04 - csgo
        colors.put("hud", Color.web("#5319e7"));
        colors.put("config", Color.web("#0052cc"));
        colors.put("skybox", Color.web("#c7def8"));
        colors.put("vpk", Color.web("#cccccc"));
    }

    private final HBox box = new HBox(3);

    @Override
    protected void updateItem(ObservableSet<String> item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            box.getChildren().clear();
            for (String tag : item) {
                Label label = new Label(tag);
                Color color = colors.getOrDefault(tag, Color.web("#cccccc"));
                label.setPadding(new Insets(0, 3, 0, 3));
                label.setStyle("-fx-text-fill: " + toRGBCode(contrast(color))
                        + "; -fx-background-color: " + toRGBCode(color)
                        + "; -fx-border-radius: 3; -fx-background-radius: 3;");
                box.getChildren().add(label);
            }
            setGraphic(box);
            setText("");
        }
    }

}
