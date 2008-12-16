package jlibs.core.lang;

import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class Util{
    /**
     * returns hashCode of given argument.
     * if argument is null, then returns 0
     */
    public static int hashCode(Object obj){
        return obj!=null ? obj.hashCode() : 0;
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
                    return Arrays.equals((Object[])obj1, (Object[])obj2);
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
                    return false;
            }else
                return false;
        }else
            return obj1.equals(obj2);
    }
}
