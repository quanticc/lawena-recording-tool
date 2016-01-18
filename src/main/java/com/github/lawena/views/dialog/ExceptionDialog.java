package com.github.lawena.views.dialog;

import com.github.lawena.util.FXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public final class ExceptionDialog {

    private String title = "Exception Dialog";
    private String header = "An exception was found";
    private String content = "";
    private Throwable throwable = null;

    private ExceptionDialog() {
    }

    public static Optional<ButtonType> show(String title, String header, String content, Throwable t) {
        return FXUtils.ensureRunAndGet(() -> {
            ExceptionDialog d = new ExceptionDialog();
            d.title = title;
            d.header = header;
            d.content = content;
            d.throwable = t;
            return d.show();
        }, null);
    }

    private Optional<ButtonType> show() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (throwable != null) {
            // Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);
        }
        return alert.showAndWait();
    }

}

