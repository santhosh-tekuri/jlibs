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

package jlibs.xml.xsd.sequences;

import jlibs.core.graph.sequences.AbstractSequence;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;

/**
 * @author Santhosh Kumar T
 */
public class XSNamespaceItemListSequence extends AbstractSequence<XSNamespaceItem>{
    private XSNamespaceItemList list;

    public XSNamespaceItemListSequence(XSNamespaceItemList list){
        this.list = list;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int i;

    @Override
    protected XSNamespaceItem findNext(){
        return list.item(++i);
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
    public XSNamespaceItemListSequence copy(){
        return new XSNamespaceItemListSequence(list);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return list.getLength();
    }
}