package com.github.lawena.ui;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lawena.app.Tasks;

public class TableDropTarget extends DropTarget {

  private static final Logger log = LoggerFactory.getLogger(TableDropTarget.class);

  private static final long serialVersionUID = 1L;

  private Tasks tasks;

  public TableDropTarget(Tasks tasks) {
    this.tasks = tasks;
  }

  @Override
  public synchronized void dragOver(DropTargetDragEvent dtde) {
    JTable table = (JTable) getComponent();
    Point point = dtde.getLocation();
    int row = table.rowAtPoint(point);
    if (row < 0) {
      table.clearSelection();
    } else {
      table.setRowSelectionInterval(row, row);
    }
    dtde.acceptDrag(DnDConstants.ACTION_COPY);
  }

  @Override
  public synchronized void drop(DropTargetDropEvent dtde) {
    JTable table = (JTable) getComponent();
    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      Transferable t = dtde.getTransferable();
      List<?> fileList = null;
      try {
        fileList = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
        if (fileList.size() > 0) {
          table.clearSelection();
          for (Object value : fileList) {
            if (value instanceof File) {
              File f = (File) value;
              log.info("Attempting to copy " + f.toPath());
              tasks.new PathCopyTask(f.toPath()).execute();
            }
          }
        }
      } catch (UnsupportedFlavorException e) {
        log.warn("Drag and drop operation failed", e);
      } catch (IOException e) {
        log.warn("Drag and drop operation failed", e);
      }
    } else {
      dtde.rejectDrop();
    }
  }

}
