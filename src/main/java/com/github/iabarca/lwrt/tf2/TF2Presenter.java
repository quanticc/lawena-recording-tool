
package com.github.iabarca.lwrt.tf2;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import com.github.iabarca.lwrt.custom.CustomContent;
import com.github.iabarca.lwrt.lwrt.Lwrt;
import com.github.iabarca.lwrt.lwrt.LwrtPresenter;
import com.github.iabarca.lwrt.lwrt.ParticlesManager;
import com.github.iabarca.lwrt.managers.Custom;
import com.github.iabarca.lwrt.managers.Profiles;
import com.github.iabarca.lwrt.ui.AboutDialog;
import com.github.iabarca.lwrt.ui.LwrtView;
import com.github.iabarca.lwrt.util.StartLogger;
import com.github.iabarca.lwrt.util.WatchDir;
import com.github.iabarca.lwrt.vdm.DemoEditor;

public class TF2Presenter implements LwrtPresenter {

    private static final Logger log = Logger.getLogger("lawena");

    private Lwrt model;
    private LwrtView view;

    private Profiles profiles;

    private Custom customFiles;
    private DemoEditor demos;
    private ParticlesManager particles;

    private WatchDir watchService;
    private Thread watchThread;
    private AboutDialog dialog;

    public TF2Presenter() {
        startCustomFolderWatcher();
    }

    public void setModel(Lwrt m) {
        model = m;
        profiles = model.getProfiles();
        customFiles = new Custom(profiles);
        demos = new DemoEditor(profiles, watchService);
        particles = new ParticlesManager(profiles);
    }

    private void startCustomFolderWatcher() {
        try {
            watchService = new WatchDir(false);
            watchService.register(Paths.get("custom"), customFiles.getWatchAction());
        } catch (IOException e) {
            log.log(Level.FINE, "Could not register directory with watcher", e);
        }
        watchThread = new Thread(new Runnable() {

            @Override
            public void run() {
                watchService.processEvents();
            }
        }, "FolderWatcher");
        watchThread.setDaemon(true);
    }

    @Override
    public void startUI() {
        view = new LwrtView();
        // TODO: Move to non-reflection call
        new StartLogger("lawena").toTextComponent(
                Level.parse(profiles.getProfile().getString("uiLogLevel")), view.getTextAreaLog());
        new StartLogger("status").toLabel(Level.FINE, view.getLblStatus());

        log.fine("Lawena Recording Tool " + profiles.getArguments().get("version") + " build "
                + profiles.getArguments().get("build"));
        log.fine("TF2 path: " + profiles.getProfile().getGamePath().toAbsolutePath());
        log.fine("Movie path: " + profiles.getProfile().getMoviePath().toAbsolutePath());
        log.fine("Lawena path: " + Paths.get(".").getParent().toAbsolutePath());

        view.setTitle("Lawena Recording Tool " + profiles.getArguments().get("shortVersion"));
        try {
            view.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("ui/tf2.png"))
                    .getImage());
        } catch (Exception e) {
            log.log(Level.FINER, "Could not load icon", e);
        }

        profiles.addProfileListener(this);

        LwrtListeners actions = new LwrtListeners(this);
        LwrtTasks tasks = new LwrtTasks(this);

        view.addWindowListener(actions.getCloseWindowListener());
        view.getMntmAbout().addActionListener(actions.getAboutListener());

        view.getTableCustomContent().setModel(customFiles);
        view.getTableCustomContent().getColumnModel().getColumn(0).setMaxWidth(20);
        view.getTableCustomContent().getColumnModel().getColumn(2).setMaxWidth(50);
        TableRowSorter<Custom> sorter = new TableRowSorter<>(customFiles);
        view.getTableCustomContent().setRowSorter(sorter);

        // TODO: renew filtering system
        RowFilter<? super Custom, ? super Integer> filter = new RowFilter<Custom, Object>() {
            public boolean include(Entry<? extends Custom, ? extends Object> entry) {
                CustomContent c = (CustomContent) entry.getValue(Custom.Column.NAME
                        .ordinal());
                // return !cf.getContents().contains(PathContents.READONLY);
                return true;
            }
        };
        sorter.setRowFilter(filter);

        tasks.getPathScanTask().execute();
        
        // TODO: adapt skybox preview loading task to v5 system
        tasks.getConfigureSkyboxesTask().execute();

        // updateUISettings()

        view.getMntmChangeTfDirectory().addActionListener(actions.getChangeGamePathListener());
        view.getMntmChangeMovieDirectory().addActionListener(actions.getChangeMoviePathListener());
        view.getMntmRevertToDefault().addActionListener(actions.getRevertToDefaultsListener());
        view.getMntmExit().addActionListener(actions.getSaveAndExitListener());
        view.getMntmSaveSettings().addActionListener(actions.getSaveSettingsListener());
        view.getBtnStartTf().addActionListener(actions.getStartGameListener());
        view.getMntmClearMovieFiles().addActionListener(actions.getClearMovieFilesListener());
        view.getMntmOpenMovieFolder().addActionListener(actions.getOpenMovieFolderListener());
        view.getMntmOpenCustomFolder().addActionListener(actions.getOpenCustomFolderListener());

        view.getTabbedPane().addTab("VDM", demos.start());
        view.getSideTabbedPane().addTab("Particles", particles.start());

        view.setVisible(true);
    }

    @Override
    public void onRefresh(Profiles profiles) {

    }

    @Override
    public void hide() {
        view.setVisible(false);
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public void restoreGameFiles() {
        model.getFiles().restoreAll();
    }

    @Override
    public void showAboutDialog() {
        if (dialog == null) {
            String version = profiles.getArguments().get("version");
            String build = profiles.getArguments().get("build");
            dialog = new AboutDialog(version, build);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setModalityType(ModalityType.APPLICATION_MODAL);
            dialog.getBtnUpdater().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    model.getUpdatesManager().showSwitchUpdateChannelDialog();
                }
            });
        }
        dialog.setVisible(true);
    }

    @Override
    public void saveAndExit() {
        // TODO: make this call not needed
        // saveSettings();
        hide();
        Profiles profiles = getProfiles();
        try {
            profiles.saveProfiles();
        } catch (IOException e) {
            log.log(Level.INFO, "", e);
        }
        if (profiles.getInterface().isGameRunning()) {
            restoreGameFiles();
        }
        System.exit(0);
    }

}
