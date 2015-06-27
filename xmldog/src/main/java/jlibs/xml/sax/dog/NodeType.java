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

package jlibs.xml.sax.dog;

import org.w3c.dom.Node;

/**
 * This class contains constants to specify type of xml node.
 *
 * @see jlibs.xml.sax.dog.sniff.Event#type()
 * @see jlibs.xml.sax.dog.NodeItem#type
 *
 * @author Santhosh Kumar T
 */
public interface NodeType{
    public static final int ANY         = -1;
    public static final int MAX         = 13;

    public static final int NAMESPACE   = 13;
    public static final int DOCUMENT    = Node.DOCUMENT_NODE;
    public static final int ELEMENT     = Node.ELEMENT_NODE;
    public static final int TEXT        = Node.TEXT_NODE;
    public static final int ATTRIBUTE   = Node.ATTRIBUTE_NODE;
    public static final int COMMENT     = Node.COMMENT_NODE;
    public static final int PI          = Node.PROCESSING_INSTRUCTION_NODE;
}
