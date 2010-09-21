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
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

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
                for(Node state: rule.states()){
                    startCase(state.id);
                    println(state.paths(), new ArrayDeque<Path>());
                    endCase();
                }
                finishRuleMethod(rule);
                printer.emptyLine(true);
            }
        }

        printTitleComment("Consumer");
        printer.emptyLine(true);
        addEOFMember();
        addStateMember();
        addRequiredMember();
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

    public void println(Paths paths, ArrayDeque<Path> pathStack){
        int lastDepth = 0;
        Path pathWithoutMatcher = null;
        for(Path path: paths){
            int depth = path.depth();
            if(depth>lastDepth && paths.charIndex==0){
                if(lastDepth!=0){
                    printer.printlns(
                            MINUS,
                        "}"
                    );
                }
                printer.printlns(
                    "if(lookAheadBuffer.length()<"+depth+"){",
                    PLUS
                );
                if(depth>1){
                    printer.printlns(
                        "if(lookAheadBuffer.length()!="+(depth-1)+"){",
                            PLUS,
                            "lookAheadBuffer.append(ch);",
                            "return "+((Node)path.get(0)).id+";",
                            MINUS,
                        "}"
                    );
                    for(int i=0; i<depth-1; i++)
                        printer.println("char ch"+i+" = lookAheadBuffer.charAt("+i+");");
                }
            }
            lastDepth = depth;
            if(path.matcher()!=null){
                println(path, pathStack);
            }else
                pathWithoutMatcher = path;
        }

        if(pathWithoutMatcher!=null)
            println(pathWithoutMatcher, pathStack);

        if(paths.charIndex==0){
            printer.printlns(
                    MINUS,
                "}"
            );
        }

        if(pathWithoutMatcher==null && paths.charIndex==0)
            printer.printlns("expected(String.valueOf(ch), \""+StringUtil.toLiteral(paths.toString(), false)+"\");");
    }

    public void println(Path path, ArrayDeque<Path> pathStack){
        pathStack.push(path);
        Matcher matcher = path.matcher();
        if(matcher!=null){
            String variable = "ch";
            if(path.paths!=null)
                variable += path.paths.charIndex-1;
            String condition = matcher._javaCode(variable);
            if(path.paths==null){ // eof shouldn't be checked for char from lookAheadBuffer
                if(matcher.name==null)
                    condition = '('+condition+')';
                condition = "!eof && "+condition;
            }
            printer.printlns(
                "if("+condition+"){",
                    PLUS
            );
        }

        if(path.paths==null)
            printActions(pathStack);
        else
            println(path.paths, pathStack);
        
        if(matcher!=null){
            printer.printlns(
                    MINUS,
                "}"
            );
        }
        pathStack.pop();
    }

    private void printActions(ArrayDeque<Path> pathStack){
        ArrayList<Path> paths = new ArrayList<Path>(pathStack);
        Collections.reverse(paths);

        int nextID = -1;
        for(Path path: paths){
            for(Object obj: path){
                if(obj instanceof Node){
                    Node node = (Node)obj;
                    if(debuggable)
                        printer.println("consumer.hitNode("+node.id+");");
                    else if(node.action!=null)
                        printer.println(node.action.javaCode()+';');
                }else if(obj instanceof Edge){
                    Edge edge = (Edge)obj;
                    if(edge.rule!=null){
                        if(debuggable)
                            printer.println("consumer.currentRule(RULE_"+edge.rule.name+");");
                        printer.printlns(
                            "ruleStack.push(RULE_"+edge.rule.name+");",
                            "stateStack.push("+edge.target.id+");"
                        );
                    }else if(edge.matcher!=null){
                        nextID = ((Node)path.get(path.size()-1)).id;
                        Matcher matcher = path.matcher();
                        if(matcher!=null){
                            if(debuggable)
                                printer.println("consumer.currentNode("+nextID+");");
                            if(path.paths!=null){
                                String variable = "ch";
                                if(path.paths!=null)
                                    variable += path.paths.charIndex-1;
                                printer.println("consumed("+variable+");");
                            }
                        }else
                            nextID = -1;
                        break;
                    }
                }
            }
        }

        if(pathStack.size()>1)
            printer.println("lookAheadBuffer.setLength(0);");
        printer.printlns("return "+nextID+";");
    }

    protected boolean debuggable;
    public void setDebuggable(){
        this.debuggable = true;
    }

    protected abstract void startCase(int id);
    protected abstract void endCase();
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
    protected abstract void addRequiredMember();
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
