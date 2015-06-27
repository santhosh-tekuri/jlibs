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

package jlibs.xml.xsd;

import jlibs.core.lang.ImpossibleException;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.dom.DOMXSImplementationSourceImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.xs.LSInputList;
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
    
    public XSModel parse(LSInputList inputList){
        XSModel xsModel = xsLoader.loadInputList(inputList);
        if(xsModel==null)
            throw new RuntimeException("Couldn't load XMLSchema from "+ inputList);
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
