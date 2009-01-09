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

package jlibs.xml.sax.sniff;

import org.jaxen.saxpath.*;
import org.jaxen.saxpath.helpers.XPathReaderFactory;
import org.jaxen.saxpath.helpers.DefaultXPathHandler;
import jlibs.core.lang.reflect.TeeProxy;

/**
 * @author Santhosh Kumar T
 */
public class DebugXPathHandler implements XPathHandler{
    private int depth = 0;

    public void println(String str, Object... args){
        for(int i=0; i<depth; i++)
            System.out.print("  |");
        System.out.format(str, args);
        System.out.println();
    }

    @Override
    public void startXPath() throws SAXPathException{
        println("startXPath");
        depth++;
    }

    @Override
    public void endXPath() throws SAXPathException{
        depth--;
        println("endXPath");
    }

    @Override
    public void startPathExpr() throws SAXPathException{
        println("startPathExpr");
        depth++;
    }

    @Override
    public void endPathExpr() throws SAXPathException{
        depth--;
        println("endPathExpr");
    }

    @Override
    public void startAbsoluteLocationPath() throws SAXPathException{
        println("startAbsoluteLocationPath");
        depth++;
    }

    @Override
    public void endAbsoluteLocationPath() throws SAXPathException{
        depth--;
        println("endAbsoluteLocationPath");
    }

    @Override
    public void startRelativeLocationPath() throws SAXPathException{
        println("startRelativeLocationPath");
        depth++;
    }

    @Override
    public void endRelativeLocationPath() throws SAXPathException{
        depth--;
        println("endRelativeLocationPath");
    }

    @Override
    public void startNameStep(int axis, String prefix, String localName) throws SAXPathException{
        println("startNameStep(%s, '%s', '%s')", Axis.lookup(axis), prefix, localName);
        depth++;
    }

    @Override
    public void endNameStep() throws SAXPathException{
        depth--;
        println("endNameStep");
    }

    @Override
    public void startTextNodeStep(int axis) throws SAXPathException{
        println("startTextNodeStep(%s)", Axis.lookup(axis));
    }

    @Override
    public void endTextNodeStep() throws SAXPathException{
        println("endTextNodeStep");
    }

    @Override
    public void startCommentNodeStep(int axis) throws SAXPathException{
        println("startCommentNodeStep(%s)", Axis.lookup(axis));
    }

    @Override
    public void endCommentNodeStep() throws SAXPathException{
        println("endCommentNodeStep");
    }

    @Override
    public void startAllNodeStep(int axis) throws SAXPathException{
        println("startAllNodeStep(%s)", Axis.lookup(axis));
    }

    @Override
    public void endAllNodeStep() throws SAXPathException{
        println("endAllNodeStep");
    }

    @Override
    public void startProcessingInstructionNodeStep(int axis, String name) throws SAXPathException{
        println("startProcessingInstructionNodeStep(%s, %s)", Axis.lookup(axis), name);
    }

    @Override
    public void endProcessingInstructionNodeStep() throws SAXPathException{
        println("endProcessingInstructionNodeStep");
    }

    @Override
    public void startPredicate() throws SAXPathException{
        println("startPredicate");
    }

    @Override
    public void endPredicate() throws SAXPathException{
        println("endPredicate");
    }

    @Override
    public void startFilterExpr() throws SAXPathException{
        println("startFilterExpr");
        depth++;
    }

    @Override
    public void endFilterExpr() throws SAXPathException{
        depth--;
        println("endFilterExpr");
    }

    @Override
    public void startOrExpr() throws SAXPathException{
        println("startOrExpr");
        depth++;
    }

    @Override
    public void endOrExpr(boolean create) throws SAXPathException{
        depth--;
        println("endOrExpr(%s)", create);
    }

    @Override
    public void startAndExpr() throws SAXPathException{
        println("startAndExpr");
        depth++;
    }

    @Override
    public void endAndExpr(boolean create) throws SAXPathException{
        depth--;
        println("endAndExpr(%s)", create);
    }

    @Override
    public void startEqualityExpr() throws SAXPathException{
        println("startEqualityExpr");
    }

    @Override
    public void endEqualityExpr(int equalityOperator) throws SAXPathException{
        println("endEqualityExpr(%s)", equalityOperator);
    }

    @Override
    public void startRelationalExpr() throws SAXPathException{
        println("startRelationalExpr");
    }

    @Override
    public void endRelationalExpr(int relationalOperator) throws SAXPathException{
        println("endRelationalExpr(%s)", relationalOperator);
    }

    @Override
    public void startAdditiveExpr() throws SAXPathException{
        println("startAdditiveExpr");
    }

    @Override
    public void endAdditiveExpr(int additiveOperator) throws SAXPathException{
        println("endAdditiveExpr");
    }

    @Override
    public void startMultiplicativeExpr() throws SAXPathException{
        println("startMultiplicativeExpr");
    }

    @Override
    public void endMultiplicativeExpr(int multiplicativeOperator) throws SAXPathException{
        println("endMultiplicativeExpr");
    }

    @Override
    public void startUnaryExpr() throws SAXPathException{
        println("startUnaryExpr");
    }

    @Override
    public void endUnaryExpr(int unaryOperator) throws SAXPathException{
        println("endUnaryExpr");
    }

    @Override
    public void startUnionExpr() throws SAXPathException{
        println("startUnionExpr");
        depth++;
    }

    @Override
    public void endUnionExpr(boolean create) throws SAXPathException{
        println("endUnionExpr(%s)", create);
        depth--;
    }

    @Override
    public void number(int number) throws SAXPathException{
        println("number(%d)", number);
    }

    @Override
    public void number(double number) throws SAXPathException{
        println("number(%f)", number);
    }

    @Override
    public void literal(String literal) throws SAXPathException{
        println("literal('%s')", literal);
    }

    @Override
    public void variableReference(String prefix, String variableName) throws SAXPathException{
        println("variableReference('%s', '%s')", prefix, variableName);
    }

    @Override
    public void startFunction(String prefix, String functionName) throws SAXPathException{
        println("startFunction('%s', '%s')", prefix, functionName);
    }

    @Override
    public void endFunction() throws SAXPathException{
        println("endFunction");
    }

    public static void main(String[] args) throws SAXPathException{
        XPathReader reader = XPathReaderFactory.createReader();
        XPathHandler handler = new DebugXPathHandler();
        handler = TeeProxy.create(XPathHandler.class, handler, new DefaultXPathHandler());
        reader.setXPathHandler(handler);
        reader.parse("/child::elem1//elem2/elem3/@abcd");
    }
}
