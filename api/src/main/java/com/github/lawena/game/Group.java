package com.github.lawena.game;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * A group of commands bound to a configuration key, which can be related to a game configuration or
 * not. To help bridging with a JSON representation without the use of generics, a set of concrete
 * classes derived from {@link Group} was created.
 *
 * @author Ivan
 */
public abstract class Group {

    private transient BooleanProperty enabled = new SimpleBooleanProperty(this, "enabled", false); //$NON-NLS-1$
    private String name;
    private List<String> commands;
    private boolean gameConfig = true;

    public static Group create(String key, List<String> cmds, boolean defValue, boolean skipWhenFalse) {
        BooleanGroup group = new BooleanGroup();
        group.setName(key);
        group.setCommands(cmds);
        group.setDefaultValue(defValue);
        group.setSkipWhenFalse(skipWhenFalse);
        return group;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final List<String> getCommands() {
        return commands;
    }

    public final void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public final boolean isGameConfig() {
        return gameConfig;
    }

    public final void setGameConfig(boolean gameConfig) {
        this.gameConfig = gameConfig;
    }

    public abstract Object getDefaultValue();

    public final BooleanProperty enabledProperty() {
        return this.enabled;
    }

    public final java.lang.Boolean getEnabled() {
        return this.enabledProperty().get();
    }

    public final void setEnabled(final java.lang.Boolean enabled) {
        this.enabledProperty().set(enabled);
    }

}
