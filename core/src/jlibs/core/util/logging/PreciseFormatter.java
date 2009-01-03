package jlibs.core.util.logging;

import jlibs.core.lang.ImpossibleException;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Santhosh Kumar T
 */
public class PreciseFormatter extends Formatter{
    private CharArrayWriter writer = new CharArrayWriter();

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public String format(LogRecord record){

        try{
            writer.write(record.getLevel().toString());
            writer.write('\t');
            writer.write(':');
            writer.write(formatMessage(record));
            writer.write('\n');
        }catch(IOException ex){
            throw new ImpossibleException(ex);
        }
        if(record.getThrown()!=null){
            PrintWriter printer = new PrintWriter(writer);
            record.getThrown().printStackTrace(printer);
            printer.close();
        }
        
        String result = writer.toString();
        writer.reset();
        return result;
    }
}
