package com.github.lawena.app;

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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.model.LwrtSettings;
import com.github.lawena.ui.SegmentsDialog;

public class Segments {

  private static final Logger log = LoggerFactory.getLogger(Segments.class);

  private SegmentsDialog view;
  private Lawena parent;
  private LwrtSettings settings;

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
      JOptionPane.showMessageDialog(view, "There are no segments to delete", "Delete Segments",
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
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(settings.getMoviePath(), "*.wav")) {
      for (Path path : stream) {
        String segname = path.getFileName().toString();
        String key = segname.substring(0, segname.indexOf("_"));
        if (!existingSegments.contains(key))
          existingSegments.add(key);
      }
    } catch (NoSuchFileException e) {
      // TODO: add a check for the reparse point (junction) to confirm it's SrcDemo2
      log.info("Could not scan for existing segments. Is SrcDemo2 running?");
    } catch (IOException e) {
      log.warn("Problem while scanning movie folder", e);
    }
    return existingSegments;
  }

  private void init() {
    view = new SegmentsDialog();
    view.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
      };

      @Override
      public String getColumnName(int column) {
        return column == 0 ? "" : "Segment";
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
          log.info("No segments selected to remove");
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
