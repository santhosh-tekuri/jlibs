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

package jlibs.jdbc;

/**
 * @author Santhosh Kumar T
 */
public class IncorrectResultSizeException extends DAOException{
    public final int expected;
    public final int found;

    public IncorrectResultSizeException(String objectName, int expected, int found){
        super(expected==1 ? objectName+" doesn't exist" : objectName+" expected: "+expected+" found: "+found);
        this.expected = expected;
        this.found = found;
    }
}
