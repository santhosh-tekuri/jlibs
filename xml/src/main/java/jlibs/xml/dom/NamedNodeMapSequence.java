/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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