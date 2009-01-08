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

package jlibs.xml.xsl;

import jlibs.core.lang.StringUtil;

import javax.xml.transform.*;

/**
 * @author Santhosh Kumar T
 */
public class TransformerUtil{
    public static final String OUTPUT_KEY_INDENT_AMOUT = "{http://xml.apache.org/xslt}indent-amount";

    public static Transformer newTransformer(Source source, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerConfigurationException{
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = source!=null ? factory.newTransformer(source) : factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration ? "yes" : "no");

        // indentation
        if(indentAmount>=0){
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            if(indentAmount>0)
                transformer.setOutputProperty(OUTPUT_KEY_INDENT_AMOUT, String.valueOf(indentAmount));
        }

        if(!StringUtil.isWhitespace(encoding))
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding.trim());
        
        return transformer;
    }
}
