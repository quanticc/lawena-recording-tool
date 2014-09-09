package com.github.lawena.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.github.lawena.util.Images;
import com.github.lawena.vdm.Demo;

public class DemoRenderer extends DefaultTableCellRenderer {

  private static final long serialVersionUID = 1L;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    JLabel c =
        (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
            column);
    Demo demo = (Demo) value;
    if (!demo.getStreaks().isEmpty()) {
      c.setIcon(Images.get("ui/fugue/film-bookmark.png"));
    } else {
      c.setIcon(Images.get("ui/fugue/film.png"));
    }
    return c;
  }
}
