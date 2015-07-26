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

package jlibs.xml.sax.dog;

import jlibs.core.lang.ImpossibleException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.util.Collections;
import java.util.List;

/**
 * This class represents various possible datatypes of the xpath result.
 *
 * This class also has methods to convert result from one datatype to
 * another. Note that convertion of NodeSet to another and viceversa
 * is unsupported.
 *
 * @author Santhosh Kumar T
 */
public enum DataType{
    /**
     * following are standard datatypes
     */
    NODESET(XPathConstants.NODESET, Collections.emptyMap()),
    STRING(XPathConstants.STRING, ""),
    NUMBER(XPathConstants.NUMBER, 0d),
    BOOLEAN(XPathConstants.BOOLEAN, false),

    /**
     * these are extra datatypes that are used internally
     */
    STRINGS(new QName("http://jlibs.org", "strings"), Collections.emptyList()),
    NUMBERS(new QName("http://jlibs.org", "numbers"), Collections.emptyList()),
    PRIMITIVE(new QName("http://jlibs.org", "primitive"), null);

    public final QName qname;
    public final Object defaultValue;
    private DataType(QName qname, Object defaultValue){
        this.qname = qname;
        this.defaultValue = defaultValue;
    }

    /*-------------------------------------------------[ Convertions ]---------------------------------------------------*/

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

    public static final Double ZERO = 0d;
    public static final Double ONE = 1d;
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

    /*-------------------------------------------------[ Guessing Datatype ]---------------------------------------------------*/

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
}
