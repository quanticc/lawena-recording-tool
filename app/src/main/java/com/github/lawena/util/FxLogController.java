package com.github.lawena.util;

import com.github.lawena.Controller;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Manages strings (like logging messages) can be appended to a RichTextFx-based UI control.
 *
 * @author Ivan
 */
@SuppressWarnings("nls")
public class FxLogController implements LogController {

    private static final String DEBUG_PATTERN = "\\b(DEBUG)\\b";
    private static final String INFO_PATTERN = "\\b(INFO)\\b";
    private static final String WARN_PATTERN = "\\b(WARN)\\b";
    private static final String ERROR_PATTERN = "\\b(ERROR)\\b";
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
                    matcher.group("DEBUG") != null ? "debug" : matcher.group("INFO") != null ? "info"
                            : matcher.group("WARN") != null ? "warn" : matcher.group("ERROR") != null ? "error"
                            : matcher.group("TIME") != null ? "time"
                            : matcher.group("COMMENT") != null ? "comment" : null; /*
                                                                              * never happens
                                                                              */
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
        Pane pane = parent.getLogPane();
        pane.getChildren().add(area);
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
