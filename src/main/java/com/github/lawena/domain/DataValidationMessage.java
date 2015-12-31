package com.github.lawena.domain;

import com.github.lawena.Messages;
import javafx.scene.control.Control;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;

public class DataValidationMessage implements ValidationMessage {

    private final Type type;
    private final String text;
    private final Severity severity;

    public DataValidationMessage(Type type) {
        this(type, Messages.getString("validation." + type));
    }

    public DataValidationMessage(Type type, Object... args) {
        this(type, Messages.getString("validation." + type, args));
    }

    private DataValidationMessage(Type type, String text) {
        this.type = type;
        this.text = text;
        this.severity = type.severity;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public Control getTarget() {
        return null;
    }

    public enum Type {
        genericError(Severity.ERROR),
        genericWarning(Severity.WARNING),
        missingLaunchMode(Severity.ERROR),
        missingSteamPath(Severity.ERROR),
        missingGamePath(Severity.ERROR);

        private final Severity severity;

        Type(Severity severity) {
            this.severity = severity;
        }
    }
}
