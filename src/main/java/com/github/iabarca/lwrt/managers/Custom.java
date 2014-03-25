
package com.github.iabarca.lwrt.managers;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import com.github.iabarca.lwrt.custom.CustomContent;
import com.github.iabarca.lwrt.profile.ProfilesListener;
import com.github.iabarca.lwrt.tree.ListVpkFilesVisitor;
import com.github.iabarca.lwrt.tree.TreeWalker;
import com.github.iabarca.lwrt.tree.VpkTree;
import com.github.iabarca.lwrt.util.ListFilesVisitor;
import com.github.iabarca.lwrt.util.WatchDir.WatchAction;

public class Custom extends AbstractTableModel implements ProfilesListener {

    private static final Logger log = Logger.getLogger("lawena");

    public enum Column {
        SELECTED, NAME;
    }

    private static final long serialVersionUID = 1L;

    private List<CustomContent> customContent;
    private Profiles profilesManager;
    private Map<String, String> startsWithTagMap;

    public Custom(Profiles profiles) {
        profilesManager = profiles;
        customContent = profiles.getProfile().getCustomContent();
        startsWithTagMap = profiles.getGame().getStartsWithTagMap();
        profiles.addProfileListener(this);
    }

    public List<CustomContent> getCustomFiles() {
        return customContent;
    }

    @Override
    public int getRowCount() {
        return customContent.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CustomContent content = customContent.get(rowIndex);
        switch (Column.values()[columnIndex]) {
            case SELECTED:
                return content.isSelected();
            case NAME:
                return content;
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        CustomContent file = customContent.get(rowIndex);
        switch (Column.values()[columnIndex]) {
            case SELECTED:
                file.setSelected((boolean) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
                return;
            default:
        }
    }

    public WatchAction getWatchAction() {
        return new WatchAction() {

            @Override
            public void entryCreated(Path child) {
                try {
                    addAllCustomFromPath(child);
                } catch (IOException e) {
                    log.log(Level.INFO, "Could not add custom paths from: " + child, e);
                }
            }

            @Override
            public void entryModified(Path child) {
                try {
                    addAllCustomFromPath(child);
                } catch (IOException e) {
                    log.log(Level.INFO, "Could not add custom paths from: " + child, e);
                }
            };

            @Override
            public void entryDeleted(Path child) {
                removeCustomPath(child);
            }
        };
    }

    private void addRow(CustomContent e) {
        int index = customContent.indexOf(e);
        if (index >= 0) {
            customContent.set(index, e);
            fireTableRowsUpdated(index, index);
        } else {
            int row = getRowCount();
            customContent.add(e);
            fireTableRowsInserted(row, row);
        }
    }

    private void addRows(Collection<CustomContent> c) {
        for (CustomContent e : c) {
            addRow(e);
        }
    }

    private void insertRow(int index, CustomContent e) {
        customContent.add(index, e);
        fireTableRowsInserted(index, index);
    }

    private void removeRow(CustomContent e) {
        int index = customContent.indexOf(e);
        if (index >= 0) {
            customContent.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    private void removeRows(Collection<CustomContent> c) {
        for (CustomContent e : c) {
            removeRow(e);
        }
    }

    private Map<String, CustomContent> getCustomFilesFrom(Path path) {
        Map<String, CustomContent> map = new LinkedHashMap<>();
        List<String> contents = getContentsList(path);
        for (String file : contents) {
            String key = getKey(path, file);
            if (!map.containsKey(key)) {
                map.put(key, new CustomContent(path, getTag(file)));
            }
            map.get(key).getFiles().add(file);
        }
        return map;
    }

    private List<CustomContent> scanForCustomFiles(Path root) throws IOException {
        Map<String, CustomContent> newCustomFiles = new LinkedHashMap<>();
        if (Files.isDirectory(root)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
                for (Path path : stream) {
                    newCustomFiles.putAll(getCustomFilesFrom(path));
                }
            }
        } else {
            newCustomFiles.putAll(getCustomFilesFrom(root));
        }
        return new ArrayList<>(newCustomFiles.values());
    }

    public List<CustomContent> addAllCustomFromPath(Path root) throws IOException {
        List<CustomContent> files = scanForCustomFiles(root);
        addRows(files);
        return files;
    }

    public List<CustomContent> removeCustomPath(Path path) {
        List<CustomContent> toRemove = new ArrayList<>();
        for (CustomContent file : customContent) {
            if (file.getPath().equals(path.getFileName().toString())) {
                toRemove.add(file);
            }
        }
        removeRows(toRemove);
        return toRemove;
    }

    public List<String> getContentsList(Path path) {
        return getContentsList(path, Integer.MAX_VALUE);
    }

    public List<String> getContentsList(Path path, int maxDepth) {
        if (path.toString().endsWith(".vpk")) {
            VpkTree vpk = VpkTree.create(path, profilesManager);
            ListVpkFilesVisitor visitor = new ListVpkFilesVisitor();
            TreeWalker.walk(vpk, maxDepth, visitor);
            return visitor.getFiles();
        } else if (Files.isDirectory(path)) {
            ListFilesVisitor visitor = new ListFilesVisitor(path);
            try {
                Set<FileVisitOption> set = new HashSet<>();
                set.add(FileVisitOption.FOLLOW_LINKS);
                Files.walkFileTree(path, set, maxDepth, visitor);
                return visitor.getFiles();
            } catch (IllegalArgumentException | IOException e) {
                log.log(Level.FINE, "Could not walk through the file tree", e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void onRefresh(Profiles profiles) {
        profilesManager = profiles;
        customContent = profiles.getProfile().getCustomContent();
        fireTableDataChanged();
    }

    private String getTag(String file) {
        for (String start : startsWithTagMap.keySet()) {
            if (file.startsWith(start)) {
                return startsWithTagMap.get(start);
            }
        }
        return null;
    }

    public String getKey(Path path, String file) {
        String tag = getTag(file);
        if (tag != null) {
            return "[" + tag + "] " + path.getFileName();
        }
        return path.getFileName().toString();
    }

}
