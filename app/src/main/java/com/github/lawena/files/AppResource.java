package com.github.lawena.files;

import com.github.lawena.i18n.Messages;

import java.nio.file.Path;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class AppResource implements Resource {

    private BooleanProperty enabled = new SimpleBooleanProperty(this, "enabled", false); //$NON-NLS-1$
    private ObjectProperty<Path> path = new SimpleObjectProperty<>(this, "path", null); //$NON-NLS-1$
    private SetProperty<String> tags = new SimpleSetProperty<>(this, "tags", //$NON-NLS-1$
            FXCollections.emptyObservableSet());

    public AppResource(Path path) {
        this.path.set(path);
    }

    @Override
    public final String getName() {
        if (path == null || path.get() == null || path.get().getFileName() == null) {
            return Messages.getString("AppResource.NoPathDefined"); //$NON-NLS-1$
        }
        return path.get().getFileName().toString();
    }

    public final ObjectProperty<Path> pathProperty() {
        return this.path;
    }

    @Override
    public final Path getPath() {
        return this.pathProperty().get();
    }

    public final void setPath(final java.nio.file.Path path) {
        this.pathProperty().set(path);
    }

    public final BooleanProperty enabledProperty() {
        return this.enabled;
    }

    @Override
    public final boolean isEnabled() {
        return this.enabledProperty().get();
    }

    @Override
    public final void setEnabled(final boolean enabled) {
        this.enabledProperty().set(enabled);
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path.get() == null) ? 0 : path.get().hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppResource other = (AppResource) obj;
        if (path.get() == null) {
            if (other.path.get() != null)
                return false;
        } else if (!path.get().equals(other.path.get()))
            return false;
        return true;
    }

    public final SetProperty<String> tagsProperty() {
        return this.tags;
    }

    @Override
    public final ObservableSet<String> getTags() {
        return this.tagsProperty().get();
    }

    @Override
    public final void setTags(final ObservableSet<String> tags) {
        this.tagsProperty().set(tags);
    }

}
