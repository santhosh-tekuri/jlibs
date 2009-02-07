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

package jlibs.xml.sax.sniff.model;

import jlibs.core.lang.ImpossibleException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.util.Collections;

/**
 * @author Santhosh Kumar T
 */
public enum Datatype{
    NODESET(XPathConstants.NODESET, Collections.emptyMap()),
    STRING(XPathConstants.STRING, ""),
    NUMBER(XPathConstants.NUMBER, 0.0d),
    BOOLEAN(XPathConstants.BOOLEAN, false),
    STRINGS(new QName("http://jlibs.org", "STRINGS"), Collections.emptyList());

    private QName qname;
    private Object defaultValue;
    private Datatype(QName qname, Object defaultValue){
        this.qname = qname;
        this.defaultValue = defaultValue;
    }

    public QName qname(){
        return qname;
    }

    public Object defaultValue(){
        return defaultValue;
    }

    public Object convert(Object result){
        switch(this){
            case STRING:
                return asString(result);
            case BOOLEAN:
                return asBoolean(result);
            case NUMBER:
                return asNumber(result);
        }
        throw new UnsupportedOperationException("can't be converted to "+this);
    }

    public static boolean asBoolean(Object obj){
        if(obj instanceof String)
            return ((String)obj).length()>0;
        else if(obj instanceof Double){
            double number = (Double)obj;
            return number!=0 && !Double.isNaN(number);
        }else if(obj instanceof Boolean)
            return (Boolean)obj;
        else
            throw new ImpossibleException(obj.getClass().getName());
    }

    public static double asNumber(Object obj){
        if(obj instanceof String){
            try{
                return Double.parseDouble((String)obj);
            }catch(NumberFormatException ex){
                return Double.NaN;
            }
        }else if(obj instanceof Double)
            return (Double)obj;
        else if(obj instanceof Boolean)
            return (Boolean)obj ? 1 : 0;
        else
            throw new ImpossibleException(obj.getClass().getName());
    }

    public static String asString(Object obj){
        return String.valueOf(obj);
    }
}
