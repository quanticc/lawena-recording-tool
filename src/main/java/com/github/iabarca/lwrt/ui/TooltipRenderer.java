
package com.github.iabarca.lwrt.ui;

import java.awt.Component;
import java.nio.file.Path;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.github.iabarca.lwrt.lwrt.CustomPath;
import com.github.iabarca.lwrt.lwrt.SettingsManager;

public class TooltipRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    private SettingsManager cfg;

    public TooltipRenderer(SettingsManager cfg) {
        this.cfg = cfg;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus,
            int row, int column) {
        CustomPath cp = (CustomPath) value;
        Path tfpath = cfg.getTfPath();
        Path path = cp.getPath();
        StringBuilder sb = new StringBuilder();
        sb.append("<html>Filename: <b>");
        sb.append(cp.getPath().getFileName());
        sb.append("</b><br>Location: ");
        sb.append((path.startsWith(tfpath) ? "TF2 Path: " + tfpath.relativize(path) : path)
                .toString());
        setToolTipText(sb.toString());
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
