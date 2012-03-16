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

package jlibs.xml.xsd;

import jlibs.core.lang.ArrayUtil;
import jlibs.core.lang.StringUtil;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

/**
 * This class is used to write xmlschema documents
 *
 * @author Santhosh Kumar T
 */
public class XSDocument extends Namespaces{
    private XMLDocument xml;

    public XSDocument(XMLDocument xml){
        this.xml = xml;
    }

    public XSDocument(Result result, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerConfigurationException{
        xml = new XMLDocument(result, omitXMLDeclaration, indentAmount, encoding);
    }

    public XMLDocument xml(){
        return xml;
    }

    /*-------------------------------------------------[ Document ]---------------------------------------------------*/

    public XSDocument startDocument() throws SAXException{
        xml.startDocument();
        return this;
    }

    public XSDocument endDocument() throws SAXException{
        xml.endDocument();
        return this;
    }

    /*-------------------------------------------------[ Schema ]---------------------------------------------------*/

    /**
     * <xsd:schema [targetNamespace="$targetNamespace"]>
     */
    public XSDocument startSchema(String targetNamespace) throws SAXException{
        xml.startElement(URI_XSD, "schema");
        if(targetNamespace!=null && !targetNamespace.isEmpty()){
            xml.declarePrefix(targetNamespace);
            xml.addAttribute("targetNamespace", targetNamespace);
        }
        return this;
    }

    /**
     * </xsd:schema>
     */
    public XSDocument endSchema() throws SAXException{
        xml.endElement(URI_XSD, "schema");
        return this;
    }

    /*-------------------------------------------------[ Imports & Includes ]---------------------------------------------------*/

    /**
     * <xsd:import [namespace="$namespace"] [schemaLocation="$schemaLocation"]/>
     */
    public XSDocument addImport(String namespace, String schemaLocation) throws SAXException{
        xml.startElement(URI_XSD, "import");
        xml.addAttribute("namespace", namespace);
        xml.addAttribute("schemaLocation", schemaLocation);
        xml.endElement();
        return this;
    }

    /**
     * <xsd:include schemaLocation="$schemaLocation"/>
     */
    public XSDocument addInclude(String schemaLocation) throws SAXException{
        xml.startElement(URI_XSD, "include");
        xml.addAttribute("schemaLocation", schemaLocation);
        xml.endElement();
        return this;
    }

    /*-------------------------------------------------[ General Attributes ]---------------------------------------------------*/

    /**
     * name="$name"
     */
    public XSDocument name(String name) throws SAXException{
        xml.addAttribute("name", name);
        return this;
    }

    /**
     * type="qname($typeNS, $typeLocalpart)"
     */
    public XSDocument type(String typeNS, String typeLocalpart) throws SAXException{
        xml.addAttribute("type", xml.toQName(typeNS, typeLocalpart));
        return this;
    }

    /**
     * ref="qname($typeNS, $typeLocalpart)"
     */
    public XSDocument ref(String typeNS, String typeLocalpart) throws SAXException{
        xml.addAttribute("ref", xml.toQName(typeNS, typeLocalpart));
        return this;
    }

    /**
     * if minOccurs!=1
     *      minOccurs="$minOccurs"
     *
     * if maxOccurs!=1
     *      maxOccurs="$maxOccurs"
     */
    public void occurs(int minOccurs, int maxOccurs) throws SAXException{
        if(minOccurs!= 1)
            xml.addAttribute("minOccurs", String.valueOf(minOccurs));
        if(maxOccurs!=1)
            xml.addAttribute("maxOccurs", maxOccurs==-1 ? "unbounded" : String.valueOf(maxOccurs));
    }

    /*-------------------------------------------------[ Element ]---------------------------------------------------*/

    /**
     * <xsd:element>
     */
    public XSDocument startElement() throws SAXException{
        xml.startElement(URI_XSD, "element");
        return this;
    }

    /**
     * </xsd:element>
     */
    public XSDocument endElement() throws SAXException{
        xml.endElement(URI_XSD, "element");
        return this;
    }

    /*-------------------------------------------------[ Attribute ]---------------------------------------------------*/

    public enum Constraint{ DEFAULT, FIXED, NONE }

    /**
     * $constraint="$defaultValue"
     */
    public XSDocument constraint(Constraint constraint, String constraintValue) throws SAXException{
        if(constraint!=Constraint.NONE)
            xml.addAttribute(constraint.name().toLowerCase(), constraintValue);
        return this;
    }

    /**
     * use="$reqd ? required : optional"
     */
    public XSDocument required(boolean reqd) throws SAXException{
        xml.addAttribute("use", reqd ? "required" : "optional");
        return this;
    }

    /**
     * <xsd:attribute>
     */
    public XSDocument startAttribute() throws SAXException{
        xml.startElement(URI_XSD, "attribute");
        return this;
    }

    /**
     * </xsd:attribute>
     */
    public XSDocument endAttribute() throws SAXException{
        xml.endElement(URI_XSD, "attribute");
        return this;
    }

    /*-------------------------------------------------[ SimpleType ]---------------------------------------------------*/

    public XSDocument startSimpleType() throws SAXException{
        xml.startElement(URI_XSD, "simpleType");
        return this;
    }

    public XSDocument endSimpleType() throws SAXException{
        xml.endElement(URI_XSD, "simpleType");
        return this;
    }

    /*-------------------------------------------------[ Compositor ]---------------------------------------------------*/

    public enum Compositor{ SEQUENCE, ALL, CHOICE }

    public XSDocument startCompositor(Compositor compositor) throws SAXException{
        xml.startElement(URI_XSD, compositor.name().toLowerCase());
        return this;
    }

    public XSDocument endCompositor() throws SAXException{
        xml.endElement();
        return this;
    }

    /*-------------------------------------------------[ Derivations ]---------------------------------------------------*/

    public enum Derivation{ EXTENSION, RESTRICTION, SUBSTITUTION }

    public XSDocument startDerivation(Derivation derivation, String baseNS, String baseLocalpart) throws SAXException{
        xml.startElement(URI_XSD, derivation.name().toLowerCase());
        xml.addAttribute("base", xml.toQName(baseNS, baseLocalpart));
        return this;
    }

    public XSDocument endDerivation() throws SAXException{
        xml.endElement();
        return this;
    }

    /*-------------------------------------------------[ ComplexType ]---------------------------------------------------*/

    public XSDocument startComplexType() throws SAXException{
        xml.startElement(URI_XSD, "complexType");
        return this;
    }

    public XSDocument prohibit(Derivation... derivations) throws SAXException{
        if(derivations.length>0){
            boolean prohibitExtension = ArrayUtil.contains(derivations, Derivation.EXTENSION);
            boolean prohibitRestriction = ArrayUtil.contains(derivations, Derivation.RESTRICTION);

            if(prohibitExtension && prohibitRestriction)
                xml.addAttribute("final", "#all");
            else if(prohibitExtension)
                xml.addAttribute("final", "extension");
            else if(prohibitRestriction)
                xml.addAttribute("final", "restriction");
        }
        return this;
    }

    public XSDocument endComplexType() throws SAXException{
        xml.endElement(URI_XSD, "complexType");
        return this;
    }

    /*-------------------------------------------------[ Content ]---------------------------------------------------*/

    public enum Content{ SIMPLE, ELEMENT, EMPTY, MIXED }

    public XSDocument startContent(Content content) throws SAXException{
        xml.startElement(URI_XSD, (content==Content.SIMPLE) ? "simpleContent" : "complexContent");
        if(content==Content.MIXED)
            xml.addAttribute("mixed", "true");
        return this;
    }

    public XSDocument endContent() throws SAXException{
        xml.endElement();
        return this;
    }

    /*-------------------------------------------------[ Facet ]---------------------------------------------------*/

    public enum Facet{
        NONE, WHITE_SPACE,
        LENGTH, MIN_LENGTH, MAX_LENGTH,
        PATTERN, ENUMERATION, // multivalue facets
        MAX_INCLUSIVE, MAX_EXCLUSIVE, MIN_EXCLUSIVE, MIN_INCLUSIVE,
        TOTAL_DIGITS, FRACTION_DIGITS;

        @Override
        public String toString(){
            String str = name().toLowerCase();
            int underscore = str.indexOf('_');
            if(underscore==-1)
                return str;
            else
                return str.substring(0, underscore)+ StringUtil.capitalize(str.substring(underscore+1));
        }
    }

    public XSDocument addFacet(Facet facet, String value, boolean fixed) throws SAXException{
        xml.startElement(URI_XSD, facet.toString());
        xml.addAttribute("value", value);
        if(fixed)
            xml.addAttribute("fixed", "true");
        xml.endElement();
        return this;
    }

    public XSDocument addMultiValueFacet(Facet facet, String... values) throws SAXException{
        String facetName = facet.toString();
        for(String value: values){
            xml.startElement(URI_XSD, facetName);
            xml.addAttribute("value", value);
            xml.endElement();
        }
        return this;
    }

    /*-------------------------------------------------[ List ]---------------------------------------------------*/

    public XSDocument addList(String itemTypeNS, String itemTypeLocalpart) throws SAXException{
        xml.startElement(URI_XSD, "list");
        xml.addAttribute("itemType", xml.toQName(itemTypeNS, itemTypeLocalpart));
        xml.endElement();
        return this;
    }

    /*-------------------------------------------------[ Union ]---------------------------------------------------*/

    public XSDocument startUnion(String... memberParts) throws SAXException{
        xml.startElement(URI_XSD, "union");

        StringBuilder buff = new StringBuilder();
        for(int i=0; i<memberParts.length; i+=2){
            if(buff.length()>0)
                buff.append(' ');
            buff.append(xml.toQName(memberParts[i], memberParts[i+1]));
        }
        if(buff.length()>0)
            xml.addAttribute("memberTypes", buff.toString());

        xml.endElement();
        return this;
    }

    public XSDocument endUnion() throws SAXException{
        xml.endElement();
        return this;
    }

    /*-------------------------------------------------[ Annotation ]---------------------------------------------------*/

    public XSDocument startAnnotation() throws SAXException{
        xml.startElement(URI_XSD, "annotation");
        return this;
    }

    public XSDocument addDocumentation(String doc) throws SAXException{
        xml.addElement(URI_XSD, "documentation", doc);
        return this;
    }

    public XSDocument endAnnotation() throws SAXException{
        xml.endElement(URI_XSD, "annotation");
        return this;
    }

    /*-------------------------------------------------[ Testing ]---------------------------------------------------*/

    public static void main(String[] args) throws TransformerConfigurationException, SAXException{
        XSDocument xsd = new XSDocument(new StreamResult(System.out), false, 4, null);
        xsd.startDocument();
        {
            String n1 = "http://www.example.com/N1";
            String n2 = "http://www.example.com/N2";
            xsd.xml().declarePrefix("n1", n1);
            xsd.xml().declarePrefix("n2", n2);
            xsd.startSchema(n1);
            {
                xsd.addImport(n2, "imports/b.xsd");
                xsd.startComplexType().name("MyType");
                {
                    xsd.startCompositor(Compositor.SEQUENCE);
                    xsd.startElement().ref(n1, "e1").endElement();
                    xsd.endCompositor();
                }
                xsd.endComplexType();
                xsd.startElement().name("root").type(n1, "MyType").endElement();
            }
            xsd.endSchema();
        }
        xsd.endDocument();
    }
}
