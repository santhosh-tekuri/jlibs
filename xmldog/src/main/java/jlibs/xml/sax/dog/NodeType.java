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
