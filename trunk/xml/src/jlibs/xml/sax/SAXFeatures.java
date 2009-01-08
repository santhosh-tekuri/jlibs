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

package jlibs.xml.sax;

/**
 * @author Santhosh Kumar T
 */
public interface SAXFeatures{
    /**
     * A value of "true" indicates namespace URIs and unprefixed
     * local names for element and attribute names will be available.
     *
     * access: read/write
     * default: true
     */
    String NAMESPACES = "http://xml.org/sax/features/namespaces"; //NOI18N

    /**
     * A value of "true" indicates that XML qualified names (with prefixes)
     * and attributes (including xmlns* attributes) will be available.
     *
     * access: read/write
     * default: false
     */
    String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes"; //NOI18N

    /**
     * Controls whether the parser is reporting all validity errors;
     * if true, all external entities will be read.
     *
     * access: read/write
     * default: unspecified
     */
    String VALIDATION = "http://xml.org/sax/features/validation"; //NOI18N

    /**
     * Reports whether this parser processes external general entities; always true if validating.
     *
     * access: read/write
     * default: unspecified
     */
    String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities"; //NOI18N

    /**
     * Reports whether this parser processes external parameter entities; always true if validating.
     *
     * access: read/write
     * default: unspecified
     */
    String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities"; //NOI18N

    /**
     * May be examined only during a parse, after the startDocument() callback
     * has been completed; read-only. The value is true if the document specified
     * standalone="yes" in its XML declaration, and otherwise is false.
     *
     * access: (parsing) read-only, (not parsing) none
     * default: not-applicable
     */
    String IS_STANDALONE = "http://xml.org/sax/features/is-standalone"; //NOI18N

    /**
     * A value of "true" indicates that the LexicalHandler will report
     * the beginning and end of parameter entities.
     *
     * access: read/write
     * default: unspecified
     */
    String LEXICAL_HANDLER_PARAMETER_ENTITIES = "http://xml.org/sax/features/lexical-handler/parameter-entities"; //NOI18N

    /**
     * A value of "true" indicates that system IDs in declarations
     * will be absolutized (relative to their base URIs) before reporting.
     * (That is the default behavior for all SAX2 XML parsers.) A value
     * of "false" indicates those IDs will not be absolutized; parsers
     * will provide the base URI from Locator.getSystemId().
     * This applies to system IDs passed in
     *
     * DTDHandler.notationDecl(),
     * DTDHandler.unparsedEntityDecl(), and
     * DeclHandler.externalEntityDecl().
     * It does not apply to EntityResolver.resolveEntity(), which is not used
     * to report declarations, or to LexicalHandler.startDTD(), which
     * already provides the non-absolutized URI.
     *
     * access: read/write
     * default: true
     */
    String RESOLVE_DTD_URIS = "http://xml.org/sax/features/resolve-dtd-uris"; //NOI18N

    /**
     * Has a value of "true" if all XML names (for elements, prefixes, attributes,
     * entities, notations, and local names), as well as Namespace URIs, will have
     * been interned using java.lang.String.intern. This supports fast testing of
     * equality/inequality against string constants, rather than forcing slower
     * calls to String.equals().
     *
     * access: read/write
     * default: unspecified
     */
    String STRING_INTERNING = "http://xml.org/sax/features/string-interning"; //NOI18N

    /**
     * Controls whether the parser reports Unicode normalization errors as described
     * in section 2.13 and Appendix B of the XML 1.1 Recommendation. If true,
     * Unicode normalization errors are reported using the ErrorHandler.error() callback.
     * Such errors are not fatal in themselves (though, obviously,
     * other Unicode-related encoding errors may be).
     *
     * access: read/write
     * default: false
     */
    String UNICODE_NORMALIZATION_CHECKING = "http://xml.org/sax/features/unicode-normalization-checking"; //NOI18N

    /**
     * Returns "true" if the Attributes objects passed by this parser in
     * ContentHandler.startElement() implement the org.xml.sax.ext.Attributes2 interface.
     * That interface exposes additional DTD-related information, such as
     * whether the attribute was specified in the source text rather than defaulted.
     *
     * access: read-only
     * default: not-applicable
     */
    String USE_ATTRIBUTES2 = "http://xml.org/sax/features/use-attributes2"; //NOI18N

    /**
     * Returns "true" if the Locator objects passed by this parser in
     * ContentHandler.setDocumentLocator() implement the org.xml.sax.ext.Locator2 interface.
     * That interface exposes additional entity information, such as the
     * character encoding and XML version used.
     *
     * access: read-only
     * default: not-applicable
     */
    String USE_LOCATOR2 = "http://xml.org/sax/features/use-locator2"; //NOI18N

    /**
     * Returns "true" if, when setEntityResolver is given an object implementing
     * the org.xml.sax.ext.EntityResolver2 interface, those new methods will be used.
     * Returns "false" to indicate that those methods will not be used.
     *
     * access: read-write
     * default: true
     */
    String USE_ENTITY_RESOLVER2 = "http://xml.org/sax/features/use-entity-resolver2"; //NOI18N

    /**
     * Controls whether, when the namespace-prefixes feature is set,
     * the parser treats namespace declaration attributes as being in
     * the http://www.w3.org/2000/xmlns/ namespace. By default, SAX2
     * conforms to the original "Namespaces in XML" Recommendation,
     * which explicitly states that such attributes are not in any namespace.
     * Setting this optional flag to "true" makes the SAX2 events conform to a
     * later backwards-incompatible revision of that recommendation,
     * placing those attributes in a namespace.
     *
     * access: read-write
     * default: false
     */
    String XML_URIS = "http://xml.org/sax/features/xmlns-uris"; //NOI18N

    /**
     * Returns "true" if the parser supports both XML 1.1 and XML 1.0.
     * Returns "false" if the parser supports only XML 1.0.
     *
     * access: read-write
     * default: not-applicable
     */
    String XML_1_1 = "http://xml.org/sax/features/xml-1.1"; //NOI18N
}
