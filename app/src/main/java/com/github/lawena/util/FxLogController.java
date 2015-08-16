package com.github.lawena.util;

import com.github.lawena.Controller;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Manages strings (like logging messages) can be appended to a RichTextFx-based UI control.
 *
 * @author Ivan
 */
public class FxLogController implements LogController {
    private static final Logger log = LoggerFactory.getLogger(FxLogController.class);

    private static final String DEBUG_PATTERN = "\\b(DEBUG)\\b"; //NON-NLS
    private static final String INFO_PATTERN = "\\b(INFO)\\b"; //NON-NLS
    private static final String WARN_PATTERN = "\\b(WARN)\\b"; //NON-NLS
    private static final String ERROR_PATTERN = "\\b(ERROR)\\b"; //NON-NLS
    private static final String TIME_PATTERN = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
    private static final Pattern PATTERN = Pattern.compile("(?<DEBUG>" + DEBUG_PATTERN + ")"
            + "|(?<INFO>" + INFO_PATTERN + ")" + "|(?<WARN>" + WARN_PATTERN + ")" + "|(?<ERROR>"
            + ERROR_PATTERN + ")" + "|(?<TIME>" + TIME_PATTERN + ")");

    private Controller parent;
    private StyleClassedTextArea area;

    public FxLogController(Controller parent) {
        this.parent = parent;
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("DEBUG") != null ? "debug" : matcher.group("INFO") != null ? "info" //NON-NLS
                            : matcher.group("WARN") != null ? "warn" : matcher.group("ERROR") != null ? "error" //NON-NLS
                            : matcher.group("TIME") != null ? "time" //NON-NLS
                            : matcher.group("COMMENT") != null ? "comment" : null; //NON-NLS
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @Override
    public void startController() {
        area = new StyleClassedTextArea();
        area.setWrapText(false);
        area.setEditable(false);
        area.textProperty().addListener((obs, oldText, newText) -> {
            area.setStyleSpans(0, computeHighlighting(newText));
        });

        LogAppender appender = parent.getModel().getLogAppender();
        Button copyLog = new Button("Copy");
        Button openLog = new Button("Open");
        Pane separator = new Pane();
        HBox.setHgrow(separator, Priority.ALWAYS);
        Label levelLabel = new Label("Level");
        ComboBox<String> levelSelector = new ComboBox<>();
        levelSelector.getItems().addAll("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL"); //NON-NLS
        levelSelector.getSelectionModel().select(appender.getMinLevel());
        HBox actions = new HBox(5, copyLog, openLog, separator, levelLabel, levelSelector);
        actions.setAlignment(Pos.CENTER_LEFT);

        copyLog.setOnAction(e -> {
            area.selectAll();
            area.copy();
            area.selectRange(0, 0);
        });
        openLog.setOnAction(e -> parent.submitTask(
                new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        try {
                            parent.getModel().getHostServices().showDocument(Paths.get("logs", "lawena.log").toUri().toString());
                        } catch (Exception e) {
                            log.warn("Could not open file", e);
                        }
                        return null;
                    }
                }));
        ChangeListener<String> listener = (obs, _old, _new) -> appender.setMinLevel(_new);
        levelSelector.getSelectionModel().selectedItemProperty().addListener(new WeakChangeListener<>(listener));

        Pane pane = parent.getLogPane();
        pane.getChildren().addAll(area, actions);
        VBox.setVgrow(area, Priority.ALWAYS);
        parent.getStage().getScene().getStylesheets()
                .add(FxLogController.class.getResource("logging.css").toExternalForm());
    }

    @Override
    public void stopController() {

    }

    @Override
    public void append(String text) {
        area.appendText(text);
    }

    @Override
    public void clear() {
        area.clear();
    }

}
