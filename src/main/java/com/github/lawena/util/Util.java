package com.github.lawena.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

  private static final Logger log = LoggerFactory.getLogger(Util.class);

  private Util() {}

  public static int compareCreationTime(File newgd, File curgd) {
    try {
      Path newp = newgd.toPath();
      BasicFileAttributes newAttributes = Files.readAttributes(newp, BasicFileAttributes.class);
      FileTime newCreationTime = newAttributes.creationTime();
      Path curp = curgd.toPath();
      BasicFileAttributes curAttributes = Files.readAttributes(curp, BasicFileAttributes.class);
      FileTime curCreationTime = curAttributes.creationTime();
      log.debug("Comparing creationTime: new={} cur={}", newCreationTime, curCreationTime);
      return newCreationTime.compareTo(curCreationTime);
    } catch (IOException e) {
      log.warn("Could not retrieve file creation time: " + e);
    }
    return 1;
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    try {
      byte[] buffer = new byte[4096];
      for (int read = 0; (read = in.read(buffer)) > 0;) {
        out.write(buffer, 0, read);
      }
    } catch (IOException e) {
      throw e;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public static String now(String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(Calendar.getInstance().getTime());
  }

  public static String getManifestString(String key, String defaultValue) {
    try (JarFile jar =
        new JarFile(
            new File(Util.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
      String value = jar.getManifest().getMainAttributes().getValue(key);
      return (value == null ? "no-gradle " + defaultValue : value);
    } catch (IOException | URISyntaxException e) {
    }
    return "no-jar " + defaultValue;
  }

  public static ImageIcon createPreviewIcon(String imageName) throws IOException {
    int size = 96;
    BufferedImage image;
    File input = new File(imageName);
    image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    image.createGraphics().drawImage(
        ImageIO.read(input).getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
    return new ImageIcon(image);
  }

  public static void registerValidation(JComboBox<String> combo, final String validationRegex,
      final JLabel label) {
    final JTextComponent tc = (JTextComponent) combo.getEditor().getEditorComponent();
    tc.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void changedUpdate(DocumentEvent e) {}

      @Override
      public void insertUpdate(DocumentEvent e) {
        validateInput();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        validateInput();
      }

      private void validateInput() {
        if (tc.getText().matches(validationRegex)) {
          label.setForeground(Color.BLACK);
        } else {
          label.setForeground(Color.RED);
        }
      }
    });
  }

  public static void selectComboItem(JComboBox<String> combo, String selectedValue,
      List<String> possibleValues) {
    if (possibleValues == null || possibleValues.isEmpty()) {
      combo.setSelectedItem(selectedValue);
    } else {
      int i = possibleValues.indexOf(selectedValue);
      if (i >= 0) {
        combo.setSelectedIndex(i);
      }
    }
  }

}
