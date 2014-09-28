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

/**
 * @author Santhosh Kumar Tekuri
 */
class ExpressionParser{
    public static Expression compile(String str) throws ParseException{
        CharBuffer buffer = CharBuffer.wrap(str.toCharArray());
        skipWhitespace(buffer);
        Expression expr = parseValue(buffer);
        skipWhitespace(buffer);
        if(buffer.hasRemaining())
            throw new ParseException("invalid expression", buffer.position());
        return expr;
    }

    private static Expression parseValue(CharBuffer buffer) throws ParseException{
        char ch = buffer.get(buffer.position());
        if(ch=='\'')
            return parseString(buffer);
        else{
            Variable var = new Variable();
            var.children.add(new GetField(parseIdentifier(buffer)));
            skipWhitespace(buffer);
            while(buffer.hasRemaining()){
                ch = buffer.get(buffer.position());
                if(ch=='.'){
                    buffer.get();
                    skipWhitespace(buffer);
                    var.children.add(new GetField(parseIdentifier(buffer)));
                }else if(ch=='['){
                    buffer.get();
                    skipWhitespace(buffer);
                    var.children.add(new Lookup(parseValue(buffer)));
                    skipWhitespace(buffer);
                    if(buffer.get()!=']')
                        throw new ParseException("expected ]", buffer.position()-1);
                }else
                    break;
            }
            return var;
        }
    }

    private static Literal parseString(CharBuffer buffer) throws ParseException{
        buffer.get();
        StringBuilder builder = new StringBuilder();
        while(true){
            if(buffer.hasRemaining()){
                char ch = buffer.get();
                if(ch=='\'')
                    break;
                builder.append(ch);
            }else
                throw new ParseException("Unexpected EOF", buffer.position());
        }
        return new Literal(builder.toString());
    }

    private static String parseIdentifier(CharBuffer buffer) throws ParseException{
        if(!buffer.hasRemaining())
            throw new ParseException("Unexpected EOF", buffer.position());

        StringBuilder builder = new StringBuilder();
        char ch = buffer.get();
        if(!Character.isJavaIdentifierStart(ch))
            throw new ParseException("Invalid Identifier", buffer.position()-1);
        builder.append(ch);
        while(buffer.hasRemaining()){
            ch = buffer.get();
            if(Character.isJavaIdentifierPart(ch))
                builder.append(ch);
            else{
                buffer.position(buffer.position()-1);
                break;
            }
        }
        return builder.toString();
    }

    private static void skipWhitespace(CharBuffer buffer){
        while(buffer.hasRemaining()){
            if(!Character.isWhitespace(buffer.get())){
                buffer.position(buffer.position()-1);
                break;
            }
        }
    }
}
