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
        List<Path> list = new ArrayList<>();
        for (String s : suffixes) {
            list.add(Paths.get(parent, prefix + s + extension));
        }
        return list;
    }

    private static BufferedReader newProcessReader(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8"))); //$NON-NLS-1$
    }

    private static Canvas concatenate(List<Path> outputs) {
        int width = Skybox.WIDTH;
        int height = Skybox.HEIGHT;
        int totalwidth = width * outputs.size();
        Canvas canvas = new Canvas(totalwidth, height);
        for (int i = 0; i < outputs.size(); i++) {
            Path out = outputs.get(i);
            if (!Files.exists(out)) {
                log.warn("Expected file {} but it does not exist!", out); //$NON-NLS-1$
            } else {
                try {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    Image image = new Image(Files.newInputStream(out), width, height, false, true);
                    gc.drawImage(image, i * width, 0);
                } catch (IOException e) {
                    log.warn("Could not read image: {}", e.toString()); //$NON-NLS-1$
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
                log.warn("Could not delete some of the temporary skybox files: {}", e.toString()); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected ObservableList<Skybox> call() throws Exception {
        updateTitle(Messages.getString("PreviewTask.TaskTitle")); //$NON-NLS-1$
        ObservableList<Skybox> list = FXCollections.observableArrayList();
        if (candidates.isEmpty())
            return list;
        // get the path of the preview generator (vtfcmd.exe)
        int count = 0;
        List<String> boxes = Arrays.asList("lf", "bk", "rt", "ft"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String inputDir = "lwrt/tf2/skybox/vtf"; //$NON-NLS-1$
        String outputDir = "lwrt/tf2/skybox-preview"; //$NON-NLS-1$
        String format = "png"; //$NON-NLS-1$
        for (Skybox sky : candidates) {
            count++;
            updateProgress(count, candidates.size());
            if (sky.equals(Skybox.DEFAULT) || sky.getPreview() != null) {
                list.add(sky);
                continue;
            }
            String name = sky.getName();
            log.info("Creating skybox preview for {}", name); //$NON-NLS-1$
            updateMessage(String.format(Messages.getString("PreviewTask.TaskMessage"), name)); //$NON-NLS-1$
            List<Path> inputs = withParams(inputDir, name, boxes, ".vtf"); //$NON-NLS-1$
            List<Path> outputs = withParams(outputDir, name, boxes, "." + format); //$NON-NLS-1$
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
            log.debug("Adding skybox preview: {}", name); //$NON-NLS-1$
            Canvas canvas = concatenate(outputs);
            Platform.runLater(() -> {
                sky.setPreview(canvas.snapshot(null, null));
            });
            list.add(sky);
            deleteTempFiles(outputs);
        }
        return list;
    }

    @Override
    protected void done() {
        String outputDir = "lwrt/tf2/skybox-preview"; //$NON-NLS-1$
        try {
            Files.deleteIfExists(Paths.get(outputDir));
        } catch (IOException e) {
            log.warn("Could not remove temporary skybox preview folder: {}", e.toString()); //$NON-NLS-1$
        }
    }

    private void generate(List<Path> inputs, String outputDir, String ext) {
        try {
            Files.createDirectories(Paths.get(outputDir));
            ProcessBuilder pb =
                    new ProcessBuilder(vtfCmdPath.toString(), "-output", outputDir, "-exportformat", ext); //$NON-NLS-1$ //$NON-NLS-2$
            for (Path in : inputs) {
                pb.command().add("-file"); //$NON-NLS-1$
                pb.command().add(in.toString());
            }
            log.debug("Invoking process: {}", pb.command()); //$NON-NLS-1$
            Process pr = pb.start();
            try (BufferedReader input = newProcessReader(pr)) {
                String line;
                while ((line = input.readLine()) != null) {
                    log.trace("[vtfcmd] {}", line); //$NON-NLS-1$
                }
            }
            pr.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Problem while generating png from vtf file", e); //$NON-NLS-1$
        }
    }

}
