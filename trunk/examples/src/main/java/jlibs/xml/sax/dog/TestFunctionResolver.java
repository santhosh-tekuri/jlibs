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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class TestFunctionResolver implements XPathFunctionResolver{
    @Override
    public XPathFunction resolveFunction(QName functionName, int arity) {
        if(functionName.equals(new QName("http://jlibs.googlecode.com", "reverse")))
            return REVERSE;
        else
            return null;
    }
    
    private static final XPathFunction REVERSE = new XPathFunction() {
        @Override
        public Object evaluate(List args) throws XPathFunctionException{
            char[] ch = ((String)args.get(0)).toCharArray();
            for(int i=0, j=ch.length-1; i<j; i++, j--){
                char temp = ch[i];
                ch[i] = ch[j];
                ch[j] = temp;
            }
            return new String(ch);
        }
    }; 
}
