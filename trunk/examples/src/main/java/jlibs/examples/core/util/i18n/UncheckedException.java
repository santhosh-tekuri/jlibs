package jlibs.examples.core.util.i18n;

/**
 * @author Santhosh Kumar T
 */
public class UncheckedException extends RuntimeException{
    private String errorCode;

    public UncheckedException(String errorCode, String message){
        super(message);
        this.errorCode = errorCode;
        printStackTrace();
    }
    
    public String getErrorCode(){
        return errorCode;
    }

    @Override
    public String toString(){
        String s = getClass().getName()+": "+errorCode;
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
