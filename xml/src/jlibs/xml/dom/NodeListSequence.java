/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.dom;

import jlibs.core.graph.sequences.AbstractSequence;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Santhosh Kumar T
 */
public class NodeListSequence extends AbstractSequence<Node>{
    private NodeList nodeList;

    public NodeListSequence(NodeList nodeList){
        this.nodeList = nodeList;
    }


    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    private int i;

    @Override
    protected Node findNext(){
        return nodeList.item(++i);
    }

    @Override
    public NodeListSequence copy(){
        return new NodeListSequence(nodeList);
    }
}
