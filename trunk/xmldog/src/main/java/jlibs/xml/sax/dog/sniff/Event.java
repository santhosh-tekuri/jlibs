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

package jlibs.xml.sax.dog.sniff;

import jlibs.core.lang.NotImplementedException;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.NodeType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.*;
import jlibs.xml.sax.dog.expr.nodset.NodeSet;
import jlibs.xml.sax.dog.expr.nodset.NodeSetListener;
import jlibs.xml.sax.dog.expr.nodset.PositionTracker;
import jlibs.xml.sax.dog.expr.nodset.StringEvaluation;
import jlibs.xml.sax.dog.path.EventID;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import org.xml.sax.Attributes;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public final class Event extends EvaluationListener implements NodeSetListener{
    private List<Expression> exprList;
    private EventID.ConstraintEntry listenersArray[][];

    @SuppressWarnings({"unchecked"})
    public Event(NamespaceContext givenNSContext, List<Expression> exprList, int noOfConstraints, boolean documentRequired){
        this.givenNSContext = givenNSContext;
        this.exprList = exprList;

        int noOfXPaths = exprList.size();
        results = new Object[noOfXPaths];
        pendingInstantResults = new int[noOfXPaths];
        listeners = new List[noOfXPaths];
        instantListenersCount = new int[noOfXPaths];
        finished = new BitSet(noOfXPaths);
        listenersArray = new EventID.ConstraintEntry[6][noOfConstraints];
        if(documentRequired)
            docNodeItem = nodeItem = new NodeItem();
    }

    public NamespaceContext getNamespaceContext(){
        return nsContext;
    }

    /*-------------------------------------------------[ Information ]---------------------------------------------------*/

    private long order;
    private int type;
    private String namespaceURI;
    private String localName;
    private String qualifiedName;
    private String value;

    public long order(){
        return order;
    }

    public int type(){
        return type;
    }

    public String namespaceURI(){
        return namespaceURI;
    }

    public String localName(){
        return localName;
    }

    public String qualifiedName(){
        return qualifiedName;
    }

    public String value(){
        if(type==NodeType.TEXT && value==null)
            value = buff.length()>0 ? buff.toString() : null;
        return value;
    }

    public String language(){
        return tailInfo==null ? "" : tailInfo.lang;
    }

    private StringBuilder elementLocation = new StringBuilder();
    private Info locationInfo;
    public String location(){
        if(type==NodeType.DOCUMENT)
            return "/";

        StringBuilder elementLocation = this.elementLocation;

        // update elementLocation
        Info info = locationInfo.next;
        while(info!=null){
            assert info.slash==-1;
            info.slash = elementLocation.length();
            elementLocation.append('/');
            elementLocation.append(info.elem).append('[').append(info.elemntPos).append(']');
            info = info.next;
        }
        locationInfo = tailInfo;

        int len;
        switch(type){
            case NodeType.ELEMENT:
                return elementLocation.toString();
            case NodeType.ATTRIBUTE:
                len = elementLocation.length();
                elementLocation.append("/@").append(qname(namespaceURI, localName));
                break;
            case NodeType.NAMESPACE:
                len = elementLocation.length();
                elementLocation.append("/namespace::").append(localName);
                break;
            case NodeType.TEXT:
                len = elementLocation.length();
                elementLocation.append("/text()[").append(tailInfo.textCount).append(']');
                break;
            case NodeType.COMMENT:
                len = elementLocation.length();
                elementLocation.append("/comment()[").append(tailInfo.commentCount).append(']');
                break;
            case NodeType.PI:
                len = elementLocation.length();
                elementLocation.append("/processing-instruction('").append(localName).append("')[").append(tailInfo.piMap.get(localName).value).append(']');
                break;
            default:
                throw new NotImplementedException();
        }

        String location = elementLocation.toString();
        elementLocation.setLength(len);
        return location;
    }

    @Override
    public String toString(){
        return location();
    }

    /*-------------------------------------------------[ IDList ]---------------------------------------------------*/

    private EventID current;

    public EventID getID(){
        if(current==null){
            current = new EventID(type, listenersArray);
//            current.location = location();
        }
        return current;
    }

    private boolean interestedInAttributes;
    private boolean interestedInNamespaces;
    private boolean interestedInText;

    private void fireEvent(){
        EventID id = current;
        current = null;

        EventID firstID = null;
        EventID activeID = null;

        boolean interestedInAttributes = false;
        boolean interestedInNamespaces = false;
        boolean interestedInText = false;
        do{
            if(!id.onEvent(this)){
                if(firstID==null)
                    firstID = id;
                else
                    activeID.previous = id;

                activeID = id;
                interestedInAttributes |= id.interestedInAttributes;
                interestedInNamespaces |= id.interestedInNamespaces;
                interestedInText |= id.interestedInText>0;
            }
            id = id.previous;
        }while(id!=null);
        if(activeID!=null)
            activeID.previous = null;

        EventID current = this.current;
        if(current!=null && current.axisEntryCount!=0){
            current.previous = firstID;
            current.listenersAdded();
            interestedInAttributes |= current.interestedInAttributes;
            interestedInNamespaces |= current.interestedInNamespaces;
            interestedInText |= current.interestedInText>0;
        }else
            this.current = firstID;

        this.interestedInAttributes = interestedInAttributes;
        this.interestedInNamespaces = interestedInNamespaces;
        this.interestedInText = interestedInText;
    }

    private void firePush(){
        EventID id = current;
        EventID firstID = null;
        EventID activeID = null;

        boolean interestedInAttributes = false;
        boolean interestedInNamespaces = false;
        boolean interestedInText = false;
        do{
            if(!id.push()){
                if(firstID==null)
                    firstID = id;
                else
                    activeID.previous = id;

                activeID = id;
                interestedInAttributes |= id.interestedInAttributes;
                interestedInNamespaces |= id.interestedInNamespaces;
                interestedInText |= id.interestedInText>0;
            }
            id = id.previous;
        }while(id!=null);
        if(activeID!=null)
            activeID.previous = null;
        current = firstID;

        this.interestedInAttributes = interestedInAttributes;
        this.interestedInNamespaces = interestedInNamespaces;
        this.interestedInText = interestedInText;
    }

    private void firePop(){
        EventID id = current;
        EventID firstID = null;
        EventID activeID = null;

        boolean interestedInAttributes = false;
        boolean interestedInNamespaces = false;
        boolean interestedInText = false;
        boolean doc = tailInfo==null;
        do{
            if(!id.pop(doc)){
                if(firstID==null)
                    firstID = id;
                else
                    activeID.previous = id;

                activeID = id;
                interestedInAttributes |= id.interestedInAttributes;
                interestedInNamespaces |= id.interestedInNamespaces;
                interestedInText |= id.interestedInText>0;
            }
            id = id.previous;
        }while(id!=null);
        if(activeID!=null)
            activeID.previous = null;
        current = firstID;

        this.interestedInAttributes = interestedInAttributes;
        this.interestedInNamespaces = interestedInNamespaces;
        this.interestedInText = interestedInText;
    }

    /*-------------------------------------------------[ NodeItem ]---------------------------------------------------*/

    private NodeItem nodeItem;

    public NodeItem nodeItem(){
        if(nodeItem==null)
            nodeItem = new NodeItem(this);
        return nodeItem;
    }

    /*-------------------------------------------------[ XMLBuilder ]---------------------------------------------------*/

    private XMLBuilder xmlBuilder = null;
    private NodeItem docNodeItem;
    public NodeItem documentNodeItem(){
        return docNodeItem;
    }

    public void setXMLBuilder(XMLBuilder xmlBuilder){
        this.xmlBuilder = xmlBuilder;
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    private boolean isXMLRequired(){
        if(xmlBuilder==null)
            return false;
        else if(xmlBuilder.active)
            return true;
        else
            return xmlBuilder.active = nodeItem!=null;
    }

    private void notifyXMLBuilder(){
        if(xmlBuilder==null)
            return;
        else if(!xmlBuilder.active && nodeItem==null)
            return;
        Object xml = xmlBuilder.onEvent(this);
        if(nodeItem!=null){
            nodeItem.xml = xml;
            nodeItem.xmlBuilt = true;
            finishedXMLBuild(nodeItem);
        }
    }

    /*-------------------------------------------------[ NodeSetListener ]---------------------------------------------------*/

    @Override
    public void mayHit(){
        nodeItem.refCount++;
    }

    @Override
    public void discard(long order){
        xmlBuilder.discard(order);
    }

    @Override
    public void finished(){}

    /*-------------------------------------------------[ Results ]---------------------------------------------------*/

    private final Object results[];
    private final int pendingInstantResults[];
    private final BitSet finished;
    private int pendingExpressions;

    public static final RuntimeException STOP_PARSING = new RuntimeException("STOP_PARSING");
    private boolean stopped;

    @Override
    public void finished(Evaluation evaluation){
        assert evaluation.expression.scope()==Scope.DOCUMENT;
        assert hasInstantListener(evaluation.expression) ? evaluation.getResult()==null : evaluation.getResult()!=null;
        assert pendingExpressions>0;

        int id = evaluation.expression.id;
        assert results[id]==null || results[id]==evaluation; // null for StaticEvaluation

//        store result if this doc expression is used in some predicate
        boolean needEvaluation = false;
        finished.set(id);
        List<EvaluationListener> listeners = this.listeners[id];
        if(listeners!=null){
            boolean hasPendingInstantResults = pendingInstantResults[id]!=0;
            boolean clearListeners = true;
            for(EvaluationListener listener: listeners){
                if(!listener.disposed){
                    if(listener instanceof InstantEvaluationListener && hasPendingInstantResults){
                        clearListeners = false;
                        continue;
                    }
                    listener.finished(evaluation);
                }
            }
            if(clearListeners)
                this.listeners[id] = null;
            else
                needEvaluation = true;
        }
        if(!needEvaluation)
            results[id] = evaluation.expression.storeResult ? evaluation.getResult() : null;
        if(--pendingExpressions==0 && tailInfo!=null){
            tailInfo = null;
            if(xmlBuilder==null)
                throw STOP_PARSING;
            stopped = true;
        }
    }

    public static final Object DUMMY_VALUE = new Object();

    public void onInstantResult(Expression expression, NodeItem nodeItem){
        BitSet bitSet = nodeItem.expressions;
        if(bitSet==null){
            nodeItem.expressions = bitSet = new BitSet();
            bitSet.set(expression.id);
        }else if(bitSet.get(expression.id))
            return;
        else
            bitSet.set(expression.id);

        if(xmlBuilder==null || nodeItem.xmlBuilt)
            fireInstantResult(expression, nodeItem);
        else
            pendingInstantResults[expression.id]++;
    }

    private void fireInstantResult(Expression expression, NodeItem nodeItem){
        if(expression.getXPath()==null) // non-user given absolute xpath
            return;
        List<EvaluationListener> listeners = this.listeners[expression.id];
        for(EvaluationListener listener: listeners){
            if(!listener.disposed && listener instanceof InstantEvaluationListener)
                ((InstantEvaluationListener)listener).onNodeHit(expression, nodeItem);
        }
    }

    public void finishedXMLBuild(NodeItem nodeItem){
        nodeItem.xmlBuilt = true;
        BitSet bitSet = nodeItem.expressions;
        if(bitSet==null)
            return;

        int i=0;
        while(true){
            i = bitSet.nextSetBit(i);
            if(i==-1)
                return;
            fireInstantResult(exprList.get(i), nodeItem);
            pendingInstantResults[i]--;
            if(finished.get(i) && pendingInstantResults[i]==0){
                List<EvaluationListener> listeners = this.listeners[i];
                if(listeners!=null){
                    this.listeners[i] = null;
                    for(EvaluationListener listener: listeners){
                        if(!listener.disposed && listener instanceof InstantEvaluationListener)
                            listener.finished((Evaluation)results[i]);
                    }
                    results[i] = null;
                }
            }
            i++;
        }
    }

    private EvaluationListener listener;
    public void setListener(EvaluationListener listener){
        if(this.listener!=null)
            throw new IllegalStateException("EvaluationListener can be set only once");
        this.listener = listener;
        for(Expression expr: exprList)
            addListener(expr, this.listener);
    }

    public EvaluationListener getListener(){
        return listener;
    }

    private final List<EvaluationListener> listeners[];
    private final int instantListenersCount[];
    public Evaluation addListener(Expression expr, EvaluationListener evaluationListener){
        assert expr.scope()==Scope.DOCUMENT;
        int id = expr.id;

        List<EvaluationListener> listeners = this.listeners[id];
        if(listeners==null)
            this.listeners[id] = listeners=new ArrayList<EvaluationListener>();
        listeners.add(evaluationListener);
        if(supportsInstantResults(expr) && evaluationListener instanceof InstantEvaluationListener)
            instantListenersCount[id]++;

        Object value = results[id];
        if(value instanceof Evaluation)
            return (Evaluation)value;
        else{
            if(value==null)
                return null;
            else
                throw new IllegalStateException();
        }
    }

    public void removeListener(Expression expr, EvaluationListener evaluationListener){
        List<EvaluationListener> listeners = this.listeners[expr.id];
        if(listeners!=null){
            if(listeners.remove(evaluationListener)){
                if(supportsInstantResults(expr) && evaluationListener instanceof InstantEvaluationListener)
                    instantListenersCount[expr.id]--;
            }
        }else
            evaluationListener.disposed = true;
    }

    private boolean supportsInstantResults(Expression expr){
        return expr instanceof NodeSet;
    }

    public boolean hasInstantListener(Expression expr){
        return supportsInstantResults(expr) && instantListenersCount[expr.id]>0;
    }

    public Object result(Expression expr){
        assert expr.scope()==Scope.DOCUMENT;
        return results[expr.id];
    }
    
    /*-------------------------------------------------[ OnEvent ]---------------------------------------------------*/

    void setData(int type, String namespaceURI, String localName, String qualifiedName, String value){
        nodeItem = null;

        this.type = type;
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.qualifiedName = qualifiedName;
        this.value = value;
    }

    private void onEvent(int type, String namespaceURI, String localName, String qualifiedName, String value){
        nodeItem = null;
        order++;

        this.type = type;
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.qualifiedName = qualifiedName;
        this.value = value;

        if(!stopped && type!=NodeType.ELEMENT)
            fireEvent();
    }

    public void onStartDocument(){
        int noOfXPaths = exprList.size();
        pendingExpressions = noOfXPaths;
        nsContext = new DefaultNamespaceContext();
        locationInfo = tailInfo = new Info();
        tailInfo.lang = "";
        tailInfo.slash = 0;

        order = 0L;
        type = NodeType.DOCUMENT;
        value = namespaceURI = localName = qualifiedName = "";

        Object results[] = this.results;
        for(int i=noOfXPaths-1; i>=0; i--){
            Expression expression = exprList.get(i);
            Object result = expression.getResult(this);
            if(result instanceof Evaluation){
                results[i] = result;
                Evaluation eval = (Evaluation)result;
                eval.addListener(this);
                if(xmlBuilder!=null && expression.resultType==DataType.NODESET && eval instanceof NodeSetListener.Support)
                    ((Support)eval).setNodeSetListener(this);
                eval.start();
            }else{
                Evaluation eval;
                if(expression.resultType==DataType.NODESET && hasInstantListener(expression)){
                    onInstantResult(expression, nodeItem);
                    results[i] = eval = new StaticEvaluation<Expression>(expression, order, null);
                }else
                    eval = new StaticEvaluation<Expression>(expression, order, result);
                finished(eval);
            }
        }
        current.listenersAdded();
        firePush();
        if(isXMLRequired())
            nodeItem.xml = xmlBuilder.doStartDocument(nodeItem);
        else if(stopped)
            throw STOP_PARSING;
    }

    public void onEndDocument(){
        if(!stopped)
            pop();
        assert pendingExpressions==0;
        assert tailInfo==null;
        if(xmlBuilder!=null)
            xmlBuilder.doEndDocument(this);
    }

    public void onStartElement(String uri, String localName, String qualifiedName, String lang){
        onEvent(NodeType.ELEMENT, uri, localName, qualifiedName, null);

        if(!stopped){
            Info info = new Info();
            info.elem = qname(uri, localName);
            info.elemntPos = tailInfo.updateElementPosition(info.elem);
            info.lang = lang!=null ? lang : language();

            push(info);
            fireEvent();
        }
        if(!stopped)
            firePush();
        if(isXMLRequired()){
            Object xml = xmlBuilder.doStartElement(this, nodeItem);
            if(nodeItem!=null)
                nodeItem.xml = xml;
        }else if(stopped)
            throw STOP_PARSING;
    }

    public void onEndElement(){
        if(!stopped)
            pop();
        if(xmlBuilder!=null && xmlBuilder.active){
            if(xmlBuilder.doEndElement(this)==null && stopped)
                throw STOP_PARSING;
        }
    }

    public void onText(){
        if(buff.length()>0){
            if(!stopped)
                tailInfo.textCount++;
            if(xmlBuilder!=null || interestedInText)
                onEvent(NodeType.TEXT, "", "", "", null);
            notifyXMLBuilder();
            buff.setLength(0);
        }
    }

    public void onComment(char[] ch, int start, int length){
        if(!stopped)
            tailInfo.commentCount++;
        onEvent(NodeType.COMMENT, "", "", "", new String(ch, start, length));
        notifyXMLBuilder();
    }

    public void onPI(String target, String data){
        if(!stopped)
            tailInfo.updatePIPosition(target);
        onEvent(NodeType.PI, "", target, target, data);
        notifyXMLBuilder();
    }

    public void onAttributes(Attributes attrs){
        if(interestedInAttributes){
            int len = attrs.getLength();
            for(int i=0; i<len; i++){
                onEvent(NodeType.ATTRIBUTE, attrs.getURI(i), attrs.getLocalName(i), attrs.getQName(i), attrs.getValue(i));
                notifyXMLBuilder();
            }
        }else if(xmlBuilder!=null && xmlBuilder.active)
            xmlBuilder.onAttributes(this, attrs);
    }

    public void onAttributes(XMLStreamReader reader){
        if(interestedInAttributes){
            int len = reader.getAttributeCount();
            for(int i=0; i<len; i++){
                String prefix = reader.getAttributePrefix(i);
                String localName = reader.getAttributeLocalName(i);
                String qname = prefix.length()==0 ? localName : prefix+':'+localName;
                String uri = reader.getAttributeNamespace(i);
                if(uri==null)
                    uri = "";
                onEvent(NodeType.ATTRIBUTE, uri, localName, qname, reader.getAttributeValue(i));
                notifyXMLBuilder();
            }
        }else if(xmlBuilder!=null && xmlBuilder.active)
            xmlBuilder.onAttributes(this, reader);
    }

    public void onNamespaces(MyNamespaceSupport nsSupport){
        if(interestedInNamespaces){
            Enumeration<String> prefixes = nsSupport.getPrefixes();
            while(prefixes.hasMoreElements()){
                String prefix = prefixes.nextElement();
                String uri = nsSupport.getURI(prefix);
                onEvent(NodeType.NAMESPACE, "", prefix, prefix, uri);
                notifyXMLBuilder();
            }
        }else if(xmlBuilder!=null && xmlBuilder.active)
            xmlBuilder.onNamespaces(this, nsSupport);
    }

    /*-------------------------------------------------[ Stack ]---------------------------------------------------*/

    private Info tailInfo;

    private void push(Info info){
        info.prev = tailInfo;
        tailInfo.next = info;
        tailInfo = info;
    }

    private void pop(){
        Info curTailInfo = tailInfo;
        if(curTailInfo.slash!=-1)
            elementLocation.setLength(curTailInfo.slash);
        if(locationInfo==curTailInfo)
            locationInfo = curTailInfo.prev;

        tailInfo = curTailInfo.prev;
        if(tailInfo!=null)
            tailInfo.next = null;

        firePop();
    }

    static final class IntWrapper{
        int value = 1;
    }

    static final class Info{
        Info prev;
        Info next;

        int slash = -1;
        String elem;
        String lang;

        int elemntPos = 1;

        private static int updatePosition(Map<String, IntWrapper> map, String key){
            IntWrapper position = map.get(key);
            if(position==null){
                map.put(key, new IntWrapper());
                return 1;
            }else
                return ++position.value;
        }

        Map<String, IntWrapper> elemMap;
        public int updateElementPosition(String qname){
            if(elemMap==null)
                elemMap = new HashMap<String, IntWrapper>();
            return updatePosition(elemMap, qname);
        }

        Map<String, IntWrapper> piMap;
        public int updatePIPosition(String target){
            if(piMap==null)
                piMap = new HashMap<String, IntWrapper>();
            return updatePosition(piMap, target);
        }

        int textCount;
        int commentCount;
    }

    /*-------------------------------------------------[ NamespaceContext ]---------------------------------------------------*/

    private DefaultNamespaceContext nsContext;
    public final NamespaceContext givenNSContext;

    private String qname(String uri, String name){
        String prefix = nsContext.getPrefix(uri);
        if(prefix==null){
            prefix = givenNSContext.getPrefix(uri);
            if(prefix!=null)
                nsContext.declarePrefix(prefix, uri);
            else
                prefix = nsContext.declarePrefix(uri);
        }

        if(prefix.length()==0)
            return name;
        else{
            // doing:
            //   return prefix+';'+name;
            // manually to avoid StringBuilder creation
            int prefixLen = prefix.length();
            int nameLen = name.length();
            char ch[] = new char[prefixLen+1+nameLen];
            prefix.getChars(0, prefixLen, ch, 0);
            ch[prefixLen] = ':';
            name.getChars(0, name.length(), ch, prefixLen+1);

            assert new String(ch).equals(prefix+':'+name);
            return new String(ch);
        }
    }

    /*-------------------------------------------------[ StringContent ]---------------------------------------------------*/

    public final StringBuilder buff = new StringBuilder(500);

    public void appendText(char[] ch, int start, int length){
        if(xmlBuilder!=null || interestedInText)
            buff.append(ch, start, length);
        else if(buff.length()==0)
            buff.append('x');
    }

    /*-------------------------------------------------[ Evaluation Helper ]---------------------------------------------------*/

    public Evaluation evaluation;
    public Object evaluate(Expression expr){
        evaluation = null;
        switch(expr.scope()){
            case Scope.GLOBAL:
                return expr.getResult();
            case Scope.DOCUMENT:
                Object value = results[expr.id];
                return value instanceof Evaluation ? null : value;
            default:
                assert expr.scope()==Scope.LOCAL;
                Object result = expr.getResult(this);
                assert result!=null;
                if(result instanceof Evaluation){
                    evaluation = (Evaluation)result;
                    return null;
                }else
                    return result;
        }
    }

    public ArrayDeque<PositionTracker> positionTrackerStack = new ArrayDeque<PositionTracker>();
    public StringEvaluation stringEvaluation;
}
