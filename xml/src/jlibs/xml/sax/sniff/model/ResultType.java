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

        public double asNumber(TreeMap<Integer, String> results){
            try{
                return Double.parseDouble(asString(results));
            }catch(NumberFormatException ex){
                return Double.NaN;
            }
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
}
