package com.github.lawena.vdm;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.Messages;

public class TickTableModel extends AbstractTableModel {

  private static final Logger log = LoggerFactory.getLogger(TickTableModel.class);
  private static final long serialVersionUID = 1L;

  public enum Column {
    DEMO(Messages.getString("TickTableModel.demoName")), //$NON-NLS-1$
    START(Messages.getString("TickTableModel.startTick")), //$NON-NLS-1$
    END(Messages.getString("TickTableModel.endTick")); //$NON-NLS-1$

    String columnName;

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
          log.warn("Cannot set start tick, bad numeric format in: {}", aValue); //$NON-NLS-1$
        }
        break;
      case END:
        try {
          tick.setEnd(Integer.parseInt(aValue.toString()));
          fireTableCellUpdated(rowIndex, columnIndex);
        } catch (NumberFormatException e) {
          log.warn("Cannot set end tick, bad numeric format in: {}", aValue); //$NON-NLS-1$
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

}
