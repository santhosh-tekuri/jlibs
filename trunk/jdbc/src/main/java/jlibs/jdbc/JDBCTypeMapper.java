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
 * This class defines conversion user type to native type
 * and vice versa.
 *
 * @param <U> User Type
 * @param <N> Native Type (Java type which is supported by JDBC)
 * 
 * @author Santhosh Kumar T
 */
public interface JDBCTypeMapper<U, N>{
    public U nativeToUser(N nativeValue);
    public N userToNative(U userValue);
}