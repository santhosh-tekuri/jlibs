package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public class NotImplementedException extends RuntimeException{
    public NotImplementedException(){
    }

    public NotImplementedException(String message){
        super(message);
    }
}
