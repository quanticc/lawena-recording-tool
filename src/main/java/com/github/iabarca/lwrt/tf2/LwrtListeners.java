
package com.github.iabarca.lwrt.tf2;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.swing.event.TableModelListener;

import com.github.iabarca.lwrt.lwrt.LwrtPresenter;
import com.github.iabarca.lwrt.lwrt.Lawena.PathCopyTask;
import com.github.iabarca.lwrt.managers.Profiles;
import com.github.iabarca.lwrt.ui.AboutDialog;

public class LwrtListeners {

    private LwrtPresenter presenter;

    public LwrtListeners(LwrtPresenter presenter) {
        this.presenter = presenter;
    }

    public WindowListener getCloseWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                presenter.saveAndExit();
            }
        };
    }

    public ActionListener getAboutListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                presenter.showAboutDialog();
            }
        };
    }

    public ActionListener getRevertToDefaultsListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getSaveAndExitListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getSaveSettingsListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getStartGameListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getClearMovieFilesListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getOpenMovieFolderListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getOpenCustomFolderListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getChangeGamePathListener() {
        // TODO Auto-generated method stub
        return null;
    }

    public ActionListener getChangeMoviePathListener() {
        // TODO Auto-generated method stub
        return null;
    }

}
