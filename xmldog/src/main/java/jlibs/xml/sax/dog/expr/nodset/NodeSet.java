/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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
        super(locationPath.scope, locationPath, DataType.NODESET, true, false);
    }

    @Override
    public Object getResult(){
        assert locationPath==LocationPath.IMPOSSIBLE;
        return rawResult ? new LongTreeMap() : Collections.EMPTY_LIST;
    }

    @Override
    protected Object getResultItem(Event event){
        return event.nodeItem();
    }

    @Override
    public Object getResult(Event event){
        if(locationPath.steps.length==0){
            NodeItem nodeItem = event.nodeItem();
            if(rawResult){
                LongTreeMap<NodeItem> result = new LongTreeMap<NodeItem>();
                result.put(nodeItem.order, nodeItem);
                return result;
            }else
                return Collections.singletonList(nodeItem);
        }else
            return super.getResult(event);
    }

    @Override
    protected String getName(){
        return "nodeset";
    }
}
