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

package jlibs.xml;

import jlibs.core.lang.ImpossibleException;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Interface containing commonly used namespaces and their common prefixes.
 *
 * NOTE: The naming convention that should be followed for an uri is:
 *             String URI_<suggestedPrefixInUppercase> = "<namespaceURI>
 * @author Santhosh Kumar T
 */
public class Namespaces{
    public static final String URI_XML   = "http://www.w3.org/XML/1998/namespace";                     //NOI18N
    public static final String URI_XMLNS   = "http://www.w3.org/2000/xmlns/";                          //NOI18N

    /** Schema namespace as defined by XSD **/
    public static final String URI_XSD   = "http://www.w3.org/2001/XMLSchema";                         //NOI18N

    /** Instance namespace as defined by XSD **/
    public static final String URI_XSI   = "http://www.w3.org/2001/XMLSchema-instance";                //NOI18N


    public static final String URI_XSL   = "http://www.w3.org/1999/XSL/Transform";                     //NOI18N
    public static final String URI_BPWS  = "http://schemas.xmlsoap.org/ws/2003/03/business-process/";  //NOI18N
    public static final String URI_XHTML = "http://www.w3.org/1999/xhtml";                             //NOI18N
    public static final String URI_XPATH  = "http://www.w3.org/TR/1999/REC-xpath-19991116";            //NOI18N
    public static final String URI_PLINK = "http://schemas.xmlsoap.org/ws/2003/05/partner-link/";      //NOI18N

    /** WSDL namespace for WSDL framework **/
    public static final String URI_WSDL  = "http://schemas.xmlsoap.org/wsdl/";                         //NOI18N

    /** WSDL namespace for WSDL HTTP GET & POST binding **/
    public static final String URI_HTTP = "http://schemas.xmlsoap.org/wsdl/http/"; //NOI18N

    /** WSDL namespace for WSDL MIME binding **/
    public static final String URI_MIME = "http://schemas.xmlsoap.org/wsdl/mime/"; //NOI18N

    /**	WSDL namespace for WSDL SOAP binding **/
    public static final String URI_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/"; //NOI18N

    /** Encoding namespace as defined by SOAP 1.1 **/
    public static final String URI_SOAPENC = "http://schemas.xmlsoap.org/soap/encoding/"; //NOI18N

    /** Envelope namespace as defined by SOAP 1.1 **/
    public static final String URI_SOAPENV = "http://schemas.xmlsoap.org/soap/envelope/"; //NOI18N

    /*-------------------------------------------------[ Suggestions ]---------------------------------------------------*/

    private static final Properties suggested = new Properties();

    static{
        for(Field field: Namespaces.class.getFields()){
            if(field.getName().startsWith("URI_")) //NOI18N
                try{
                    String prefix = field.getName().substring("URI_".length()).toLowerCase(); //NOI18N
                    String uri = (String)field.get(null);
                    suggested.put(uri, prefix);
                } catch(IllegalAccessException ex){
                    throw new ImpossibleException(ex);
                }
        }
    }

    public static String suggestPrefix(String uri){
        if(uri==null)
            uri = "";
        return suggested.getProperty(uri);
    }

    public static Properties getSuggested(){
        return new Properties(suggested);
    }
}