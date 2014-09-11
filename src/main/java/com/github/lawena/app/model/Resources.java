package com.github.lawena.app.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtSettings;
import com.github.lawena.os.OSInterface;

public class Resources extends AbstractTableModel {

  public enum Column {
    SELECTED, PATH, CONTENTS;
  }

  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(Resources.class);

  private List<Resource> list = new ArrayList<>();
  private LwrtSettings cfg;
  private OSInterface os;

  public Resources(LwrtSettings cfg, OSInterface os) {
    this.cfg = cfg;
    this.os = os;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    Resource resource = list.get(row);
    Set<String> tags = resource.getTags();
    // only the enabled column is editable
    // editable tags: (no tag), config
    // multi-tagged resources can't be enabled, user will be notified
    return column == Column.SELECTED.ordinal()
        && (tags.isEmpty() || tags.contains(Resource.CONFIG));
  }

  @Override
  public Class<?> getColumnClass(int column) {
    switch (Column.values()[column]) {
      case SELECTED:
        return Boolean.class;
      case PATH:
        return Resource.class;
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
  public String getColumnName(int column) {
    switch (Column.values()[column]) {
      case CONTENTS:
        return "Type";
      case SELECTED:
        return "";
      case PATH:
        return "Resource";
      default:
        return "";
    }
  }

  @Override
  public Object getValueAt(int row, int column) {
    Resource r = list.get(row);
    switch (Column.values()[column]) {
      case CONTENTS:
        return cleanCollection(r.getTags());
      case SELECTED:
        return r.isEnabled();
      case PATH:
        return r;
      default:
        return "";
    }
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    Resource r = list.get(row);
    switch (Column.values()[column]) {
      case SELECTED:
        r.setEnabled((boolean) value);
        fireTableCellUpdated(row, column);
        return;
      default:
    }
  }

  private String cleanCollection(Collection<?> l) {
    return l.toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\\+");
  }

}
