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

    public static QName getQName(XSObject obj, MyNamespaceSupport nsSupport){
        if(obj instanceof XSAttributeUse)
            obj = ((XSAttributeUse)obj).getAttrDeclaration();
        
        if(obj.getName()==null)
            return new QName("");
        String ns = obj.getNamespace()==null ? "" : obj.getNamespace();
        String prefix = nsSupport.findPrefix(ns);
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
}
