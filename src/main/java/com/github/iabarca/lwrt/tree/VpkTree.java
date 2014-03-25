
package com.github.iabarca.lwrt.tree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.iabarca.lwrt.managers.Profiles;

public class VpkTree extends Tree<VpkPath> {

    public static VpkTree create(Path vpkPath, Profiles profiles) {
        if (vpkPath.toString().endsWith(".vpk")) {
            VpkTree tree = new VpkTree(vpkPath);
            List<String> files = profiles.getInterface().getVpkContents(
                    profiles.getProfile().getGamePath(), vpkPath);
            for (String path : files) {
                Tree<VpkPath> node = tree;
                String[] splits = path.split("/");
                List<String> partials = new ArrayList<>();
                for (int i = 0; i < splits.length; i++) {                    
                    String partial = splits[0];
                    for (int j = 1; j <= i; j++) {
                        partial += "/" + splits[j];
                    }
                    partials.add(partial);
                }
                for (int i = 0; i < splits.length; i++) {
                    node = node.getTree(new VpkPath(vpkPath, partials.get(i), splits[i]));
                }
            }
            return tree;
        } else {
            throw new IllegalArgumentException("File must end with .vpk");
        }
    }

    private VpkTree(Path path) {
        super(new VpkPath(path));
    }

}
