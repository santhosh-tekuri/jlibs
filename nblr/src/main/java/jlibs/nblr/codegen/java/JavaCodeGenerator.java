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

import jlibs.core.lang.StringUtil;
import jlibs.nblr.Syntax;
import jlibs.nblr.codegen.CodeGenerator;
import jlibs.nblr.matchers.Any;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.matchers.Not;
import jlibs.nblr.rules.*;
import jlibs.nbp.NBParser;

import java.util.ArrayList;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public class JavaCodeGenerator extends CodeGenerator{
    public JavaCodeGenerator(Syntax syntax){
        super(syntax);
    }

    @Override
    protected void printTitleComment(String title){
        printer.println("/*-------------------------------------------------[ "+title+" ]---------------------------------------------------*/");
    }

    @Override
    protected void startParser(){
        String className[] = className(parserName);
        if(className[0].length()>0){
            printer.printlns(
                "package "+className[0]+";",
                ""
            );
        }

        printer.printClassDoc();

        printer.printlns(
            "public class "+className[1]+" extends "+superClass.getName()+"{",
                PLUS
        );
    }

    private String[] className(String className){
        String pakage = "";
        String simpleName = className;
        int dot = simpleName.lastIndexOf('.');
        if(dot!=-1){
            pakage = simpleName.substring(0, dot);
            simpleName = simpleName.substring(dot+1);
        }
        return new String[]{ pakage, simpleName };
    }

    @Override
    protected void finishParser(int maxLookAhead){
        String className = className(parserName)[1];

        String debuggerArgs = debuggable ? "handler, " : "";
        printer.emptyLine(true);
        printer.printlns(
                "@Override",
                "public void onSuccessful() throws Exception{",
                    PLUS,
                    "handler.onSuccessful();",
                    MINUS,
                "}",
                "",
                "@Override",
                "public void fatalError(String message) throws Exception{",
                    PLUS,
                    "handler.fatalError(message);",
                    MINUS,
                "}",
                "",
                "protected final "+ handlerName +" handler;",
                "public "+className+"("+ handlerName +" handler, int startingRule){",
                    PLUS,
                    "super("+debuggerArgs+maxLookAhead+", startingRule);",
                    "this.handler = handler;",
                    MINUS,
                "}",
                MINUS,
            "}"
        );
    }

    @Override
    protected void printMatcherMethod(Matcher matcher){
        if(!matcher.canInline()){
            printer.printlns(
                "private static boolean "+matcher.name+"(int ch){",
                    PLUS,
                    "return "+matcher.javaCode("ch")+';',
                    MINUS,
                "}"
            );
        }
    }

    @Override
    protected void addRuleID(String name, int id){
        printer.println("public static final int RULE_"+name.toUpperCase()+" = "+id+';');
    }

    @Override
    protected void startRuleMethod(Rule rule){
        printer.printlns(
            "private int "+rule.name+"(int ch) throws Exception{",
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
            "protected int "+prefix+"callRule(int ch) throws Exception{",
                PLUS,
                "switch(ruleStack.peek()){",
                    PLUS
        );
    }

    @Override
    protected void callRuleMethod(String ruleName){
        printer.println("return "+ruleName+"(ch);");
    }

    @Override
    protected void finishCallRuleMethod(){
        finishRuleMethod(null);
    }

    @Override
    protected void addRoutes(Routes routes){
        String expected = "expected(ch, \""+ StringUtil.toLiteral(routes.toString(), false)+"\");";

        boolean lookAheadBufferReqd = routes.maxLookAhead>1;
        if(lookAheadBufferReqd)
            printer.printlns("lookAhead.add(ch);");

        for(int lookAhead: routes.lookAheads()){
            if(lookAheadBufferReqd){
                printer.printlns(
                    "if(ch!=-1 && lookAhead.length()<"+lookAhead+")",
                        PLUS,
                        "return "+routes.fromNode.id+";",
                        MINUS
                );

                printer.printlns(
                    "if(lookAhead.length()=="+lookAhead+"){",
                        PLUS
                );
            }
            print(routes.determinateRoutes(lookAhead), 1 ,lookAheadBufferReqd);
            if(lookAheadBufferReqd){
                printer.printlns(
                        MINUS,
                    "}"
                );
            }
        }

        if(routes.indeterminateRoute !=null){
            Path path = routes.indeterminateRoute.route()[0];
            Matcher matcher = path.matcher();
            startIf(matcher, 0);

            int state = _travelPath(path, true);
            println("lookAhead.reset();");
            println("return "+ state +';');

            endIf(1);
        }

        if(routes.routeStartingWithEOF!=null)
            travelPath(routes.routeStartingWithEOF, false);
        else
            printer.println(expected);
    }

    private void print(List<Path> routes, int depth, boolean consumeLookAhead){
        List<List<Path>> groups = new ArrayList<List<Path>>();
        Matcher matcher = null;
        for(Path route: routes){
            Path path = route.route()[depth-1];
            Matcher curMatcher = path.matcher();
            if(curMatcher==null)
                curMatcher = eofMatcher;

            if(matcher==null || !curMatcher.same(matcher)){
                groups.add(new ArrayList<Path>());
                matcher = curMatcher;
            }
            groups.get(groups.size()-1).add(route);
        }

        for(List<Path> group: groups){
            Path route = group.get(0);
            matcher = route.route()[depth-1].matcher();
            boolean endIf = false;
            if(matcher!=null){
                int lookAheadIndex = route.depth>1 && depth!=route.depth ? depth-1 : -1;
                startIf(matcher, lookAheadIndex);
                endIf = true;
            }
            if(depth<routes.get(0).depth)
                print(group, depth+1, consumeLookAhead);
            if(depth==route.depth)
                travelRoute(route, consumeLookAhead);
            if(endIf)
                endIf(1);
        }
    }

    private void startIf(Matcher matcher, int lookAheadIndex){
        String ch = lookAheadIndex==-1 ? "ch" : "lookAhead.charAt("+lookAheadIndex+')';

        String condition = matcher._javaCode(ch);
        Not.minValue = -1;
        try{
            if(lookAheadIndex==-1 && (matcher.hasCustomJavaCode() || matcher.clashesWith(new Any(-1)))){
                if(matcher.name==null)
                    condition = '('+condition+')';
                condition = "ch!=-1 && "+condition;
            }
        }finally{
            Not.minValue = Character.MIN_VALUE;
        }
        printer.printlns(
            "if("+condition+"){",
                PLUS
        );
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
            printer.println("handler.execute("+nodesToBeExecuted+");");
            nodesToBeExecuted.setLength(0);
        }
        printer.println(line);
    }

    private void travelRoute(Path route, boolean consumeLookAhead){
        int state = -1;
        for(Path path: route.route())
            state = _travelPath(path, consumeLookAhead);
        println("return "+ state +';');
    }

    private void travelPath(Path path, boolean consumeLookAhead){
        int state = _travelPath(path, consumeLookAhead);
        println("return "+ state +';');
    }

    private int _travelPath(Path path, boolean consumeLookAhead){
        nodesToBeExecuted.setLength(0);

        int nextState = -1;
        boolean wasNode = false;
        for(Object obj: path){
            if(obj instanceof Node){
                if(wasNode)
                    println("pop();");
                wasNode = true;

                Node node = (Node)obj;
                if(debuggable){
                    if(nodesToBeExecuted.length()>0)
                        nodesToBeExecuted.append(", ");
                    nodesToBeExecuted.append(node.id);
                }else if(node.action!=null)
                    printer.println(node.action.javaCode()+';');
            }else if(obj instanceof Edge){
                wasNode = false;
                Edge edge = (Edge)obj;
                if(edge.ruleTarget!=null)
                    println("push(RULE_"+edge.ruleTarget.rule.name.toUpperCase()+", "+edge.target.id+", "+edge.ruleTarget.node().id+");");
                else if(edge.matcher!=null){
                    nextState = edge.target.id;
                    if(consumeLookAhead)
                        println("consumed();");
                    break;
                }
            }
        }
        return nextState;
    }

    /*-------------------------------------------------[ Handler ]---------------------------------------------------*/

    protected void startHandler(){
        printer.printClassDoc();

        String className[] = className(handlerName);
        if(className[0].length()>0){
            printer.printlns(
                "package "+className[0]+";",
                ""
            );
        }

        String keyWord = handlerClass ? "class" : "interface";
        String suffix = handlerClass ? " implements " : " extends ";
        printer.printlns(
            "public "+keyWord+" "+className[1]+"<E extends Exception>"+suffix+"<E>{",
                PLUS
        );
    }

    protected void addPublishMethod(String name){
        if(handlerClass){
            printer.printlns(
                "public void "+name+"(Chars data){",
                    PLUS,
                        "System.out.println(\""+name+"(\\\"\"+data+\"\\\")\");",
                    MINUS,
                "}"
            );
        }else
            printer.println("public void "+name+"(Chars data);");
    }

    protected void addEventMethod(String name){
        if(handlerClass){
            printer.printlns(
                "public void "+name+"(){",
                    PLUS,
                        "System.out.println(\""+name+"\");",
                    MINUS,
                "}"
            );
        }else
            printer.println("public void "+name+"();");
    }

    protected void finishHandler(){
        if(handlerClass){
            printer.printlns(
                    "@Override",
                    "public void fatalError(String message) throws E{",
                        PLUS,
                        "throw new Exception(message);",
                        MINUS,
                    "}",
                    MINUS,
                "}"
            );
        }
        printer.printlns(
                MINUS,
            "}"
        );
    }

    /*-------------------------------------------------[ Customization ]---------------------------------------------------*/

    private String parserName = "jlibs.xml.sax.async.XMLScanner";
    private Class superClass = NBParser.class;
    public void setParserName(String parserName){
        this.parserName = parserName;
    }

    private String handlerName = "jlibs.xml.sax.async.AsyncXMLReader";
    private boolean handlerClass = false;
    public void setHandlerName(String handlerName, boolean isClass){
        this.handlerName = handlerName;
        this.handlerClass = isClass;
    }

    public void setDebuggable(Class superClass, Class handler){
        debuggable = true;
        this.superClass = superClass;
        this.handlerName = handler.getName();
    }
}
