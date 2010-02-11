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

package jlibs.core.graph;

import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public abstract class Navigator2<E> extends Ladder<E> implements Navigator<E>{
    public String getRelativePath(E fromElem, E toElem, Convertor<E, String> convertor, String separator, boolean predicates){
        if(predicates)
            convertor = new PredicateConvertor<E>(this, convertor);
        return super.getRelativePath(fromElem, toElem, convertor, separator);
    }

    public E resolve(E node, String path, Convertor<E, String> convertor, String separator){
        if(path.equals("."))
            return node;

        String tokens[] = Pattern.compile(separator, Pattern.LITERAL).split(path);
        for(String token: tokens){
            if(token.equals("..")){
                node = parent(node);
                continue;
            }
            int predicate = 1;
            int openBrace = token.lastIndexOf('[');
            if(openBrace!=-1){
                predicate = Integer.parseInt(token.substring(openBrace+1, token.length()-1));
                token = token.substring(0, openBrace);
            }

            Sequence<? extends E> children = children(node);
            while(children.hasNext()){
                E child = children.next();
                if(token.equals(convertor.convert(child))){
                    if(predicate==1){
                        node = child;
                        break;
                    }else
                        predicate--;
                }
            }
        }
        return null;
    }
}
