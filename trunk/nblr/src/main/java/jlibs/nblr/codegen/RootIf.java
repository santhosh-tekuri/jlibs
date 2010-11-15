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
import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class RootIf extends IfBlock implements Iterable<IfBlock>{
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
    
    private void generateRead(Printer printer){
        IfBlock first = children.get(0);
        if(first.matcher!=null && !first.usesFinishAll()){
            List<String> body = new ArrayList<String>();
            if(SyntaxClass.DEBUGGABLE)
                body.add("handler.currentNode("+state.ruleMethod.rule.id+", "+state.fromNode.stateID+");");
            body.add(state.breakStatement());
            printer.printlnIf("(ch="+readMethod()+")==EOC", body);
        }
    }

    @Override
    public void generate(Printer printer, State next){
        generateRead(printer);
        generateChildren(printer, next);
    }

    @Override
    public Iterator<IfBlock> iterator(){
        return new Iterator<IfBlock>(){
            IfBlock block;

            private IfBlock getNext(){
                if(block==null)
                    return RootIf.this;
                if(block.children.size()>0){
                    return block.children.get(0);
                }else{
                    IfBlock block = this.block;
                    IfBlock next = null;
                    while(block!=null){
                        List<IfBlock> siblings = block.siblings();
                        int index = siblings.indexOf(block);
                        if(index+1<siblings.size()){
                            next = siblings.get(index+1);
                            break;
                        }
                        block = block.parent;
                    }
                    return next;
                }
            }

            @Override
            public boolean hasNext(){
                return getNext()!=null;
            }

            @Override
            public IfBlock next(){
                return block = getNext();
            }

            @Override
            public void remove(){
                throw new UnsupportedOperationException();
            }
        };
    }
}
