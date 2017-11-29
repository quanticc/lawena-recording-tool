package vdm;

import vdm.Tick.Tick;
import vdm.Tick.TickFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TickTableModel extends AbstractTableModel {

    private static final Logger log = Logger.getLogger("lawena");
    private static final long serialVersionUID = 1L;
    private List<Tick> list = new ArrayList<>();
    private final Component parent;

    public TickTableModel(Component parent) {
        this.list = list;
        this.parent = parent;
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        Tick tick = list.get(rowIndex);
        Column c = Column.values()[columnIndex];
        switch (c) {
            case DEMO:
                return tick.getDemoName();
            case START:
                return tick.getStart();
            case END:
                return tick.getEnd();
            case TYPE:
                return tick.getSegment();
            case TEMPLATE:
                return tick.getTemplate();
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
                    list.set(rowIndex,
                        TickFactory.makeTick(
                            tick.getDemoFile(),
                            tick.getDemoName(),
                            Integer.parseInt(aValue.toString()),
                            tick.getEnd(),
                            tick.getSegment(),
                            tick.getTemplate()));
                    fireTableCellUpdated(rowIndex, columnIndex);
                } catch (NumberFormatException e) {
                    log.fine("Cannot set start tick, bad numeric format in: " + aValue);
                }
                break;
            case END:
                try {
                    list.set(rowIndex,
                        TickFactory.makeTick(
                            tick.getDemoFile(),
                            tick.getDemoName(),
                            tick.getStart(),
                            Integer.parseInt(aValue.toString()),
                            tick.getSegment(),
                            tick.getTemplate()));
                    fireTableCellUpdated(rowIndex, columnIndex);
                } catch (NumberFormatException e) {
                    log.fine("Cannot set end tick, bad numeric format in: " + aValue);
                }
                break;
            case TYPE:
                Tick newTick = TickFactory.makeTick(
                    tick.getDemoFile(),
                    tick.getDemoName(),
                    tick.getStart(),
                    tick.getEnd(),
                    (String) aValue,
                    tick.getTemplate());
                list.set(rowIndex, newTick);
                fireTableRowsUpdated(rowIndex, rowIndex);
                if (!newTick.isValid()) {
                    log.fine(String.format("Error row %d column %d: %s", rowIndex + 1, columnIndex + 1, newTick.getReason()));
                    JOptionPane.showMessageDialog(parent,
                        newTick.getReason(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                break;
            case TEMPLATE:
                list.set(rowIndex,
                    TickFactory.makeTick(
                        tick.getDemoFile(),
                        tick.getDemoName(),
                        tick.getStart(),
                        tick.getEnd(),
                        tick.getSegment(),
                        (String) aValue));
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == Column.TEMPLATE.ordinal()) {
            if (rowIndex >= 0 && rowIndex < list.size()) {
                Tick tick = list.get(rowIndex);
                return tick.getSegment().startsWith("exec");
            }
        }
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
        if (rowsCleared > 0) {
            list.clear();
            fireTableRowsDeleted(0, rowsCleared - 1);
        }
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

    public enum Column {
        DEMO("Demo name"), TYPE("Segment"), TEMPLATE("Template"), START("Starting Tick"), END(
            "Ending Tick");

        private String columnName;

        Column(String columnName) {
            this.columnName = columnName;
        }
    }

}
