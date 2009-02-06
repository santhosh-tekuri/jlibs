package jlibs.xml.sax.sniff.model.expr;

/**
 * @author Santhosh Kumar T
 */
public interface ResultListener{
    public void finishedEvaluation(Expression member, Object result);
}
