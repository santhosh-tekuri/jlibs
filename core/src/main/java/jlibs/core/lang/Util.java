/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.core.lang;

import jlibs.core.io.ByteArrayOutputStream2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class Util{
    /**
     * Returns first non null element in <code>values</code>.
     * If all elements are null, it returns null.
     */
    public static <T> T notNull(T... values){
        for(T value: values){
            if(value!=null)
                return value;
        }
        return null;
    }

    /**
     * Returns true if given two objects are same.
     *
     * null values are handled as follows:
     *    null != non-null
     *    null == null
     *
     * Arrays are handled using Arrays.equals(...)
     */
    public static boolean equals(Object obj1, Object obj2){
        if(obj1==obj2)
            return true;
        else if(obj1==null || obj2==null)
            return false;
        else if(obj1.getClass().isArray()){
            if(obj2.getClass().isArray()){
                if(obj1 instanceof Object[] && obj2 instanceof Object[])
                    return Arrays.deepEquals((Object[])obj1, (Object[])obj2);
                else if(obj1 instanceof boolean[] && obj2 instanceof boolean[])
                    return Arrays.equals((boolean[])obj1, (boolean[])obj2);
                else if(obj1 instanceof char[] && obj2 instanceof char[])
                    return Arrays.equals((char[])obj1, (char[])obj2);
                else if(obj1 instanceof byte[] && obj2 instanceof byte[])
                    return Arrays.equals((byte[])obj1, (byte[])obj2);
                else if(obj1 instanceof short[] && obj2 instanceof short[])
                    return Arrays.equals((short[])obj1, (short[])obj2);
                else if(obj1 instanceof int[] && obj2 instanceof int[])
                    return Arrays.equals((int[])obj1, (int[])obj2);
                else if(obj1 instanceof long[] && obj2 instanceof long[])
                    return Arrays.equals((long[])obj1, (long[])obj2);
                else if(obj1 instanceof float[] && obj2 instanceof float[])
                    return Arrays.equals((float[])obj1, (float[])obj2);
                else if(obj1 instanceof double[] && obj2 instanceof double[])
                    return Arrays.equals((double[])obj1, (double[])obj2);
                else
                    throw new ImpossibleException("couldn't do equals for"+obj1.getClass().getComponentType().getSimpleName()+"[]");
            }else
                return false;
        }else
            return obj1.equals(obj2);
    }

    /*-------------------------------------------------[ HashCode ]---------------------------------------------------*/
    
    /**
     * returns hashCode of given argument.
     * if argument is null, returns 0
     */
    public static int hashCode(Object obj){
        if(obj==null)
            return 0;
        else if(obj.getClass().isArray()){
            if(obj instanceof Object[])
                return Arrays.deepHashCode((Object[])obj);
            else if(obj instanceof boolean[])
                return Arrays.hashCode((boolean[])obj);
            else if(obj instanceof char[])
                return Arrays.hashCode((char[])obj);
            else if(obj instanceof byte[])
                return Arrays.hashCode((byte[])obj);
            else if(obj instanceof short[])
                return Arrays.hashCode((short[])obj);
            else if(obj instanceof int[])
                return Arrays.hashCode((int[])obj);
            else if(obj instanceof long[])
                return Arrays.hashCode((long[])obj);
            else if(obj instanceof float[])
                return Arrays.hashCode((float[])obj);
            else if(obj instanceof double[])
                return Arrays.hashCode((double[])obj);
            else
                throw new ImpossibleException("couldn't find hascode for"+obj.getClass().getComponentType().getSimpleName()+"[]");
        }else
            return obj.hashCode();
    }

    /**
     * returns hashCode of given arguments.
     * if any argument is null, then it considers
     * its hashCode as zero
     */
    public static int hashCode(Object... objects){
        int hashCode = 0;
        for(Object obj: objects)
            hashCode = 31*hashCode + hashCode(obj);
        return hashCode;
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown", "unchecked"})
    public static <T> T clone(T obj) throws CloneNotSupportedException{
        if(obj==null)
            return null;
        
        try{
            if(obj instanceof Cloneable){
                Method method = obj.getClass().getMethod("clone");
                if(!Modifier.isPublic(method.getModifiers()))
                    method.setAccessible(true);
                return (T)method.invoke(obj);
            }else if(obj instanceof Serializable){
                ByteArrayOutputStream2 bout = new ByteArrayOutputStream2();
                ObjectOutputStream objOut = new ObjectOutputStream(bout);
                objOut.writeObject(obj);
                objOut.close();
                ObjectInputStream objIn = new ObjectInputStream(bout.toByteSequence().asInputStream());
                return (T)objIn.readObject();
            }else
                throw new CloneNotSupportedException(obj.getClass().getName());
        }catch(Exception ex){
            if(ex instanceof CloneNotSupportedException)
                throw (CloneNotSupportedException)ex;
            else
                throw (CloneNotSupportedException)new CloneNotSupportedException().initCause(ex);
        }
    }

    public static long parseLong(String value){
        int len = value.length();
        if(len==0)
            throw new NumberFormatException("empty string");

        int i = 0;
        boolean negative = false;
        char firstChar = value.charAt(0);
        if(firstChar<'0'){
            if(firstChar=='-')
                negative = true;
            else if(firstChar!='+')
                throw new NumberFormatException("for input string \""+value+"\"");
            if(len==1)
                throw new NumberFormatException("cannot have lone + or -");
            ++i;
        }

        int result = 0;
        while(i<len){
            char ch = value.charAt(i++);
            if(ch>='0' && ch<='9')
                result = result*10 + (ch-'0');
            else
                throw new NumberFormatException("for input string \""+value+"\"");
        }
        return negative ? -result : result;
    }
}
