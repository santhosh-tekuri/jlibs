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
import jlibs.nblr.actions.EventAction;
import jlibs.nblr.actions.PublishAction;
import jlibs.nblr.codegen.java.SyntaxClass;
import jlibs.nblr.matchers.Any;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.matchers.Not;
import jlibs.nblr.rules.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public class Decision{
    public final State state;
    public Matcher matchers[];
    public Path path;
    public boolean fallback;

    public Decision(State state, Path route){
        this.state = state;
        this.fallback = route.fallback();

        Path paths[] = route.route();
        matchers = new Matcher[paths.length];
        for(int i=0; i<matchers.length; i++)
            matchers[i] = paths[i].matcher();
        path = paths[0];

        int index = indexOfedgeWithRule();
        if(index!=-1){
            index++; // edgeWithRule.target
            while(index!=path.size()-1)
                path.remove(index+1);
            //path.travelWithoutMatching();
        }else
            path.travelWithoutMatching();
    }

    private String ruleID(){
        return "RULE_"+state.ruleMethod.rule.name.toUpperCase();
    }

    private String exiting(String rule, int state){
        return state==-1 ? "" : "exiting("+rule+", "+state+");";
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj instanceof Decision){
            Decision that = (Decision)obj;
            return Arrays.equals(this.matchers, that.matchers) && this.path.equals(that.path);
        }else
            return false;
    }

    private int indexOfedgeWithRule(){
        for(int i=0; i<path.size(); i++){
            Object obj = path.get(i);
            if(obj instanceof Edge){
                Edge edge = (Edge)obj;
                if(edge.ruleTarget!=null)
                    return i;
            }
        }
        return -1;
    }

    public Edge edgeWithRule(){
        int index = indexOfedgeWithRule();
        return index==-1 ? null : (Edge)path.get(index);
    }

    private Node stateAfterRule(Edge edgeWithRule){
        if(new Routes(state.ruleMethod.rule, edgeWithRule.target).isEOF())
            return null;
        else
            return edgeWithRule.target;
    }

    private int idAfterRule(Edge edgeWithRule){
        Node stateAfterRule = stateAfterRule(edgeWithRule);
        return stateAfterRule==null ? -1 : stateAfterRule.stateID;
    }

    private String ruleName(Edge edgeWithRule){
        String ruleName = edgeWithRule.ruleTarget.rule.name;
        if(!SyntaxClass.DEBUGGABLE && Node.DYNAMIC_STRING_MATCH.equals(edgeWithRule.source.name))
            ruleName = "DYNAMIC_STRING_MATCH"; 
        return ruleName;
    }

    private String ruleID(Edge edgeWithRule){
        return "RULE_"+ruleName(edgeWithRule).toUpperCase();
    }

    public void computeNextStates(ArrayList<Node> statesVisited, LinkedHashSet<Node> statesPending){
        Node target;
        Edge edgeWithRule = edgeWithRule();
        if(edgeWithRule!=null)
            target = stateAfterRule(edgeWithRule);
        else{
            target = returnTarget();
            if(id(target)==-1)
                target = null;
        }

        if(target!=null && !statesVisited.contains(target))
            statesPending.add(target);
    }

    public Node returnTarget(){
        return (Node)path.get(path.size()-1);
    }

    public boolean isLoop(){
        return matchers.length==1 && path.size()>1 && state.fromNode==returnTarget();
    }

    public boolean usesFinishAll(){
        return isLoop() && !fallback && edgeWithRule()==null;
    }
    
    public boolean readCodePoint(){
        for(Matcher matcher: matchers){
            if(matcher!=null)
                return true;
        }
        return false;
    }

    public String expected(){
        StringBuilder builder = new StringBuilder();
        for(Matcher matcher: matchers){
            if(matcher==null)
                builder.append("<EOF>");
            else if(matcher.name!=null)
                builder.append('<').append(matcher.name).append('>');
            else
                builder.append(matcher.toString());
        }
        return builder.toString();
    }

    private static boolean checkEOF(Matcher matcher){
        Not.minValue = -1;
        try{
            return matcher.hasCustomJavaCode() || matcher.clashesWith(new Any(-1));
        }finally{
            Not.minValue = Character.MIN_VALUE;
        }
    }

    public static final int ADD_CONTINUE = 1;
    public static final int ADD_RETURN = 2;
    public static final int GOTO_NEXT_CASE = 4;
    public static final int GOTO_NEXT_DECISION = 5;

    public static final int CALL_RULE_AND_CONTINUE = 6;
    public static final int CALL_RULE_AND_RETURN = 7;
    public static final int CALL_RULE_AND_NEXT_DECISION = 8;
    public int returnAction(State nextState){
        if(usesFinishAll())
            return GOTO_NEXT_DECISION;
        
        Node returnTarget = returnTarget();
        Edge edge = edgeWithRule();
        if(edge ==null){
            if(nextState==null || nextState.fromNode!=returnTarget){
                return id(returnTarget)==-1 ? ADD_RETURN : ADD_CONTINUE;
            }else
                return state.lookAheadRequired() ? ADD_CONTINUE : GOTO_NEXT_CASE;
        }else{
            if(idAfterRule(edge)==-1)
                return CALL_RULE_AND_RETURN;
            Node stateAfterRule = stateAfterRule(edge);
            if(nextState==null || nextState.fromNode!=stateAfterRule)
                return CALL_RULE_AND_CONTINUE;
            else
                return state.lookAheadRequired() ? CALL_RULE_AND_CONTINUE : CALL_RULE_AND_NEXT_DECISION;
        }
    }

    public boolean requiresContinue(State nextState){
        int returnAction = returnAction(nextState);
        return returnAction==ADD_CONTINUE || returnAction==CALL_RULE_AND_CONTINUE;
    }

    private void useFinishAll(Printer printer){
        Matcher matcher = matchers[0];
        String methodName = state.ruleMethod.syntaxClass.addToFinishAll(matcher);

        String ch = state.lookAheadRequired() ? "ch" : "codePoint()";
        String methodCall;
        if(methodName.equals(SyntaxClass.FINISH_ALL) || methodName.equals(SyntaxClass.FINISH_ALL_OTHER_THAN)){
            Any any = (Any)(methodName.equals(SyntaxClass.FINISH_ALL_OTHER_THAN) ? ((Not)matcher).delegate : matcher);
            methodCall = methodName+"("+ch+", "+Matcher.toJava(any.chars[0])+')';
        }else
            methodCall = "finishAll_"+methodName+"("+ch+")";

        boolean returnValueRequired = false;
        for(int i = state.decisions.indexOf(this)+1; i<state.decisions.size(); i++){
            Decision decision = state.decisions.get(i);
            if(decision.matchers[0]!=null){
                returnValueRequired = true;
                break;
            }
        }
        if(returnValueRequired)
            methodCall = "(ch="+methodCall+")";

        printer.printlns(
            "if("+methodCall+"==EOC){",
                PLUS,
                exiting(ruleID(), state.fromNode.stateID),
                "return false;",
                MINUS,
            "}"
        );
    }

    public void generate(Printer printer, State nextState){
        if(usesFinishAll()){
            useFinishAll(printer);
            return;
        }

        for(int i=0; i<matchers.length; i++)
            startMatcher(printer, i);

        addBody(printer, nextState);

        for(int i=0; i<matchers.length; i++)
            endMatcher(printer, i);
    }

    public static String condition(Matcher matcher, String ch){
        String condition = matcher._javaCode(ch);
        if(checkEOF(matcher)){
            if(matcher.name==null)
                condition = '('+condition+')';
            condition = "ch!=EOF && "+condition;
        }
        return condition;
    }
    
    public void startMatcher(Printer printer, int i){
        Matcher matcher = matchers[i];
        if(matcher==null)
            return;

        boolean useLookAhead = false;
        if(i==matchers.length-1){
            int idecision = state.decisions.indexOf(this);
            if(idecision!=0 && state.decisions.get(idecision-1).matchers.length>matchers.length) // indeterminateroute
                useLookAhead = true;
        }else
            useLookAhead = true;
        String ch = useLookAhead ? "la["+i+"]" : "ch";
        String condition = condition(matcher, ch);

        printer.printlns(
            "if("+condition+"){",
                PLUS
        );
    }

    public void addBody(Printer printer, State nextState){
        boolean checkStop = generatePath(printer);

        int returnAction = returnAction(nextState);
        Node returnTarget = returnTarget();
        if(id(returnTarget)!=-1 && returnAction==ADD_CONTINUE)
            printer.println("state = "+id(returnTarget)+';');

        boolean resetLookAhead = false;
        if(matchers.length>1)
            resetLookAhead = true;
        else if(matchers.length==1 && state.lookAheadRequired()){
            int lookAheadSize = 0;
            for(Decision decision: state.decisions){
                if(decision==this)
                    break;
                lookAheadSize = Math.max(lookAheadSize, decision.matchers.length);
            }
            if(lookAheadSize>1)
                resetLookAhead = true;
        }

        if(resetLookAhead)
            printer.println("resetLookAhead();");

        switch(returnAction){
            case ADD_CONTINUE:
                if(checkStop)
                    doCheckStop(printer);
                printer.println("continue;");
                break;
            case ADD_RETURN:
                if(checkStop && id(returnTarget)!=-1){
                    printer.printlns(
                        "if(stop)",
                            PLUS,
                            exiting(ruleID(), id(returnTarget)),
                            MINUS
                    );
                }
                if(SyntaxClass.DEBUGGABLE)
                    printer.printlns("handler.currentNode("+ruleID()+", "+returnTarget.id+");");
                printer.printlns("return "+(checkStop ? "!stop" : "true")+";");
                break;
            case CALL_RULE_AND_CONTINUE:
            case CALL_RULE_AND_RETURN:
            case CALL_RULE_AND_NEXT_DECISION:
                Edge edgeWithRule = edgeWithRule();
                String methodCall;
                if(!SyntaxClass.DEBUGGABLE && Node.DYNAMIC_STRING_MATCH.equals(edgeWithRule.source.name))
                    methodCall = "matchString("+id(returnTarget)+", dynamicStringToBeMatched)";
                else{
                    Rule rule = edgeWithRule.ruleTarget.rule;
                    if(rule.id<0)
                        methodCall = "matchString(RULE_"+rule.name.toUpperCase()+", "+id(returnTarget)+", STRING_IDS[-RULE_"+rule.name.toUpperCase()+"])";
                    else
                        methodCall = rule.name+"("+id(returnTarget)+")";
                }

                if(checkStop){
                    printer.printlns(
                        "if(stop){",
                            PLUS,
                            exiting(ruleID(edgeWithRule), id(edgeWithRule.ruleTarget.node())),
                            exiting(ruleID(), idAfterRule(edgeWithRule)),
                            "return false;",
                            MINUS,
                        "}else"
                    );
                }

                List<String> methodCallList = new ArrayList<String>();
                switch(returnAction){
                    case CALL_RULE_AND_RETURN:
                        methodCallList.add("return true;");
                        break;
                    case CALL_RULE_AND_CONTINUE:
                        methodCallList.add("state = "+idAfterRule(edgeWithRule)+";");
                        methodCallList.add("continue;");
                        break;
                    case CALL_RULE_AND_NEXT_DECISION:
                        //methodCallList.add("state = "+idAfterRule(edgeWithRule)+";");
                        break;
                }

                List<String> elseList = new ArrayList<String>();
                String line = exiting(ruleID(), idAfterRule(edgeWithRule));
                if(line.length()>0)
                    elseList.add(line);
                elseList.add("return false;");

                if(methodCallList.size()==1 && elseList.size()==1 && methodCallList.get(0).equals("return true;") && elseList.get(0).equals("return false;"))
                    printer.println("return "+methodCall+";");
                else if(methodCallList.size()==0){
                    printer.printlns(
                        "if(!"+methodCall+"){",
                            PLUS
                    );
                    printer.printlns(elseList.toArray(new String[elseList.size()]));
                    printer.printlns(
                            MINUS,
                        "}"
                    );
                }else{
                    printer.printlns(
                        "if("+methodCall+"){",
                            PLUS
                    );
                    printer.printlns(methodCallList.toArray(new String[methodCallList.size()]));
                    printer.printlns(
                            MINUS,
                        "}else{",
                            PLUS
                    );
                    printer.printlns(elseList.toArray(new String[elseList.size()]));
                    printer.printlns(
                            MINUS,
                        "}"
                    );
                }
                break;
            case GOTO_NEXT_CASE:
            case GOTO_NEXT_DECISION:
                if(checkStop)
                    doCheckStop(printer);
                break;
        }
    }

    private void doCheckStop(Printer printer){
        printer.printlns(
            "if(stop){",
                PLUS,
                exiting(ruleID(), id(returnTarget())),
                "return false;",
                MINUS,
            "}"
        );
    }

    public void endMatcher(Printer printer, int i){
        Matcher matcher = matchers[i];
        if(matcher==null)
            return;

        printer.printlns(
                MINUS,
            "}"
        );
    }

    // NOTE: don't use for edgeRuleTarget
    private int id(Node node){
        return node==null || node.outgoing.size()==0 ? -1 : node.stateID;
    }

    private boolean generatePath(Printer printer){
        boolean checkStop = false;

        int index = -1;
        for(Object obj: path){
            ++index;
            if(obj instanceof Node){
                Node node = (Node)obj;

                if(index<path.size()-1 || node.outgoing.size()==0){ // !lastNode || sinkNode
                    if(node.action!=null){
                        if(SyntaxClass.DEBUGGABLE)
                            printer.println("handler.execute("+state.ruleMethod.rule.id+", "+node.stateID+");");
                        else
                            printer.println(node.action.javaCode()+';');
                        if(node.action instanceof EventAction || node.action instanceof PublishAction){
                            if(node.action.toString().startsWith("#"))
                                checkStop = true;
                        }
                    }
                }
            }else if(obj instanceof Edge){
                Edge edge = (Edge)obj;
                if(edge.ruleTarget!=null){
//                    RuleTarget ruleTarget = edge.ruleTarget;
//                    int idAfterRule = idAfterRule(edge);
//                    String ruleName = ruleName(edge);
//                    printer.println("push(RULE_"+ruleName.toUpperCase()+", "+idAfterRule+", "+id(ruleTarget.node())+");");
                }else if(edge.matcher!=null){
                    if(matchers.length==1)
                        printer.println("consume(ch);");
                    else
                        printer.println("consume(FROM_LA);");
                }
            }
        }
        return checkStop;
    }
}
