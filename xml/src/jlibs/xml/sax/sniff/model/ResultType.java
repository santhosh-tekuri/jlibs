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
import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public enum ResultType{
    NODESET(XPathConstants.NODESET, null){
        @Override
        public boolean asBoolean(TreeMap<Integer, String> results){
            return results!=null && results.size()>0;
        }
    },
    
    STRING(XPathConstants.STRING, ""){
        public String asString(TreeMap<Integer, String> results){
            return results!=null && results.size()>0 ? results.firstEntry().getValue() : "";
        }

        @Override
        public boolean asBoolean(TreeMap<Integer, String> results){
            return asString(results).length()>0;
        }

        @Override
        public double asNumber(TreeMap<Integer, String> results){
            try{
                return Double.parseDouble(asString(results));
            }catch(NumberFormatException ex){
                return Double.NaN;
            }
        }

        @Override
        public Object convert(String result){
            return result;
        }
    },

    NUMBER(XPathConstants.NUMBER, "0.0"){
        public String asString(TreeMap<Integer, String> results){
            return String.valueOf(asNumber(results));
        }

        public boolean asBoolean(TreeMap<Integer, String> results){
            double number = asNumber(results);
            return number!=0 && !Double.isNaN(number);
        }

        public double asNumber(TreeMap<Integer, String> results){
            String str = results!=null && results.size()>0 ? results.firstEntry().getValue() : "0.0";
            try{
                return Double.parseDouble(str);
            }catch(NumberFormatException ex){
                return Double.NaN;
            }
        }

        @Override
        public Object convert(String result){
            return asNumber(result);
        }
    },

    BOOLEAN(XPathConstants.BOOLEAN, "false"){
        public String asString(TreeMap<Integer, String> results){
            return String.valueOf(asBoolean(results));
        }

        public boolean asBoolean(TreeMap<Integer, String> results){
            String str = results!=null && results.size()>0 ? results.firstEntry().getValue() : "";
            return Boolean.valueOf(str);
        }

        public double asNumber(TreeMap<Integer, String> results){
            return asBoolean(results) ? 1 : 0;
        }

        @Override
        public Object convert(String result){
            return asBoolean(result);
        }
    },
    
    STRINGS(new QName("http://www.w3.org/1999/XSL/Transform", "STRINGS"), null);

    private QName qname;
    private String defaultValue;
    private ResultType(QName qname, String defaultValue){
        this.qname = qname;
        this.defaultValue = defaultValue;
    }

    public QName qname(){
        return qname;
    }

    public String defaultValue(){
        return defaultValue;
    }

    public boolean asBoolean(TreeMap<Integer, String> results){
        throw new UnsupportedOperationException(toString()+" can't be converted to Boolean");
    }

    public String asString(TreeMap<Integer, String> results){
        throw new UnsupportedOperationException(toString()+" can't be converted to String");
    }

    public double asNumber(TreeMap<Integer, String> results){
        throw new UnsupportedOperationException(toString()+" can't be converted to Number");
    }

    public Object convert(String result){
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
