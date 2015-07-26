/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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
