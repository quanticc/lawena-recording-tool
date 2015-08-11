package com.github.lawena.tf2;

import com.github.lawena.exts.TagProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ro.fortsoft.pf4j.Extension;

@Extension
public class Tagger implements TagProvider {

    @Override
    public String getName() {
        return "tf2-tags";
    }

    @Override
    public Collection<String> tag(Collection<String> contents) {
        Set<String> tags = new HashSet<>();
        boolean hasResourceUiFolder = false;
        boolean hasScriptsFolder = false;
        for (String content : contents) {
            if (content.startsWith("resource/ui")) { //$NON-NLS-1$
                hasResourceUiFolder = true;
            } else if (content.startsWith("scripts/")) { //$NON-NLS-1$
                hasScriptsFolder = true;
            } else if (content.startsWith("cfg/") && content.endsWith(".cfg")) { //$NON-NLS-1$ //$NON-NLS-2$
                tags.add("config"); //$NON-NLS-1$
            } else if (content.startsWith("materials/skybox/")) { //$NON-NLS-1$
                tags.add("skybox"); //$NON-NLS-1$
            }
        }
        if (hasResourceUiFolder && hasScriptsFolder) {
            tags.add("hud"); //$NON-NLS-1$
        }
        return tags;
    }
}
