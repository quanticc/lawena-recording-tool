
package lwrt;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class CustomPathList extends AbstractTableModel {

    public enum Column {
        SELECTED, PATH;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

        @Override
        public boolean accept(Path entry) throws IOException {
            return (Files.isDirectory(entry) || entry.toString().endsWith(".vpk"))
                    && !entry.getFileName().toString().equals("skybox.vpk");
        }
    };

    private List<CustomPath> list = new ArrayList<>();

    public List<CustomPath> getList() {
        return list;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == Column.SELECTED.ordinal();
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

    private void addPath(CustomPath e) {
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

    public void addAllFromPath(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
            for (Path path : stream) {
                CustomPath cp = new CustomPath(path);
                addPath(cp);
            }
        }
    }

}
