
package com.github.iabarca.lwrt.tree;

import java.util.LinkedHashSet;
import java.util.Set;

public class Tree<T> {

    private final Set<Tree<T>> children = new LinkedHashSet<Tree<T>>();
    private final T data;

    public Tree(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
    
    public Tree<T> getTree(T data) {
        for (Tree<T> child : children) {
            if (data.equals(child.getData())) {
                return child;
            }
        }
        Tree<T> child = new Tree<T>(data);
        children.add(child);
        return child;
    }
    
    public Iterable<Tree<T>> getChildren() {
        return children;
    }
    
    public boolean isLeaf() {
        return children.isEmpty();
    }

}
