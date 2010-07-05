package jlibs.jdbc.paging;

/**
 * @author Santhosh Kumar T
 */
public enum Order{
    ASCENDING("ASC", ">"),
    DESCENDING("DESC", "<");

    public final String keyword;
    public final String operand;
    private Order(String keyword, String operand){
        this.keyword = keyword;
        this.operand = operand;
    }

    public Order reverse(){
        return this==ASCENDING ? DESCENDING : ASCENDING;
    }
}
