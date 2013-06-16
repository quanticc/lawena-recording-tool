
package ui;

import java.awt.Component;
import java.nio.file.Path;
import java.util.EnumSet;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import lwrt.CustomPath;
import lwrt.CustomPath.PathContents;
import lwrt.SettingsManager;

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
        EnumSet<PathContents> contents = cp.getContents();
        StringBuilder sb = new StringBuilder();
        sb.append("<html>Filename: <b>");
        sb.append(cp.getPath().getFileName());
        sb.append("</b><br>Location: ");
        sb.append((path.startsWith(tfpath) ? "[tf] " + tfpath.relativize(path) : path).toString());
        if (contents.contains(PathContents.HUD)) {
            sb.append("<br><font color='#ff0000'>This resource might contain HUD files that can conflict with the selected one on the left.</font>");
            sb.append("<br>To avoid this, select the \"Custom\" HUD option on the left, or deselect this resource.");
        }
        if (contents.contains(PathContents.CONFIG)) {
            sb.append("<br><font color='#ff0000'>This resource might contain CFG files that can conflict with those included with the tool.</font>");
            sb.append("<br>To avoid this, deselect this resource from this list.");
        }
        if (contents.contains(PathContents.SKYBOX)) {
            sb.append("<br><font color='#ff0000'>This resource might contain skybox files that can conflict with those included with the tool.</font>");
            sb.append("<br>To avoid this, select the \"Default\" skybox option on the left, or deselect this resource.");
        }
        setToolTipText(sb.toString());
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
