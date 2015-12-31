package com.github.lawena.views;

import com.github.lawena.Messages;
import com.github.lawena.domain.Resource;
import com.github.lawena.domain.Tag;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.stream.Collectors;

public class TagsCell extends TableCell<Resource, ObservableSet<Tag>> {

    private final HBox box = new HBox(3);

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

    @Override
    protected void updateItem(ObservableSet<Tag> item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            box.getChildren().clear();
            for (Tag tag : item) {
                Label label = new Label(tag.getName());
                Color color = tag.getCellColor();
                label.setPadding(new Insets(0, 3, 0, 3));
                label.setStyle("-fx-text-fill: " + toRGBCode(contrast(color))
                        + "; -fx-background-color: " + toRGBCode(color)
                        + "; -fx-border-radius: 3; -fx-background-radius: 3;");
                box.getChildren().add(label);
            }
            setGraphic(box);
            setText("");
            String tooltip = Messages.getString("ui.tf2.resources.tagsTooltip",
                    item.stream().map(t -> t.getName() + ": " + t.getDescription()).collect(Collectors.joining("\n")));
            setTooltip(new Tooltip(tooltip));
        }
    }

}
