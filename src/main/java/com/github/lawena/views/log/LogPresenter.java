package com.github.lawena.views.log;

import com.github.lawena.service.TaskService;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
@ConditionalOnBean(LogView.class)
public class LogPresenter {

    private static final Logger log = LoggerFactory.getLogger(LogPresenter.class);

    @Autowired
    private StyleClassedTextArea area;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HostServices hostServices;

    @Value("${logging.file}")
    private String loggingFile;

    @FXML
    private VBox container;

    @FXML
    private void initialize() {
        area.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(area, Priority.ALWAYS);
        VBox.setVgrow(area, Priority.ALWAYS);
        container.getChildren().add(area);
    }

    @FXML
    private void copy(ActionEvent event) {
        int anchor = area.getAnchor();
        int caretPosition = area.getCaretPosition();
        area.selectAll();
        area.copy();
        area.selectRange(anchor, caretPosition);
    }

    @FXML
    private void open(ActionEvent event) {
        taskService.submitTask(new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {
                    hostServices.showDocument(Paths.get(loggingFile).toUri().toString());
                } catch (Exception e) {
                    log.warn("Could not open file: ", e.toString());
                }
                return null;
            }
        });
    }

}
