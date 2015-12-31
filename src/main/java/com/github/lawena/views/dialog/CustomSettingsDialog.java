package com.github.lawena.views.dialog;

import com.github.lawena.Messages;
import com.github.lawena.views.dialog.data.CustomSettingsData;
import javafx.geometry.Dimension2D;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomSettingsDialog extends Dialog<CustomSettingsData> {

    private static final String[] COMMANDS = new String[]{
            "exec", "alias", "bind", "unbind", "toggle", "bindtoggle", "incrementvar"
    };

    private static final String[] KEYS = new String[]{
            "SEMICOLON", "BACKSPACE", "DEL", "DOWNARROW", "END", "ESCAPE", "HOME", "INS", "ALT", "LEFTARROW", "CTRL",
            "SHIFT", "MOUSE4", "MOUSE5", "MOUSE1", "MOUSE2", "MOUSE3", "MWHEELDOWN", "MWHEELUP", "KP_MULTIPLY",
            "KP_PLUS", "KP_MINUS", "KP_DEL", "KP_SLASH", "KP_INS", "KP_END", "KP_DOWNARROW", "KP_PGDN", "KP_LEFTARROW",
            "KP_5", "KP_RIGHTARROW", "KP_HOME", "KP_UPARROW", "KP_PGUP", "KP_ENTER", "PGDN", "PGUP", "NUMLOCK", "RALT",
            "RIGHTARROW", "RCTRL", "RSHIFT", "SCROLLLOCK", "SPACE", "TAB", "UPARROW"
    };

    private static final String COMMAND_PATTERN = "\\b(" + String.join("|", COMMANDS) + ")\\b";
    private static final String SPECIAL_PATTERN = "\\b(echo|wait)\\b";
    private static final String KEY_PATTERN = "\\b(" + String.join("|", KEYS) + ")\\b";
    private static final String TEMPLATE_PATTERN = "\\{\\{([^\\}]|\\\\.)*\\}\\}";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMAND>" + COMMAND_PATTERN + ")"
                    + "|(?<SPECIAL>" + SPECIAL_PATTERN + ")"
                    + "|(?<KEY>" + KEY_PATTERN + ")"
                    + "|(?<TEMPLATE>" + TEMPLATE_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
            , Pattern.CASE_INSENSITIVE);

    private CodeArea codeArea;

    protected CustomSettingsDialog() {

    }

    public CustomSettingsDialog(Window owner) {
        setTitle(Messages.getString("ui.dialog.customSettings.title"));
        setHeaderText(Messages.getString("ui.dialog.customSettings.header"));
        initOwner(owner);
        setResizable(true);

        // Set the button types.
        ButtonType saveButtonType = new ButtonType(Messages.getString("ui.dialog.customSettings.ok"), ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        VBox container = new VBox(5);

        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStylesheets().add(getClass().getResource("custom.css").toExternalForm());
        codeArea.richChanges().subscribe(change -> {
            codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
        });
        container.getChildren().add(codeArea);
        codeArea.setMaxWidth(Double.MAX_VALUE);
        codeArea.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(codeArea, Priority.ALWAYS);
        VBox.setVgrow(codeArea, Priority.ALWAYS);

        getDialogPane().setContent(container);

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                CustomSettingsData result = new CustomSettingsData();
                result.setContent(codeArea.getText());
                result.setDimension(new Dimension2D(getWidth(), getHeight()));
                return result;
            }
            codeArea.clear();
            return null;
        });
    }

    public CustomSettingsDialog withInputData(CustomSettingsData input) {
        codeArea.clear();
        codeArea.appendText(input.getContent());
        Dimension2D dimension = input.getDimension();
        setWidth(dimension.getWidth());
        setHeight(dimension.getHeight());
        return this;
    }

    @SuppressWarnings("Duplicates")
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("COMMAND") != null ? "command" :
                            matcher.group("SPECIAL") != null ? "special" :
                                    matcher.group("KEY") != null ? "key" :
                                            matcher.group("TEMPLATE") != null ? "template" :
                                                    matcher.group("STRING") != null ? "string" :
                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

}
