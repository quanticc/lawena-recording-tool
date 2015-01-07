package com.github.lawena.app;

import static com.github.lawena.util.Util.toPath;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.Messages;
import com.github.lawena.app.model.Settings;
import com.github.lawena.profile.Key;
import com.github.lawena.ui.SegmentsDialog;

public class Segments {

  static final Logger log = LoggerFactory.getLogger(Segments.class);

  SegmentsDialog view;
  Lawena parent;
  private Settings settings;

  public Segments(Lawena parent) {
    this.parent = parent;
    this.settings = parent.getModel().getSettings();
  }

  public void start() {
    if (view == null) {
      init();
    }
    DefaultTableModel tmodel = (DefaultTableModel) view.getTableSegments().getModel();
    tmodel.setRowCount(0);
    List<String> segs = getExistingSegments();
    if (segs.isEmpty()) {
      JOptionPane.showMessageDialog(view, Messages.getString("Segments.noSegmentsToDelete"), //$NON-NLS-1$
          Messages.getString("Segments.noSegmentsToDeleteTitle"), //$NON-NLS-1$
          JOptionPane.INFORMATION_MESSAGE);
    } else {
      for (String seg : segs) {
        tmodel.addRow(new Object[] {false, seg});
      }
      view.setVisible(true);
    }
  }

  private List<String> getExistingSegments() {
    List<String> existingSegments = new ArrayList<>();
    Path recPath = toPath(Key.recordingPath.getValue(settings));
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(recPath, "*.wav")) { //$NON-NLS-1$
      for (Path path : stream) {
        String segname = path.getFileName().toString();
        String key = segname.substring(0, segname.indexOf("_")); //$NON-NLS-1$
        if (!existingSegments.contains(key))
          existingSegments.add(key);
      }
    } catch (NoSuchFileException e) {
      // TODO: add a check for the reparse point (junction) to confirm it's SrcDemo2
      log.info("Could not scan for existing segments. Is SrcDemo2 running?"); //$NON-NLS-1$
    } catch (IOException e) {
      log.warn("Problem while scanning movie folder", e); //$NON-NLS-1$
    }
    return existingSegments;
  }

  private void init() {
    view = new SegmentsDialog();
    view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    view.setModalityType(ModalityType.APPLICATION_MODAL);
    DefaultTableModel dtm = new DefaultTableModel(0, 2) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isCellEditable(int row, int column) {
        return column == 0;
      }

      @Override
      public java.lang.Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
      }

      @Override
      public String getColumnName(int column) {
        return column == 0 ? "" : Messages.getString("Segments.segmentColumnName"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };
    final JTable tableSegments = view.getTableSegments();
    view.getOkButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        List<String> selected = new ArrayList<>();
        int selectCount = 0;
        for (int i = 0; i < tableSegments.getRowCount(); i++) {
          if ((boolean) tableSegments.getValueAt(i, 0)) {
            selectCount++;
            selected.add((String) tableSegments.getValueAt(i, 1));
          }
        }
        if (selectCount > 0) {
          parent.clearSegmentFiles(selected);
        } else {
          log.info("No segments selected to remove"); //$NON-NLS-1$
        }
        view.setVisible(false);
      }
    });
    view.getCancelButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        view.setVisible(false);
      }
    });
    tableSegments.setModel(dtm);
    tableSegments.getColumnModel().getColumn(0).setMaxWidth(20);
  }

}
