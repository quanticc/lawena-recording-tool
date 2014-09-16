package com.github.lawena.ui;

import static com.github.lawena.util.Util.toPath;

import java.awt.Component;
import java.nio.file.Path;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.github.lawena.app.model.Resource;
import com.github.lawena.app.model.Settings;
import com.github.lawena.profile.Key;

public class TooltipRenderer extends DefaultTableCellRenderer {

  private static final long serialVersionUID = 1L;

  private Settings settings;

  public TooltipRenderer(Settings settings) {
    this.settings = settings;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    Resource cp = (Resource) value;
    Path tfpath = toPath(Key.gamePath.getValue(settings));
    Path path = cp.getPath();
    StringBuilder sb = new StringBuilder();
    sb.append("<html>Filename: <b>");
    sb.append(cp.getPath().getFileName());
    sb.append("</b><br>Location: ");
    sb.append((path.startsWith(tfpath) ? "TF2 Path: " + tfpath.relativize(path) : path).toString());
    setToolTipText(sb.toString());
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }

}
