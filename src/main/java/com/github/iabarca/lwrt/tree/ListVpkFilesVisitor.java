
package com.github.iabarca.lwrt.tree;

import java.util.ArrayList;
import java.util.List;

public class ListVpkFilesVisitor implements Visitor<VpkPath> {

    private List<String> files = new ArrayList<>();

    @Override
    public VisitResult visitEnter(Tree<VpkPath> composite) {
        return VisitResult.CONTINUE;
    }

    @Override
    public VisitResult visitLeave(Tree<VpkPath> composite) {
        return VisitResult.CONTINUE;
    }

    @Override
    public VisitResult visit(Tree<VpkPath> leaf) {
        files.add(leaf.getData().getPath());
        return VisitResult.CONTINUE;
    }

    public List<String> getFiles() {
        return files;
    }

}
