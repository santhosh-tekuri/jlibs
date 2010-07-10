/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

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
