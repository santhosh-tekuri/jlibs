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
    protected void startClassDeclaration(){
        printer.printClassDoc();

        String className[] = className();
        if(className[0].length()>0){
            printer.printlns(
                "package "+className[0]+";",
                ""
            );
        }

        String extend = (debuggable ? DebuggableNBParser.class : NBParser.class).getName();
        printer.printlns(
            "public class "+className[1]+" extends "+extend+"{",
                PLUS
        );
    }

    private String[] className(){
        String pakage = "";
        String simpleName = parserName;
        int dot = simpleName.lastIndexOf('.');
        if(dot!=-1){
            pakage = simpleName.substring(0, dot);
            simpleName = simpleName.substring(dot+1);
        }
        return new String[]{ pakage, simpleName };
    }

    @Override
    protected void finishClassDeclaration(int maxLookAhead){
        String className = className()[1];
        String debuggerArgs = "";
        if(debuggable)
            debuggerArgs = ", consumer";

        printer.printlns(
                "private final "+consumerName+" consumer;",
                "public "+className+"("+consumerName+" consumer){",
                    PLUS,
                    "super("+maxLookAhead+debuggerArgs+");",
                    "this.consumer = consumer;",
                    MINUS,
                "}",
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
        String expected = "expected(ch, eof, \""+ StringUtil.toLiteral(routes.toString(), false)+"\");";

        boolean lookAheadBufferReqd = routes.maxLookAhead>1;
        if(lookAheadBufferReqd)
            printer.printlns("lookAhead.add(ch, eof);");

        int lastDepth = 0;
        for(int iroute=0; iroute<routes.determinateBranchRoutes.size(); iroute++){
            Path[] route = routes.determinateBranchRoutes.get(iroute).route();
            int curDepth = route.length;
            if(lookAheadBufferReqd && curDepth>lastDepth){
                printer.printlns(
                    "if(lookAhead.length()<"+curDepth+" && !eof)",
                        PLUS,
                        "return "+routes.fromNode.id+";",
                        MINUS,
                     "if(lookAhead.length()=="+curDepth+"){",
                        PLUS
                );
            }

            int ifCount = 0;
            for(int ipath=0; ipath<route.length; ipath++){
                if(startIf(route[ipath], curDepth>1, ipath))
                    ifCount++;
            }

            print(route[0], lookAheadBufferReqd);

            endIf(ifCount);

            if(lookAheadBufferReqd){
                boolean lastRoute = iroute+1==routes.determinateBranchRoutes.size();
                if(lastRoute || routes.determinateBranchRoutes.get(iroute+1).depth>curDepth){
                    printer.printlns(
                            MINUS,
                        "}"
                    );
                }
            }

            lastDepth = curDepth;
        }

        if(routes.indeterminateBranchRoutes.size()>0){
            Path path = routes.indeterminateBranchRoutes.get(0).route()[0];
            if(startIf(path, true, 0)){
                print(path, true);
                endIf(1);
            }
        }

        if(routes.routeStartingWithEOF!=null)
            print(routes.routeStartingWithEOF, false);
        else
            printer.println(expected);
    }

    private boolean startIf(Path path, boolean useLookAheadBuffer, int lookAheadIndex){
        Matcher matcher = path.matcher();
        if(matcher!=null){
            String ch = "ch";
            String eof = "eof";
            if(useLookAheadBuffer){
                ch = "lookAhead.charAt("+lookAheadIndex+')';
                eof = "lookAhead.isEOF("+lookAheadIndex+')';
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
    private void print(Path path, boolean consumeLookAhead){
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
                if(edge.ruleTarget!=null)
                    println("push(RULE_"+edge.ruleTarget.rule.name+", "+edge.target.id+", "+edge.ruleTarget.node().id+");");
                else if(edge.matcher!=null){
                    nextState = edge.target.id;
                    if(consumeLookAhead)
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
