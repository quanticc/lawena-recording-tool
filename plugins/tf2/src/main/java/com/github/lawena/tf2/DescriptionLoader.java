package com.github.lawena.tf2;

import com.github.lawena.exts.DescriptorProvider;
import com.github.lawena.game.GameDescription;

import ro.fortsoft.pf4j.Extension;

@Extension
public class DescriptionLoader implements DescriptorProvider {

    @Override
    public final GameDescription getDescriptor() {
        return GameDescription.load(getClass().getResourceAsStream("tf2.json"));
    }
}
