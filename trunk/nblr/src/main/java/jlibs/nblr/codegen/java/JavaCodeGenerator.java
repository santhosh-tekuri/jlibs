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
import jlibs.core.lang.StringUtil;
import jlibs.nblr.Syntax;
import jlibs.nblr.codegen.CodeGenerator;
import jlibs.nblr.editor.debug.Debugger;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.*;
import jlibs.nbp.NBParser;

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
    protected void startClassDeclaration(int maxLookAhead){
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
        String debuggerArgs = "";
        if(debuggable)
            debuggerArgs = ", consumer";

        String extend = (debuggable ? DebuggableNBParser.class : NBParser.class).getName();
        printer.printlns(
            "public class "+className+" extends "+extend+"{",
                PLUS,
                "private final "+consumerName+" consumer;",
                "public "+className+"("+consumerName+" consumer){",
                    PLUS,
                    "super("+maxLookAhead+debuggerArgs+");",
                    "this.consumer = consumer;",
                    MINUS,
                "}"
        );
    }

    @Override
    protected void finishClassDeclaration(){
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
            "private int "+rule.name+"(char ch, boolean eof) throws java.text.ParseException{",
                PLUS,
                "switch(stateStack.peek()){",
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
    protected void startCallRuleMethod(){
        String prefix = debuggable ? "_" : "";
        printer.printlns(
            "@Override",
            "protected int "+prefix+"callRule(char ch, boolean eof) throws java.text.ParseException{",
                PLUS,
                "switch(ruleStack.peek()){",
                    PLUS
        );
    }

    @Override
    protected void callRuleMethod(String ruleName){
        printer.println("return "+ruleName+"(ch, eof);");
    }

    @Override
    protected void finishCallRuleMethod(){
        finishRuleMethod(null);
    }

    @Override
    protected void addRoutes(Routes routes){
        int lastDepth = 0;
        for(Path[] route: routes.determinateBranchRoutes){
            if(routes.maxLookAhead>1 && route.length>lastDepth){
                if(lastDepth!=0){
                    printer.printlns(
                            MINUS,
                        "}"
                    );
                }
                printer.printlns(
                    "if(lookAhead.length()<"+route.length+"){",
                        PLUS
                );
                if(route.length>1){
                    printer.printlns(
                            "lookAhead.add(ch, eof);",
                            "if(!eof && lookAhead.length()<"+route.length+")",
                                PLUS,
                                "return "+((Node)route[0].get(0)).id+";",
                                MINUS
                    );
                }
            }

            int ipath = 0;
            int ifCount = 0;
            for(Path path: route){
                if(startIf(path, route.length>1, ipath))
                    ifCount++;
                ipath++;
            }

            print(route[0], route.length>1);
            
            endIf(ifCount);
            lastDepth = route.length;
        }
        if(routes.maxLookAhead>1 && lastDepth!=0){
            printer.printlns(
                    MINUS,
                "}"
            );
        }

        if(routes.indeterminateBranchRoutes.size()>0){
            Path path = routes.indeterminateBranchRoutes.get(0)[0];
            if(startIf(path, true, 0)){
                print(routes.indeterminateBranchRoutes.get(0)[0], true);
                endIf(1);
            }
        }

        if(routes.routeStartingWithEOF!=null)
            print(routes.routeStartingWithEOF[0], false);
        else
            printer.println("expected(ch, eof, \""+ StringUtil.toLiteral(routes.toString(), false)+"\");");
    }

    private boolean startIf(Path path, boolean lookAhead, int index){
        Matcher matcher = path.matcher();
        if(matcher!=null){
            String ch = "ch";
            String eof = "eof";
            if(lookAhead){
                ch = "lookAhead.charAt("+index+')';
                eof = "lookAhead.isEOF("+index+')';
            }

            String condition = matcher._javaCode(ch);
            if(matcher.name==null)
                condition = '('+condition+')';
            printer.printlns(
                "if(!"+eof+" && "+condition+"){",
                    PLUS
            );
            return true;
        }else
            return false;
    }

    private void endIf(int count){
        while(count-->0){
            printer.printlns(
                    MINUS,
                "}"
            );
        }
    }

    private StringBuilder nodesToBeExecuted = new StringBuilder();
    private void println(String line){
        if(nodesToBeExecuted.length()>0){
            printer.println("consumer.execute("+nodesToBeExecuted+");");
            nodesToBeExecuted.setLength(0);
        }
        printer.println(line);
    }
    private void print(Path path, boolean lookAhead){
        nodesToBeExecuted.setLength(0);
        
        int nextState = -1;
        for(Object obj: path){
            if(obj instanceof Node){
                Node node = (Node)obj;
                if(debuggable){
                    if(nodesToBeExecuted.length()>0)
                        nodesToBeExecuted.append(", ");
                    nodesToBeExecuted.append(node.id);
                }else if(node.action!=null)
                    printer.println(node.action.javaCode()+';');
            }else if(obj instanceof Edge){
                Edge edge = (Edge)obj;
                if(edge.rule!=null)
                    println("push(RULE_"+edge.rule.name+", "+edge.target.id+");");
                else if(edge.matcher!=null){
                    nextState = edge.target.id;
                    if(lookAhead)
                        println("consumed();");
                    break;
                }
            }
        }
        println("return "+nextState+';');
    }
    
    /*-------------------------------------------------[ Customization ]---------------------------------------------------*/
    
    private String parserName = "UntitledParser";
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
        consumerName = Debugger.class.getName();
    }
}
