/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.dom;

import jlibs.core.graph.sequences.AbstractSequence;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Santhosh Kumar T
 */
public class NamedNodeMapSequence extends AbstractSequence<Node>{
    private NamedNodeMap nodeMap;

    public NamedNodeMapSequence(NamedNodeMap nodeMap){
        this.nodeMap = nodeMap;
        _reset();
    }


    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    private int i;

    @Override
    protected Node findNext(){
        return nodeMap.item(++i);
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        i = -1;
    }

    @Override
    public NamedNodeMapSequence copy(){
        return new NamedNodeMapSequence(nodeMap);
    }
}