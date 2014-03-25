
package com.github.iabarca.lwrt.lwrt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.github.iabarca.lwrt.managers.Profiles;
import com.github.iabarca.lwrt.tf2.TF2Presenter;
import com.github.iabarca.lwrt.util.Updates;

public class Lwrt {

    private static final Logger log = Logger.getLogger("lawena");

    private LwrtPresenter presenter;

    private Profiles profiles;
    private FileManager files;
    private MovieManager movies;
    private Updates updates;

    public Lwrt() {

    }

    public void setProfiles(Profiles manager) {
        // Save older profiles data in case it is needed
        if (profiles != null) {
            try {
                profiles.saveProfiles();
            } catch (IOException e) {
                log.log(Level.INFO, "Problem while saving profiles to a file", e);
            }
        }
        if (files != null) {
            files.restoreAll();
        }

        profiles = manager;
        profiles.validateGamePath();
        profiles.validateMoviePath();
        try {
            profiles.saveProfiles();
        } catch (IOException e) {
            log.log(Level.INFO, "Problem while saving profiles to a file", e);
        }
        files = new FileManager(profiles);
        files.restoreAll();

        // TODO: Decouple this
        if (profiles.getGame().getShortName().equals("TF2")) {
            presenter = new TF2Presenter();
            presenter.setModel(this);
        }
    }

    public void startUI() {
        if (profiles == null) {
            throw new IllegalArgumentException("Profiles need to be set first");
        }
        log.finer("Starting User Interface");
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        presenter.startUI();
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Problem while running the GUI", e);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            log.log(Level.INFO, "GUI launch interrupted", e);
        }
    }

    public Profiles getProfiles() {
        return profiles;
    }

    public FileManager getFiles() {
        return files;
    }

    public MovieManager getMovies() {
        return movies;
    }

    public Updates getUpdatesManager() {
        return updates;
    }

    public void setUpdatesManager(Updates updates) {
        this.updates = updates;
    }

}
