
package ui;

import config.CustomPath;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TooltipRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus,
            int row, int column) {
        CustomPath cp = (CustomPath) value;
        String str = "<html>Filename: <b>" + cp.getPath().getFileName() + "</b><br>Location: "
                + cp.getPath() + "<br>Files: " + cp.getContents().size();
        setToolTipText(str);
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
