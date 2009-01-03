package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public class ArrayUtil{
    public static int indexOf(Object array[], Object item){
        if(array==null)
            return -1;
        
        for(int i=0; i<array.length; i++){
            if(Util.equals(array[i], item))
                return i;
        }
        return -1;
    }

    public static boolean contains(Object array[], Object item){
        return indexOf(array, item)!=-1;
    }
}
