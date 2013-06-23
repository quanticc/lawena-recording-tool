
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
        if (cp.isSelected()) {
            if (contents.contains(PathContents.HUD)) {
                sb.append("<br><font color='#ff0000'>Possible HUD file conflict.</font>");
                sb.append("<br>If you are not sure of the contents you can:");
                sb.append("<br>Choose <b>\"Custom\"</b> HUD option");
                sb.append("<br>or <b>Deselect</b> this custom resource.");
            }
            if (contents.contains(PathContents.CONFIG)) {
                sb.append("<br><font color='#ff0000'>Possible CFG file conflict.</font>");
                sb.append("<br><b>Deselect</b> this resource if you are not sure.");
            }
            if (contents.contains(PathContents.SKYBOX)) {
                sb.append("<br><font color='#ff0000'>Possible Skybox file conflict.</font>");
                sb.append("<br>If you are not sure of the contents you can:");
                sb.append("<br>Choose <b>\"Default\"</b> Skybox option");
                sb.append("<br>or <b>Deselect</b> this custom resource.");
            }
        }
        setToolTipText(sb.toString());
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
