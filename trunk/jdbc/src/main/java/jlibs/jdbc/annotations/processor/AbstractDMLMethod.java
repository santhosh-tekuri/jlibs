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
import jlibs.core.graph.Visitor;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.core.util.regex.TemplateMatcher;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import java.util.Locale;

/**
 * @author Santhosh Kumar T
 */
abstract class AbstractDMLMethod extends DMLMethod{
    protected AbstractDMLMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    protected String userSQL(){
        return ModelUtil.getAnnotationValue(method, mirror, "value");
    }

    @Override
    protected String[] code(){
        CharSequence[] sequences;

        String userSQL = userSQL();
        if(userSQL.length()==0){
            if(method.getParameters().size()==0)
                throw new AnnotationError(method, "method with "+mirror.getAnnotationType().asElement().getSimpleName()+" annotation should take atleast one argument");
            sequences = defaultSQL();
        }else
            sequences = preparedSQL(userSQL);

        String code = queryMethod(sequences)+';';
        if(method.getReturnType().getKind()!=TypeKind.VOID)
            code = "return "+code;
        
        return new String[]{ code };
    }

    protected String methodName(){
        return mirror.getAnnotationType().asElement().getSimpleName().toString().toLowerCase(Locale.US);
    }

    protected String queryMethod(CharSequence sequences[]){
        CharSequence query = sequences[0];
        CharSequence params = sequences[1];

        query = '"'+ StringUtil.toLiteral(query, false)+'"';
        String code = methodName()+'('+query;
        if(params.length()>0)
            code += ", "+params;
        return code += ")";
    }

    protected abstract CharSequence[] defaultSQL();

    protected CharSequence[] preparedSQL(String value){
        CharSequence query;
        final StringBuilder params = new StringBuilder();

        TemplateMatcher matcher = new TemplateMatcher("#{", "}");
        value = matcher.replace(value, new TemplateMatcher.VariableResolver(){
            @Override
            public String resolve(String propertyName){
                String columnName = columns.columnName(propertyName);
                if(columnName==null)
                    throw new AnnotationError(method, mirror, "unknown property: "+propertyName);
                return columnName;
            }
        });
        matcher = new TemplateMatcher("${", "}");
        query = matcher.replace(value, new TemplateMatcher.VariableResolver(){
            @Override
            public String resolve(String paramName){
                VariableElement param = ModelUtil.getParameter(method, paramName);
                if(param==null)
                    throw new AnnotationError(method, mirror, "unknown parameter: "+paramName);
                if(params.length()>0)
                    params.append(", ");
                params.append(paramName);
                return "?";
            }
        });

        return new CharSequence[]{ query, params };
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    private StringBuilder join(boolean useColumnName, Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        StringBuilder buff = new StringBuilder();
        int i = 0;
        for(VariableElement param : method.getParameters()){
            String paramName = param.getSimpleName().toString();
            String propertyName = propertyVisitor==null ? paramName : propertyVisitor.visit(paramName);
            if(propertyName!=null){
                ColumnProperty column = columns.findByProperty(propertyName);
                if(column==null)
                    throw new AnnotationError(method, "invalid column property: "+paramName+"->"+propertyName);
                if(column.propertyType()!=param.asType())
                    throw new AnnotationError(param, paramName+" must be of type "+ModelUtil.toString(column.propertyType(), true));

                String item =  useColumnName ? column.columnName() : paramName;
                String value = visitor == null ? item : visitor.visit(item);
                if(value!=null){
                    if(i>0)
                        buff.append(separator);
                    buff.append(value);
                    i++;
                }
            }
        }
        return buff;
    }

    protected StringBuilder columns(Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        return join(true, propertyVisitor, visitor, separator);
    }

    protected StringBuilder parameters(Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        return join(false, propertyVisitor, visitor, separator);
    }

    /*-------------------------------------------------[ Visitors ]---------------------------------------------------*/

    static final Visitor<String, String> ASSIGN_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String columnName){
            return columnName+"=?";
        }
    };

    static final Visitor<String, String> SET_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String paramName){
            return paramName.startsWith("where") ? null : paramName;
        }
    };
}
