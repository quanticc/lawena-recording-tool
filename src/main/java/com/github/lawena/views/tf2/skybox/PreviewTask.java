package com.github.lawena.views.tf2.skybox;

import com.github.lawena.Messages;
import com.github.lawena.util.LwrtUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.lawena.util.LwrtUtils.newProcessReader;

public class PreviewTask extends Task<ObservableList<Skybox>> {
    private static final Logger log = LoggerFactory.getLogger(PreviewTask.class);

    private ReadOnlyObjectWrapper<ObservableList<Skybox>> partialResults =
            new ReadOnlyObjectWrapper<>(this, "partialResults", FXCollections.observableArrayList(new ArrayList<>()));
    private final Path vtfCmdPath;
    private final List<Skybox> candidates;

    public PreviewTask(Path vtfCmdPath, List<Skybox> candidates) {
        this.vtfCmdPath = vtfCmdPath;
        this.candidates = candidates;
    }

    private static List<Path> withParams(String parent, String prefix, List<String> suffixes, String extension) {
        return suffixes.stream().map(s -> Paths.get(parent, prefix + s + extension)).collect(Collectors.toList());
    }

    private static List<Path> withParams(Path parent, String prefix, List<String> suffixes, String extension) {
        return suffixes.stream().map(s -> parent.resolve(prefix + s + extension)).collect(Collectors.toList());
    }

    private static Canvas concatenate(List<Path> outputs) {
        int width = Skybox.WIDTH;
        int height = Skybox.HEIGHT;
        int totalwidth = width * outputs.size();
        Canvas canvas = new Canvas(totalwidth, height);
        for (int i = 0; i < outputs.size(); i++) {
            Path out = outputs.get(i);
            if (!Files.exists(out)) {
                log.warn("Expected file {} but it does not exist!", out);
            } else {
                try {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    Image image = new Image(Files.newInputStream(out), width, height, false, true);
                    gc.drawImage(image, i * width, 0);
                } catch (IOException e) {
                    log.warn("Could not read image: {}", e.toString());
                }
            }
        }
        return canvas;
    }

    @Override
    protected ObservableList<Skybox> call() throws Exception {
        try {
            if (LwrtUtils.isWindows()) {
                return checkedCall();
            }
            log.warn("Can not create skybox previews in this OS");
        } catch (Exception e) {
            log.warn("Could not complete skybox preview task", e);
        }
        return partialResults.get();
    }

    private ObservableList<Skybox> checkedCall() throws Exception {
        updateTitle(Messages.getString("ui.tf2.config.previewTask.title"));
        int count = 0;
        List<String> boxes = Arrays.asList("lf", "bk", "rt", "ft");
        String inputDir = "lwrt/tf2/skybox/vtf";
        Path outputPath = Files.createTempDirectory("skybox-preview.").toAbsolutePath();
        outputPath.toFile().deleteOnExit();
        String format = "png";
        for (Skybox sky : candidates) {
            if (isCancelled()) {
                break;
            }
            count++;
            updateProgress(count, candidates.size());
            if (sky.equals(Skybox.DEFAULT) || sky.getPreview() != null) {
                Platform.runLater(() -> partialResults.get().add(sky));
                continue;
            }
            String name = sky.getName();
            log.info("Creating skybox preview for {}", name);
            updateMessage(Messages.getString("ui.tf2.config.previewTask.message", name));
            List<Path> inputs = withParams(inputDir, name, boxes, ".vtf");
            List<Path> outputs = withParams(outputPath, name, boxes, "." + format);
            List<Path> vtfin = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                Path in = inputs.get(i);
                Path out = outputs.get(i);
                if (!Files.exists(out)) {
                    vtfin.add(in);
                }
            }
            if (!vtfin.isEmpty()) {
                generate(vtfin, outputPath, format);
            }
            log.debug("Adding skybox preview: {}", name);
            Canvas canvas = concatenate(outputs);
            Platform.runLater(() -> {
                sky.setPreview(canvas.snapshot(null, null));
                partialResults.get().add(sky);
            });
            // schedule temporary files for deletion on (normal) exit
            outputs.stream().map(Path::toFile).forEach(File::deleteOnExit);
        }
        return partialResults.get();
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            log.info("Task cancelled");
        } else {
            log.info("Task completed");
        }
    }

    private void generate(List<Path> inputs, Path outputPath, String ext) {
        try {
            if (!Files.exists(outputPath) || !Files.isDirectory(outputPath)) {
                Files.createDirectories(outputPath);
            }
            ProcessBuilder pb =
                    new ProcessBuilder(vtfCmdPath.toString(), "-output", outputPath.toString(), "-exportformat", ext);
            for (Path in : inputs) {
                pb.command().add("-file");
                pb.command().add(in.toString());
            }
            log.debug("Invoking process: {}", pb.command());
            Process pr = pb.start();
            try (BufferedReader input = newProcessReader(pr)) {
                String line;
                while ((line = input.readLine()) != null) {
                    log.trace("[vtfcmd] {}", line);
                }
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Problem while generating png from vtf file", e);
        }
    }

    public final ObservableList<Skybox> getPartialResults() {
        return partialResults.get();
    }

    public final ReadOnlyObjectProperty<ObservableList<Skybox>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
    }

}
