/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.sniff.model.functions;

import jlibs.core.lang.NotImplementedException;
import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;

/**
 * @author Santhosh Kumar T
 */
public abstract class Function extends Node{
    public abstract String getName();
    public abstract boolean singleHit();

    @Override
    public boolean matches(Event event){
        return true;
    }

    @Override
    public boolean equivalent(Node node){
        return node.getClass()==getClass();
    }

    @Override
    public ResultType resultType(){
        return ResultType.STRING;
    }
    
    @Override
    public String toString(){
        return getName()+"()";
    }

    /*-------------------------------------------------[ Evaluate ]---------------------------------------------------*/

    public abstract String evaluate(Event event, String lastResult);
    
    public void evaluateWithLastResult(Event event){
        String lastResult = null;
        if(results!=null)
            lastResult = results.remove(results.lastKey());
        addResult(event.order(), evaluate(event, lastResult));
    }

    /*-------------------------------------------------[ UserResults ]---------------------------------------------------*/
    
    public String join(String result1, String result2){
        throw new NotImplementedException(getClass().getName());
    }

    public void joinResults(){
        if(results!=null && results.size()==2){
            String result1 = results.remove(results.firstKey());
            String result2 = results.lastEntry().getValue();
            String result = join(result1, result2);
            results.put(results.lastKey(), result);
        }
    }

    public abstract String defaultResult();

    @Override
    public void prepareResults(){
        joinResults();
        if(results==null || results.size()==0)
            addResult(-1, defaultResult());
    }

    /*-------------------------------------------------[ Hit ]---------------------------------------------------*/

    public boolean hit(Context context, Event event){
        if(singleHit()){
            if(consumable(event)){
                if(context.node!=this){
                    if(!hasResult()){
                        addResult(event.order(), evaluate(event, null));
                        return true;
                    }else
                        return false;
                }else{
                    evaluateWithLastResult(event);
                    return true;
                }
            }else{
                if(!hasResult())
                    addResult(event.order(), evaluate(event, null));
            }
        }else{
            if(consumable(event)){
                if(context.node!=this){
                    joinResults();
                    addResult(event.order(), evaluate(event, null));
                }else
                    evaluateWithLastResult(event);

                return true;
            }else
                evaluateWithLastResult(event);
        }
        return false;
    }

    /*-------------------------------------------------[ Factory ]---------------------------------------------------*/
    
    public static Function newInstance(String name){
        if("namespace-uri".equals(name))
            return new NamespaceURI();
        else if("string".equals(name))
            return new StringFunction();
        else if("strings".equals(name))
            return new Strings();
        else if("sum".equals(name))
            return new Sum();
        else
            throw new NotImplementedException("function "+name+"() is not supported");
    }
}
