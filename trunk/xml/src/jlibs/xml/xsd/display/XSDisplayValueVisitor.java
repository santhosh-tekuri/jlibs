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

package jlibs.xml.xsd.display;

import jlibs.core.graph.visitors.PathReflectionVisitor;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsd.XSUtil;
import org.apache.xerces.xs.*;

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
        else{
            XSSimpleTypeDefinition simpleType = (XSSimpleTypeDefinition)type;
            XSObjectList facets = simpleType.getMultiValueFacets();
            StringBuilder buff = new StringBuilder();
            for(int i=0; i<facets.getLength(); i++){
                XSMultiValueFacet facet = (XSMultiValueFacet)facets.item(i);
                switch(facet.getFacetKind()){
                    case XSSimpleTypeDefinition.FACET_ENUMERATION:
                        StringList list = facet.getLexicalFacetValues();
                        for(int j=0; j<list.getLength(); j++){
                            if(j!=0)
                                buff.append("|");
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

            return XSUtil.getQName(type, nsSupport);
        }
    }

    protected String process(XSAttributeUse attrUse){
        return XSUtil.getQName(attrUse.getAttrDeclaration().getTypeDefinition(), nsSupport);
    }
}
