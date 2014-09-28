/*
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

package jlibs.nio.http.expr;

import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.ArrayDeque;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface Expression{
    public Object evaluate(Object root, Object current);

    public default Object evaluate(Object root){
        return evaluate(root, root);
    }

    public static Expression compile(String expression) throws ParseException{
        return ExpressionParser.compile(expression);
    }
}

class Compiler{
    private enum Type{ EOF, WHITESPACE, STRING, IDENTIFIER, DOT, OPEN_BRACE, CLOSE_BRACE }

    private ArrayDeque<ArrayDeque> frames = new ArrayDeque<>();

    public Expression compile(String string){
        CharBuffer buffer = CharBuffer.wrap(string.toCharArray());
        while(true){
            Type type = parseToken(buffer);
            if(type==Type.EOF)
                return (Expression)frames.pop().pop();

        }
    }

    private StringBuilder token = new StringBuilder();
    private Type parseToken(CharBuffer buffer){
        token.setLength(0);
        if(buffer.hasRemaining()){
            char ch = buffer.get();
            if(Character.isWhitespace(ch)){
                token.append(ch);
                while(buffer.hasRemaining()){
                    ch = buffer.get();
                    if(Character.isWhitespace(ch))
                        token.append(ch);
                    else{
                        buffer.position(buffer.position()-1);
                        break;
                    }
                }
                return Type.WHITESPACE;
            }else if(ch=='\''){
                while(true){
                    ch = buffer.get();
                    if(ch=='\'')
                        break;
                    token.append(ch);
                }
                return Type.STRING;
            }else if(ch=='.')
                return Type.DOT;
            else if(ch=='[')
                return Type.OPEN_BRACE;
            else if(ch==']')
                return Type.CLOSE_BRACE;
            else if(Character.isJavaIdentifierStart(ch)){
                token.append(ch);
                while(buffer.hasRemaining()){
                    ch = buffer.get();
                    if(Character.isJavaIdentifierPart(ch))
                        token.append(ch);
                    else{
                        buffer.position(buffer.position()-1);
                        break;
                    }
                }
                return Type.IDENTIFIER;
            }else
                throw new IllegalArgumentException();
        }else
            return Type.EOF;
    }
}
