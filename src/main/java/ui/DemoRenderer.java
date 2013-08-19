
package ui;

import vdm.Demo;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DemoRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
    private static final Color selectionBlue = new Color(0x3399FF);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus,
            int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);
        Demo demo = (Demo) value;
        if (!demo.getStreaks().isEmpty()) {
            if (isSelected) {
                c.setForeground(Color.BLUE);
                c.setBackground(selectionBlue);
            } else {
                c.setForeground(Color.BLUE);
                c.setBackground(Color.WHITE);
            }
        } else {
            if (isSelected) {
                c.setForeground(Color.WHITE);
                c.setBackground(selectionBlue);
            } else {
                c.setForeground(Color.BLACK);
                c.setBackground(Color.WHITE);
            }
        }
        return c;
    }

}
