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

package jlibs.jdbc.annotations.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
class UpdateMethod extends WhereMethod{
    protected UpdateMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);

        String returnType = ModelUtil.toString(method.getReturnType(), true);
        if(!returnType.equals("void") && !returnType.equals(Integer.class.getName())){
            throw new AnnotationError("method with @Update annotation should return void/int/Intger");
        }
    }

    @Override
    protected CharSequence[] defaultSQL(){
        List<VariableElement> elements = new ArrayList<VariableElement>(method.getParameters());

        List<CharSequence> params = new ArrayList<CharSequence>();

        List<String> set = new ArrayList<String>();
        Iterator<VariableElement> iter=elements.iterator();
        while(iter.hasNext()){
            VariableElement param = iter.next();
            String paramName = param.getSimpleName().toString();
            if(paramName.indexOf('_')==-1){
                ColumnProperty column = getColumn(param);
                set.add(column.columnName()+"=?");
                params.add(paramName);
                iter.remove();
            }
        }
        if(set.size()==0)
            throw new AnnotationError(method, "no columns to be set in query");
        initialQuery = "SET "+StringUtil.join(set.iterator(), ", ");

        List<CharSequence> code = new ArrayList<CharSequence>();
        CharSequence[] where = defaultSQL(elements.iterator());
        boolean dynamicWhere = where.length>2;
        if(dynamicWhere){
            code.add(where[0]);
            code.add(where[1]);
            for(CharSequence param: params)
                code.add("__params.add("+param+");");
            params.clear();
            for(int i=2; i<where.length-2; i++)
                code.add(where[i].toString());
            where = new CharSequence[]{ where[where.length-2], where[where.length-1] };
        }

        code.add(where[0]);
        if(where[1].length()>0)
            params.add(where[1]);
        code.add(StringUtil.join(params.iterator(), ", "));

        return code.toArray(new CharSequence[code.size()]);
    }
}
