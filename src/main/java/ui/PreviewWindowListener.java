
package ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class PreviewWindowListener extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent e) {
        JFrame frame = (JFrame) (e.getWindow());
        frame.dispose();
    }

}
