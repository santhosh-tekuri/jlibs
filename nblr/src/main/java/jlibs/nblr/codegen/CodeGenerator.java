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
                for(Node state: rule.states())
                    print(state.paths(), state.id);
//                travel(rule);
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

    public void print(Paths paths, int caseID){
        startCase(caseID);

        String prefix = "";
        Path pathWithoutMatcher = null;
        for(Path path: paths){
            if(path.matcher()!=null){
                printer.print(prefix);
                print(path);
                prefix = "else ";
            }else
                pathWithoutMatcher = path;

        }

        printer.print(prefix);
        if(pathWithoutMatcher!=null){
            print(pathWithoutMatcher);
            printer.println();
        }else{
            printer.printlns(
                "",
                    PLUS,
                    "expected(String.valueOf(ch), \""+StringUtil.toLiteral(paths.toString(), false)+"\");",
                    MINUS
            );
        }

        endCase();
    }

    public void print(Path path){
        Matcher matcher = path.matcher();
        if(matcher!=null){
            String condition = matcher._javaCode();
            if(matcher.name==null)
                condition = '('+condition+')';
            condition = "!eof && "+condition;
            printer.print("if("+condition+")");
        }
        printer.printlns(
            "{",
                PLUS
        );

        // actions
        for(Object obj: path){
            if(obj instanceof Node){
                Node node = (Node)obj;
                if(node.action!=null){
                    if(debuggable)
                        printer.println("consumer.hitNode("+node.id+");");
                    else
                        printer.println(node.action.javaCode()+';');
                }
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
                    break;
            }
        }

        // returning
        int nextID = ((Node)path.get(path.size()-1)).id;
        if(debuggable && matcher!=null)
            printer.println("consumer.currentNode("+nextID+");");
        printer.printlns("return "+ (matcher==null ? -1 : nextID) +";");

        printer.printlns(
                MINUS
        );
        printer.print(
            "}"
        );
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
