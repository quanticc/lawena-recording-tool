package ui;

import lwrt.CustomPath;
import lwrt.SettingsManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.nio.file.Path;

public class TooltipRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private SettingsManager cfg;

	public TooltipRenderer(SettingsManager cfg) {
		this.cfg = cfg;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		CustomPath cp = (CustomPath) value;
		Path tfpath = cfg.getTfPath();
		Path path = cp.getPath();
		String sb = "<html>Filename: <b>" + cp.getPath().getFileName() + "</b><br>Location: " +
				(path.startsWith(tfpath) ? "TF2 Path: " + tfpath.relativize(path) : path).toString();
		setToolTipText(sb);
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}
