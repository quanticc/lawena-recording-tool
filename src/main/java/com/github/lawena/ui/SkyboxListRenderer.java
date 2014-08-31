package com.github.lawena.ui;

import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

public class SkyboxListRenderer extends DefaultListCellRenderer {

  private static final long serialVersionUID = 1L;

  private Map<String, ImageIcon> skyboxes;

  public SkyboxListRenderer(Map<String, ImageIcon> skyboxes) {
    this.skyboxes = skyboxes;
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index,
      boolean isSelected, boolean cellHasFocus) {
    JLabel label =
        (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    ImageIcon icon = skyboxes.get(value);
    label.setIcon(icon);

    return label;
  }

}
