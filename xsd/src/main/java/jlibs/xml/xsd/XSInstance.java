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

package jlibs.xml.xsd;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Path;
import jlibs.core.graph.Processor;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.WalkerUtil;
import jlibs.core.graph.navigators.FilteredTreeNavigator;
import jlibs.core.graph.sequences.DuplicateSequence;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.graph.sequences.RepeatSequence;
import jlibs.core.graph.visitors.ReflectionVisitor;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.core.io.IOUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.OS;
import jlibs.core.net.URLUtil;
import jlibs.core.util.CollectionUtil;
import jlibs.core.util.RandomUtil;
import jlibs.xml.Namespaces;
import jlibs.xml.XMLUtil;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.display.XSDisplayFilter;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSFacet;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

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

    // TRUE=always, FALSE=never, null=random
    public Boolean generateOptionalElements = Boolean.TRUE;
    public Boolean generateOptionalAttributes = Boolean.TRUE;
    public Boolean generateFixedAttributes = Boolean.TRUE;
    public Boolean generateDefaultAttributes = Boolean.TRUE;
    public Boolean generateDefaultElementValues = Boolean.TRUE;

    public boolean generateAllChoices = false;

    // TRUE=always, FALSE=never, null=when_appropriate
    public Boolean showContentModel = null;

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
                if(!Boolean.FALSE.equals(showContentModel) || !rootElement.getPrefix().isEmpty())
                    doc.declarePrefix(rootElement.getPrefix(), rootElement.getNamespaceURI());
            }

            XSNamespaceItemList namespaceItems = xsModel.getNamespaceItems();
            for(int i=0; i<namespaceItems.getLength(); i++){
                XSNamespaceItem namespaceItem = namespaceItems.item(i);
                if(!Namespaces.URI_XSD.equals(namespaceItem.getSchemaNamespace()))
                    doc.declarePrefix(namespaceItem.getSchemaNamespace());
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
                if(substitutionGroup.getLength()==0)
                    return EmptySequence.getInstance();
                int rand = RandomUtil.random(0, substitutionGroup.getLength() - 1);
                return new DuplicateSequence(substitutionGroup.item(rand));
            }
            if(elem.getTypeDefinition() instanceof XSComplexTypeDefinition){
                XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                if(complexType.getAbstract()){
                    XSTypeDefinition subType = null;
                    if(sampleValueGenerator!=null)
                        subType = sampleValueGenerator.selectSubType(elem);
                    if(subType==null){
                        List<XSComplexTypeDefinition> subTypes = XSUtil.getSubTypes(xsModel, complexType);
                        if(subTypes.isEmpty())
                            return EmptySequence.getInstance();
                        int rand = RandomUtil.random(0, subTypes.size() - 1);
                        subType = subTypes.get(rand);
                    }
                    return new DuplicateSequence<XSTypeDefinition>(subType);
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
                    if(!Boolean.FALSE.equals(showContentModel) && elem.getTypeDefinition() instanceof XSComplexTypeDefinition){
                        XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();

                        StringBuilder contentModel = new StringBuilder();

                        XSObjectList attributeUses = complexType.getAttributeUses();
                        if(!attributeUses.isEmpty()){
                            contentModel.append("@(");
                            for(int i=0; i<attributeUses.getLength(); i++){
                                XSAttributeUse attrUse = (XSAttributeUse)attributeUses.get(i);
                                if(i>0)
                                    contentModel.append(", ");
                                contentModel.append(XMLUtil.getQName(XSUtil.getQName(attrUse, doc.getNamespaceSupport())));
                                if(!attrUse.getRequired())
                                    contentModel.append('?');
                            }
                            contentModel.append(")");
                        }

                        switch(complexType.getContentType()){
                            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                            case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
                                if(contentModel.length()>0)
                                    contentModel.append(" ");
                                contentModel.append(new XSContentModel().toString(complexType, doc));
                        }

                        if(contentModel.length()>0){
                            boolean addComment = false;
                            if(Boolean.TRUE.equals(showContentModel))
                                addComment = true;
                            else{
                                for(char ch: "?*+|;[".toCharArray()){
                                    if(contentModel.indexOf(String.valueOf(ch))!=-1){
                                        addComment = true;
                                        break;
                                    }
                                }
                            }
                            if(addComment)
                                addComment(path, contentModel.toString());
                        }
                    }
                    doc.startElement(elem.getNamespace(), elem.getName());
                    addXSILocations();
                    return true;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            private void addComment(Path path, String comment) throws SAXException{
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
                doc.addComment(comment);
                doc.addText("\n");
                for(int i=depth; i>0; i--)
                    doc.addText("   ");
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
                            if(RandomUtil.randomBoolean(generateDefaultElementValues)){
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
                                    sampleValue = sampleValueGenerator.generateSampleValue(elem, simpleType, path);
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
                                    sampleValue = sampleValueGenerator.generateSampleValue(decl, decl.getTypeDefinition(), path);
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
                if(wildcard.getProcessContents()==XSWildcard.PC_STRICT)
                    return true;
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
                if(wildcard.getProcessContents()==XSWildcard.PC_STRICT)
                    return;
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
                return randomNumber(simpleType, name);

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

                XSFacet lengthFacet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_LENGTH);

                XSFacet facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MINLENGTH);
                if(facet==null)
                    facet = lengthFacet;
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
                if(facet==null)
                    facet = lengthFacet;
                if(facet!=null){
                    int maxLen = Integer.parseInt(facet.getLexicalFacetValue());
                    int len = maxLen;
                    len = hint.length() + countStr.length() - len;
                    if(len>0){
                        if(hint.length()>len)
                            hint = hint.substring(0, hint.length()-len);
                        else{
                            hint = hint.length()>maxLen ? hint.substring(0, maxLen) : hint;
                            countStr = "";
                        }
                    }
                }
                String value = hint+countStr;

                if("base64binary".equals(name))
                    return DatatypeConverter.printBase64Binary(value.getBytes(IOUtil.UTF_8));
                else
                    return value;
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

        private String randomNumber(XSSimpleTypeDefinition simpleType, String builtinName){
            boolean exponentAllowed = "double".equals(builtinName) || "float".equals(builtinName);

            XSFacet minInclusive = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MININCLUSIVE);

            XSFacet minExclusive = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MINEXCLUSIVE);

            XSFacet maxInclusive = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXINCLUSIVE);

            XSFacet maxExclusive = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE);

            int totalDigits = -1;
            XSFacet facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_TOTALDIGITS);
            if (facet != null)
                totalDigits = facet.getIntFacetValue();

            int fractionDigits = -1;
            facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_FRACTIONDIGITS);
            if (facet != null)
                fractionDigits = facet.getIntFacetValue();

            Object randomNumber;
            if(fractionDigits==0){
                // NOTE: min/max facets can have fractional part even though fractionDigits is zero
                Long min = null;
                if (minInclusive != null)
                    min = new BigDecimal(minInclusive.getLexicalFacetValue()).longValue();
                if (minExclusive != null)
                    min = new BigDecimal(minExclusive.getLexicalFacetValue()).longValue() + 1;

                Long max = null;
                if (maxInclusive != null)
                    max = new BigDecimal(maxInclusive.getLexicalFacetValue()).longValue();
                if (maxExclusive != null)
                    max = new BigDecimal(maxExclusive.getLexicalFacetValue()).longValue() - 1;

                if(min==null && max==null){
                    min = -1000L;
                    max = 1000L;
                }else if(min==null)
                    min = Math.max(Long.MIN_VALUE, max - 1000);
                else if(max==null)
                    max = Math.min(Long.MAX_VALUE, min+1000);

                randomNumber = RandomUtil.random(min, max);
            }else{
                Double min = null;
                if (minInclusive != null)
                    min = Double.parseDouble(minInclusive.getLexicalFacetValue());
                if (minExclusive != null)
                    min = Double.parseDouble(minExclusive.getLexicalFacetValue()) + 1;

                Double max = null;
                if (maxInclusive != null)
                    max = Double.parseDouble(maxInclusive.getLexicalFacetValue());
                if (maxExclusive != null)
                    max = Double.parseDouble(maxExclusive.getLexicalFacetValue()) - 1;

                if(min==null && max==null){
                    min = -1000d;
                    max = 1000d;
                }else if(min==null)
                    min = Math.max(Double.MIN_VALUE, max-1000);
                else if(max==null)
                    max = Math.min(Double.MAX_VALUE, min+1000);

                randomNumber = RandomUtil.random(min, max);
            }

            String str;
            if(randomNumber instanceof Double && !exponentAllowed)
                str = String.format(Locale.US, "%."+(fractionDigits>=0?fractionDigits:3)+"f", (Double)randomNumber);
            else
                str = String.valueOf(randomNumber);
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
    }

    public void loadOptions(Properties options){
        String value = options.getProperty("minimumElementsGenerated");
        if(value!=null)
            minimumElementsGenerated = Integer.parseInt(value);
        value = options.getProperty("maximumElementsGenerated");
        if(value!=null)
            maximumElementsGenerated = Integer.parseInt(value);
        value = options.getProperty("minimumListItemsGenerated");
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
        value = options.getProperty("generateDefaultAttributes");
        if(value!=null)
            generateDefaultAttributes = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateDefaultElementValues");
        if(value!=null)
            generateDefaultElementValues = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateAllChoices");
        if(value!=null)
            generateAllChoices = Boolean.parseBoolean(value);
        value = options.getProperty("showContentModel");
        if(value!=null)
            showContentModel = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
    }

    public SampleValueGenerator sampleValueGenerator;

    public static interface SampleValueGenerator{
        public String generateSampleValue(XSElementDeclaration element, XSSimpleTypeDefinition simpleType, Path path);
        public String generateSampleValue(XSAttributeDeclaration attribute, XSSimpleTypeDefinition simpleType, Path path);
        public XSTypeDefinition selectSubType(XSElementDeclaration element);
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
        XMLDocument xml = new XMLDocument(new StreamResult(System.out), true, 4, null);
        XSUtil.suggestNamespacePrefixes(URLUtil.toURL(args[0]), xml);
        xsInstance.generate(xsModel, rootElement, xml);
        System.out.println();
    }
}
