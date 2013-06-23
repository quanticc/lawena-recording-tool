
package lwrt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import lwrt.CustomPath.PathContents;

public class CustomPathList extends AbstractTableModel {

    public enum Column {
        SELECTED, PATH;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger("lawena");
    private static final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

        @Override
        public boolean accept(Path entry) throws IOException {
            return (Files.isDirectory(entry) || entry.toString().endsWith(".vpk"))
                    && !ignoredPaths.contains(entry);
        }
    };

    private static final Map<Path, CustomPath> defaultPaths = new LinkedHashMap<>();
    private static final List<Path> ignoredPaths = new ArrayList<>();

    {
        List<CustomPath> list = new ArrayList<>();
        list.add(new CustomPath(Paths.get("custom/default_cfgs.vpk"), "default_cfgs.vpk", EnumSet
                .of(PathContents.READONLY)));
        list.add(new CustomPath(Paths.get("custom/no_announcer_voices.vpk"),
                "Disable announcer voices"));
        list.add(new CustomPath(Paths.get("custom/no_applause_sounds.vpk"),
                "Disable applause sounds"));
        list.add(new CustomPath(Paths.get("custom/no_domination_sounds.vpk"),
                "Disable domination/revenge sounds"));
        list.add(new CustomPath(Paths.get("custom/pldx_particles.vpk"),
                "Enable enhanced particles"));
        for (CustomPath path : list) {
            path.getContents().add(PathContents.REQUIRED);
            path.setSelected(true);
            defaultPaths.put(path.getPath(), path);
        }
        ignoredPaths.add(Paths.get("custom/skybox.vpk"));
    }

    private List<CustomPath> list = new ArrayList<>();

    public List<CustomPath> getList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        CustomPath cp = list.get(row);
        return !cp.getContents().contains(PathContents.READONLY)
                && column == Column.SELECTED.ordinal();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (Column.values()[column]) {
            case SELECTED:
                return Boolean.class;
            case PATH:
                return CustomPath.class;
            default:
                return String.class;
        }
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        CustomPath cp = list.get(row);
        switch (Column.values()[column]) {
            case SELECTED:
                return cp.isSelected();
            case PATH:
                return cp;
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        CustomPath cp = list.get(row);
        switch (Column.values()[column]) {
            case SELECTED:
                cp.setSelected((boolean) value);
            default:
        }
    }

    private void insertRow(CustomPath e) {
        int row = getRowCount();
        list.add(e);
        fireTableRowsInserted(row, row);
    }

    public CustomPath getPath(int index) {
        return list.get(index);
    }

    public void clear() {
        int size = list.size();
        if (size > 0) {
            list.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public void addPaths(Path... dirs) {
        for (Path dir : dirs) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
                for (Path path : stream) {
                    CustomPath cp = defaultPaths.get(path);
                    if (cp == null) {
                        cp = new CustomPath(path);
                    }
                    insertRow(cp);
                }
            } catch (IOException e) {
                log.log(Level.FINE, "Problem while loading custom paths", e);
            }
        }
    }

    // for each "required" custom path, extract it from jar and add it
    // in case the user deleted the custom vpks/folders included with lawena
    public void validateRequired() {
        for (CustomPath cp : defaultPaths.values()) {
            if (cp.getContents().contains(PathContents.REQUIRED) && !list.contains(cp)) {
                Path filename = cp.getPath().getFileName();
                Path destdir = Paths.get("custom");
                unpackFileFromJar(Paths.get("lwrtvpks.jar"), filename.toString(), destdir);
                if (Files.exists(destdir.resolve(filename))) {
                    insertRow(cp);
                }
            }
        }
    }

    private void unpackFileFromJar(Path jarpath, String name, Path destpath) {
        JarFile jar;
        try {
            jar = new JarFile(jarpath.toFile());
            JarEntry entry = jar.getJarEntry(name);
            if (entry != null) {
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(destpath.resolve(name).toFile());
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            } else {
                log.warning("File " + name + " does not exist in " + jarpath);
            }
            jar.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not unpack file " + name + " from jar: " + jarpath, e);
        }
    }

}
