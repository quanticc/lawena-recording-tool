package com.github.lawena.exts;

import com.github.lawena.game.GameDescription;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Extension point to provide a way to add {@link GameDescription} definitions to the application.
 *
 * @author Ivan
 */
public interface DescriptorProvider extends ExtensionPoint {

    /**
     * Obtain the {@link DescriptorProvider} defined by this extension.
     *
     * @return a descriptor used to construct a launch profile for a Source game and add it to the
     * main application.
     */
    GameDescription getDescriptor();

}
