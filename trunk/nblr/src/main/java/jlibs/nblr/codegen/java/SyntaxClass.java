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
import jlibs.nblr.Syntax;
import jlibs.nblr.codegen.RuleMethod;
import jlibs.nblr.codegen.State;
import jlibs.nblr.matchers.Any;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.matchers.Not;
import jlibs.nblr.matchers.Range;
import jlibs.nblr.rules.Rule;
import jlibs.nbp.NBParser;

import java.util.*;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public class SyntaxClass{
    public static boolean DEBUGGABLE = false;

    public Syntax syntax;
    public List<RuleMethod> ruleMethods = new ArrayList<RuleMethod>();

    public SyntaxClass(Syntax syntax){
        this.syntax = syntax.copy();
        if(!DEBUGGABLE){
            detectStringRules();
            syntax = this.syntax;
        }

        // NOTE: ids of all rules should be computed before calculating Routes
        for(Rule rule: syntax.rules.values())
            rule.computeIDS();

        for(Rule rule: syntax.rules.values()){
            if(rule.id>=0)
                ruleMethods.add(new RuleMethod(this, rule));
        }
        for(RuleMethod ruleMethod: ruleMethods){
            int i = -1;
            for(State state: ruleMethod.states){
                ++i;
                state.fromNode.stateID = DEBUGGABLE ? state.fromNode.id : i;
            }
        }

        syntax.computeBufferingStates();

        /*
        // print no of continues used for each rule
        for(RuleMethod ruleMethod: ruleMethods){
            int count = 0;
            for(int i=0; i<ruleMethod.states.size(); i++){
                State state = ruleMethod.states.get(i);
                for(Decision decision: state.decisions){
                    switch(decision.returnAction(i<ruleMethod.states.size()-1 ? ruleMethod.states.get(i+1) : null)){
                        case Decision.ADD_CONTINUE:
                        case Decision.CALL_RULE_AND_CONTINUE:
                            count++;
                    }
                }
            }
            System.out.println(count+" - "+ruleMethod.rule.name);
        }
        */
    }

    public int maxLookAhead(){
        int maxLookAhead = 0;
        for(RuleMethod ruleMethod: ruleMethods)
            maxLookAhead = Math.max(maxLookAhead, ruleMethod.maxLookAhead());
        return maxLookAhead;
    }

    public final List<Rule> stringRules = new ArrayList<Rule>();
    private void detectStringRules(){
        syntax = syntax.copy();

        int stringRuleID = -1;
        int id = 0;
        for(Rule r: syntax.rules.values()){
            int codePoints[] = r.matchString();
            if(codePoints!=null){
                r.id = stringRuleID--;
                stringRules.add(r);
            }else
                r.id = id++;
        }
    }

    private int unnamed_finishAllMethods = 0;
    private Map<Matcher, String> finishAllMethods = new LinkedHashMap<Matcher, String>();
    public static final String FINISH_ALL = "finishAll";
    public static final String FINISH_ALL_OTHER_THAN = "finishAll_OtherThan";
    public String addToFinishAll(Matcher matcher){
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

    public void generate(Printer printer){
        if(!DEBUGGABLE)
            generateStringRules(printer);
        generateMatcherMethods(printer);
        generaleRuleMethods(printer);
        generateFinishAllMethods(printer);
        generateCallRuleMethod(printer);
    }

    private void generaleRuleMethods(Printer printer){
        if(ruleMethods.size()>0){
            printer.titleComment("Rules");
            for(RuleMethod ruleMethod: ruleMethods){
                ruleMethod.generate(printer);
                printer.emptyLine(true);
            }
        }
    }

    private void generateFinishAllMethods(Printer printer){
        for(Map.Entry<Matcher, String> entry: finishAllMethods.entrySet()){
            printer.emptyLine(true);
            Matcher matcher = entry.getKey();
            String condition = matcher._javaCode("ch");
            if(matcher.checkFor(NBParser.EOF) || matcher.checkFor(NBParser.EOC))
                condition = "ch>=0 && "+condition;

            boolean supplemental = matcher.clashesWith(Range.SUPPLIMENTAL);

            printer.printlns(
                "private int finishAll_"+entry.getValue()+"(int ch) throws IOException{",
                    PLUS
            );

            if(!supplemental && !matcher.clashesWith(Any.NEW_LINE)){
                condition = matcher._javaCode("input[position]");
                printer.printlns(
                    "int _position = position;",
                    "while(position<limit && "+condition+")",
                        PLUS,
                        "++position;",
                        MINUS,
                    "int len = position-_position;",
                    "if(len>0 && buffer.isBuffering())",
                        PLUS,
                        "buffer.append(input, _position, len);",
                        MINUS,
                    "return codePoint();"
                );
            }else{
                printer.printlns(
                    "while("+condition+"){",
                        PLUS,
                        "consume(ch);",
                        "ch = codePoint();",
                        MINUS,
                    "}",
                    "return ch;"
                );
            }

            printer.printlns(
                    MINUS,
                 "}"
            );
        }
    }

    private void generateStringRules(Printer printer){
        printer.printlns(
            "private static final int STRING_IDS[][] = {",
                PLUS,
                "{}, // dummy one"
        );

        for(Rule rule: syntax.rules.values()){
            if(rule.id<0){
                int[] codePoints = rule.matchString();
                String str = Arrays.toString(codePoints);

                printer.print("{");
                printer.print(str.substring(1, str.length()-1));
                printer.print("}, // ");
                printer.println(new String(codePoints, 0, codePoints.length));
            }
        }

        printer.printlns(
                MINUS,
            "};"
        );

        printer.emptyLine(true);
        for(Rule rule: syntax.rules.values()){
            if(rule.id<0)
                printer.println("public static final int RULE_"+rule.name.toUpperCase()+" = "+rule.id+';');
        }
    }

    private void generateMatcherMethods(Printer printer){
        if(syntax.matchers.size()>0){
            printer.titleComment("Matchers");
            for(Matcher matcher: syntax.matchers.values()){
                if(!matcher.canInline()){
                    printer.printlns(
                        "private static boolean "+matcher.name+"(int ch){",
                            PLUS,
                            "return "+matcher.javaCode("ch")+';',
                            MINUS,
                        "}"
                    );
                    printer.emptyLine(true);
                }
            }
        }
    }

    private void generateCallRuleMethod(Printer printer){
        printer.emptyLine(true);
        printer.printlns(
            "@Override",
            "protected final boolean callRule(int rule, int state) throws Exception{",
                PLUS,
                "if(SHOW_STATS)",
                    PLUS,
                    "callRuleCount++;",
                    MINUS
        );
        if(!DEBUGGABLE){
            printer.printlns(
                    "if(rule<0){",
                        PLUS,
                        "if(rule==RULE_DYNAMIC_STRING_MATCH)",
                            PLUS,
                            "return matchString(state, dynamicStringToBeMatched);",
                            MINUS,
                        "else",
                            PLUS,
                            "return matchString(rule, state, STRING_IDS[-rule]);",
                            MINUS,
                        MINUS,
                    "}"
            );
        }
        printer.printlns(
                    "switch(rule){",
                        PLUS
        );

        for(Rule rule: syntax.rules.values()){
            if(rule.id>=0){
                printer.printlns(
                    "case "+rule.id+":",
                        PLUS,
                        "return "+rule.name+"(state);",
                        MINUS
                );
            }
        }
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
}
