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

package jlibs.nblr.codegen.java;

import jlibs.core.annotation.processing.Printer;
import jlibs.core.util.regex.TemplateMatcher;
import jlibs.nblr.Syntax;
import jlibs.nblr.codegen.CodeGenerator;
import jlibs.nblr.editor.debug.Debugger;
import jlibs.nblr.editor.debug.NBParser;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Rule;

import java.util.HashMap;
import java.util.Map;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public class JavaCodeGenerator extends CodeGenerator{
    public JavaCodeGenerator(Syntax syntax, Printer printer){
        super(syntax, printer);
    }

    @Override
    protected void printTitleComment(String title){
        printer.println("/*-------------------------------------------------[ "+title+" ]---------------------------------------------------*/");
    }

    @Override
    protected void startClassDeclaration(){
        printer.printClassDoc();

        String className = parserName;
        int dot = className.lastIndexOf('.');
        if(dot!=-1){
            String pakage = className.substring(0, dot);
            printer.printlns(
                "package "+pakage+";",
                ""
            );
            className = className.substring(dot+1);
        }

        String implementing = "";
        if(debuggable)
            implementing = " implements "+ NBParser.class.getName();
        printer.printlns(
            "public class "+className+implementing+"{",
                PLUS,
                "private final "+consumerName+" consumer;"
        );
    }

    @Override
    protected void addConstructor(){
        String className = parserName;
        int dot = className.lastIndexOf('.');
        if(dot!=-1)
            className = className.substring(dot+1);
        printer.printlns(
            "public "+className+"("+consumerName+" consumer){",
                PLUS,
                "this.consumer = consumer;",
                MINUS,
            "}"
        );
    }

    @Override
    protected void addEOFMember(){
        printer.printlns("private boolean eof;");
    }

    @Override
    protected void addStateMember(){
        printer.printlns("private int state;");
    }

    @Override
    protected void addRequiredMember(){
        printer.printlns("private int required;");
    }

    @Override
    protected void finishClassDeclaration(){
        if(debuggable)
            printer.printlns(getClass().getResourceAsStream("finishClassDeclaration.txt"));
        printer.printlns(
                MINUS,
            "}"
        );
    }

    @Override
    protected void printMatcherMethod(Matcher matcher){
        printer.printlns(
            "private boolean "+matcher.name+"(char ch){",
                PLUS,
                "return "+matcher.javaCode("ch")+';',
                MINUS,
            "}"
        );
    }

    @Override
    protected void addRuleID(String name, int id){
        printer.println("public static final int RULE_"+name+" = "+id+';');
    }

    @Override
    protected void startRuleMethod(Rule rule){
        printer.printlns(
            "private int "+rule.name+"(char ch) throws java.text.ParseException{",
                PLUS,
                "switch(state){",
                    PLUS
        );
    }

    @Override
    protected void startCase(int id){
        printer.printlns(
            "case "+id+":",
                PLUS
        );
    }

    @Override
    protected void endCase(){
        printer.printlns(
            MINUS
        );
    }

    @Override
    protected void addBreak(){
        printer.println("break;");
    }

    @Override
    protected void finishRuleMethod(Rule rule){
        printer.printlns(
                    "default:",
                        PLUS,
                        "throw new Error(\"impossible\");",
                        MINUS,
                    MINUS,
                "}",
                MINUS,
            "}"
        );
    }

    @Override
    protected void addRuleStackMemeber(){
        printer.println("private final java.util.ArrayDeque<Integer> ruleStack = new java.util.ArrayDeque<Integer>();");
    }

    @Override
    protected void addStateStackMemeber(){
        printer.println("private final java.util.ArrayDeque<Integer> stateStack = new java.util.ArrayDeque<Integer>();");
    }

    @Override
    protected void addStartParsingMethod(){
        printer.printlns(
            "public void startParsing(int rule){",
                PLUS,
                "ruleStack.push(rule);"
        );
        if(debuggable){
            printer.printlns(
                "consumer.currentRule(rule);",
                "consumer.currentNode(0);"
            );
        }
        printer.printlns(
                MINUS,
             "}"
        );
    }

    @Override
    protected void startConsumeMethod(){
        printer.printlns(getClass().getResourceAsStream("startConsumeMethod.txt"));
        printer.printlns(
            "private void _consume(char ch) throws java.text.ParseException{",
                PLUS,
                "switch(ruleStack.peek()){",
                    PLUS
        );
    }

    @Override
    protected void finishConsumeMethod(){
        printer.printlns(
                    MINUS,
                "}",
                "if(state==-1){",
                    PLUS,
                    "if(!stateStack.isEmpty()){",
                        PLUS,
                        "ruleStack.pop();",
                        "state = stateStack.pop();"
        );
        if(debuggable){
            printer.printlns(
                "consumer.currentRule(ruleStack.peek());",
                "consumer.currentNode(state);"
            );
        }
        printer.printlns(
                        "_consume(ch);",
                        MINUS,
                    "}else",
                        PLUS,
                        "expectEOF(ch);",
                        MINUS,
                    MINUS,
                "}",
                MINUS,
            "}"
        );
    }

    @Override
    protected void callRuleMethod(String ruleName){
        printer.println("state = "+ruleName+"(ch);");
    }

    @Override
    protected void addExpectedMethod(){
        printer.printlns(getClass().getResourceAsStream("addExpectedMethod.txt"));
    }

    @Override
    protected void addExpectEOFMethod(){
        printer.printlns(getClass().getResourceAsStream("addExpectEOFMethod.txt"));
    }

    @Override
    protected void addEOFMethod(){
        printer.printlns(getClass().getResourceAsStream("addEOFMethod.txt"));
    }

    @Override
    protected void addBufferingSection(){
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("MODIFIER", debuggable ? "public" : "private");
        printer.printlns(getClass().getResourceAsStream("addBufferingSection.txt"), new TemplateMatcher("${", "}"), new TemplateMatcher.MapVariableResolver(variables));
    }
    
    /*-------------------------------------------------[ Customization ]---------------------------------------------------*/
    
    private String parserName = "NBParser";
    public void setParserName(String parserName){
        this.parserName = parserName;
    }

    private String consumerName = "Consumer";
    public void setConsumerName(String consumerName){
        this.consumerName = consumerName;
    }

    @Override
    public void setDebuggable(){
        super.setDebuggable();
        parserName = "DebuggableNBParser";
        consumerName = Debugger.class.getName();
    }
}
