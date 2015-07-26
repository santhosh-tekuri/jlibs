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

package jlibs.xml.xsl;

import jlibs.core.lang.StringUtil;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

/**
 * This class contains utility method related to transformation
 *
 * @author Santhosh Kumar T
 */
public class TransformerUtil{
    public static final String OUTPUT_KEY_INDENT_AMOUT = "{http://xml.apache.org/xslt}indent-amount";

    /**
     * to set various output properties on given transformer.
     *
     * @param transformer           transformer on which properties are set
     * @param omitXMLDeclaration    omit xml declaration or not
     * @param indentAmount          the number fo spaces used for indentation.
     *                              use <=0, in case you dont want indentation
     * @param encoding              required encoding. use null to don't set any encoding
     *
     * @return the same transformer which is passed as argument
     */
    public static Transformer setOutputProperties(Transformer transformer, boolean omitXMLDeclaration, int indentAmount, String encoding){
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration ? "yes" : "no");

        // indentation
        if(indentAmount>0){
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OUTPUT_KEY_INDENT_AMOUT, String.valueOf(indentAmount));
        }

        if(!StringUtil.isWhitespace(encoding))
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding.trim());

        return transformer;
    }

    /**
     * Creates Transformer
     *
     * @param source                source of xsl document, use null for identity transformer
     * @param omitXMLDeclaration    omit xml declaration or not
     * @param indentAmount          the number fo spaces used for indentation.
     *                              use <=0, in case you dont want indentation
     * @param encoding              required encoding. use null to don't set any encoding
     *
     * @return the same transformer which is passed as argument
     */
    public static Transformer newTransformer(Source source, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerConfigurationException{
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = source!=null ? factory.newTransformer(source) : factory.newTransformer();
        return setOutputProperties(transformer, omitXMLDeclaration, indentAmount, encoding);
    }

    /**
     * Creates TransformerHandler
     *
     * @param source                source of xsl document, use null for identity transformer
     * @param omitXMLDeclaration    omit xml declaration or not
     * @param indentAmount          the number fo spaces used for indentation.
     *                              use <=0, in case you dont want indentation
     * @param encoding              required encoding. use null to don't set any encoding
     *
     * @return the same transformer which is passed as argument
     */
    public static TransformerHandler newTransformerHandler(Source source, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerConfigurationException{
        SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();
        TransformerHandler handler = source!=null ? factory.newTransformerHandler(source) : factory.newTransformerHandler();
        setOutputProperties(handler.getTransformer(), omitXMLDeclaration, indentAmount, encoding);
        return handler;
    }
}
