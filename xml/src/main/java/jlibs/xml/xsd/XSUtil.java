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

package jlibs.xml.xsd;

import jlibs.core.graph.*;
import jlibs.core.graph.navigators.FilteredTreeNavigator;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import org.apache.xerces.xs.*;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XSUtil{
    public static MyNamespaceSupport createNamespaceSupport(XSModel model){
        MyNamespaceSupport nsSupport = new MyNamespaceSupport();
        StringList list = model.getNamespaces();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i)!=null) // default namespace is null
                nsSupport.declarePrefix(list.item(i));
        }
        return nsSupport;
    }

    public static QName getQName(XSObject obj){
        return getQName(obj, null);
    }

    public static QName getQName(XSObject obj, MyNamespaceSupport nsSupport){
        if(obj instanceof XSAttributeUse)
            obj = ((XSAttributeUse)obj).getAttrDeclaration();
        
        if(obj.getName()==null)
            return new QName("");
        String ns = obj.getNamespace()==null ? "" : obj.getNamespace();
        String prefix = nsSupport==null ? "" : nsSupport.findPrefix(ns);
        return new QName(ns, obj.getName(), prefix);
    }

    public static List<XSComplexTypeDefinition> getSubTypes(XSModel xsModel, XSComplexTypeDefinition complexType){
        List<XSComplexTypeDefinition> subTypes = new ArrayList<XSComplexTypeDefinition>();
        XSNamedMap namedMap = xsModel.getComponents(XSConstants.TYPE_DEFINITION);
        XSObject anyType = namedMap.itemByName(Namespaces.URI_XSD, "anyType");
        for(int i=0; i<namedMap.getLength(); i++){
            XSObject item = namedMap.item(i);
            if(item instanceof XSComplexTypeDefinition){
                XSComplexTypeDefinition complexItem = (XSComplexTypeDefinition)item;
                if(!complexItem.getAbstract()){
                    do{
                        complexItem = (XSComplexTypeDefinition)complexItem.getBaseType();
                    }while(complexItem!=anyType && complexItem!=complexType);
                    if(complexItem==complexType)
                        subTypes.add((XSComplexTypeDefinition)item);
                }
            }
        }
        return subTypes;
    }

    @SuppressWarnings({"unchecked"})
    public static List<XSElementDeclaration> guessRootElements(final XSModel model){
        XSNamedMap components = model.getComponents(XSConstants.ELEMENT_DECLARATION);
        if(components.getLength()==0)
            return Collections.emptyList();

        final List<XSElementDeclaration> elements = new ArrayList<XSElementDeclaration>(components.getLength());
        for(int i=0; i<components.getLength(); i++){
            XSElementDeclaration elem = (XSElementDeclaration)components.item(i);
            if(!elem.getAbstract())
                elements.add(elem);
        }

        Filter filter = new Filter(){
            @Override
            public boolean select(Object elem){
                return elem instanceof XSModel || elem instanceof XSElementDeclaration;
            }
        };
        Navigator navigator = new FilteredTreeNavigator(new XSNavigator(), filter);
        WalkerUtil.walk(new PreorderWalker(model, navigator), new Processor<Object>(){
            @Override
            public boolean preProcess(Object elem, Path path){
                if(path.getRecursionDepth()>0)
                    return false;
                if(elements.size()>1 && path.getLength()>2){
                    XSElementDeclaration elemDecl = (XSElementDeclaration)elem;
                    if(elemDecl.getAbstract()){
                        XSObjectList substitutionGroup = model.getSubstitutionGroup(elemDecl);
                        for(int i=0; i<substitutionGroup.getLength(); i++){
                            elements.remove(substitutionGroup.item(i));
                            if(elements.size()==1)
                                break;
                        }
                    }else
                        elements.remove(elem);
                }
                return true;
            }

            @Override
            public void postProcess(Object elem, Path path){}
        });
        return elements;
    }

    public static List<String> getEnumeratedValues(XSSimpleTypeDefinition simpleType){
        ArrayList<String> enums = new ArrayList<String>();

        XSObjectList facets = simpleType.getMultiValueFacets();
        if(facets!=null){
            for(int i=0; i<facets.getLength(); i++){
                XSMultiValueFacet facet = (XSMultiValueFacet)facets.item(i);
                if(facet.getFacetKind()==XSSimpleTypeDefinition.FACET_ENUMERATION) {
                    StringList values = facet.getLexicalFacetValues();
                    for(int j=0; j<values.getLength(); j++)
                        enums.add(values.item(j));
                }
            }
        }
        return enums;
    }

    public static XSNamespaceItem getNamespaceItem(XSModel schema, String namespace){
        XSNamespaceItemList list = schema.getNamespaceItems();
        for(int i=0; i<list.getLength(); i++){
            XSNamespaceItem item = list.item(i);
            String ns = item.getSchemaNamespace();
            if(ns==null)
                ns = "";
            if(ns.equals(namespace))
                return item;
        }
        return null;
    }

    /*-------------------------------------------------[ Find Declaration ]---------------------------------------------------*/

    private static final RuntimeException STOP_SEARCHING = new RuntimeException("STOP_SEARCHING");

    @SuppressWarnings("unchecked")
    public static XSElementDeclaration findElementDeclaration(XSModel schema, final List<QName> xpath){
        XSNamespaceItem nsItem = XSUtil.getNamespaceItem(schema, xpath.get(0).getNamespaceURI());
        if(nsItem==null)
            return null;

        FilteredTreeNavigator navigator = new FilteredTreeNavigator(new XSNavigator(), new Filter(){
            @Override
            public boolean select(Object elem){
                return elem instanceof XSElementDeclaration;
            }
        });

        final XSElementDeclaration declaration[] = new XSElementDeclaration[1];

        Walker walker = new PreorderWalker(nsItem, navigator);

        try{
            WalkerUtil.walk(walker, new Processor(){
                int i = 0;
                @Override
                public boolean preProcess(Object elem, Path path){
                    if(path.getParentPath()!=null){
                        QName qname = XSUtil.getQName((XSElementDeclaration)elem);
                        if(qname.equals(xpath.get(i))){
                            i++;
                            if(i==xpath.size()){
                                declaration[0] = (XSElementDeclaration)elem;
                                throw STOP_SEARCHING;
                            }
                            return true;
                        }else
                            return false;
                    }
                    return true;
                }

                @Override
                public void postProcess(Object elem, Path path){}
            });
        }catch(RuntimeException ex){
            if(ex!=STOP_SEARCHING)
                throw ex;
        }

        return declaration[0];
    }

    @SuppressWarnings("unchecked")
    public static XSAttributeDeclaration findAttributeDeclaration(XSModel schema, final List<QName> xpath){
        XSNamespaceItem nsItem = XSUtil.getNamespaceItem(schema, xpath.get(0).getNamespaceURI());
        if(nsItem==null)
            return null;

        FilteredTreeNavigator navigator = new FilteredTreeNavigator(new XSNavigator(), new Filter(){
            @Override
            public boolean select(Object elem){
                return elem instanceof XSElementDeclaration || elem instanceof XSAttributeUse;
            }
        });

        final XSAttributeDeclaration declaration[] = new XSAttributeDeclaration[1];

        Walker walker = new PreorderWalker(nsItem, navigator);

        try{
            WalkerUtil.walk(walker, new Processor(){
                int i = 0;
                @SuppressWarnings("ConstantConditions")
                @Override
                public boolean preProcess(Object elem, Path path){
                    if(path.getParentPath()!=null){
                        QName qname = XSUtil.getQName(elem instanceof XSAttributeUse? ((XSAttributeUse)elem).getAttrDeclaration() : (XSObject)elem);
                        if(qname.equals(xpath.get(i)) && (i!=xpath.size()-1 || elem instanceof XSAttributeUse)){
                            i++;
                            if(i==xpath.size()){
                                declaration[0] = ((XSAttributeUse)elem).getAttrDeclaration();
                                throw STOP_SEARCHING;
                            }
                            return true;
                        }else
                            return false;
                    }
                    return true;
                }

                @Override
                public void postProcess(Object elem, Path path){}
            });
        }catch(RuntimeException ex){
            if(ex!=STOP_SEARCHING)
                throw ex;
        }

        return declaration[0];
    }
}
