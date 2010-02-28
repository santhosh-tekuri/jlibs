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

package jlibs.xml.sax;

/**
 * This interface contains constants for various SAX Properties.
 * 
 * @author Santhosh Kumar T
 */
public interface SAXProperties{
    /**
     * <table border=1>
     *  <tr>
     *    <td>Description</td>
     *    <td>
     *      Set the handler for lexical parsing events: comments, CDATA delimiters,
     *      selected general entity inclusions, and the start and end of the DTD
     *      (and declaration of document element name).
     *    </td>
     *  </tr>
     *  <tr>
     *    <td>Type</td>
     *    <td>{@link org.xml.sax.ext.LexicalHandler}</td>
     *  </tr>
     *  <tr>
     *    <td>Access</td>
     *    <td>read-write</td>
     *  </tr>
     * </table>
     */
    String LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler"; //NOI18N

    /**
     * <table border=1>
     *  <tr>
     *    <td>Description</td>
     *    <td>Set the handler for DTD declarations</td>
     *  </tr>
     *  <tr>
     *    <td>Type</td>
     *    <td>{@link org.xml.sax.ext.DeclHandler}</td>
     *  </tr>
     *  <tr>
     *    <td>Access</td>
     *    <td>read-write</td>
     *  </tr>
     * </table>
     */
    String DECL_HANDLER = "http://xml.org/sax/properties/declaration-handler"; //NOI18N

    /**
     * <table border=1>
     *  <tr>
     *    <td>Description</td>
     *    <td>A literal string describing the actual XML version of the document, such as "1.0" or "1.1"</td>
     *  </tr>
     *  <tr>
     *    <td>Type</td>
     *    <td>{@link java.lang.String}</td>
     *  </tr>
     *  <tr>
     *    <td>Access</td>
     *    <td>read-only</td>
     *  </tr>
     *  <tr>
     *    <td>Note</td>
     *    <td>This property may only be examined during a parse after the startDocument callback has been completed.</td>
     *  </tr>
     * </table>
     */
    String XML_VERSION = "http://xml.org/sax/properties/document-xml-version"; //NOI18N

    /**
     * <table border=1>
     *  <tr>
     *    <td>Description</td>
     *    <td>
     *       Get the string of characters associated with the current event. <br>If the parser
     *       recognizes and supports this property but is not currently parsing text,
     *       it should return null
     *    </td>
     *  </tr>
     *  <tr>
     *    <td>Type</td>
     *    <td>{@link java.lang.String}</td>
     *  </tr>
     *  <tr>
     *    <td>Access</td>
     *    <td>read-only</td>
     *  </tr>
     * </table>
     */
    String XML_STRING = "http://xml.org/sax/properties/xml-string"; //NOI18N

    /**
     * <table border=1>
     *  <tr>
     *    <td>Description</td>
     *    <td>
     *       The DOM node currently being visited, if SAX is being used as a DOM iterator.<br>
     *       If the parser recognizes and supports this property but is not currently
     *       visiting a DOM node, it should return null.
     *    </td>
     *  </tr>
     *  <tr>
     *    <td>Type</td>
     *    <td>{@link org.w3c.dom.Node}</td>
     *  </tr>
     *  <tr>
     *    <td>Access</td>
     *    <td>(parsing) read-only; (not parsing) read-write;</td>
     *  </tr>
     * </table>
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