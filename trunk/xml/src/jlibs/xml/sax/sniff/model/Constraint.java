package jlibs.xml.sax.sniff.model;

/**
 * @author Santhosh Kumar T
 */
public class Constraint extends Node{
    public Constraint(Node node){
        parent = node.parent;
        root = node.root;
        node.constraints.add(this);
    }
}
