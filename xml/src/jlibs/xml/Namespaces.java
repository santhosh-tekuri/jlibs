package jlibs.xml;

import org.xml.sax.helpers.NamespaceSupport;

import java.lang.reflect.Field;

/**
 * Interface containing commonly used namespaces and their common prefixes.
 *
 * NOTE: The naming convention that should be followed for an uri is:
 *             String URI_<suggestedPrefixInUppercase> = "<namespaceURI>
 * @author Santhosh Kumar T
 */
public interface Namespaces{
    String URI_XML   = "http://www.w3.org/XML/1998/namespace";                     //NOI18N
    String URI_XMLNS   = "http://www.w3.org/2000/xmlns/";                          //NOI18N

    /** Schema namespace as defined by XSD **/
    String URI_XSD   = "http://www.w3.org/2001/XMLSchema";                         //NOI18N

    /** Instance namespace as defined by XSD **/
    String URI_XSI   = "http://www.w3.org/2001/XMLSchema-instance";                //NOI18N


    String URI_XSL   = "http://www.w3.org/1999/XSL/Transform";                     //NOI18N
    String URI_BPWS  = "http://schemas.xmlsoap.org/ws/2003/03/business-process/";  //NOI18N
    String URI_FESB  = "http://www.fiorano.com/ws/2005/08/business-process/";      //NOI18N
    String URI_XHTML = "http://www.w3.org/1999/xhtml";                             //NOI18N
    String URI_XPATH  = "http://www.w3.org/TR/1999/REC-xpath-19991116";            //NOI18N
    String URI_PLINK = "http://schemas.xmlsoap.org/ws/2003/05/partner-link/";      //NOI18N

    /** WSDL namespace for WSDL framework **/
    String URI_WSDL  = "http://schemas.xmlsoap.org/wsdl/";                         //NOI18N

    /** WSDL namespace for WSDL HTTP GET & POST binding **/
    String URI_HTTP = "http://schemas.xmlsoap.org/wsdl/http/"; //NOI18N

    /** WSDL namespace for WSDL MIME binding **/
    String URI_MIME = "http://schemas.xmlsoap.org/wsdl/mime/"; //NOI18N

    /**	WSDL namespace for WSDL SOAP binding **/
    String URI_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/"; //NOI18N

    /** Encoding namespace as defined by SOAP 1.1 **/
    String URI_SOAPENC = "http://schemas.xmlsoap.org/soap/encoding/"; //NOI18N

    /** Envelope namespace as defined by SOAP 1.1 **/
    String URI_SOAPENV = "http://schemas.xmlsoap.org/soap/envelope/"; //NOI18N

    NamespaceSupport SUGGESTED = new NamespaceSupport(){
        {
            for(Field field: Namespaces.class.getFields()){
                if(field.getName().startsWith("URI_")) //NOI18N
                    try{
                        String prefix = field.getName().substring("URI_".length()).toLowerCase(); //NOI18N
                        String uri = (String)field.get(null);
                        declarePrefix(prefix, uri);
                    } catch(IllegalAccessException e){ //this should never happen
                        e.printStackTrace();
                    }
            }
        }
    };
}