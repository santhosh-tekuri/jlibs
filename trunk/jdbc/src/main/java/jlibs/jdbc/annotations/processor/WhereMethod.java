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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
// @enhancement allow to return single/listOf column values
public class WhereMethod extends DMLMethod{
    protected WhereMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    @Override
    protected CharSequence[] defaultSQL(){
        List<VariableElement> elements = new ArrayList<VariableElement>(method.getParameters());

        List<String> params = new ArrayList<String>();

        List<String> where = new ArrayList<String>();
        Iterator<VariableElement> iter=elements.iterator();
        while(iter.hasNext()){
            VariableElement param = iter.next();
            String paramName = param.getSimpleName().toString();
            if(paramName.indexOf('_')==-1){
                ColumnProperty column = getColumn(param);
                where.add(column.propertyName()+"=?");
                params.add(paramName);
                iter.remove();
            }else{
                int underscore = paramName.indexOf('_');
                String hint = paramName.substring(0, underscore);
                String propertyName = paramName.substring(underscore+1);
                ColumnProperty column = getColumn(param, propertyName);

                String hintValue = HINTS.get(hint);
                if(hintValue!=null){
                    where.add(column.propertyName()+hintValue);
                    params.add(paramName);
                    iter.remove();
                }else if("from".equals(hint)){
                    iter.remove();
                    final VariableElement nextParam = iter.next();
                    final String nextParamName = nextParam.getSimpleName().toString();
                    if(!nextParamName.equals("to_"+propertyName))
                        throw new AnnotationError(method, "the next parameter of "+paramName+" must be to_"+propertyName);
                    if(param.asType()!=nextParam.asType())
                        throw new AnnotationError(method, paramName+" and "+nextParamName+" must be of same type");
                    where.add(column.propertyName()+" BETWEEN ? and ?");
                    params.add(paramName);
                    params.add(nextParamName);
                    iter.remove();
                }else
                    throw new AnnotationError(param, "invalid hint: "+hint);
            }
        }

        StringBuilder query = new StringBuilder();
        if(where.size()>0)
            query.append(" WHERE ").append(StringUtil.join(where.iterator(), " AND "));

        return new CharSequence[]{
            query,
            StringUtil.join(params.iterator(), ", ")
        };
    }
}