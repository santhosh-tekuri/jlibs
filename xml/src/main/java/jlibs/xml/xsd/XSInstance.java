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

import jlibs.core.graph.*;
import jlibs.core.graph.navigators.FilteredTreeNavigator;
import jlibs.core.graph.sequences.DuplicateSequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.graph.sequences.RepeatSequence;
import jlibs.core.graph.visitors.ReflectionVisitor;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.OS;
import jlibs.core.util.CollectionUtil;
import jlibs.core.util.RandomUtil;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.display.XSDisplayFilter;
import org.apache.xerces.xs.*;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
public class XSInstance{
    public int minimumElementsGenerated = 2;
    public int maximumElementsGenerated = 2;
    public int minimumListItemsGenerated = 2;
    public int maximumListItemsGenerated = 2;
    public int maximumRecursionDepth = 1;
    public Boolean generateOptionalElements = Boolean.TRUE;
    public Boolean generateOptionalAttributes = Boolean.TRUE;
    public Boolean generateFixedAttributes = Boolean.TRUE;
    public Boolean generateDefaultAttributes = Boolean.TRUE;
    public boolean generateAllChoices = false;
    public boolean showContentModel = true;

    private int generateRepeatCount(int minOccurs, int maxOccurs){
        if(minOccurs==0 && maxOccurs==1) //optional case
            return RandomUtil.randomBoolean(generateOptionalElements) ? 1 : 0;

        if(maxOccurs==-1)
            maxOccurs = Math.max(minOccurs, maximumElementsGenerated);

        int min, max;
        if(minimumElementsGenerated>maxOccurs || maximumElementsGenerated<minOccurs){ // doesn't intersect
            min = minOccurs;
            max = maxOccurs;
        }else { // find intersecting range
            min = Math.max(minOccurs, minimumElementsGenerated);
            max = Math.min(maxOccurs, maximumElementsGenerated);
        }
        return (min == max)
                ? min
                : RandomUtil.random(min, max);
    }

    public void generate(XSModel xsModel, QName rootElement, XMLDocument doc){
        generate(xsModel, rootElement, doc, null, null);
    }

    public void generate(XSModel xsModel, QName rootElement, XMLDocument doc, String xsiSchemaLocation, String xsiNoNamespaceSchemaLocation){
        String namespace = rootElement.getNamespaceURI();
        XSElementDeclaration root = xsModel.getElementDeclaration(rootElement.getLocalPart(), namespace);
        if(root==null)
            throw new IllegalArgumentException("Element "+rootElement+" is not found");

        Navigator navigator = new FilteredTreeNavigator(new XSSampleNavigator(xsModel), new XSDisplayFilter(){
            protected boolean process(XSElementDeclaration elem){
                return !elem.getAbstract();
            }

            protected boolean process(XSTypeDefinition type){
                return type.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE;
            }
        });
        try{
            doc.startDocument();
            doc.declarePrefix(Namespaces.URI_XSI);
            if(rootElement.getPrefix()!=null && !rootElement.getNamespaceURI().isEmpty()){
                if(!showContentModel || !rootElement.getPrefix().isEmpty())
                    doc.declarePrefix(rootElement.getPrefix(), rootElement.getNamespaceURI());
            }
            WalkerUtil.walk(new PreorderWalker(root, navigator), new XSSampleVisitor(doc, xsiSchemaLocation, xsiNoNamespaceSchemaLocation));
            doc.endDocument();
        }catch(SAXException ex){
            throw new ImpossibleException(ex);
        }
    }

    private class XSSampleNavigator extends XSNavigator{
        private XSModel xsModel;
        private XSSampleNavigator(XSModel xsModel){
            this.xsModel = xsModel;
        }

        protected Sequence<XSTerm> process(XSParticle particle){
            XSTerm term = particle.getTerm();
            if(term instanceof XSModelGroup){
                XSModelGroup group = (XSModelGroup)term;
                if(group.getCompositor()==XSModelGroup.COMPOSITOR_CHOICE){
                    XSObjectList particles = group.getParticles();
                    int count = particles.getLength();
                    if(!generateAllChoices && !particle.getMaxOccursUnbounded())
                        count = Math.min(count, particle.getMaxOccurs());
                    List<XSParticle> list = new ArrayList<XSParticle>(particles.getLength());
                    for(int i=0; i<particles.getLength(); i++)
                        list.add((XSParticle)particles.item(i));
                    Collections.shuffle(list);
                    return new IterableSequence(list.subList(0, count));
                }
            }

            int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
            int repeatCount = generateRepeatCount(particle.getMinOccurs(), maxOccurs);
            return new RepeatSequence<XSTerm>(super.process(particle), repeatCount);
        }

        protected Sequence<XSParticle> process(XSModelGroup modelGroup){
            switch(modelGroup.getCompositor()){
                case XSModelGroup.COMPOSITOR_ALL :
                    XSObjectList particles = modelGroup.getParticles();
                    List<XSParticle> list = new ArrayList<XSParticle>(particles.getLength());
                    for(int i=0; i<particles.getLength(); i++)
                        list.add((XSParticle)particles.item(i));
                    Collections.shuffle(list);
                    return new IterableSequence<XSParticle>(list);
                default:
                    return super.process(modelGroup);
            }
        }

        protected Sequence process(XSElementDeclaration elem){
            if(elem.getAbstract()){
                XSObjectList substitutionGroup = xsModel.getSubstitutionGroup(elem);
                int rand = RandomUtil.random(0, substitutionGroup.getLength() - 1);
                return new DuplicateSequence(substitutionGroup.item(rand));
            }
            if(elem.getTypeDefinition() instanceof XSComplexTypeDefinition){
                XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                if(complexType.getAbstract()){
                    List<XSComplexTypeDefinition> subTypes = XSUtil.getSubTypes(xsModel, complexType);
                    int rand = RandomUtil.random(0, subTypes.size() - 1);
                    return new DuplicateSequence<XSTypeDefinition>(subTypes.get(rand));
                }
            }
            return new DuplicateSequence<XSTypeDefinition>(elem.getTypeDefinition());
        }
    }

    private class XSSampleVisitor extends ReflectionVisitor<Object, Processor<Object>>{
        private XMLDocument doc;
        private String xsiSchemaLocation;
        private String xsiNoNamespaceSchemaLocation;

        private XSSampleVisitor(XMLDocument doc, String xsiSchemaLocation, String xsiNoNamespaceSchemaLocation){
            this.doc = doc;
            this.xsiSchemaLocation = xsiSchemaLocation;
            this.xsiNoNamespaceSchemaLocation = xsiNoNamespaceSchemaLocation;
        }

        private void addXSILocations() throws SAXException{
            if(doc.getDepth()==1){
                if(xsiSchemaLocation!=null)
                    doc.addAttribute(Namespaces.URI_XSI, "schemaLocation", xsiSchemaLocation);
                if(xsiNoNamespaceSchemaLocation!=null)
                    doc.addAttribute(Namespaces.URI_XSI, "noNamespaceSchemaLocation", xsiNoNamespaceSchemaLocation);
            }
        }

        @Override
        protected Processor getDefault(Object elem){
            return null;
        }

        protected Processor process(XSElementDeclaration elem){
            return elemProcessor;
        }

        protected Processor process(XSWildcard wildcard){
            return wildcardProcessor;
        }

        protected Processor process(XSComplexTypeDefinition complexType){
            return complexTypeProcessor;
        }

        protected Processor process(XSAttributeUse attr){
            return attrProcessor;
        }

        private Processor<XSElementDeclaration> elemProcessor = new Processor<XSElementDeclaration>(){
            private boolean isRecursionDepthCrossed(XSElementDeclaration elem, Path path){
                if(path.getRecursionDepth()>maximumRecursionDepth)
                    return true;

                int typeRecursionDepth = -1;
                while(path!=null){
                    if(path.getElement()==elem.getTypeDefinition())
                        typeRecursionDepth++;
                    path = path.getParentPath();
                }

                return typeRecursionDepth>maximumRecursionDepth;
            };


            @Override
            public boolean preProcess(XSElementDeclaration elem, Path path){
                if(isRecursionDepthCrossed(elem, path))
                    return false;
                try{
                    if(showContentModel && elem.getTypeDefinition() instanceof XSComplexTypeDefinition){
                        XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                        switch(complexType.getContentType()){
                            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                            case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
                                String contentModel = new XSContentModel().toString(complexType, doc.getNamespaceSupport());
                                boolean showContentModel = false;
                                for(char ch: "?*+|;[".toCharArray()){
                                    if(contentModel.indexOf(ch)!=-1){
                                        showContentModel = true;
                                        break;
                                    }
                                }
                                if(showContentModel){
                                    int depth = 0;
                                    while(true){
                                        path = path.getParentPath(XSElementDeclaration.class);
                                        if(path!=null)
                                            depth++;
                                        else
                                            break;
                                    }
                                    doc.addText("\n");
                                    for(int i=depth; i>0; i--)
                                        doc.addText("   ");
                                    doc.addComment(contentModel);
                                    doc.addText("\n");
                                    for(int i=depth; i>0; i--)
                                        doc.addText("   ");
                                }
                        }
                    }
                    doc.startElement(elem.getNamespace(), elem.getName());
                    addXSILocations();
                    return true;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSElementDeclaration elem, Path path){
                if(isRecursionDepthCrossed(elem, path))
                    return;
                try{
                    switch(elem.getConstraintType()){
                        case XSConstants.VC_FIXED:
                            doc.addText(elem.getValueConstraintValue().getNormalizedValue());
                            break;
                        case XSConstants.VC_DEFAULT:
                            if(RandomUtil.randomBoolean()){
                                doc.addText(elem.getValueConstraintValue().getNormalizedValue());
                                break;
                            }
                        default:
                            XSSimpleTypeDefinition simpleType = null;
                            if(elem.getTypeDefinition().getTypeCategory()==XSTypeDefinition.SIMPLE_TYPE)
                                simpleType = (XSSimpleTypeDefinition)elem.getTypeDefinition();
                            else{
                                XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                                if(complexType.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)
                                    simpleType = complexType.getSimpleType();
                            }
                            if(simpleType!=null){
                                String sampleValue = null;
                                if(sampleValueGenerator!=null)
                                    sampleValue = sampleValueGenerator.generateSampleValue(elem, simpleType);
                                if(sampleValue==null)
                                    sampleValue = generateSampleValue(simpleType, elem.getName());
                                doc.addText(sampleValue);
                            }
                    }
                    doc.endElement();
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }
        };

        private Processor<XSAttributeUse> attrProcessor = new Processor<XSAttributeUse>(){
            @Override
            public boolean preProcess(XSAttributeUse attr, Path path){
                try{
                    XSAttributeDeclaration decl = attr.getAttrDeclaration();

                    String sampleValue = null;
                    switch(attr.getConstraintType()){
                        case XSConstants.VC_FIXED:
                            if(RandomUtil.randomBoolean(generateFixedAttributes))
                                sampleValue = attr.getValueConstraintValue().getNormalizedValue();
                            break;
                        case XSConstants.VC_DEFAULT:
                            if(RandomUtil.randomBoolean(generateDefaultAttributes))
                                sampleValue = attr.getValueConstraintValue().getNormalizedValue();
                            break;
                        default:
                            if(attr.getRequired() || RandomUtil.randomBoolean(generateOptionalAttributes)){
                                if(sampleValueGenerator!=null)
                                    sampleValue = sampleValueGenerator.generateSampleValue(decl, decl.getTypeDefinition());
                                if(sampleValue==null)
                                    sampleValue = generateSampleValue(decl.getTypeDefinition(), decl.getName());
                            }
                    }
                    if(sampleValue!=null)
                        doc.addAttribute(decl.getNamespace(), decl.getName(), sampleValue);
                    return false;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSAttributeUse elem, Path path){}
        };

        private Processor<XSComplexTypeDefinition> complexTypeProcessor = new Processor<XSComplexTypeDefinition>(){
            @Override
            public boolean preProcess(XSComplexTypeDefinition complexType, Path path){
                try{
                    XSElementDeclaration elem = (XSElementDeclaration)path.getParentPath().getElement();
                    XSComplexTypeDefinition elemType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                    if(elemType.getAbstract())
                        doc.addAttribute(Namespaces.URI_XSI, "type", doc.toQName(complexType.getNamespace(), complexType.getName()));
                    return true;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSComplexTypeDefinition complexType, Path path){}
        };

        private Processor<XSWildcard> wildcardProcessor = new Processor<XSWildcard>(){
            @Override
            public boolean preProcess(XSWildcard wildcard, Path path){
                try{
                    String uri;
                    switch(wildcard.getConstraintType()){
                        case XSWildcard.NSCONSTRAINT_ANY:
                            uri = "anyNS";
                            break;
                        case XSWildcard.NSCONSTRAINT_LIST:
                            StringList list = wildcard.getNsConstraintList();
                            int rand = RandomUtil.random(0, list.getLength()-1);
                            uri = list.item(rand);
                            if(uri==null)
                                uri = ""; // <xs:any namespace="##local"/> returns nsConstraintList with null
                            break;
                        case XSWildcard.NSCONSTRAINT_NOT:
                            list = wildcard.getNsConstraintList();
                            List<String> namespaces = new ArrayList<String>();
                            for(int i=0; i<list.getLength(); i++)
                                namespaces.add(list.item(i));
                            uri = "anyNS";
                            if(namespaces.contains(uri)){
                                for(int i=1;;i++){
                                    if(!namespaces.contains(uri+i)){
                                        uri += i;
                                        break;
                                    }
                                }
                            }
                            break;
                        default:
                            throw new ImpossibleException();
                    }
                    if(isAttribute(wildcard, path))
                        doc.addAttribute(uri, "anyAttr", "anyValue");
                    else{
                        doc.startElement(uri, "anyElement");
                        addXSILocations();
                    }
                    return true;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSWildcard wildcard, Path path){
                try{
                    if(!isAttribute(wildcard, path))
                        doc.endElement();
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            private boolean isAttribute(XSWildcard wildcard, Path path){
                if(path.getParentPath().getElement() instanceof XSComplexTypeDefinition){
                    XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)path.getParentPath().getElement();
                    if(complexType.getAttributeWildcard()==wildcard)
                        return true;
                }
                return false;
            }
        };

        private Map<String, Integer> counters = new HashMap<String, Integer>();

        private static final String XSD_DATE_FORMAT = "yyyy-MM-dd";
        private static final String XSD_TIME_FORMAT = "HH:mm:ss";

        private String generateSampleValue(XSSimpleTypeDefinition simpleType, String hint){
            if(simpleType.getBuiltInKind()==XSConstants.LIST_DT){
                XSSimpleTypeDefinition itemType = simpleType.getItemType();

                int len;
                XSFacet facet = getFacet(itemType, XSSimpleTypeDefinition.FACET_LENGTH);
                if(facet!=null)
                    len = Integer.parseInt(facet.getLexicalFacetValue());
                else{
                    int minOccurs = 0;
                    facet = getFacet(itemType, XSSimpleTypeDefinition.FACET_MINLENGTH);
                    if(facet!=null)
                        minOccurs = Integer.parseInt(facet.getLexicalFacetValue());
                    int maxOccurs = -1;
                    facet = getFacet(itemType, XSSimpleTypeDefinition.FACET_MAXLENGTH);
                    if(facet!=null)
                        maxOccurs = Integer.parseInt(facet.getLexicalFacetValue());

                    if(maxOccurs==-1)
                        maxOccurs = Math.max(minOccurs, maximumListItemsGenerated);

                    int min, max;
                    if(minimumListItemsGenerated>maxOccurs || maximumListItemsGenerated<minOccurs){ // doesn't intersect
                        min = minOccurs;
                        max = maxOccurs;
                    }else { // find intersecting range
                        min = Math.max(minOccurs, minimumListItemsGenerated);
                        max = Math.min(maxOccurs, maximumListItemsGenerated);
                    }
                    len = (min == max)
                            ? min
                            : RandomUtil.random(min, max);
                }

                List<String> enums = XSUtil.getEnumeratedValues(itemType);
                if(enums.isEmpty()){
                    StringBuilder buff = new StringBuilder();
                    while(len>0){
                        buff.append(" ");
                        buff.append(generateSampleValue(itemType, hint));
                        len--;
                    }
                    return buff.toString().trim();
                }else{
                    while(enums.size()<len)
                        enums.addAll(new ArrayList<String>(enums));
                    Collections.shuffle(enums);

                    StringBuilder buff = new StringBuilder();
                    while(len>0){
                        buff.append(" ");
                        buff.append(enums.remove(0));
                        len--;
                    }
                    return buff.toString().trim();
                }
            }else if(simpleType.getMemberTypes().getLength()>0){
                XSObjectList members = simpleType.getMemberTypes();
                int rand = RandomUtil.random(0, members.getLength()-1);
                return generateSampleValue((XSSimpleTypeDefinition)members.item(rand), hint);
            }

            List<String> enums = XSUtil.getEnumeratedValues(simpleType);
            if(!enums.isEmpty())
                return enums.get(RandomUtil.random(0, enums.size()-1));

            XSSimpleTypeDefinition builtInType = simpleType;
            while(!Namespaces.URI_XSD.equals(builtInType.getNamespace()))
                builtInType = (XSSimpleTypeDefinition)builtInType.getBaseType();


            String name = builtInType.getName().toLowerCase();
            if("boolean".equals(name))
                return RandomUtil.randomBoolean() ? "true" : "false";

            if("double".equals(name)
                    || "decimal".equals(name)
                    || "float".equals(name)
                    || name.endsWith("integer")
                    || name.endsWith("int")
                    || name.endsWith("long")
                    || name.endsWith("short")
                    || name.endsWith("byte"))
                return new Range(simpleType).randomNumber();

            if("date".equals(name))
                return new SimpleDateFormat(XSD_DATE_FORMAT).format(new Date());
            if("time".equals(name))
                return new SimpleDateFormat(XSD_TIME_FORMAT).format(new Date());
            if("datetime".equals(name)){
                Date date = new Date();
                return new SimpleDateFormat(XSD_DATE_FORMAT).format(date)+'T'+new SimpleDateFormat(XSD_TIME_FORMAT).format(date);
            }else{
                Integer count = counters.get(hint);
                count = count==null ? 1 : ++count;
                counters.put(hint, count);
                String countStr = count.toString();
                XSFacet facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MINLENGTH);
                if(facet!=null){
                    int len = Integer.parseInt(facet.getLexicalFacetValue());
                    len -= hint.length();
                    len -= countStr.length();
                    if(len>0){
                        char ch[] = new char[len];
                        Arrays.fill(ch, '_');
                        hint += new String(ch);
                    }
                }
                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXLENGTH);
                if(facet!=null){
                    int maxLen = Integer.parseInt(facet.getLexicalFacetValue());
                    int len = maxLen;
                    len = hint.length() + countStr.length() - len;
                    if(len>0){
                        if(hint.length()>len)
                            hint = hint.substring(0, hint.length()-len);
                        else{
                            hint = hint.substring(0, maxLen);
                            countStr = "";
                        }
                    }
                }
                return hint+countStr;
            }
        }

        private XSFacet getFacet(XSSimpleTypeDefinition simpleType, int kind){
            XSObjectList facets = simpleType.getFacets();
            for(int i=0; i<facets.getLength(); i++){
                XSFacet facet = (XSFacet)facets.item(i);
                if(facet.getFacetKind()==kind)
                    return facet;
            }
            return null;
        }

        class Range{
            String minInclusive;
            String minExclusive;
            String maxInclusive;
            String maxExclusive;
            int totalDigits = -1;
            int fractionDigits = -1;

            Range(XSSimpleTypeDefinition simpleType){
                XSFacet facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MININCLUSIVE);
                if(facet!=null)
                    minInclusive = facet.getLexicalFacetValue();
                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MINEXCLUSIVE);
                if(facet!=null)
                    minExclusive = facet.getLexicalFacetValue();

                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXINCLUSIVE);
                if(facet!=null)
                    maxInclusive = facet.getLexicalFacetValue();
                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE);
                if(facet!=null)
                    maxExclusive = facet.getLexicalFacetValue();

                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_TOTALDIGITS);
                if(facet!=null)
                    totalDigits = Integer.parseInt(facet.getLexicalFacetValue());

                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_FRACTIONDIGITS);
                if(facet!=null)
                    fractionDigits = Integer.parseInt(facet.getLexicalFacetValue());
            }

            private String applyDigits(Object obj){
                String str = applyExponent(String.valueOf(obj));
                String number, fraction;
                int dot = str.indexOf(".");
                if(dot==-1){
                    number = str;
                    fraction = "";
                }else{
                    number = str.substring(0, dot);
                    fraction = str.substring(dot+1);
                }
                boolean negative = false;
                if(number.startsWith("-")){
                    negative = true;
                    number = number.substring(1);
                }
                if(totalDigits>=0){
                    if(number.length()>totalDigits)
                        number = number.substring(0, totalDigits);
                }
                if(fractionDigits>=0){
                    if(fraction.length()>fractionDigits)
                        fraction = fraction.substring(0, fractionDigits);
                }

                str = negative ? "-" : "";
                str += number;
                if(fraction.length()>0)
                    str += '.' + fraction;
                return str;
            }

            private String applyExponent(String str){
                int index = str.indexOf('E');
                if(index==-1)
                    return str;

                int exponent = Integer.parseInt(str.substring(index+(str.charAt(index+1)=='+'?2:1)));
                str = str.substring(0, index);

                boolean negative = false;
                if(str.charAt(0)=='-'){
                    negative = true;
                    str = str.substring(1);
                }

                if(exponent!=0){
                    int dot = str.indexOf('.');
                    String beforeDot, afterDot;
                    if(dot==-1){
                        beforeDot = str;
                        afterDot = "";
                    }else{
                        beforeDot = str.substring(0, dot);
                        afterDot = str.substring(dot+1);
                    }

                    if(exponent<0){
                        while(exponent!=0){
                            if(beforeDot.length()==1)
                                beforeDot = "0"+beforeDot;
                            afterDot = beforeDot.substring(beforeDot.length()-1)+afterDot;
                            beforeDot = beforeDot.substring(0, beforeDot.length()-1);
                            exponent++;
                        }
                    }else{
                        while(exponent!=0){
                            if(afterDot.isEmpty())
                                afterDot = "0";
                            beforeDot = beforeDot+afterDot.substring(0, 1);
                            afterDot = afterDot.substring(1);
                            exponent--;
                        }
                    }
                    str = afterDot.isEmpty() ? beforeDot : beforeDot+"."+afterDot;
                }
                if(negative)
                    str = "-"+str;

                return str;
            }

            public String randomNumber(){
                if(fractionDigits==0){
                    // NOTE: min/max facets can have fractional part
                    //       even though fractionDigits is zero
                    long min = Long.MIN_VALUE;
                    if(minInclusive!=null)
                        min = (long)Double.parseDouble(minInclusive);
                    if(minExclusive!=null)
                        min = (long)Double.parseDouble(minExclusive)+1;

                    long max = Long.MAX_VALUE;
                    if(maxInclusive!=null)
                        max = (long)Double.parseDouble(maxInclusive);
                    if(maxExclusive!=null)
                        max = (long)Double.parseDouble(maxExclusive)-1;

                    return applyDigits(RandomUtil.random(min, max));
                }else{
                    double min = Double.MIN_VALUE;
                    if(minInclusive!=null)
                        min = Double.parseDouble(minInclusive);
                    if(minExclusive!=null)
                        min = Double.parseDouble(minExclusive)+1;

                    double max = Double.MAX_VALUE;
                    if(maxInclusive!=null)
                        max = Double.parseDouble(maxInclusive);
                    if(maxExclusive!=null)
                        max = Double.parseDouble(maxExclusive)-1;

                    return applyDigits(RandomUtil.random(min, max));
                }
            }
        }
    }

    public void loadOptions(Properties options){
        String value = options.getProperty("minimumElementsGenerated");
        if(value!=null)
            minimumElementsGenerated = Integer.parseInt(value);
        value = options.getProperty("maximumElementsGenerated");
        if(value!=null)
            maximumElementsGenerated = Integer.parseInt(value);
        value = options.getProperty("minimumElementsGenerated");
        if(value!=null)
            minimumListItemsGenerated = Integer.parseInt(value);
        value = options.getProperty("maximumListItemsGenerated");
        if(value!=null)
            maximumListItemsGenerated = Integer.parseInt(value);
        value = options.getProperty("maximumRecursionDepth");
        if(value!=null)
            maximumRecursionDepth = Integer.parseInt(value);

        value = options.getProperty("generateOptionalElements");
        if(value!=null)
            generateOptionalElements = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateOptionalAttributes");
        if(value!=null)
            generateOptionalAttributes = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateFixedAttributes");
        if(value!=null)
            generateFixedAttributes = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateOptionalElements");
        if(value!=null)
            generateOptionalElements = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateDefaultAttributes");
        if(value!=null)
            generateDefaultAttributes = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateAllChoices");
        if(value!=null)
            generateDefaultAttributes = Boolean.parseBoolean(value);
        value = options.getProperty("showContentModel");
        if(value!=null)
            showContentModel = Boolean.parseBoolean(value);
    }

    public SampleValueGenerator sampleValueGenerator;

    public static interface SampleValueGenerator{
        public String generateSampleValue(XSElementDeclaration element, XSSimpleTypeDefinition simpleType);
        public String generateSampleValue(XSAttributeDeclaration attribute, XSSimpleTypeDefinition simpleType);
    }

    public static void main(String[] args) throws Exception{
        if(args.length==0){
            System.err.println("Usage:");
            System.err.println("\txsd-instance."+(OS.get().isWindows()?"bat":"sh")+" <xsd-file> [root-element]");
            System.err.println("Example:");
            System.err.println("\txsd-instance."+(OS.get().isWindows()?"bat":"sh")+" purchase-order.xsd {http://jlibs.org}PurchaseOrder");
            System.exit(1);
        }

        XSModel xsModel = new XSParser().parse(args[0]);
        QName rootElement = null;
        if(args.length>1)
            rootElement = QName.valueOf(args[1]);
        else{
            List<XSElementDeclaration> elements = XSUtil.guessRootElements(xsModel);
            if(elements.size()==0){
                System.err.println("no elements found in given xml schema");
                System.exit(1);
            }else if(elements.size()==1){
                XSElementDeclaration elem = elements.get(0);
                rootElement = XSUtil.getQName(elem);
            }else{
                int i = 1;
                for(XSElementDeclaration elem: elements)
                    System.err.println(i++ +": "+XSUtil.getQName(elem));
                System.err.print("Select Root Element: ");
                String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
                XSElementDeclaration elem = elements.get(Integer.parseInt(line)-1);
                rootElement = XSUtil.getQName(elem);
            }
        }

        XSInstance xsInstance = new XSInstance();
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("xsd-instance.properties");
        if(is!=null)
            xsInstance.loadOptions(CollectionUtil.readProperties(is, null));
        xsInstance.generate(xsModel, rootElement, new XMLDocument(new StreamResult(System.out), true, 4, null));
        System.out.println();
    }
}
