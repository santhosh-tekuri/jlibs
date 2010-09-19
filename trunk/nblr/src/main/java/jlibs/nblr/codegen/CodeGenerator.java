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

package jlibs.nblr.codegen;

import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.StringUtil;
import jlibs.nblr.Syntax;
import jlibs.nblr.actions.BufferAction;
import jlibs.nblr.actions.PublishAction;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public abstract class CodeGenerator{
    protected final Syntax syntax;
    protected final Printer printer;

    public CodeGenerator(Syntax syntax, Printer printer){
        this.syntax = syntax;
        this.printer = printer;
    }

    public final void generateCode(){
        startClassDeclaration();
        addConstructor();
        printer.emptyLine(true);
        
        if(syntax.matchers.size()>0){
            printTitleComment("Matchers");
            printer.emptyLine(true);
            for(Matcher matcher: syntax.matchers.values()){
                printMatcherMethod(matcher);
                printer.emptyLine(true);
            }
        }

        if(syntax.rules.size()>0){
            printTitleComment("Rules");
            printer.emptyLine(true);
            addExpectedMethod();
            printer.emptyLine(true);
            
            int id = 0;
            for(Rule rule: syntax.rules.values()){
                addRuleID(rule.name, id++);
                rule.computeIDS();
                startRuleMethod(rule);
                travel(rule);
                finishRuleMethod(rule);
                printer.emptyLine(true);
            }
        }

        printTitleComment("Consumer");
        printer.emptyLine(true);
        addEOFMember();
        addStateMember();
        addRuleStackMemeber();
        addStateStackMemeber();

        printer.emptyLine(true);
        addStartParsingMethod();
        printer.emptyLine(true);
        startConsumeMethod();
        int id = 0;
        for(Rule rule: syntax.rules.values()){
            startCase(id++);
            callRuleMethod(rule.name);
            addBreak();
            printer.printlns(MINUS);
        }
        finishConsumeMethod();

        printer.emptyLine(true);
        addExpectEOFMethod();
        printer.emptyLine(true);
        addEOFMethod();

        printer.emptyLine(true);
        printTitleComment("Buffering");
        printer.emptyLine(true);
        addBufferringSection();
        printer.emptyLine(true);

        printer.emptyLine(false);
        finishClassDeclaration();
    }

    class Match{
        Object[] path;
        Node[] nodesPath;
        Rule[] rulesPath;
        Matcher matcher;

        Match(ArrayDeque<Object> stack){
            ArrayList<Object> list = new ArrayList<Object>(stack);
            Collections.reverse(list);
            path = list.toArray();

            ArrayList<Node> nodes = new ArrayList<Node>();
            ArrayList<Rule> rules = new ArrayList<Rule>();
            for(Object obj: list){
                if(obj instanceof Node)
                    nodes.add((Node)obj);
                else if(obj instanceof Edge){
                    Edge edge = (Edge)obj;
                    if(edge.matcher!=null)
                        matcher = edge.matcher;
                    else if(edge.rule!=null)
                        rules.add(edge.rule);
                }
            }
            nodesPath = nodes.toArray(new Node[nodes.size()]);
            rulesPath = rules.toArray(new Rule[rules.size()]);
        }

        public void printIF(String prefix){
            String ifCondition = "";
            if(matcher!=null){
                if(matcher.name==null)
                    ifCondition = "if(!eof && ("+matcher._javaCode()+"))";
                else
                    ifCondition = "if(!eof && "+matcher._javaCode()+")";
            }else
                prefix = prefix.trim();

            if(prefix.length()+ifCondition.length()>0){
                printer.printlns(
                    prefix+ifCondition+"{",
                        PLUS
                );
            }
            printActions();

            int nextID = -1;
            if(matcher!=null)
                nextID = nodesPath[nodesPath.length-1].id;
            if(debuggable && matcher!=null)
                printer.println("consumer.currentNode("+nodesPath[nodesPath.length-1].id+");");
            printer.printlns("return "+ nextID +";");
            if(prefix.length()+ifCondition.length()>0)
                printer.printlns(MINUS);
        }

        private void printActions(){
            for(Object obj: path){
                if(obj instanceof Node){
                    Node pathNode = (Node)obj;
                    if(debuggable){
                        if(pathNode.action==BufferAction.INSTANCE){
                            printer.println(pathNode.action.javaCode()+';');
                            printer.println("consumer.hitNode("+pathNode.id+", null);");
                        }else if(pathNode.action instanceof PublishAction){
                            PublishAction publishAction = (PublishAction)pathNode.action;
                            printer.println("consumer.hitNode("+pathNode.id+", data("+publishAction.begin+", "+publishAction.end+"));");
                        }else
                            printer.println("consumer.hitNode("+pathNode.id+", null);");
                    }else if(pathNode.action!=null)
                        printer.println(pathNode.action.javaCode()+';');
                }else if(obj instanceof Edge){
                    Edge edge = (Edge)obj;
                    if(edge.rule!=null){
                        if(debuggable)
                            printer.println("consumer.currentRule(RULE_"+edge.rule.name+");");
                        printer.printlns(
                            "ruleStack.push(RULE_"+edge.rule.name+");",        
                            "stateStack.push("+edge.target.id+");"
                        );
                    }else if(edge.matcher!=null)
                        return;
                }
            }
        }
    }

    class Matches{
        private List<Match> matches = new ArrayList<Match>();

        public void add(ArrayDeque<Object> stack){
            matches.add(new Match(stack));
        }

        public void printCase(int fromID){
            startCase(fromID);

            Match matchWithoutMatcher = null;
            String prefix = "";
            for(Match match: matches){
                if(match.matcher!=null){
                    match.printIF(prefix);
                    prefix = "}else ";
                }else
                    matchWithoutMatcher = match;
            }
            if(matchWithoutMatcher==null){
                printer.printlns(
                    "}else",
                        PLUS,
                        expected(),
                        MINUS,
                    MINUS
                );
            }else{
                matchWithoutMatcher.printIF(prefix);
                if(prefix.length()>0)
                    printer.printlns("}");
                printer.printlns(MINUS);
            }
        }

        public String expected(){
            StringBuilder buff = new StringBuilder();
            for(Match match: matches){
                if(buff.length()>0)
                    buff.append(", ");
                if(match.matcher==null)
                    buff.append("\"EOF\"");
                else{
                    String str = match.matcher.name;
                    if(str==null)
                        str = StringUtil.toLiteral(match.matcher.toString(), false);
                    buff.append('"').append(str).append('"');
                }
            }
            return "expected(String.valueOf(ch), "+buff+");";
        }
    }

    private void travel(Rule rule){
        for(Node state: rule.states()){
            Matches matches = new Matches();
            travel(new ArrayList<Node>(), state, new ArrayDeque<Object>(), matches);
            matches.printCase(state.id);
        }
    }

    private void travel(List<Node> visited, Node fromNode, ArrayDeque<Object> stack, Matches matches){
        if(!visited.contains(fromNode)){
            visited.add(fromNode);
            stack.push(fromNode);
            if(fromNode.outgoing.size()>0){
                for(Edge edge: fromNode.outgoing()){
                    stack.push(edge);
                    if(edge.matcher!=null){
                        stack.push(edge.target);
                        matches.add(stack);
                        stack.pop();
                    }else if(edge.rule!=null)
                        travel(visited, edge.rule.node, stack, matches);
                    else
                        travel(visited, edge.target, stack, matches);
                    stack.pop();
                }
            }else
                matches.add(stack);
            stack.pop();
        }
    }

    protected boolean debuggable;
    public void setDebuggable(){
        this.debuggable = true;
    }

    protected abstract void startCase(int id);
    protected abstract void addBreak();
    
    protected abstract void printTitleComment(String title);
    protected abstract void startClassDeclaration();
    protected abstract void addConstructor();
    protected abstract void finishClassDeclaration();

    protected abstract void printMatcherMethod(Matcher matcher);
    
    protected abstract void addRuleID(String name, int id);
    protected abstract void startRuleMethod(Rule rule);

    protected abstract void finishRuleMethod(Rule rule);

    protected abstract void addEOFMember();
    protected abstract void addStateMember();
    protected abstract void addRuleStackMemeber();
    protected abstract void addStateStackMemeber();
    protected abstract void addStartParsingMethod();
    protected abstract void startConsumeMethod();
    protected abstract void callRuleMethod(String ruleName);
    protected abstract void finishConsumeMethod();

    protected abstract void addExpectedMethod();
    protected abstract void addExpectEOFMethod();
    protected abstract void addEOFMethod();
    protected abstract void addBufferringSection();
}
