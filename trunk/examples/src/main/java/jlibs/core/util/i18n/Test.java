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

package jlibs.core.util.i18n;

/**
 * @author Santhosh Kumar T
 */
@Bundle({
    @Entry("this is comment"),
    @Entry(lhs="keyName", rhs="rhsValue")
})
public class Test{
    @Bundle({
        @Entry("this is comment"),
        @Entry(hint=Hint.DISPLAY_NAME, rhs="this is field1")
    })
    private String field1;

    @Bundle({
        @Entry("this is comment"),
        @Entry(hintName="customHint", rhs="this is field2")
    })
    private String field2;

    @Bundle({
        @Entry("{0} - id specified"),
        @Entry(lhs="invalidID", rhs="you have specified invalid id {0}")
    })
    public void test(){

    }
}
