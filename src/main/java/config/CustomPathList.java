
package config;

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

    public void addPath(CustomPath e) {
        int row = getRowCount();
        list.add(e);
        fireTableRowsInserted(row, row);
    }

    public CustomPath getPath(int index) {
        return list.get(index);
    }

}
