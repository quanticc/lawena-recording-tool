package com.github.lawena.tf2.skybox;

import com.github.lawena.tf2.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class PreviewTask extends Task<ObservableList<Skybox>> {
    private static final Logger log = LoggerFactory.getLogger(PreviewTask.class);

    private Path vtfCmdPath;
    private List<Skybox> candidates;

    public PreviewTask(Path vtfCmdPath, List<Skybox> candidates) {
        this.vtfCmdPath = vtfCmdPath;
        this.candidates = candidates;
    }

    private static List<Path> withParams(String parent, String prefix, List<String> suffixes,
                                         String extension) {
        return suffixes.stream().map(s -> Paths.get(parent, prefix + s + extension)).collect(Collectors.toList());
    }

    private static BufferedReader newProcessReader(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")));
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

    private static void deleteTempFiles(List<Path> outputs) {
        for (Path out : outputs) {
            try {
                Files.deleteIfExists(out);
            } catch (IOException e) {
                log.warn("Could not remove some of the temporary skybox files: {}", e.toString());
            }
        }
    }

    @Override
    protected ObservableList<Skybox> call() throws Exception {
        try {
            return checkedCall();
        } catch (Exception e) {
            log.warn("Could not complete skybox preview task", e);
        }
        // TODO: return empty list or null?
        return null;
    }

    private ObservableList<Skybox> checkedCall() throws Exception {
        updateTitle(Messages.getString("PreviewTask.TaskTitle"));
        ObservableList<Skybox> list = FXCollections.observableArrayList();
        if (candidates.isEmpty())
            return list;
        // get the path of the preview generator (vtfcmd.exe)
        int count = 0;
        List<String> boxes = Arrays.asList("lf", "bk", "rt", "ft"); // NON-NLS
        String inputDir = "lwrt/tf2/skybox/vtf"; // NON-NLS
        String outputDir = "lwrt/tf2/skybox-preview"; // NON-NLS
        String format = "png"; //$NON-NLS-1$
        for (Skybox sky : candidates) {
            count++;
            updateProgress(count, candidates.size());
            if (sky.equals(Skybox.DEFAULT) || sky.getPreview() != null) {
                list.add(sky);
                continue;
            }
            String name = sky.getName();
            log.info("Creating skybox preview for {}", name);
            updateMessage(String.format(Messages.getString("PreviewTask.TaskMessage"), name));
            List<Path> inputs = withParams(inputDir, name, boxes, ".vtf"); // NON-NLS
            List<Path> outputs = withParams(outputDir, name, boxes, "." + format);
            List<Path> vtfin = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                Path in = inputs.get(i);
                Path out = outputs.get(i);
                if (!Files.exists(out)) {
                    vtfin.add(in);
                }
            }
            if (!vtfin.isEmpty()) {
                generate(vtfin, outputDir, format);
            }
            log.debug("Adding skybox preview: {}", name);
            Canvas canvas = concatenate(outputs);
            Platform.runLater(() -> sky.setPreview(canvas.snapshot(null, null)));
            list.add(sky);
            deleteTempFiles(outputs);
        }
        return list;
    }

    @Override
    protected void done() {
        String outputDir = "lwrt/tf2/skybox-preview"; // NON-NLS
        try {
            Files.deleteIfExists(Paths.get(outputDir));
        } catch (IOException e) {
            log.warn("Could not remove temporary skybox preview folder: {}", e.toString());
        }
    }

    private void generate(List<Path> inputs, String outputDir, String ext) {
        try {
            Files.createDirectories(Paths.get(outputDir));
            ProcessBuilder pb =
                    new ProcessBuilder(vtfCmdPath.toString(), "-output", outputDir, "-exportformat", ext);
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

}
