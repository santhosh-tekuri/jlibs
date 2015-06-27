/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.nblr.codegen;

import jlibs.core.annotation.processing.Printer;
import jlibs.nblr.codegen.java.SyntaxClass;

import java.util.ArrayList;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

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
            return inputType.characterOnly() && (!inputType.newLine || !inputType.consumes);
        }
        return false;
    }

    private boolean endsWithEOFMatcher(){
        IfBlock last = children.get(children.size()-1);
        for(IfBlock block: last){
            if(block.matcher==null)
                return true;
        }
        return false;
    }
    
    private String readMethod(){
        InputType inputType = analalizeInput();
        if(inputType.characterOnly())
            return "position==limit ? marker : input[position]";
        else
            return "codePoint()";
    }

    private void generateRead(Printer printer){
        IfBlock first = children.get(0);
        if(first.matcher!=null && !first.usesFinishAll() && first.height()==1){
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
                InputType inputType = analalizeInput();
                if(inputType.characterOnly() && inputType.newLine && inputType.consumes)
                    printer.println("increment = 1;");
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

    public void fillLookAhead(Printer printer, int prevHeight, int curHeight){
        String prefix, condition;
        if(curHeight==prevHeight+1){
            prefix = "if";
            condition = "ch!=EOF";
        }else{
            prefix = "while";
            condition = "ch!=EOF && laLen<"+curHeight;
        }
        printer.printlns(
            prefix+"("+condition+"){",
                PLUS,
                "if((ch=codePoint())==EOC)",
                    PLUS,
                    state.breakStatement(),
                    MINUS,
                "addToLookAhead(ch);",
                MINUS,
            "}"
        );
    }
}
