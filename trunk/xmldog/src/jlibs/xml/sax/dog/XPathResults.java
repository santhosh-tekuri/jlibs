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

package jlibs.xml.sax.dog;

import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.EvaluationListener;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.sniff.Event;

import javax.xml.namespace.NamespaceContext;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class XPathResults extends EvaluationListener{
    private Event event;
    private Map<Expression, Object> results = new HashMap<Expression, Object>();

    public XPathResults(Event event, Iterable<Expression> expressions){
        this.event = event;
        for(Expression expr: expressions){
            if(expr.scope()==Scope.GLOBAL)
                results.put(expr, expr.getResult());
            else
                event.addListener(expr, this);
        }
    }

    @Override
    public void finished(Evaluation evaluation){
        results.put(evaluation.expression, evaluation.getResult());
    }

    public NamespaceContext getNamespaceContext(){
        return event.getNamespaceContext();
    }

    public Object getResult(Expression expr){
        return results.get(expr);
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/

    private void print(PrintStream out, String xpath, Object result){
        out.printf("XPath: %s%n", xpath);
        if(result instanceof Collection){
            int i = 0;
            Collection c = (Collection)result;
            String format = "  %0"+String.valueOf(c.size()).length()+"d: %s%n";
            for(Object item: c)
                out.printf(format, ++i, item);
        }else
            out.printf("  %s\n", result);
    }

    public void printResult(PrintStream out, Expression expr){
        print(out, expr.getXPath(), results.get(expr));
    }

    public void print(PrintStream out){
        for(Map.Entry<Expression, Object> entry: results.entrySet()){
            print(out, entry.getKey().getXPath(), entry.getValue());
            out.println();
        }
    }
}
