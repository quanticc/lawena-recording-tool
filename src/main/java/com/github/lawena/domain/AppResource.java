package com.github.lawena.domain;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.nio.file.Path;

public class AppResource implements Resource {

    private BooleanProperty enabled = new SimpleBooleanProperty(this, "enabled", false);
    private ObjectProperty<Path> path = new SimpleObjectProperty<>(this, "path", null);
    private SetProperty<Tag> tags = new SimpleSetProperty<>(this, "tags", FXCollections.observableSet());
    private ListProperty<String> excludedPaths = new SimpleListProperty<>(this, "excludedPaths", FXCollections.observableArrayList());

    public AppResource(Path path) {
        this.path.set(path);
    }

    @Override
    public final String getName() {
        if (path == null || path.get() == null || path.get().getFileName() == null) {
            return "No path defined";
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

    public final void setPath(final Path path) {
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

    public final SetProperty<Tag> tagsProperty() {
        return this.tags;
    }

    @Override
    public final ObservableSet<Tag> getTags() {
        return this.tagsProperty().get();
    }

    @Override
    public final void setTags(final ObservableSet<Tag> tags) {
        this.tagsProperty().set(tags);
    }

    public final ListProperty<String> excludedPathsProperty() {
        return excludedPaths;
    }

    @Override
    public ObservableList<String> getExcludedPaths() {
        return excludedPaths.get();
    }

    @Override
    public void setExcludedPaths(ObservableList<String> excludedPaths) {
        this.excludedPaths.set(excludedPaths);
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

    @Override
    public String toString() {
        return "AppResource [" +
                "enabled=" + enabled.get() +
                ", path=" + path.get() +
                ", tags=" + tags.get() +
                ", excluded=" + excludedPaths.get() +
                ']';
    }
}
