package jlibs.xml.sax.dog;

/**
 * This class contains constants to specify evaluation scope of an expression.
 * Scope tells you, how result of expression is affected.
 *
 * @see jlibs.xml.sax.dog.expr.Expression#scope()
 *
 * @author Santhosh Kumar T
 */
public interface Scope{
    /**
     * used for those expressions whose result doesn't depend
     * on input xml documents. for example:
     *      1+2-3       -> always evaluates to 0
     *      count(/*)   -> always evaluates to 1
     */
    public static final int GLOBAL = 0;

    /**
     * used for absolute expressions. their result depends on
     * the input xml document
     */
    public static final int DOCUMENT = 1;

    /**
     * used for relative expressions. their result depends on
     * the context node used for evaluation
     */
    public static final int LOCAL = 2;
}
