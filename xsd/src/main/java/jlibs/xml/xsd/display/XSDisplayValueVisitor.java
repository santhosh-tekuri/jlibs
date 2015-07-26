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
import jlibs.xml.Namespaces;
import jlibs.xml.XMLUtil;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsd.XSUtil;
import org.apache.xerces.xs.*;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class XSDisplayValueVisitor extends PathReflectionVisitor<Object, String>{
    private MyNamespaceSupport nsSupport;

    public XSDisplayValueVisitor(MyNamespaceSupport nsSupport){
        this.nsSupport = nsSupport;
    }

    @Override
    protected String getDefault(Object elem){
        return null;
    }

    protected String process(XSNamespaceItem nsItem){
        String ns = nsItem.getSchemaNamespace();
        return nsSupport.findPrefix(ns!=null ? ns : "");
    }

    protected String process(XSElementDeclaration elem){
        XSTypeDefinition type = elem.getTypeDefinition();
        if(type instanceof XSComplexTypeDefinition){
            XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)type;
            if(complexType.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)
                type = complexType.getBaseType();
        }
        if(type instanceof XSComplexTypeDefinition)
            return null; //'{'+XSUtil.getQName(type, nsSupport)+'}';
        else
            return process((XSSimpleTypeDefinition)type);
    }

    protected String process(XSAttributeUse attrUse){
        String value = process(attrUse.getAttrDeclaration().getTypeDefinition());
        String constraintValue = attrUse.getConstraintValue();
        if(constraintValue==null)
            return value;
        else
            return value+' '+(attrUse.getConstraintType()==XSConstants.VC_DEFAULT ? "default(" : "fixed(")+constraintValue+')';
    }

    protected String process(XSSimpleTypeDefinition simpleType){
        QName qname = XSUtil.getQName(simpleType, nsSupport);
        if(Namespaces.URI_XSD.equals(qname.getNamespaceURI()))
            return XMLUtil.getQName(qname);

        XSObjectList facets = simpleType.getMultiValueFacets();
        StringBuilder buff = new StringBuilder();
        for(int i=0; i<facets.getLength(); i++){
            XSMultiValueFacet facet = (XSMultiValueFacet)facets.item(i);
            switch(facet.getFacetKind()){
                case XSSimpleTypeDefinition.FACET_ENUMERATION:
                    StringList list = facet.getLexicalFacetValues();
                    for(int j=0; j<list.getLength(); j++){
                        if(j!=0)
                            buff.append('|');
                        buff.append(list.item(j));
                    }
            }
        }
        if(buff.length()>0)
            return buff.toString();

        String min = null;
        String max = null;
        facets = simpleType.getFacets();
        for(int i=0; i<facets.getLength(); i++){
            XSFacet facet = (XSFacet)facets.item(i);
            switch(facet.getFacetKind()){
                case XSSimpleTypeDefinition.FACET_MININCLUSIVE:
                    min = '['+facet.getLexicalFacetValue();
                    break;
                case XSSimpleTypeDefinition.FACET_MINEXCLUSIVE:
                    min = '('+facet.getLexicalFacetValue();
                    break;
                case XSSimpleTypeDefinition.FACET_MAXINCLUSIVE:
                    max = facet.getLexicalFacetValue()+']';
                    break;
                case XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE:
                    max = facet.getLexicalFacetValue()+')';
                    break;
            }
        }
        if(min!=null && max!=null)
            return min+", "+max;
        else if(min!=null || max!=null){
            if(min!=null){
                String str = min.substring(1);
                return min.charAt(0)=='[' ? ">= "+str : "> "+str;
            }else{
                String str = max.substring(0, max.length()-1);
                return max.charAt(max.length()-1)=='[' ? "<= "+str : "< "+str;
            }
        }

        return XMLUtil.getQName(qname);
    }
}
