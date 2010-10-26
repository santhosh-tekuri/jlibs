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

package jlibs.nblr.editor.serialize;

import jlibs.core.lang.NotImplementedException;
import jlibs.nblr.Syntax;
import jlibs.nblr.actions.*;
import jlibs.nblr.matchers.*;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public class SyntaxDocument extends XMLDocument{
    public SyntaxDocument(Result result) throws TransformerConfigurationException{
        super(result, false, 4, null);
    }

    public void add(Syntax syntax) throws SAXException{
        startElement("syntax");
        for(Matcher matcher: syntax.matchers.values())
            add(matcher);
        for(Rule rule: syntax.rules.values())
            add(rule);
        endElement();
    }

    public void add(Matcher matcher) throws SAXException{
        startElement(matcher.getClass().getSimpleName().toLowerCase());
        addAttribute("name", matcher.name);
        addAttribute("javaCode", matcher.javaCode);

        if(matcher instanceof Any){
            Any any = (Any)matcher;
            if(any.chars!=null)
                addAttribute("chars", new String(any.chars, 0, any.chars.length));
        }else if(matcher instanceof Range){
            Range range = (Range)matcher;
            addAttribute("from", new String(Character.toChars(range.from)));
            addAttribute("to", new String(Character.toChars(range.to)));
        }else if(matcher instanceof Not){
            Not not = (Not)matcher;            
            addNestedMatcher(not.delegate);
        }else if(matcher instanceof And){
            And and = (And)matcher;
            for(Matcher operand: and.operands)
                addNestedMatcher(operand);
        }else if(matcher instanceof Or){
            Or or = (Or)matcher;
            for(Matcher operand: or.operands)
                addNestedMatcher(operand);
        }else
            throw new NotImplementedException(matcher.getClass().getName());

        endElement();
    }
    
    private void addNestedMatcher(Matcher matcher) throws SAXException{
        if(matcher.name==null)
            add(matcher);
        else{
            startElement("matcher");
            addAttribute("name", matcher.name);
            endElement();
        }
    }        

    public void add(Rule rule) throws SAXException{
        int cp[] = rule.matchString();
        if(cp!=null){
            boolean hasNamedNode = false;
            for(Node node: rule.nodes()){
                if(node.name!=null){
                    hasNamedNode = true;
                    break;
                }
            }
            if(!hasNamedNode){
                startElement("string-rule");
                addAttribute("name", rule.name);
                addAttribute("string", new String(cp, 0, cp.length));
                endElement();
                return;
            }
        }

        startElement("rule");
        addAttribute("name", rule.name);

        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        rule.computeIDS(nodes, edges, rule.node);
        for(Node node: nodes)
            add(node);
        for(Edge edge: edges)
            add(edge);
        endElement();
    }

    public void add(Node node) throws SAXException{
        startElement("node");
        addAttribute("name", node.name);
        if(node.action!=null)
            add(node.action);
        endElement();
    }

    public void add(Edge edge) throws SAXException{
        startElement("edge");
        addAttribute("source", ""+edge.source.id);
        addAttribute("target", ""+edge.target.id);
        addAttribute("fallback", ""+edge.fallback);
        if(edge.matcher!=null)
            addNestedMatcher(edge.matcher);
        else if(edge.ruleTarget!=null){
            startElement("rule");
            addAttribute("name", edge.ruleTarget.rule.name);
            addAttribute("node", edge.ruleTarget.name);
            endElement();
        }
        endElement();
    }

    public void add(Action action) throws SAXException{
        if(action instanceof BufferAction)
            addElement("buffer", "");
        else if(action instanceof PublishAction){
            PublishAction publishAction = (PublishAction)action;
            startElement("publish");
            addAttribute("name", publishAction.name);
            addAttribute("begin", ""+publishAction.begin);
            addAttribute("end", ""+publishAction.end);
            endElement();
        }else if(action instanceof EventAction){
            EventAction eventAction = (EventAction)action;
            startElement("event");
            addAttribute("name", eventAction.name);
            endElement();
        }else if(action instanceof ErrorAction){
            ErrorAction errorAction = (ErrorAction)action;
            startElement("error");
            addAttribute("message", errorAction.errorMessage);
            endElement();
        }else
            throw new NotImplementedException(action.getClass().getName());
    }
}
