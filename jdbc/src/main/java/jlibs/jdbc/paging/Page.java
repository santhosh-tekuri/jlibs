/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.jdbc.paging;

import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Page<T>{
    public final Paging<T> paging;
    public final int pageSize;

    public Page(Paging<T> paging, int pageSize){
        this.paging = paging;
        this.pageSize = pageSize;
    }

    /*-------------------------------------------------[ TotalRowCount ]---------------------------------------------------*/

    private int totalRowCount = -1;

    public int getTotalRowCount(){
        if(totalRowCount==-1)
            totalRowCount = paging.getTotalRowCount();
        return totalRowCount;
    }

    public void setTotalRowCount(int count){
        totalRowCount = count;
    }

    /*-------------------------------------------------[ Index ]---------------------------------------------------*/

    private int index = -1;

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    /*-------------------------------------------------[ FirstRow ]---------------------------------------------------*/

    private T firstRow;

    public T getFirstRow(){
        return firstRow;
    }

    public void setFirstRow(T firstRow){
        this.firstRow = firstRow;
    }

    /*-------------------------------------------------[ LastRow ]---------------------------------------------------*/

    private T lastRow;

    public T getLastRow(){
        return lastRow;
    }

    public void setLastRow(T lastRow){
        this.lastRow = lastRow;
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    public int getTotalPageCount(){
        return (int)Math.ceil(getTotalRowCount()*1.0/pageSize);
    }

    /*-------------------------------------------------[ Navigation ]---------------------------------------------------*/

    private List<T> records(int index, List<T> records, boolean reverse){
        this.index = index;
        if(reverse)
            Collections.reverse(records);
        firstRow = records.get(0);
        lastRow = records.get(records.size()-1);
        return records;
    }

    public List<T> first(){
        if(getTotalRowCount()==0)
            return Collections.emptyList();
        int page = 0;
        String orderBy = paging.orderBy(false);
        String condition = StringUtil.isEmpty(paging.condition) ? orderBy : paging.condition+' '+orderBy;
        return records(page, paging.dao.top(pageSize, condition, paging.args), false);
    }

    public List<T> last(){
        if(getTotalRowCount()==0)
            return Collections.emptyList();
        String orderBy = paging.orderBy(true);
        int page = getTotalPageCount()-1;
        String condition = StringUtil.isEmpty(paging.condition) ? orderBy : paging.condition+' '+orderBy;
        int max = getTotalRowCount() - page*pageSize;
        return records(page, paging.dao.top(max, condition, paging.args), true);
    }

    private List<T> move(boolean previous){
        if(getTotalRowCount()==0)
            return Collections.emptyList();
        int page = index + (previous ? -1 : +1);

        String condition = StringUtil.isEmpty(paging.condition) ? "WHERE " : paging.condition+" AND ";
        condition = condition + paging.where(previous);
        String orderBy = paging.orderBy(previous);
        condition = condition+' '+orderBy;

        Object args[] = Arrays.copyOf(paging.args, paging.args.length+paging.orderBy.size());
        int i = paging.args.length;
        for(PagingColumn col: paging.orderBy)
            args[i] = paging.dao.getColumnValue(col.index, previous?firstRow:lastRow);

        return records(page, paging.dao.top(pageSize, condition, args), previous);
    }

    public List<T> next(){
        return move(false);
    }

    public List<T> previous(){
        return move(true);
    }

    /*-------------------------------------------------[ Actions ]---------------------------------------------------*/

    public enum Action{ FIRST, PREVIOUS, NEXT, LAST }

    public boolean canNavigate(Action action){
        if(getTotalRowCount()==0)
            return false;

        int totalPageCount = getTotalPageCount();
        switch(action){
            case FIRST:
                return getIndex()!=0;
            case LAST:
                return getIndex()!=totalPageCount-1;
            case PREVIOUS:
                return getIndex()>0;
            case NEXT:
                return getIndex()>-1 && getIndex()<totalPageCount-1;
            default:
                throw new ImpossibleException(); 
        }
    }

    public List<T> navigate(Action action){
        if(!canNavigate(action))
            throw new IllegalArgumentException("Invalid Action: "+action);
        switch(action){
            case FIRST:
                return first();
            case LAST:
                return last();
            case PREVIOUS:
                return previous();
            case NEXT:
                return next();
            default:
                throw new ImpossibleException();
        }
    }
}
