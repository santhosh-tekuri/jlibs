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
import jlibs.core.io.FileUtil;
import jlibs.core.util.Range;
import jlibs.nblr.Syntax;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Routes;
import jlibs.nblr.rules.Rule;

import java.io.File;
import java.util.*;

import static jlibs.core.annotation.processing.Printer.MINUS;

/**
 * @author Santhosh Kumar T
 */
public abstract class CodeGenerator{
    public boolean INLINE_RULES = true;

    protected Syntax syntax;
    protected Printer printer;

    public CodeGenerator(Syntax syntax){
        this.syntax = syntax;
    }

    protected boolean debuggable;
    protected void inlineRules() throws Exception{
        File tempFile = new File("temp/temp.syntax");
        FileUtil.mkdirs(tempFile.getParentFile());

        syntax.saveTo(tempFile);
        syntax = Syntax.loadFrom(tempFile);

        Set<String> rulesChecked = new HashSet<String>();

        Iterator<Rule> rules = syntax.rules.values().iterator();
        while(rules.hasNext()){
            Rule rule = rules.next();
            if(rulesChecked.contains(rule.name))
                continue;

            rulesChecked.add(rule.name);

            boolean hasNodeWithName = false;
            for(Node node: rule.nodes()){
                if(node.name!=null){
                    hasNodeWithName = true;
                    break;
                }
            }
            if(hasNodeWithName)
                continue;

            List<Rule> usages = syntax.usages(rule);
            if(usages.size()==1 && usages.get(0)!=rule){
                boolean inlined = false;
                
                Rule usingRule = usages.get(0);
                for(Edge edge: usingRule.edges()){
                    if(edge.ruleTarget!=null && edge.ruleTarget.rule==rule && edge.ruleTarget.name==null && !edge.loop()){
                        edge.inlineRule();
                        inlined = true;
                    }
                }
                if(inlined){
                    rules.remove();
                    syntax.updateRuleIDs();
                    try{
                        for(Node state: usingRule.states())
                            new Routes(usingRule, state);
                        syntax.saveTo(tempFile);
                        //System.out.println("inlined "+rule.name+" in "+usingRule.name);
                    }catch(IllegalStateException ex){
                        syntax = Syntax.loadFrom(tempFile);
                        rules = syntax.rules.values().iterator();
                    }
                }
            }
        }
    }

    public final void generateParser(Printer printer){
        this.printer = printer;
        if(!debuggable){
            try{
                if(INLINE_RULES)
                    inlineRules();
            }catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }
        
        startParser();
        printer.emptyLine(true);
        
        if(syntax.matchers.size()>0){
            printTitleComment("Matchers");
            printer.emptyLine(true);
            for(Matcher matcher: syntax.matchers.values()){
                printMatcherMethod(matcher);
                printer.emptyLine(true);
            }
        }

        int maxLookAhead = 0;
        if(syntax.rules.size()>0){
            printTitleComment("Rules");
            printer.emptyLine(true);
            
            // NOTE: ids of all rules should be computed before calculating Routes
            for(Rule rule: syntax.rules.values())
                rule.computeIDS();

            int id = 0;
            for(Rule rule: syntax.rules.values()){
                addRuleID(rule.name, id++);
                startRuleMethod(rule);
                for(Node state: rule.states()){
                    try{
                        startCase(state.id);
                        Routes routes = new Routes(rule, state);
                        if(routes.isEOF())
                            printer.println("// EOF-State");
                        maxLookAhead = Math.max(maxLookAhead, routes.maxLookAhead);
                        addRoutes(routes);
                        endCase();
                    }catch(IllegalStateException ex){
                        throw new IllegalStateException(ex.getMessage()+" in Rule '"+rule.name+"'");
                    }
                }
                finishRuleMethod(rule);
                printer.emptyLine(true);
            }
        }

        startCallRuleMethod();
        int id = 0;
        for(Rule rule: syntax.rules.values()){
            startCase(id++);
            callRuleMethod(rule.name);
            printer.printlns(MINUS);
        }
        finishCallRuleMethod();

        printer.emptyLine(false);
        finishParser(maxLookAhead);
    }

    protected abstract void startCase(int id);
    protected abstract void endCase();
    
    protected abstract void printTitleComment(String title);
    protected abstract void startParser();
    protected abstract void finishParser(int maxLookAhead);

    protected abstract void printMatcherMethod(Matcher matcher);
    
    protected abstract void addRuleID(String name, int id);
    protected abstract void startRuleMethod(Rule rule);
    protected abstract void addRoutes(Routes routes);
    protected abstract void finishRuleMethod(Rule rule);

    protected abstract void startCallRuleMethod();
    protected abstract void callRuleMethod(String ruleName);
    protected abstract void finishCallRuleMethod();

    public final void generateConsumer(Printer printer){
        this.printer = printer;

        startHandler();

        Set<String> set = syntax.publishMethods();
        if(set.size()>0){
            printTitleComment("Publishers");
            printer.emptyLine(true);
            for(String publisher: set){
                addPublishMethod(publisher);
                printer.emptyLine(true);
            }
        }

        set = syntax.eventMethods();
        if(set.size()>0){
            printTitleComment("Events");
            printer.emptyLine(true);
            for(String event: set){
                addEventMethod(event);
                printer.emptyLine(true);
            }
        }

        printer.emptyLine(false);
        finishHandler();
    }

    protected abstract void startHandler();
    protected abstract void addPublishMethod(String name);
    protected abstract void addEventMethod(String name);
    protected abstract void finishHandler();

    protected static Matcher eofMatcher = new Matcher(){
        @Override
        public String toString(){ throw new UnsupportedOperationException(); }

        @Override
        protected String __javaCode(String variable){ throw new UnsupportedOperationException(); }

        @Override
        public List<Range> ranges(){
            return Collections.emptyList();
        }
    };
}
