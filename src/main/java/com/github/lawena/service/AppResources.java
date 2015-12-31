package com.github.lawena.service;

import com.github.lawena.Messages;
import com.github.lawena.config.Constants;
import com.github.lawena.domain.AppResource;
import com.github.lawena.domain.Resource;
import com.github.lawena.domain.Tag;
import com.github.lawena.event.NewResourceEvent;
import com.github.lawena.repository.TagRepository;
import com.github.lawena.vpk.Archive;
import com.github.lawena.vpk.ArchiveException;
import com.github.lawena.vpk.Directory;
import com.github.lawena.vpk.EntryException;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.lawena.util.FXUtils.ensureRunAndGet;
import static com.github.lawena.util.FXUtils.ensureRunLater;

@Service
public class AppResources implements Resources {
    private static final Logger log = LoggerFactory.getLogger(AppResources.class);

    private final ListProperty<Path> folders = new SimpleListProperty<>(this, "folders",
            FXCollections.observableArrayList());
    private final ListProperty<Resource> resourceList = new SimpleListProperty<>(this,
            "resourceList", newResourceList());
    private final ReadOnlyListWrapper<Resource> wrapper = new ReadOnlyListWrapper<>(
            resourceList.get());
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public AppResources(TagRepository tagRepository, ApplicationEventPublisher publisher) {
        this.tagRepository = tagRepository;
        this.publisher = publisher;
    }

    @PostConstruct
    private void configure() {
        tagRepository.save(new Tag("hud", Messages.getString("ui.tags.hud"), Color.web("#5319e7")));
        tagRepository.save(new Tag("cfg", Messages.getString("ui.tags.cfg"), Color.web("#0052cc")));
        tagRepository.save(new Tag("skybox", Messages.getString("ui.tags.skybox"), Color.web("#c7def8")));
        tagRepository.save(new Tag("vpk", Messages.getString("ui.tags.vpk"), Color.web("#dddddd")));
        tagRepository.save(new Tag("lwrt", Messages.getString("ui.tags.lwrt"), Color.web("#5319e7")));
    }

    static List<String> getContents(Resource resource) throws IOException {
        Path start = resource.getPath().toAbsolutePath();
        if (Files.isDirectory(start)) {
            return Files.walk(start, 4, FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> start.toAbsolutePath().relativize(p.toAbsolutePath()))
                    .map(p -> p.toString().replace('\\', '/')).collect(Collectors.toList());
        } else if (start.toString().toLowerCase().endsWith(".vpk")) {
            try {
                List<String> contents = new ArrayList<>();
                Archive vpk = new Archive(start.toFile());
                vpk.load();
                for (Directory dir : vpk.getDirectories()) {
                    contents.addAll(dir.getEntries().stream()
                            .map(entry -> dir.getPath() + "/" + entry.getFullName())
                            .collect(Collectors.toList()));
                }
                return contents;
            } catch (ArchiveException | EntryException e) {
                throw new IOException("Invalid VPK archive", e);
            }
        } else {
            return Collections.singletonList(start.toString());
        }
    }

    @Override
    public Set<Resource> scanForResources(Path dir) {
        Set<Resource> newResourceList = new HashSet<>();
        // path must be a folder -- no vpk files at this stage!
        if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            log.warn("Skipping invalid or non existing path: {}", dir);
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path path : stream) {
                    // each resource must be a folder or a vpk file
                    newResource(path).ifPresent(newResourceList::add);
                }
            } catch (IOException e) {
                log.warn("Problem while loading custom paths: {}", e.toString());
            }
        }
        return newResourceList;
    }

    private ObservableList<Resource> newResourceList() {
        return FXCollections.checkedObservableList(FXCollections.observableArrayList(), Resource.class);
    }

    private boolean isValidPath(Path path) {
        return Files.isDirectory(path) || path.toString().toLowerCase().endsWith(".vpk");
    }

    private Optional<Resource> findResource(Path start) {
        return ensureRunAndGet(() -> getResourceList().stream()
                .filter(r -> r.getPath().equals(start))
                .findFirst(), Optional.empty());
    }

    private void refreshTags(Resource resource) {
        try {
            List<String> contents = getContents(resource);
            ObservableSet<Tag> tags = FXCollections.observableSet(tag(contents));
            if (resource.getPath().toString().toLowerCase().endsWith(".vpk")) {
                tags.add(tagRepository.findOne("vpk").get());
            }
            if (resource.getPath().startsWith(Constants.LWRT_PATH)) {
                tags.add(tagRepository.findOne("lwrt").get());
            }
            ensureRunLater(() -> {
                resource.setTags(tags);
            });
        } catch (IOException e) {
            log.warn("Could not get contents from {}: {}", resource.getPath(), e.toString());
        }
    }

    private Set<Tag> tag(Collection<String> contents) {
        SortedSet<Tag> tags = new TreeSet<>();
        for (String content : contents) {
            String c = content.toLowerCase();
            if (c.equals("scripts/hudlayout.res")) {
                tags.add(tagRepository.findOne("hud").get());
            } else if (c.startsWith("cfg/") && c.endsWith(".cfg")) {
                tags.add(tagRepository.findOne("cfg").get());
            } else if (c.startsWith("materials/skybox/")) {
                tags.add(tagRepository.findOne("skybox").get());
            }
        }
        return tags;
    }

    @Override
    public Optional<Resource> newResource(Path start) {
        // get the resource for this path or create a new one if it doesn't exist
        Optional<Resource> optional = findResource(start);
        Resource resource;
        if (!optional.isPresent()) {
            if (isValidPath(start)) {
                resource = new AppResource(start);
                ensureRunLater(() -> {
                    ObservableList<Resource> list = getResourceList();
                    list.add(resource);
                    FXCollections.sort(list);
                    publisher.publishEvent(new NewResourceEvent(resource));
                });
            } else {
                log.debug("Ignoring invalid resource at {}", start);
                return Optional.empty();
            }
        } else {
            resource = optional.get();
        }
        refreshTags(resource);
        return Optional.of(resource);
    }

    @Override
    public void refreshResource(Path start) {
        findResource(start).ifPresent(this::refreshTags);
    }

    @Override
    public void deleteResource(Path path) {
        ensureRunLater(() -> getResourceList().removeIf(r -> r.getPath().equals(path)));
    }

    @Override
    public final ListProperty<Path> foldersProperty() {
        return this.folders;
    }

    @Override
    public final ObservableList<Path> getFolders() {
        return this.foldersProperty().get();
    }

    public final void setFolders(final ObservableList<Path> folders) {
        this.foldersProperty().set(folders);
    }

    @Override
    public final ReadOnlyListProperty<Resource> resourceListProperty() {
        return this.wrapper;
    }

    @Override
    public final ObservableList<Resource> getResourceList() {
        return this.resourceListProperty().get();
    }

    @Override
    public void enableAll() {
        enableAll(true);
    }

    @Override
    public void disableAll() {
        enableAll(false);
    }

    private void enableAll(boolean enable) {
        ensureRunLater(() -> getResourceList().forEach(resource -> resource.setEnabled(enable)));
    }

    @Override
    public List<String> getResourceContents(Resource resource) {
        try {
            return getContents(resource);
        } catch (IOException e) {
            log.warn("Could not retrieve resource contents", e);
            return Collections.emptyList();
        }
    }
}
