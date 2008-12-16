package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public class ImpossibleException extends RuntimeException{
    public ImpossibleException(){
        this("this is impossible");
    }

    public ImpossibleException(String message){
        super(message);
    }

    public ImpossibleException(String message, Throwable cause){
        super(message, cause);
    }

    public ImpossibleException(Throwable cause){
        super(cause);
    }
}
