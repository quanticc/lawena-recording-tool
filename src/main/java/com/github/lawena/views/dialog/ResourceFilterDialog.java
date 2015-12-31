package com.github.lawena.views.dialog;

import com.github.lawena.Messages;
import com.github.lawena.util.FXUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.stream.Collectors;

public class ResourceFilterDialog extends Dialog<List<String>> {

    private final TableView<Content> excludeTable;

    public ResourceFilterDialog(Window owner) {
        setTitle(Messages.getString("ui.dialog.resourceFilter.title"));
        setHeaderText(Messages.getString("ui.dialog.resourceFilter.header"));
        initOwner(owner);
        setResizable(true);
        setWidth(600);
        setHeight(400);

        // Set the button types.
        ButtonType saveButtonType = new ButtonType(Messages.getString("ui.dialog.resourceFilter.ok"), ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        VBox container = new VBox(5);

        excludeTable = new TableView<>();
        excludeTable.setEditable(true);

        TableColumn<Content, Boolean> enabledCol = new TableColumn<>(Messages.getString("ui.dialog.resourceFilter.table.enabled"));
        enabledCol.setCellValueFactory(new PropertyValueFactory<>("excluded"));
        enabledCol.setCellFactory(CheckBoxTableCell.forTableColumn(enabledCol));
        enabledCol.setEditable(true);

        TableColumn<Content, String> pathCol =
                new TableColumn<>(Messages.getString("ui.dialog.resourceFilter.table.path"));
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathCol.setEditable(false);

        excludeTable.getColumns().add(enabledCol);
        excludeTable.getColumns().add(pathCol);
        FXUtils.configureColumn(enabledCol, excludeTable, 0.15, 10);
        FXUtils.configureColumn(pathCol, excludeTable, 0.85, 10);

        container.getChildren().add(excludeTable);
        excludeTable.setMaxWidth(Double.MAX_VALUE);
        excludeTable.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(excludeTable, Priority.ALWAYS);
        VBox.setVgrow(excludeTable, Priority.ALWAYS);

        HBox buttons = new HBox(5);
        Button selectAll = new Button(Messages.getString("ui.dialog.resourceFilter.selectAll"));
        selectAll.setOnAction(e -> excludeTable.getItems().forEach(c -> c.setExcluded(true)));
        Button selectNone = new Button(Messages.getString("ui.dialog.resourceFilter.selectNone"));
        selectNone.setOnAction(e -> excludeTable.getItems().forEach(c -> c.setExcluded(false)));
        buttons.getChildren().addAll(selectAll, selectNone);
        container.getChildren().add(0, buttons);

        getDialogPane().setContent(container);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return excludeTable.getItems().stream()
                        .filter(Content::getExcluded)
                        .map(Content::getPath)
                        .collect(Collectors.toList());
            }
            return null;
        });
    }

    public ResourceFilterDialog withInputData(List<String> resourceContents, List<String> excludedPaths) {
        ObservableList<Content> items = FXCollections.observableArrayList();
        items.addAll(
                resourceContents.stream()
                        .map(Content::new)
                        .peek(c -> c.setExcluded(excludedPaths.contains(c.getPath())))
                        .collect(Collectors.toList()));
        excludeTable.setItems(items);
        return this;
    }

    public static class Content {

        private BooleanProperty excluded = new SimpleBooleanProperty(this, "excluded", false);
        private StringProperty path = new SimpleStringProperty(this, "path", "");

        public Content(String path) {
            this.path.set(path);
        }

        public BooleanProperty excludedProperty() {
            return excluded;
        }

        public boolean getExcluded() {
            return excluded.get();
        }

        public void setExcluded(boolean excluded) {
            this.excluded.set(excluded);
        }

        public StringProperty pathProperty() {
            return path;
        }

        public String getPath() {
            return path.get();
        }

        public void setPath(String path) {
            this.path.set(path);
        }
    }
}
