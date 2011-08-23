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

import jlibs.core.lang.ImpossibleException;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.dom.DOMXSImplementationSourceImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSResourceResolver;

import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class XSParser{
    private XSLoader xsLoader;

    public XSParser(){
        this(null, null);
    }

    public XSParser(LSResourceResolver entityResolver, DOMErrorHandler errorHandler){
        System.setProperty(DOMImplementationRegistry.PROPERTY, DOMXSImplementationSourceImpl.class.getName());
        DOMImplementationRegistry registry;
        try{
            registry = DOMImplementationRegistry.newInstance();
        }catch(Exception ex){
            throw new ImpossibleException(ex);
        }
        XSImplementationImpl xsImpl = (XSImplementationImpl)registry.getDOMImplementation("XS-Loader");

        xsLoader = xsImpl.createXSLoader(null);
        DOMConfiguration config = xsLoader.getConfig();
        config.setParameter(Constants.DOM_VALIDATE, Boolean.TRUE);

        if(entityResolver!=null)
            config.setParameter(Constants.DOM_RESOURCE_RESOLVER, entityResolver);

        if(errorHandler!=null)
            config.setParameter(Constants.DOM_ERROR_HANDLER, errorHandler);
    }

    public XSModel parse(String uri){
        XSModel xsModel = xsLoader.loadURI(uri);
        if(xsModel==null)
            throw new RuntimeException("Couldn't load XMLSchema from "+uri);
        return xsModel;
    }

    public XSModel parse(String... uris){
        XSModel xsModel = xsLoader.loadURIList(new StringListImpl(uris, uris.length));
        if(xsModel==null)
            throw new RuntimeException("Couldn't load XMLSchema from "+ Arrays.asList(uris));
        return xsModel;
    }

    /**
     * Parse an XML Schema document from String specified
     * 
     * @param schema    String data to parse. If provided, this will always be treated as a
     *                  sequence of 16-bit units (UTF-16 encoded characters). If an XML
     *                  declaration is present, the value of the encoding attribute
     *                  will be ignored.
     * @param baseURI   The base URI to be used for resolving relative
     *                  URIs to absolute URIs.
     */
    public XSModel parseString(String schema, String baseURI){
        return xsLoader.load(new DOMInputImpl(null, null, baseURI, schema, null));
    }

    public static XSModel getBuiltInSchema(){
        return new XSModelImpl(new SchemaGrammar[0]);
    }
}
