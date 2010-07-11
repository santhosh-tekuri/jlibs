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
import jlibs.core.util.CollectionUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

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
        return defaultSQL(new ArrayList<VariableElement>(method.getParameters()).iterator());
    }

    protected String initialQuery = null;
    protected CharSequence[] defaultSQL(Iterator<VariableElement> iter){
        List<String> code = new ArrayList<String>();
        CollectionUtil.addAll(code,
            "java.util.List<String> __conditions = new java.util.ArrayList<String>();",
            "java.util.List<Object> __params = new java.util.ArrayList<Object>();"
        );

        List<String> params = new ArrayList<String>();
        List<String> where = new ArrayList<String>();
        while(iter.hasNext()){
            VariableElement param = iter.next();
            String paramName = param.getSimpleName().toString();
            boolean primitive = ModelUtil.isPrimitive(param.asType());
            if(paramName.indexOf('_')==-1){
                ColumnProperty column = getColumn(param);
                where.add(column.columnName()+"=?");
                params.add(paramName);
                if(!primitive)
                    CollectionUtil.addAll(code,
                        "if("+paramName+"!=null){",
                            PLUS
                    );
                CollectionUtil.addAll(code,
                    "__conditions.add(\""+StringUtil.toLiteral(where.get(where.size()-1), false)+"\");",
                    "__params.add("+paramName+");"
                );
                if(!primitive)
                    CollectionUtil.addAll(code,
                            MINUS,
                        "}"
                    );
                iter.remove();
            }else{
                int underscore = paramName.indexOf('_');
                String hint = paramName.substring(0, underscore);
                String propertyName = paramName.substring(underscore+1);
                ColumnProperty column = getColumn(param, propertyName);

                String hintValue = HINTS.get(hint);
                if(hintValue!=null){
                    where.add(column.columnName()+hintValue);
                    params.add(paramName);
                    if(!primitive)
                        CollectionUtil.addAll(code,
                            "if("+paramName+"!=null){",
                                PLUS
                        );
                    CollectionUtil.addAll(code,
                        "__conditions.add(\""+StringUtil.toLiteral(where.get(where.size()-1), false)+"\");",
                        "__params.add("+paramName+");"
                    );
                    if(!primitive)
                        CollectionUtil.addAll(code,
                                MINUS,
                            "}"
                        );
                    iter.remove();
                }else if("from".equals(hint)){
                    iter.remove();
                    final VariableElement nextParam = iter.next();
                    final String nextParamName = nextParam.getSimpleName().toString();
                    boolean nextPrimitive = ModelUtil.isPrimitive(nextParam.asType());
                    if(!nextParamName.equals("to_"+propertyName))
                        throw new AnnotationError(method, "the next parameter of "+paramName+" must be to_"+propertyName);
                    if(param.asType()!=nextParam.asType())
                        throw new AnnotationError(method, paramName+" and "+nextParamName+" must be of same type");
                    where.add(column.columnName()+" BETWEEN ? and ?");
                    params.add(paramName);
                    params.add(nextParamName);
                    if(!primitive || !nextPrimitive){
                        String condition = "";
                        if(!primitive)
                            condition = paramName+"!=null";
                        if(!nextPrimitive){
                            if(condition.length()>0)
                                condition += " && ";
                            condition += nextParamName+"!=null";
                        }
                        CollectionUtil.addAll(code,
                            "if("+condition+"){",
                                PLUS
                        );
                    }
                    CollectionUtil.addAll(code,
                        "__conditions.add(\""+StringUtil.toLiteral(where.get(where.size()-1), false)+"\");",
                        "__params.add("+paramName+");",
                        "__params.add("+nextParamName+");"
                    );
                    if(!primitive || !nextPrimitive)
                        CollectionUtil.addAll(code,
                                MINUS,
                            "}"
                        );
                    iter.remove();
                }else
                    throw new AnnotationError(param, "invalid hint: "+hint);
            }
        }

        Boolean ignoreNullConditions = false;
        try{
            ignoreNullConditions = ModelUtil.getAnnotationValue(method, mirror, "ignoreNullConditions");
        }catch(AnnotationError ex){
            // ignore
        }

        if(ignoreNullConditions){
            String queryInitialValue;
            if(initialQuery==null)
                queryInitialValue = "null";
            else
                queryInitialValue = '"'+ initialQuery +'"';
            CollectionUtil.addAll(code,
                "String __query = "+queryInitialValue+";",
                "if(__conditions.size()>0)",
                    PLUS,
                        "__query "+(initialQuery==null?"":"+")+"= \" WHERE \" + "+StringUtil.class.getName()+".join(__conditions.iterator(), \" AND \");",
                    MINUS,
                "__query",
                "__params.toArray()"
            );
            return code.toArray(new CharSequence[code.size()]);
        }else{
            StringBuilder query = new StringBuilder();
            if(initialQuery!=null)
                query.append(initialQuery).append(' ');
            if(where.size()>0)
                query.append("WHERE ").append(StringUtil.join(where.iterator(), " AND "));

            return new CharSequence[]{
                query,
                StringUtil.join(params.iterator(), ", ")
            };
        }
    }
}