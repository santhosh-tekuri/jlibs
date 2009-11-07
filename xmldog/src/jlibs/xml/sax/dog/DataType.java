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

import jlibs.core.lang.ImpossibleException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.util.Collections;
import java.util.List;

/**
 * This class represents various possible datatypes of the xpath result.
 * This class also has methods to conevert result from one datatype to
 * another. Note that convertion of NodeSet to another and viceversa
 * is unsupported.
 *
 * @author Santhosh Kumar T
 */
public enum DataType{
    NODESET(XPathConstants.NODESET, Collections.emptyMap()),
    STRING(XPathConstants.STRING, ""),
    NUMBER(XPathConstants.NUMBER, 0d),
    BOOLEAN(XPathConstants.BOOLEAN, false),
    STRINGS(new QName("http://jlibs.org", "strings"), Collections.emptyList()),
    NUMBERS(new QName("http://jlibs.org", "numbers"), Collections.emptyList()),
    PRIMITIVE(new QName("http://jlibs.org", "primitive"), null);

    public final QName qname;
    public final Object defaultValue;
    private DataType(QName qname, Object defaultValue){
        this.qname = qname;
        this.defaultValue = defaultValue;
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
            return (Boolean)obj ? ONE : ZERO;
        else
            throw new ImpossibleException(obj.getClass().getName());
    }

    public static String asString(Object obj){
        return String.valueOf(obj);
    }

    public static DataType valueOf(Object literal){
        if(literal instanceof String)
            return DataType.STRING;
        else if(literal instanceof Number)
            return DataType.NUMBER;
        else if(literal instanceof Boolean)
            return DataType.BOOLEAN;
        else{
            assert literal instanceof List;
            return DataType.NODESET;
        }
    }

    public static final Double ZERO = 0d;
    public static final Double ONE = 1d;
}
