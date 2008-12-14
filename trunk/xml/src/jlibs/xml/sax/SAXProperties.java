package jlibs.xml.sax;

/**
 * @author Santhosh Kumar T
 */
public interface SAXProperties{
    /**
     *  Used to see some syntax events that are essential in some applications:
     * comments, CDATA delimiters, selected general entity inclusions, and
     * the start and end of the DTD (and declaration of document element name).
     * The Object must implement org.xml.sax.ext.LexicalHandler.
     */
    String LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler"; //NOI18N

    /**
     * Used to see most DTD declarations except those treated as lexical ("document element name is ...")
     * or which are mandatory for all SAX parsers (DTDHandler).
     * The Object must implement org.xml.sax.ext.DeclHandler.
     */
    String DECL_HANDLER = "http://xml.org/sax/properties/declaration-handler"; //NOI18N

    /**
     * May be examined only during a parse, after the startDocument() callback
     * has been completed; read-only. This property is a literal string describing
     * the actual XML version of the document, such as "1.0" or "1.1".
     */
    String XML_VERSION = "http://xml.org/sax/properties/document-xml-version"; //NOI18N

    /**
     * Readable only during a parser callback, this exposes a TBS chunk
     * of characters responsible for the current event.
     */
    String XML_STRING = "http://xml.org/sax/properties/xml-string"; //NOI18N

    /**
     * For "DOM Walker" style parsers, which ignore their parser.parse() parameters,
     * this is used to specify the DOM (sub)tree being walked by the parser.
     * The Object must implement the org.w3c.dom.Node interface.
     */
    String DOM_NODE = "http://xml.org/sax/properties/dom-node"; //NOI18N

    /*-------------------------------------------------[ Non-Standard ]---------------------------------------------------*/

    /**
     * Shortcut for SAX-ext. lexical handler alternate property.
     * Although this property URI is not the one defined by the SAX
     * "standard", some parsers use it instead of the official one.
     */
    String LEXICAL_HANDLER_ALT = "http://xml.org/sax/handlers/LexicalHandler"; //NOI18N

    /** Shortcut for SAX-ext. declaration handler alternate property */
    String DECL_HANDLER_ALT = "http://xml.org/sax/handlers/DeclHandler"; //NOI18N
}