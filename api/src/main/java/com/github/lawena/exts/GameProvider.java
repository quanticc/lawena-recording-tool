package com.github.lawena.exts;

import com.github.lawena.game.SourceGame;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Extension point to provide a way to add {@link SourceGame} definitions to the application.
 *
 * @author Ivan
 */
public interface GameProvider extends ExtensionPoint {

    /**
     * Obtain the {@link GameProvider} defined by this extension.
     *
     * @return a descriptor used to construct a launch profile for a Source game and add it to the
     * main application.
     */
    SourceGame getGame();

}
