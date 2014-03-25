
package com.github.iabarca.lwrt.tree;

public class TreeWalker<T> {

    public static <T> Tree<T> walk(Tree<T> start, Visitor<T> visitor) {
        return walk(start, Integer.MAX_VALUE, visitor);
    }

    public static <T> Tree<T> walk(Tree<T> start, int maxDepth, Visitor<T> visitor) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("'maxDepth' is negative");
        }
        new TreeWalker<T>(start, visitor, maxDepth).walk();
        return start;
    }

    private Tree<T> start;
    private Visitor<T> visitor;
    private int maxDepth;

    public TreeWalker(Tree<T> start, Visitor<T> visitor, int maxDepth) {
        this.start = start;
        this.visitor = visitor;
        this.maxDepth = maxDepth;
    }

    public void walk() {
        walk(start, 0);
    }

    private VisitResult walk(Tree<T> tree, int depth) {
        if (depth >= maxDepth || tree.isLeaf()) {
            return visitor.visit(tree);
        }
        VisitResult result = visitor.visitEnter(tree);
        if (result != VisitResult.CONTINUE) {
            return result;
        }
        for (Tree<T> child : tree.getChildren()) {
            result = walk(child, depth + 1);
            if (result == null || result == VisitResult.TERMINATE) {
                return result;
            }
            if (result == VisitResult.SKIP_SIBLINGS) {
                break;
            }
        }
        return visitor.visitLeave(tree);
    }

}
