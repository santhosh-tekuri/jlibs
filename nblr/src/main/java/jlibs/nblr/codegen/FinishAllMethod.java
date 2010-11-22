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
import jlibs.nblr.matchers.Any;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.matchers.Not;
import jlibs.nblr.matchers.Range;
import jlibs.nbp.NBParser;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public class FinishAllMethod{
    Matcher matcher;
    String methodName;
    public boolean returnValueRequired = false;

    public FinishAllMethod(Matcher matcher, String methodName){
        this.matcher = matcher;
        this.methodName = methodName;
        if(SyntaxClass.FINISH_ALL.equals(methodName) || SyntaxClass.FINISH_ALL_OTHER_THAN.equals(methodName))
            returnValueRequired = true;
        else
            returnValueRequired = matcher.clashesWith(Range.SUPPLIMENTAL) || matcher.clashesWith(Any.NEW_LINE);
    }

    public void use(Printer printer, State state, boolean returnValueRequired){
        String ch = state.readMethod();
        String methodCall;
        if(methodName.equals(SyntaxClass.FINISH_ALL) || methodName.equals(SyntaxClass.FINISH_ALL_OTHER_THAN)){
            Any any = (Any)(methodName.equals(SyntaxClass.FINISH_ALL_OTHER_THAN) ? ((Not)matcher).delegate : matcher);
            methodCall = methodName+"("+ch+", "+Matcher.toJava(any.chars[0])+')';
        }else{
            if(!matcher.clashesWith(Range.SUPPLIMENTAL) && !matcher.clashesWith(Any.NEW_LINE))
                ch = "";
            methodCall = "finishAll_"+methodName+"("+ch+")";
        }

        if(returnValueRequired)
            methodCall = "(ch="+methodCall+")";

        String condition = methodCall+"==EOC";
        if(!this.returnValueRequired)
            condition = methodCall;

        printer.printlns(
            "if("+condition+")",
                PLUS,
                state.breakStatement(),
                MINUS
        );
    }

    public void generate(Printer printer){
        String condition = matcher._javaCode("ch");
        if(matcher.checkFor(NBParser.EOF) || matcher.checkFor(NBParser.EOC))
            condition = "ch>=0 && "+condition;

        if(!matcher.clashesWith(Range.SUPPLIMENTAL) && !matcher.clashesWith(Any.NEW_LINE)){
            condition = matcher._javaCode("ch");
            String returnType = returnValueRequired ? "int" : "boolean";
            String returnValue = returnValueRequired ? "codePoint()" : "position==limit && marker==EOC";
            printer.printlns(
                "private "+returnType+" finishAll_"+methodName+"() throws IOException{",
                    PLUS,
                    "int _position = position;",
                    "while(position<limit){",
                        PLUS,
                        "char ch = input[position];",
                        "if("+condition+")",
                            PLUS,
                            "++position;",
                            MINUS,
                        "else",
                            PLUS,
                            "break;",
                            MINUS,
                        MINUS,
                    "}",
                    "int len = position-_position;",
                    "if(len>0 && buffer.isBuffering())",
                        PLUS,
                        "buffer.append(input, _position, len);",
                        MINUS,
                    "return "+returnValue+";",
                    MINUS,
                "}"
            );
        }else{
            printer.printlns(
                "private int finishAll_"+methodName+"(int ch) throws IOException{",
                    PLUS,
                    "while("+condition+"){",
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
    }
}