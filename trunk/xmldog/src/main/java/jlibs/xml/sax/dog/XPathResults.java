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

package jlibs.xml.sax.dog;

import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.EvaluationListener;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.sniff.Event;

import javax.xml.namespace.NamespaceContext;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XPathResults extends EvaluationListener{
    private Event event;
    private Object results[];

    public XPathResults(Event event, int documentXPathsCount, Iterable<Expression> expressions){
        this.event = event;
        results = new Object[documentXPathsCount];
        for(Expression expr: expressions){
            if(expr.scope()==Scope.DOCUMENT)
                event.addListener(expr, this);
        }
    }

    @Override
    public void finished(Evaluation evaluation){
        results[evaluation.expression.id] = evaluation.getResult();
    }

    public NamespaceContext getNamespaceContext(){
        return event.getNamespaceContext();
    }

    @SuppressWarnings({"unchecked"})
    public Object getResult(Expression expr){
        if(expr.scope()==Scope.DOCUMENT)
            return results[expr.id];
        else{
            assert expr.scope()==Scope.GLOBAL;
            Object result = expr.getResult();
            if(expr.resultType==DataType.NODESET){
                List<NodeItem> list = (List<NodeItem>)result;
                if(list.size()==1)
                    return Collections.singletonList(event.documentNodeItem());
                else
                    return result;
            }else
                return expr.getResult();
        }
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/

    public static void print(PrintStream out, String xpath, Object result){
        out.printf("XPath: %s%n", xpath);
        print(out, result, 2);
    }

    private static void printIndent(PrintStream out, int indent){
        for(int i=0; i<indent; i++)
            out.print(" ");
    }
    
    private static void print(PrintStream out, Object result, int indent){
        if(result instanceof Collection){
            int i = 0;
            Collection c = (Collection)result;

            String format = "%0"+String.valueOf(c.size()).length()+"d: %s%n";
            for(Object item: c){
                if(item instanceof Collection){
                    printIndent(out, indent);
                    out.println("[");
                    print(out, item, indent+2);
                    printIndent(out, indent);
                    out.println("]");
                }else{
                    printIndent(out, indent);
                    out.printf(format, ++i, item);
                }
            }
        }else{
            printIndent(out, indent);
            if(result instanceof NodeItem)
                ((NodeItem)result).printTo(out);
            else
                out.print(result);
            out.println();
        }
    }

    public void printResult(PrintStream out, Expression expr){
        print(out, expr.getXPath(), getResult(expr));
    }

    public void print(Iterable<Expression> expressions, PrintStream out){
        for(Expression expr: expressions){
            print(out, expr.getXPath(), getResult(expr));
            out.println();
        }
    }
}
