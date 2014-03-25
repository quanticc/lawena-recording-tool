package com.github.iabarca.lwrt.tree;

public interface Visitor<T> {
    
    public VisitResult visitEnter(Tree<T> composite);
    
    public VisitResult visitLeave(Tree<T> composite);
    
    public VisitResult visit(Tree<T> leaf);

}
