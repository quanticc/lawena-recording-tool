
package vdm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

public class TickTableModel extends AbstractTableModel {

    private static final Logger log = Logger.getLogger("lawena");
    private static final long serialVersionUID = 1L;

    public enum Column {
        DEMO("Demo name"), START("Starting Tick"), END("Ending Tick");

        private String columnName;

        Column(String columnName) {
            this.columnName = columnName;
        }
    }

    private List<Tick> list = new ArrayList<>();

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Tick tick = list.get(rowIndex);
        Column c = Column.values()[columnIndex];
        switch (c) {
            case DEMO:
                return tick.getDemoname();
            case START:
                return tick.getStart();
            case END:
                return tick.getEnd();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Tick tick = list.get(rowIndex);
        Column c = Column.values()[columnIndex];
        switch (c) {
            case START:
                try {
                    tick.setStart(Integer.parseInt(aValue.toString()));
                    fireTableCellUpdated(rowIndex, columnIndex);
                } catch (NumberFormatException e) {
                    log.fine("Cannot set start tick, bad numeric format in: " + aValue);
                }
                break;
            case END:
                try {
                    tick.setEnd(Integer.parseInt(aValue.toString()));
                    fireTableCellUpdated(rowIndex, columnIndex);
                } catch (NumberFormatException e) {
                    log.fine("Cannot set end tick, bad numeric format in: " + aValue);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != Column.DEMO.ordinal();
    }

    @Override
    public String getColumnName(int column) {
        return Column.values()[column].columnName;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Column c = Column.values()[columnIndex];
        switch (c) {
            case START:
            case END:
                return Integer.class;
            default:
                return String.class;
        }
    }

    public void clear() {
        int rowsCleared = list.size();
        list.clear();
        fireTableRowsDeleted(0, rowsCleared - 1);
    }

    public void addTick(Tick e) {
        int row = getRowCount();
        list.add(e);
        fireTableRowsInserted(row, row);
    }

    public void removeTick(int index) {
        list.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public List<Tick> getTickList() {
        return list;
    }

}
