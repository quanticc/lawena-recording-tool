package com.github.lawena.util;

import com.google.gson.reflect.TypeToken;

import com.github.lawena.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Utility methods for Lawena Recording Tool.
 *
 * @author Ivan
 * @deprecated use {@link LwrtUtils} instead
 */
@Deprecated
public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    private Util() {
    }

    public static int compareCreationTime(File newgd, File curgd) {
        try {
            Path newp = newgd.toPath();
            BasicFileAttributes newAttributes = Files.readAttributes(newp, BasicFileAttributes.class);
            FileTime newCreationTime = newAttributes.creationTime();
            Path curp = curgd.toPath();
            BasicFileAttributes curAttributes = Files.readAttributes(curp, BasicFileAttributes.class);
            FileTime curCreationTime = curAttributes.creationTime();
            log.debug("Comparing creationTime: new={} cur={}", newCreationTime, curCreationTime); //$NON-NLS-1$
            return newCreationTime.compareTo(curCreationTime);
        } catch (IOException e) {
            log.warn("Could not retrieve file creation time: {}", e.toString()); //$NON-NLS-1$
        }
        return 1;
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buffer = new byte[4096];
            for (int read = 0; (read = in.read(buffer)) > 0; ) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String now(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String getManifestString(String key, String defaultValue) {
        try (JarFile jar =
                     new JarFile(
                             new File(Util.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
            String value = jar.getManifest().getMainAttributes().getValue(key);
            if (value == null) {
                log.warn("{}: {} (not built using Gradle)", key, defaultValue);
                return defaultValue;
            }
            return value;
        } catch (IOException | URISyntaxException e) {
        }
        log.warn("{}: {} (not packaged into a JAR file)", key, defaultValue);
        return defaultValue;
    }

    public static ImageIcon createPreviewIcon(String imageName) throws IOException {
        int size = 96;
        BufferedImage image;
        File input = new File(imageName);
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        image.createGraphics().drawImage(
                ImageIO.read(input).getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
        return new ImageIcon(image);
    }

    public static void registerValidation(JComboBox<String> combo, final String validationRegex,
                                          final JLabel label) {
        final JTextComponent tc = (JTextComponent) combo.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            private void validateInput() {
                if (tc.getText().matches(validationRegex)) {
                    label.setForeground(Color.BLACK);
                } else {
                    label.setForeground(Color.RED);
                }
            }
        });
    }

    public static void selectComboItem(JComboBox<String> combo, String selectedValue,
                                       List<String> possibleValues) {
        if (possibleValues == null || possibleValues.isEmpty()) {
            combo.setSelectedItem(selectedValue);
        } else {
            int i = possibleValues.indexOf(selectedValue);
            if (i >= 0) {
                combo.setSelectedIndex(i);
            }
        }
    }

    public static BufferedReader newProcessReader(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8"))); //$NON-NLS-1$
    }

    public static long sizeOfPath(Path startPath) throws IOException {
        final AtomicLong size = new AtomicLong(0);
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return size.get();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B"; //$NON-NLS-1$
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre); //$NON-NLS-1$
    }

    public static int startProcess(List<String> command, Consumer<String> consumer) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            Process p = builder.start();
            try (BufferedReader input = newProcessReader(p)) {
                String line;
                while ((line = input.readLine()) != null) {
                    consumer.accept(line);
                }
            }
            return p.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Process could not be completed", e); //$NON-NLS-1$
        }
        return 1;
    }

    public static int startUncheckedProcess(List<String> command, Consumer<String> consumer)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process p = builder.start();
        try (BufferedReader input = newProcessReader(p)) {
            String line;
            while ((line = input.readLine()) != null) {
                consumer.accept(line);
            }
        }
        return p.waitFor();
    }

    public static int startProcess(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            Process p = builder.start();
            return p.waitFor();
        } catch (InterruptedException | IOException e) {
            log.warn("Process could not be completed", e); //$NON-NLS-1$
        }
        return 1;
    }

    public static int startUncheckedProcess(List<String> command) throws IOException,
            InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process p = builder.start();
        return p.waitFor();
    }

    public static String leftPad(String str, int outputLength, char padChar) {
        StringBuilder sb = new StringBuilder();
        for (int toPrepend = outputLength - str.length(); toPrepend > 0; toPrepend--) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    public static String listToString(List<String> list, char separator) {
        Iterator<String> it = list.iterator();
        if (!it.hasNext())
            return ""; //$NON-NLS-1$

        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            String e = it.next();
            sb.append(e);
            if (!it.hasNext())
                return sb.toString();
            sb.append(separator);
        }
    }

    /**
     * Retrieve a value from a <code>Map</code> tree given a path, creating all needed intermediate
     * paths that do not exist.
     *
     * @param root - a <code>Map</code> representing the root to explore
     * @param type - the type of the value retrieved
     * @param key  - the key, a path separated by <code>.</code> (dot), where the value will be set
     * @return the value from the map at the given path or <code>null</code> if there is no such
     * value stored at the given path
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromTree(Map<String, Object> root, TypeToken<T> type, String key) {
        if (root == null)
            throw new IllegalArgumentException("root must not be null"); //$NON-NLS-1$
        if (type == null)
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        if (key == null)
            throw new IllegalArgumentException("null keys are not allowed"); //$NON-NLS-1$
        String[] paths = key.split("\\."); //$NON-NLS-1$
        Map<String, Object> map = getParentMap(root, paths);
        return (T) map.get(paths[paths.length - 1]);
    }

    /**
     * Set a value to a <code>Map</code> tree given a path, creating all needed intermediate paths
     * that do not exist.
     *
     * @param root  - a <code>Map</code> representing the root to explore
     * @param key   - a <code>String</code> separated by <code>.</code> (dot), where the value will
     *              be set
     * @param value - the value to set within the map
     */
    public static <T> void setToTree(Map<String, Object> root, String key, T value) {
        if (root == null)
            throw new IllegalArgumentException("root must not be null"); //$NON-NLS-1$
        if (key == null)
            throw new IllegalArgumentException("null keys are not allowed"); //$NON-NLS-1$
        if (value == null)
            throw new IllegalArgumentException("null values are not allowed"); //$NON-NLS-1$
        String[] paths = key.split("\\."); //$NON-NLS-1$
        Map<String, Object> map = getParentMap(root, paths);
        map.put(paths[paths.length - 1], value);
    }

    /**
     * Traverse the tree given by a root through a supplied path array until the parent of the last
     * path is reached, and then return it. If an intermediate path does not exist, it is created
     * within the tree.
     *
     * @param root  - a <code>Map</code> representing the root to explore
     * @param paths - a <code>String</code> array describing the path to walk through the tree
     * @return a <code>Map</code> that holds the last path supplied as a key. It is never
     * <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getParentMap(Map<String, Object> root, String[] paths) {
        if (root == null)
            throw new IllegalArgumentException("root must not be null"); //$NON-NLS-1$
        if (paths == null)
            throw new IllegalArgumentException("a path array must be supplied"); //$NON-NLS-1$
        Map<String, Object> map = root;
        for (int i = 0; i < paths.length - 1; i++) {
            String path = paths[i];
            if (!map.containsKey(path)) {
                map.put(path, new LinkedHashMap<String, Object>());
            }
            map = (Map<String, Object>) map.get(path);
        }
        return map;
    }

    public static boolean notifyBigFolder(int thresholdInMB, Path... paths) {
        try {
            long bytes = 0L;
            for (Path path : paths) {
                bytes += Util.sizeOfPath(path);
            }
            String size = Util.humanReadableByteCount(bytes, true);
            log.debug("Backup folders size: " + size); //$NON-NLS-1$
            if (bytes / 1024 / 1024 > thresholdInMB) {
                int answer =
                        JOptionPane.showConfirmDialog(null,
                                String.format(Messages.getString("Util.bigFolderWarning"), size), //$NON-NLS-1$
                                Messages.getString("Util.bigFolderWarningTitle"), JOptionPane.YES_NO_OPTION, //$NON-NLS-1$
                                JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CLOSED_OPTION) {
                    return false;
                }
            }
        } catch (IOException e) {
            log.info("Could not determine folder size: {}", e.toString()); //$NON-NLS-1$
        }
        return true;
    }

    public static Path toPath(String path) {
        return path == null ? Paths.get("") : Paths.get(path); //$NON-NLS-1$
    }

}
