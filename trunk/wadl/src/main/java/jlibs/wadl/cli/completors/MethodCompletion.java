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

package jlibs.wadl.cli.completors;

import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;
import jlibs.wadl.model.Method;
import jlibs.wadl.model.Param;
import jlibs.wadl.model.ParamStyle;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public class MethodCompletion extends PathCompletion{
    public MethodCompletion(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    protected void completeNext(Buffer buffer, Path path, String arg){
        Method method = path.findMethod(buffer.arg(0));
        if(method==null || method.getRequest()==null)
            return;

        Set<String> params = new HashSet<String>();
        while(buffer.hasNext()){
            int index = arg.indexOf('=');
            if(index==-1)
                params.add(arg.substring(0, index+1));
            else{
                index = arg.indexOf(':');
                if(index!=-1)
                    params.add(arg.substring(0, index+1));
            }
            arg = buffer.next();
        }
        
        for(Param param: method.getRequest().getParam()){
            String candidate = param.getName();
            if(param.getStyle()==ParamStyle.QUERY)
                candidate += '=';
            else if(param.getStyle()==ParamStyle.HEADER)
                candidate += ':';
            else
                continue;
            if(!params.contains(candidate)){
                if(param.getFixed()!=null)
                    candidate += param.getFixed();
                buffer.addCandidate(candidate, (char)0);
            }
        }
    }
}
