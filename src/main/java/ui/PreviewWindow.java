
package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class PreviewWindow extends Panel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    BufferedImage image;
    JFrame frame;

    public PreviewWindow(String imageName) {
        try {
            File input = new File(imageName);
            image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
            image.createGraphics()
                    .drawImage(ImageIO.read(input).getScaledInstance(500, 500, Image.SCALE_SMOOTH),
                            0, 0, null);
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    public void show() {
        frame = new JFrame("Preview");
        frame.add(this);
        frame.addWindowListener(new PreviewWindowListener());
        frame.setSize(500, 500);
        frame.setVisible(true);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (dim.width - 500) / 2;
        int y = (dim.height - 500) / 2;

        frame.setLocation(x - 300, y);
        frame.setResizable(false);
    }

    public void destroy() {
        frame.dispose();
    }
}
