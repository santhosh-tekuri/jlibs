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

package jlibs.xml.xsd.display;

import jlibs.core.graph.visitors.PathReflectionVisitor;
import org.apache.xerces.xs.*;

import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class XSFontStyleVisitor extends PathReflectionVisitor<Object, Integer>{
    XSPathDiplayFilter filter;

    public XSFontStyleVisitor(XSPathDiplayFilter filter){
        this.filter = filter;
    }

    private static final Integer STYLE_NONE = Font.PLAIN;
    private static final Integer STYLE_MANDATORY = Font.BOLD;
    private static final Integer STYLE_OPTIONAL = Font.PLAIN;

    @Override
    protected Integer getDefault(Object elem){
        return STYLE_NONE;
    }

    private int getStyle(){
        if(path.getParentPath()==null)
            return STYLE_NONE;
        else if(path.getParentPath().getElement() instanceof XSParticle){
            XSParticle particle = (XSParticle)path.getParentPath().getElement();
            if(particle.getMinOccurs()==0 && !filter.select(path.getParentPath()))
                return STYLE_OPTIONAL;
        }
        return STYLE_MANDATORY;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected int process(XSNamespaceItem nsItem){
        return STYLE_MANDATORY;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected int process(XSModelGroup modelGroup){
        return getStyle();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected int process(XSElementDeclaration elem){
        return getStyle();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected int process(XSWildcard wildcard){
        return getStyle();
    }

    protected int process(XSAttributeUse attrUse){
        if(!attrUse.getRequired())
            return STYLE_OPTIONAL;
        else
            return STYLE_MANDATORY;
    }
}