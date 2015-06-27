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

package jlibs.xml.sax;

/**
 * This interface contains constants for various SAX Features.
 *
 * @author Santhosh Kumar T
 */
public interface SAXFeatures{
    /**
     * A value of "true" indicates namespace URIs and unprefixed
     * local names for element and attribute names will be available.
     * <p>
     * <b>Default:</b> true<br>
     * <b>Access:</b> (parsing) read-only; (not parsing) read/write <br>
     * <b>Note:</b> If the validation feature is set to true, then the document
     * must contain a grammar that supports the use of namespaces
     */
    String NAMESPACES = "http://xml.org/sax/features/namespaces"; //NOI18N

    /**
     * A value of "true" indicates that XML qualified names (with prefixes)
     * and attributes (including xmlns* attributes) will be available.
     * <p>
     * <b>Default:</b> false<br>
     * <b>Access:</b> (parsing) read-only; (not parsing) read/write <br>
     */
    String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes"; //NOI18N

    /**
     * Controls whether the parser is reporting all validity errors
     * <p>
     * <b>Default:</b> false<br>
     * <b>Access:</b> (parsing) read-only; (not parsing) read/write <br>
     * <b>Note:</b> If this feature is set to true, the document must specify a grammar.
     * If this feature is set to false, the document may specify a grammar
     * and that grammar will be parsed but no validation of the document
     * contents will be performed.
     */
    String VALIDATION = "http://xml.org/sax/features/validation"; //NOI18N

    /**
     * Reports whether this parser processes external general entities
     * <p>
     * <b>Default:</b>unspecified<br>
     * <b>Access:</b> (parsing) read-only; (not parsing) read/write <br>
     * <b>Note:</b> always true if validating
     */
    String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities"; //NOI18N

    /**
     * Reports whether this parser processes external parameter entities
     * <p>
     * <b>Default:</b>unspecified<br>
     * <b>Access:</b> (parsing) read-only; (not parsing) read/write <br>
     * <b>Note:</b> always true if validating
     */
    String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities"; //NOI18N

    /**
     * May be examined only during a parse, after the startDocument() callback
     * has been completed; read-only. The value is true if the document specified
     * standalone="yes" in its XML declaration, and otherwise is false.
     * <p>
     * <b>Default:</b>not-applicable<br>
     * <b>Access:</b> (parsing) read-only; (not parsing) read/write <br>
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
     * <p>
     * <b>Default:</b>unspecified<br>
     * <b>Access:</b> (parsing) read-only; (not parsing) read/write <br>
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
