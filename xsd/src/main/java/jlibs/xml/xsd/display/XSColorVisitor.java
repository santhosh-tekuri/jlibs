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

package jlibs.xml.xsd.display;

import jlibs.core.graph.visitors.PathReflectionVisitor;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSWildcard;
import org.apache.xerces.xs.XSNamespaceItem;

import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class XSColorVisitor extends PathReflectionVisitor<Object, Color>{
    XSPathDiplayFilter filter;

    public XSColorVisitor(XSPathDiplayFilter filter){
        this.filter = filter;
    }

    @Override
    protected Color getDefault(Object elem){
        return COLOR_OTHER;
    }

    private static final Color COLOR_OTHER = Color.GRAY;
    private static final Color COLOR_ELEMENT = new Color(0, 0, 128);
    private static final Color COLOR_ATTRIBUTE = new Color(0, 128, 0);
    private static final Color COLOR_NSITEM = new Color(102, 0, 0);

    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSNamespaceItem nsItem){
        return COLOR_NSITEM;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSElementDeclaration elem){
        return COLOR_ELEMENT;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSWildcard wildcard){
        return COLOR_ELEMENT;
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    protected Color process(XSAttributeUse attrUse){
        return COLOR_ATTRIBUTE;
    }
}
