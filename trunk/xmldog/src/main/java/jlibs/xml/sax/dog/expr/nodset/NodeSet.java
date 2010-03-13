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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

import java.util.Collections;

/**
 * @author Santhosh Kumar T
 */
public final class NodeSet extends LocationExpression{
    public NodeSet(LocationPath locationPath){
        super(locationPath.enlargedScope(), locationPath, DataType.NODESET, true, false);
    }

    @Override
    public Object getResult(){
        if(locationPath==LocationPath.IMPOSSIBLE)
            return rawResult ? new LongTreeMap() : Collections.EMPTY_LIST;
        else{
            if(rawResult){
                LongTreeMap<NodeItem> result = new LongTreeMap<NodeItem>();
                result.put(0, NodeItem.NODEITEM_DOCUMENT);
                return result;
            }else
                return Collections.singletonList(NodeItem.NODEITEM_DOCUMENT);
        }
    }

    @Override
    protected Object getResultItem(Event event){
        return event.nodeItem();
    }

    @Override
    protected String getName(){
        return "nodeset";
    }
}
