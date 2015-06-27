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

package jlibs.xml;

import jlibs.core.lang.ImpossibleException;

import javax.xml.XMLConstants;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * This class most commonly used namespaces and their common prefixes.
 * <p>
 * This class contains constants to many standard namespaces which we use in our daily projects.
 * <pre class="prettyprint">
 * import static jlibs.xml.Namespaces.*;
 *
 * System.out.println({@link #URI_SOAP});
 * System.out.println({@link #URI_WSDL});
 * </pre>
 *
 * It can also suggest standard prefixes used for those namespaces.
 * <pre class="prettyprint">
 * import static jlibs.xml.Namespaces.*;
 * 
 * String prefix = Namespaces.{@link #suggestPrefix(String) suggestPrefix}({@link #URI_WSDL});
 * System.out.println(prefix); // prints "wsdl"
 * </pre>
 * {@link #getSuggested()} returns {@link java.util.Properties} object where key is {@code URI} and value is suggested {@code prefix}.
 * <p>
 * <b>NOTE:</b><br>
 * The naming convention that should be followed for an uri is:
 * <pre class="prettyprint">
 * String URI_&lt;suggestedPrefixInUppercase&gt; = "&lt;namespaceURI&gt;";
 * </pre>
 * 
 * @author Santhosh Kumar T
 */
public class Namespaces{
    /** The official XML Namespace name URI */
    public static final String URI_XML   = XMLConstants.XML_NS_URI;
    /** Namespace URI used by the official XML attribute used for specifying XML Namespace declarations <code>"xmlns"</code> */
    public static final String URI_XMLNS   = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

    /** Schema namespace as defined by XSD **/
    public static final String URI_XSD   = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    /** Instance namespace as defined by XSD **/
    public static final String URI_XSI   = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

    /** Namespace used by XSL Documents */
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

    /**	WSDL namespace for WSDL SOAP 1.1 binding **/
    public static final String URI_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/"; //NOI18N

    /**	WSDL namespace for WSDL SOAP 1.2 binding **/
    public static final String URI_SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/"; //NOI18N

    /** Envelope namespace as defined by SOAP 1.1 **/
    public static final String URI_SOAPENV = "http://schemas.xmlsoap.org/soap/envelope/"; //NOI18N

    /** Envelope namespace as defined by SOAP 1.2 **/
    public static final String URI_SOAP12ENV = "http://www.w3.org/2003/05/soap-envelope"; //NOI18N

    /** Encoding namespace as defined by SOAP 1.1 **/
    public static final String URI_SOAPENC = "http://schemas.xmlsoap.org/soap/encoding/"; //NOI18N

    /** Encoding namespace as defined by SOAP 1.2 **/
    public static final String URI_SOAP12ENC = "http://www.w3.org/2003/05/soap-encoding"; //NOI18N

    /*-------------------------------------------------[ Suggestions ]---------------------------------------------------*/

    private static final Properties suggested = new Properties();

    static{
        suggested.put("", "");
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

    /**
     * Returns suggested prefix fore the given {@code uri}.
     *
     * @param uri namespace uri
     * @return empty string if {@code uri} is null or empty.
     *         If the {@code uri} is one of the constants
     *         defined in this class, then it returns commonly
     *         used prefix. Otherwise it returns null.
     */
    public static String suggestPrefix(String uri){
        if(uri==null)
            uri = "";
        return suggested.getProperty(uri);
    }

    /** returns {@link java.util.Properties} object where key is {@code URI} and value is suggested {@code prefix}. */
    public static Properties getSuggested(){
        return new Properties(suggested);
    }
}