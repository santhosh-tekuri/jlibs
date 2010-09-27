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
import jlibs.nblr.Syntax;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Routes;
import jlibs.nblr.rules.Rule;

import static jlibs.core.annotation.processing.Printer.MINUS;

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
        startClassDeclaration(); // todo: compute maxLookAhead reqd
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
            
            int id = 0;
            for(Rule rule: syntax.rules.values()){
                addRuleID(rule.name, id++);
                rule.computeIDS();
                startRuleMethod(rule);
                for(Node state: rule.states()){
                    try{
                        startCase(state.id);
                        Routes routes = new Routes(state);
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
        finishClassDeclaration(maxLookAhead);
    }

    protected boolean debuggable;
    public void setDebuggable(){
        this.debuggable = true;
    }

    protected abstract void startCase(int id);
    protected abstract void endCase();
    
    protected abstract void printTitleComment(String title);
    protected abstract void startClassDeclaration();
    protected abstract void finishClassDeclaration(int maxLookAhead);

    protected abstract void printMatcherMethod(Matcher matcher);
    
    protected abstract void addRuleID(String name, int id);
    protected abstract void startRuleMethod(Rule rule);
    protected abstract void addRoutes(Routes routes);
    protected abstract void finishRuleMethod(Rule rule);

    protected abstract void startCallRuleMethod();
    protected abstract void callRuleMethod(String ruleName);
    protected abstract void finishCallRuleMethod();
}
