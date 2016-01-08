package com.github.lawena.views.launchers;

import javafx.scene.control.Label;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class FxScopeValueCell extends TextFieldTableCell<FxScope, String> {

    private final Label label = new Label();

    public FxScopeValueCell() {
        super(new DefaultStringConverter());
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setGraphic(null);
            setText(null);
        } else {
            refreshLabel(item);
            setGraphic(label);
            setText("");
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        String item = this.getItem();
        refreshLabel(item);
        setGraphic(label);
        setText("");
    }

    private void refreshLabel(String value) {
        FxScope scope = (FxScope) getTableRow().getItem();
        label.setText(value);
        if (scope != null && scope.getKey() != null && scope.getKey().startsWith("key.")) {
            label.setStyle("-fx-padding: 0 3 0 3"
                    + "; -fx-font-size: 90%"
                    + "; -fx-text-fill: #ffffff"
                    + "; -fx-background-color: #333333"
                    + "; -fx-background-radius: 3"
                    + "; -fx-border-radius: 3;");
        } else {
            label.setStyle("");
        }
    }
}
