package jlibs.core.lang;

import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public class Util{
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
            hashCode += hashCode(obj);
        return hashCode;
    }
}
