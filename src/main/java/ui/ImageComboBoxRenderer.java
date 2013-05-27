
package ui;

import java.awt.Component;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ImageComboBoxRenderer extends JLabel implements ListCellRenderer<String> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Map<String, ImageIcon> map;

    public ImageComboBoxRenderer(Map<String, ImageIcon> map) {
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        this.map = map;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        ImageIcon icon = map.get((String) value);
        String pet = (String) value;
        setIcon(icon);
        setText(pet);
        setFont(list.getFont());
        return this;
    }

}
