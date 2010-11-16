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
import jlibs.nblr.codegen.java.SyntaxClass;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class RootIf extends IfBlock{
    public RootIf(State state){
        super(state);
    }

    public void computeCommon(){
        for(IfBlock block: this){
            if(block.children.size()>1){
                IfBlock last = block.children.get(block.children.size()-1);
                if(last.matcher==null){
                    boolean candidate = true;
                    for(IfBlock child: block.children){
                        if(child.path==null){
                            candidate = false;
                            break;
                        }
                    }
                    if(candidate){
                        int common = block.children.get(0).path.size()-1;
                        for(int iChild=1; iChild<block.children.size(); iChild++){
                            IfBlock prev = block.children.get(iChild-1);
                            IfBlock curr = block.children.get(iChild);
                            for(int i=0; i<=common; i++){
                                if(prev.path.get(i)!=curr.path.get(i)){
                                    common = i-1;
                                    break;
                                }
                            }
                        }
                        block.common = common;
                    }
                }
            }
        }
    }
    
    public String available(){
        return "available"+state.fromNode.stateID;
    }
    
    public boolean lookAheadRequired(){
        for(IfBlock child: children){
            if(child.children.size()>0)
                return true;
        }
        return false;    
    }

    public boolean lookAheadChars(){
        if(lookAheadRequired()){
            InputType inputType = analalizeInput();
            return inputType.characterOnly() && !inputType.newLine;
        }
        return false;
    }
    
    private void generateRead(Printer printer){
        IfBlock first = children.get(0);
        if(first.matcher!=null && !first.usesFinishAll()){
            boolean readChar  = true;
            if(lookAheadChars()){
                readChar = false;
                for(IfBlock child: children){
                    if(child.path!=null){
                        readChar = true;
                        break;
                    }
                }
            }

            String currentNode = "handler.currentNode("+state.ruleMethod.rule.id+", "+state.fromNode.stateID+");";
            if(readChar){
                List<String> body = new ArrayList<String>();
                if(SyntaxClass.DEBUGGABLE)
                    body.add(currentNode);
                body.add(state.breakStatement());
                printer.printlnIf("(ch="+readMethod()+")==EOC", body);
            }else{
                if(SyntaxClass.DEBUGGABLE)
                    printer.println(currentNode);                
            }
        }
    }

    @Override
    public void generate(Printer printer, State next){
        generateRead(printer);
        generateChildren(printer, next);
    }
}
