
package lawena;

import config.CLInterface;
import ui.ImageComboBoxRenderer;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;

public class Lawena {
    
    private static final Logger log = Logger.getLogger("lwrt");
    private static final int SKYBOX_PREVIEW_SIZE = 64;

    private LawenaView view;
    private CLInterface cl;

    public Lawena() {
        cl = new CLInterface();
    }

    public void start() {
        view = new LawenaView();
        loadSkyboxes(view.getCmbSkybox());
        view.setVisible(true);
    }

    private void loadSkyboxes(JComboBox<String> cmbSkybox) {
        Map<String, ImageIcon> skyboxMap = new HashMap<>();
        Vector<String> data = new Vector<>();
        // add default skybox option
        data.add("Default");
        skyboxMap.put("Default", null);
        // load skyboxes from folder
        Path dir = Paths.get("skybox");
        if (Files.exists(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*up.vtf")) {
                for (Path path : stream) {
                    String skybox = path.toFile().getName();
                    cl.generatePreview(skybox);
                    skybox = skybox.substring(0, skybox.indexOf("up.vtf"));
                    data.add(skybox);
                    ImageIcon icon = createPreviewIcon("skybox\\" + skybox + "up.png");
                    skyboxMap.put(skybox, icon);
                }
            } catch (IOException e) {
                log.log(Level.INFO, "Problem while loading skyboxes", e);
            }
        }
        
        cmbSkybox.setModel(new DefaultComboBoxModel<String>(data));
        ImageComboBoxRenderer renderer = new ImageComboBoxRenderer(skyboxMap);
        renderer.setPreferredSize(new Dimension(SKYBOX_PREVIEW_SIZE + 50, SKYBOX_PREVIEW_SIZE + 5));
        cmbSkybox.setRenderer(renderer);
        cmbSkybox.setMaximumRowCount(3);
    }

    static ImageIcon createPreviewIcon(String imageName) {
        int size = SKYBOX_PREVIEW_SIZE;
        BufferedImage image;
        File input = new File(imageName);
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        try {
            image.createGraphics()
                    .drawImage(ImageIO.read(input).getScaledInstance(size, size, Image.SCALE_SMOOTH),
                            0, 0, null);
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
