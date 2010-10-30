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

import java.io.IOException;
import java.util.*;

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
        String className[] = className(stringProperty(PARSER_CLASS_NAME));
        if(className[0].length()>0){
            printer.printlns(
                "package "+className[0]+";",
                ""
            );
        }

        printer.importClass(IOException.class);
        printer.emptyLine(true);
        printer.printClassDoc();

        printer.printlns(
            "public"+(booleanProperty(PARSER_FINAL) ? " final " :" ")+"class "+className[1]+" extends "+stringProperty(PARSER_SUPER_CLASS)+"{",
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
        for(Map.Entry<Matcher, String> entry: finishAllMethods.entrySet()){
            printer.emptyLine(true);
            printer.printlns(
                "private int finishAll_"+entry.getValue()+"(int ch) throws IOException{",
                    PLUS,
                    "while(ch!=EOC && "+condition(entry.getKey(), -1)+"){",
                        PLUS,
                        "consume(ch);",
                        "ch = codePoint();",
                        MINUS,
                    "}",
                    "return ch;",
                    MINUS,
                 "}"
            );
        }

        String className = className(stringProperty(PARSER_CLASS_NAME))[1];
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
                "protected final "+ stringProperty(HANDLER_CLASS_NAME) +" handler;",
                "public "+className+"("+ stringProperty(HANDLER_CLASS_NAME) +" handler, int startingRule){",
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
            "private boolean "+rule.name+"() throws Exception{",
                PLUS,
                "int ch;",
                "while(true){",
                    PLUS,
                    "switch(curState){",
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
                            "throw new Error(\"impossible state: \"+curState);",
                            MINUS,
                        MINUS,
                    "}",
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
            "protected final boolean "+prefix+"callRule() throws Exception{",
                PLUS,
                "if(SHOW_STATS)",
                    PLUS,
                    "callRuleCount++;",
                    MINUS,
                    "int rule = stack[free-2];"
        );
        if(!debuggable){
            printer.printlns(
                    "if(rule<0){",
                        PLUS,
                        "if(rule==RULE_DYNAMIC_STRING_MATCH)",
                            PLUS,
                            "return matchString(dynamicStringToBeMatched);",
                            MINUS,
                        "else",
                            PLUS,
                            "return matchString(STRING_IDS[-rule]);",
                            MINUS,
                        MINUS,
                    "}"
            );
        }
        printer.printlns(
                    "switch(rule){",
                        PLUS
        );
    }

    @Override
    protected void callRuleMethod(String ruleName){
        printer.println("return "+ruleName+"();");
    }

    @Override
    protected void finishCallRuleMethod(){
        printer.printlns(
                    "default:",
                        PLUS,
                        "throw new Error(\"impossible rule: \"+stack[free-2]);",
                        MINUS,
                    MINUS,
                "}",
                MINUS,
            "}"
        );
    }

    private Rule curRule;

    @Override
    protected void addRoutes(Routes routes){
        if(routes.determinateRoutes.size()==0 & routes.indeterminateRoute==null){
            printer.printlns(
                "if(stop || codePoint()==EOC)",
                    PLUS,
                    "return false;",
                    MINUS
            );
        }else{
            printer.printlns(
                "if(stop || (ch=codePoint())==EOC)",
                    PLUS,
                    "return false;",
                    MINUS
            );
        }
        curRule = routes.rule;
        String expected = "expected(ch, \""+ StringUtil.toLiteral(routes.toString(), false)+"\");";

        boolean lookAheadBufferReqd = routes.maxLookAhead>1;
        if(lookAheadBufferReqd)
            printer.printlns("addToLookAhead(ch);");

        boolean addElse = false;
        for(int lookAhead: routes.lookAheads()){
            if(lookAheadBufferReqd){
                if(lookAhead>1){
                    printer.printlns(
                        "if(ch!=EOF && lookAhead.length()<"+lookAhead+")",
                            PLUS,
                            "continue;",
                            MINUS
                    );
                }

                printer.printlns(
                    "if(lookAhead.length()=="+lookAhead+"){",
                        PLUS
                );
            }
            addElse = print(routes.determinateRoutes(lookAhead), 1 ,lookAheadBufferReqd);
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
            if(addElse)
                printer.print("else ");
            startIf(condition(matcher, 0));

            consumeLAFirst = false;
            consumeLALen = 0;
            Destination dest = _travelPath(curRule, path, true);
            println("lookAhead.reset();");
            returnDestination(dest);

            endIf(1);
            addElse = true;
        }

        if(lookAheadBufferReqd)
            addElse = false;
        if(routes.routeStartingWithEOF!=null){
            if(addElse){
                printer.printlns(
                    "else {",
                        PLUS
                );
            }
            travelPath(routes.routeStartingWithEOF, false);
            if(addElse){
                printer.printlns(
                        MINUS,
                    "}"
                );
            }
        }else{
            if(addElse)
                printer.print("else ");
            printer.println(expected);
        }
    }

    @SuppressWarnings({"UnnecessaryLocalVariable"})
    private boolean print(List<Path> routes, int depth, boolean consumeLookAhead){
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

        // move all routes that can use finishAll to the beginning
        for(int i=1; i<groups.size(); i++){
            List<Path> group = groups.get(i);
            Path route = group.get(0);
            String finishAll = checkFinishAll(route, consumeLookAhead);
            if(finishAll!=null){ // move to beginning
                groups.remove(i);
                groups.add(0, group);
            }
        }

        int i = -1;
        boolean addElse = true;
        int elseAfter = 0;
        for(List<Path> group: groups){
            ++i;
            Path route = group.get(0);

            String finishAll = checkFinishAll(route, consumeLookAhead);
            if(finishAll!=null && i==0){
                elseAfter = 1;
                addElse = false;
                useFinishAll(route.matcher(), finishAll, false, groups.size()>1);
                continue;
            }

            matcher = route.route()[depth-1].matcher();
            boolean endIf = false;
            if(matcher!=null){
                int lookAheadIndex = route.depth>1 && depth!=route.depth ? depth-1 : -1;
                if(i>elseAfter)
                    printer.print("else ");
                startIf(condition(matcher, lookAheadIndex));
                endIf = true;
            }
            if(depth<routes.get(0).depth)
                print(group, depth+1, consumeLookAhead);
            if(depth==route.depth){
                if(finishAll!=null)
                    useFinishAll(route.matcher(), finishAll, true, false);
                else
                    travelRoute(route, consumeLookAhead);
            }
            if(endIf)
                endIf(1);
            addElse = true;
        }
        return addElse;
    }

    private String condition(Matcher matcher, int lookAheadIndex){
        String ch = lookAheadIndex==-1 ? "ch" : "lookAhead.charAt("+lookAheadIndex+')';

        String condition = matcher._javaCode(ch);
        Not.minValue = -1;
        try{
            if(lookAheadIndex==-1 && (matcher.hasCustomJavaCode() || matcher.clashesWith(new Any(-1)))){
                if(matcher.name==null)
                    condition = '('+condition+')';
                condition = "ch!=EOF && "+condition;
            }
        }finally{
            Not.minValue = Character.MIN_VALUE;
        }
        return condition;
    }

    private void startIf(String condition){
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
    private int consumeLALen = 0;
    private boolean consumeLAFirst = false;
    private void printlnConsumeLA(){
        if(consumeLALen>0){
            if(consumeLALen==1)
                printer.println("consume(FROM_LA);");
            else
                printer.println("consumeLookAhead("+consumeLALen+");");
        }
    }

    private void println(String line){
        if(consumeLAFirst)
            printlnConsumeLA();
        if(nodesToBeExecuted.length()>0){
            printer.println("handler.execute(stack[free-2], "+nodesToBeExecuted+");");
            nodesToBeExecuted.setLength(0);
        }
        if(!consumeLAFirst)
            printlnConsumeLA();
        printer.println(line);

        consumeLAFirst = false;
        consumeLALen = 0;
    }

    private int unnamed_finishAllMethods = 0;
    private Map<Matcher, String> finishAllMethods = new LinkedHashMap<Matcher, String>();
    public static final String FINISH_ALL = "finishAll";
    public static final String FINISH_ALL_OTHER_THAN = "finishAll_OtherThan";
    private String addToFinishAll(Matcher matcher){
        boolean not = false;
        Matcher givenMatcher = matcher;
        if(matcher instanceof Not){
            not = true;
            matcher = ((Not)matcher).delegate;
        }
        if(matcher instanceof Any){
            Any any = (Any)matcher;
            if(any.chars!=null && any.chars.length==1)
                return not ? FINISH_ALL_OTHER_THAN : FINISH_ALL;
        }
        matcher = givenMatcher;

        String name = null;
        if(matcher.name!=null)
            name = finishAllMethods.get(matcher);
        else{
            for(Map.Entry<Matcher, String> entry: finishAllMethods.entrySet()){
                if(entry.getKey().same(matcher)){
                    name = entry.getValue();
                    break;
                }
            }
        }
        if(name==null){
            name = matcher.name;
            if(name==null)
                name = String.valueOf(++unnamed_finishAllMethods);
            finishAllMethods.put(matcher, name);
        }
        return name;
    }

    private String checkFinishAll(Path path, boolean consumeLookAhead){
        if(!consumeLookAhead && path.parent==null){
            if(path.size()==3 && path.get(0)==path.get(2)){ // loop
                Node node = (Node)path.get(0);
                Edge edge = (Edge)path.get(1);
                if(node.action==null && edge.matcher!=null && !edge.fallback)
                    return addToFinishAll(edge.matcher);
            }
        }
        return null;
    }

    private void useFinishAll(Matcher matcher, String name, boolean addContinue, boolean storeReturnValue){
        String methodCall;
        if(name.equals(FINISH_ALL) || name.equals(FINISH_ALL_OTHER_THAN)){
            Any any = (Any)(name.equals(FINISH_ALL_OTHER_THAN) ? ((Not)matcher).delegate : matcher);
            methodCall = name+"(ch, "+Matcher.toJava(any.chars[0])+')';
        }else
            methodCall = "finishAll_"+name+"(ch)";

        if(!addContinue && storeReturnValue)
            methodCall = "(ch="+methodCall+")";
        printer.printlns(
            "if("+methodCall+"==EOC)",
                PLUS,
                "return false;",
                MINUS
        );
        if(addContinue)
            printer.println("continue;");
    }

    private void travelRoute(Path route, boolean consumeLookAhead){
        consumeLAFirst = false;
        consumeLALen = 0;
        Destination dest = new Destination(consumeLookAhead, curRule, null);
        for(Path path: route.route())
            dest = _travelPath(dest.rule, path, consumeLookAhead);
        returnDestination(dest);
    }

    private void travelPath(Path path, boolean consumeLookAhead){
        consumeLAFirst = false;
        consumeLALen = 0;
        Destination dest = _travelPath(curRule, path, consumeLookAhead);
        returnDestination(dest);
    }

    private void returnDestination(Destination dest){
        int state = dest.state();
        if(state<0){
            println("curState = -1;");
            println("return true;");
        }else if(dest.rule==curRule){
            if(!debuggable && Node.DYNAMIC_STRING_MATCH.equals(dest.node.name)){
                addState(dest.node);
                println("push(RULE_DYNAMIC_STRING_MATCH, "+state+", 0);");
                println("curState = 0;");
                println("return true;");
            }else{
                addState(dest.node);
                println("curState = "+state+";");
                if(statesPending.isEmpty() || statesPending.iterator().next()!=dest.node)
                    println("continue;");
            }
        }else{
            println("curState = "+state+";");
            println("return true;");
        }
    }

    class Destination{
        boolean consumedFromLookAhead;
        Rule rule;
        Node node;

        Destination(boolean consumedFromLookAhead, Rule rule, Node node){
            this.consumedFromLookAhead = consumedFromLookAhead;
            this.rule = rule;
            this.node = node;
        }

        public int state(){
            return node.outgoing.size()==0 ? -1 : node.id;
        }
    }

    public static boolean COELSCE_LA_CONSUME_CALLS = false;
    private Destination _travelPath(Rule rule, Path path, boolean consumeLookAhead){
        nodesToBeExecuted.setLength(0);

        Deque<Rule> ruleStack = new ArrayDeque<Rule>();
        ruleStack.push(rule);
        Node destNode = null;

        int index = -1;
        boolean wasNode = false;
        for(Object obj: path){
            ++index;
            if(obj instanceof Node){
                if(wasNode){
                    println("free -= 2;");
                    ruleStack.pop();
                }
                wasNode = true;

                Node node = (Node)obj;

                if(index<path.size()-1 || node.outgoing.size()==0){ // !lastNode || sinkNode
                    if(debuggable){
                        if(nodesToBeExecuted.length()>0)
                            nodesToBeExecuted.append(", ");
                        nodesToBeExecuted.append(node.id);
                    }else if(node.action!=null)
                        printer.println(node.action.javaCode()+';');
                }
                destNode = node;
            }else if(obj instanceof Edge){
                wasNode = false;
                Edge edge = (Edge)obj;
                if(edge.ruleTarget!=null){
                    destNode = null;
                    int stateAfterRule = new Routes(ruleStack.peek(), edge.target).isEOF() ? -1 : edge.target.id;
                    println("push(RULE_"+edge.ruleTarget.rule.name.toUpperCase()+", "+stateAfterRule+", "+edge.ruleTarget.node().id+");");
                    if(stateAfterRule!=-1 && ruleStack.peek()==curRule)
                        addState(edge.target);
                    ruleStack.push(edge.ruleTarget.rule);
                }
                else if(edge.matcher!=null){
                    destNode = edge.target;
                    if(consumeLookAhead){
                        if(COELSCE_LA_CONSUME_CALLS){
                            if(nodesToBeExecuted.length()==0)
                                consumeLAFirst = true;
                            consumeLALen++;
                        }else
                            println("consume(FROM_LA);");
                    }else
                        println("consume(ch);");
                }
            }
        }
        return new Destination(consumeLookAhead, ruleStack.peek(), destNode);
    }

    /*-------------------------------------------------[ Handler ]---------------------------------------------------*/

    protected void startHandler(){
        printer.printClassDoc();

        String className[] = className(stringProperty(HANDLER_CLASS_NAME));
        if(className[0].length()>0){
            printer.printlns(
                "package "+className[0]+";",
                ""
            );
        }

        String keyWord, suffix;
        if(booleanProperty(HANDLER_IS_CLASS)){
            keyWord = "class";
            suffix = "implements";
        }else{
            keyWord = "interface";
            suffix = " extends ";
        }
        printer.printlns(
            "public "+keyWord+" "+className[1]+"<E extends Exception>"+suffix+"<E>{",
                PLUS
        );
    }

    protected void addPublishMethod(String name){
        if(booleanProperty(HANDLER_IS_CLASS)){
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
        if(booleanProperty(HANDLER_IS_CLASS)){
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
        if(booleanProperty(HANDLER_IS_CLASS)){
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

    /*-------------------------------------------------[ StringIDs ]---------------------------------------------------*/

    @Override
    protected void startStringIDs(){
        printer.printlns(
            "int STRING_IDS[][] = {",
                PLUS,
                "{}, // dummy one"
        );
    }

    @Override
    protected void addStringID(int[] codePoints){
        StringBuilder buff = new StringBuilder(Arrays.toString(codePoints));
        buff.setCharAt(0, '{');
        buff.setCharAt(buff.length()-1, '}');
        buff.append(", // ").append(new String(codePoints, 0, codePoints.length));
        printer.println(buff.toString());
    }

    @Override
    protected void finishStringIDs(){
        printer.printlns(
                MINUS,
            "};"
        );
    }

    /*-------------------------------------------------[ Customization ]---------------------------------------------------*/

    public static final String PARSER_CLASS_NAME = "PARSER_CLASS_NAME";
    public static final String PARSER_FINAL = "PARSER_FINAL";
    private static final String PARSER_SUPER_CLASS = "PARSER_SUPER_CLASS";
    public static final String HANDLER_CLASS_NAME = "HANDLER_CLASS_NAME";
    public static final String HANDLER_IS_CLASS = "HANDLER_IS_CLASS";

    public static final Properties DEFAULTS = new Properties();
    static{
        DEFAULTS.put(PARSER_CLASS_NAME, "UntitledParser");
        DEFAULTS.put(PARSER_FINAL, "true");
        DEFAULTS.put(PARSER_SUPER_CLASS, NBParser.class.getName());

        DEFAULTS.put(HANDLER_CLASS_NAME, "UntitledHandler");
        DEFAULTS.put(HANDLER_IS_CLASS, "false");
    }
    public final Properties properties = new Properties(DEFAULTS);
    private final Properties debugProperties = new Properties(properties);
    {
        debugProperties.put(PARSER_SUPER_CLASS, "jlibs.nblr.editor.debug.DebuggableNBParser");
        debugProperties.put(HANDLER_CLASS_NAME, "jlibs.nblr.editor.debug.Debugger");
    }

    private String stringProperty(String name){
        return (debuggable ? debugProperties : properties).getProperty(name);
    }

    private boolean booleanProperty(String name){
        return "true".equals(stringProperty(name));
    }

    public void setDebuggable(){
        debuggable = true;
    }
}
