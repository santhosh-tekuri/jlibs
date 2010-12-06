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

package jlibs.nblr.actions;

import jlibs.core.lang.StringUtil;

/**
 * @author Santhosh Kumar T
 */
public class ErrorAction implements Action{
    public final String errorMessage;

    public ErrorAction(String errorMessage){
        this.errorMessage = errorMessage;
    }

    @Override
    public String javaCode(){
        return "throw handler.fatalError(\""+ StringUtil.toLiteral(errorMessage, false)+"\")";
    }

    @Override
    public String toString(){
        return "error(\""+StringUtil.toLiteral(errorMessage, false)+"\")";
    }
}
