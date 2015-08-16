package com.github.lawena.tf2;

import com.github.lawena.exts.TagProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ro.fortsoft.pf4j.Extension;

@Extension
public class Tagger implements TagProvider {

    @Override
    public final String getName() {
        return "tf2-tags"; //NON-NLS
    }

    @Override
    public final Collection<String> tag(Collection<String> contents) {
        Set<String> tags = new HashSet<>();
        for (String content : contents) {
            String c = content.toLowerCase();
            if (c.equals("scripts/hudlayout.res")) { //NON-NLS
                tags.add("hud"); //NON-NLS
            } else if (c.startsWith("cfg/") && c.endsWith(".cfg")) { //NON-NLS
                tags.add("config"); //NON-NLS
            } else if (c.startsWith("materials/skybox/")) { //NON-NLS
                tags.add("skybox"); //NON-NLS
            }
        }
        return tags;
    }
}
