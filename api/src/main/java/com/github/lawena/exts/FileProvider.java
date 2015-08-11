package com.github.lawena.exts;

import com.github.lawena.Controller;
import com.github.lawena.profile.Profile;
import com.github.lawena.util.LawenaException;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Extension that handles file replacing during launch procedure.
 */
public interface FileProvider extends ExtensionPoint {

    void init(Controller parent);

    String getName();

    void copyLaunchFiles(Profile profile) throws LawenaException;

}
