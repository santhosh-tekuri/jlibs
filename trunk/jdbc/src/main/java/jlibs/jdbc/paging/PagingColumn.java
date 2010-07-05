package jlibs.jdbc.paging;

/**
 * @author Santhosh Kumar T
 */
public class PagingColumn{
    public final int index;
    public final Order order;
    public PagingColumn(int index, Order order){
        this.index = index;
        this.order = order;
    }
}
